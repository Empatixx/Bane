package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Damageindicator.DamageIndicator;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Thompson extends Weapon{
    // audio
    private final int soundShoot;
    private final int soundEmptyShoot;
    private final int soundReload;

    private int dots;

    private ArrayList<Bullet> bullets;
    private boolean boostFirerate;

    Thompson(TileMap tm){
        super(tm);
        mindamage = 1;
        maxdamage = 3;
        inaccuracy = 0.5f;
        maxAmmo = 180;
        maxMagazineAmmo = 20;
        delayTime = 200;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootsubmachine.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadpistol.ogg");
        reloadsource.setPitch(1.6f);
        source.setPitch(1.75f);

        weaponHud = new Image("Textures\\thompson.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1830,975,0),1f);


        int numUpgrades = GameStateManager.getDb().getValueUpgrade("thompson","upgrades");
        if(numUpgrades >= 1){
            mindamage+=1;
        }
        if(numUpgrades >= 2){
            maxMagazineAmmo+=10;
            currentMagazineAmmo = maxMagazineAmmo;
        }
        if(numUpgrades >= 3){
            maxAmmo+=100;
            currentAmmo = maxAmmo;
        }
        if(numUpgrades >= 4){
            boostFirerate = true;
        }
    }

    @Override
    public void reload() {
        if (!reloading && currentAmmo != 0 && currentMagazineAmmo != maxMagazineAmmo){
            reloadDelay = System.currentTimeMillis() - InGame.deltaPauseTime();
            reloadsource.play(soundReload);
            reloading = true;
            delayTime = 200;
            dots = 0;
        }
    }

    @Override
    public void shot(float x,float y,float px,float py) {
        if(isShooting()) {
            if (currentMagazineAmmo != 0) {
                if (reloading) return;
                // delta - time between shoots
                // InGame.deltaPauseTime(); returns delayed time because of pause time
                long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
                if (delta > delayTime) {
                    double inaccuracy = 0;
                    if (delta < 200) {
                        inaccuracy = (Math.random() * 0.085) * (Random.nextInt(2) * 2 - 1);
                    }
                    delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                    Bullet bullet = new Bullet(tm, x, y, inaccuracy,30);
                    bullet.setPosition(px, py);
                    int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                    if(criticalHits){
                        if(Math.random() > 0.9){
                            damage*=2;
                            bullet.setCritical(true);
                        }
                    }
                    bullet.setDamage(damage);
                    bullets.add(bullet);
                    currentMagazineAmmo--;
                    source.play(soundShoot);
                    GunsManager.bulletShooted++;
                    if(boostFirerate){
                        delayTime -= 5;
                        if(delayTime < 130) delayTime = 130;
                    }
                }
            } else if (currentAmmo != 0) {
                reload();
            } else {
                source.play(soundEmptyShoot);
            }

        } else {
            if(boostFirerate){
                delayTime += 2;
                if(delayTime > 200) delayTime = 200;
            }
        }
    }

    @Override
    public void drawAmmo() {
        for (Bullet bullet : bullets) {
            bullet.draw();
        }
    }

    @Override
    public void draw() {
        if(reloading){
            StringBuilder builder = new StringBuilder();
            for(int i = 0;i<=dots;i++) builder.append(".");
            TextRender.renderText(builder.toString(),new Vector3f(1800,985,0),6,new Vector3f(0.886f,0.6f,0.458f));

        } else {
            TextRender.renderText(currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1740,985,0),2,new Vector3f(0.886f,0.6f,0.458f));
        }
        weaponHud.draw();
        weaponAmmo.draw();

    }

    @Override
    public void update() {
        float time = (float)(System.currentTimeMillis()-reloadDelay-InGame.deltaPauseTime())/1000;
        dots = (int)((time / 0.7f) / 0.2f);
        if(reloading && time > 0.85f) {
            if (currentAmmo - maxMagazineAmmo+currentMagazineAmmo < 0) {
                currentMagazineAmmo = currentAmmo;
                currentAmmo = 0;
            } else {
                currentAmmo -= maxMagazineAmmo - currentMagazineAmmo;
                currentMagazineAmmo = maxMagazineAmmo;
            }
            reloading = false;
        }
    }

    @Override
    public void updateAmmo() {
        for(int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
            if(bullets.get(i).shouldRemove()) {
                bullets.remove(i);
                i--;
            }
        }
    }

    @Override
    public void checkCollisions(ArrayList<Enemy> enemies) {
        ArrayList<RoomObject> objects = tm.getRoomMapObjects();
        A: for(Bullet bullet:bullets){
            for(Enemy enemy:enemies){
                if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
                    enemy.hit(bullet.getDamage());
                    int cwidth = enemy.getCwidth();
                    int cheight = enemy.getCheight();
                    int x = -cwidth/4+Random.nextInt(cwidth/2);
                    if(bullet.isCritical()){
                        DamageIndicator.addCriticalDamageShow(bullet.getDamage(),(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                                ,new Vector2f(-x/25f,-1f));
                    } else {
                        DamageIndicator.addDamageShow(bullet.getDamage(),(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                                ,new Vector2f(-x/25f,-1f));
                    }
                    bullet.playEnemyHit();
                    bullet.setHit();
                    GunsManager.hitBullets++;
                    continue A;
                }
            }
            for(RoomObject object: objects){
                if(object instanceof DestroyableObject) {
                    if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                        bullet.playEnemyHit();
                        bullet.setHit();
                        ((DestroyableObject) object).setHit(bullet.getDamage());
                        continue A;
                    }
                }
            }
        }
    }

}
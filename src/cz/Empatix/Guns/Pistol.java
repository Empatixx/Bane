package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Damageindicator.DamageIndicator;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Pistol extends Weapon {
    public static void load(){
        Loader.loadImage("Textures\\pistol.tga");
        Loader.loadImage("Textures\\pistol_bullet.tga");
    }
    // audio
    private final int[] soundShoot;
    private final int soundEmptyShoot;
    private final int soundReload;

    private int dots;

    private ArrayList<Bullet> bullets;

    private boolean doubleShots;
    private boolean secondShotReady;
    private float lastX;
    private float lastY;

    Pistol(TileMap tm){
        super(tm);
        mindamage = 1;
        maxdamage = 3;
        inaccuracy = 0.8f;
        maxAmmo = 120;
        maxMagazineAmmo = 7;
        delayTime = 250;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = new int[2];
        soundShoot[0] = AudioManager.loadSound("guns\\shootpistol_1.ogg");
        soundShoot[1] = AudioManager.loadSound("guns\\shootpistol_2.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadpistol.ogg");

        weaponHud = new Image("Textures\\pistol.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1810,975,0),1f);

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("pistol","upgrades");
        if(numUpgrades >= 1){
            maxMagazineAmmo+=2;
            currentMagazineAmmo = maxMagazineAmmo;
        }
        if(numUpgrades >= 2){
            criticalHits = true;
        }
        if(numUpgrades >= 3){
            mindamage++;
        }
        if(numUpgrades >= 4){
            doubleShots = true;
        }

    }

    @Override
    public void reload() {
        if (!reloading && currentAmmo != 0 && currentMagazineAmmo != maxMagazineAmmo){
            reloadDelay = System.currentTimeMillis() - InGame.deltaPauseTime();
            reloadsource.play(soundReload);
            reloading = true;

            dots = 0;
        }
    }

    @Override
    public void shot(float x,float y,float px,float py) {
        long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
        if(doubleShots && delta > 75 && secondShotReady){
            double inaccuracy = 0;
            delay = System.currentTimeMillis() - InGame.deltaPauseTime();
            Bullet bullet = new Bullet(tm, lastX, lastY, inaccuracy,30);
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
            GunsManager.bulletShooted++;
            secondShotReady=false;
        }
        if(isShooting()) {
            if (currentMagazineAmmo != 0) {
                if (reloading) return;
                // delta - time between shoots
                // InGame.deltaPauseTime(); returns delayed time because of pause time
                if (delta > delayTime) {
                    double inaccuracy = 0;
                    if (delta < 400) {
                        inaccuracy = 0.055 * 400 / delta * (Random.nextInt(2) * 2 - 1);
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
                    GunsManager.bulletShooted++;
                    source.play(soundShoot[cz.Empatix.Java.Random.nextInt(2)]);

                    lastX = x;
                    lastY = y;
                    if(currentMagazineAmmo > 0 && doubleShots) secondShotReady = true;
                }
            } else if (currentAmmo != 0) {
                reload();
            } else {
                source.play(soundEmptyShoot);
            }
            setShooting(false);

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
            textRender.draw(builder.toString(),new Vector3f(1820,985,0),6,new Vector3f(0.886f,0.6f,0.458f));

        } else {
            textRender.draw(currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1750,985,0),2,new Vector3f(0.886f,0.6f,0.458f));
        }
        weaponHud.draw();
        weaponAmmo.draw();

    }

    @Override
    public void update() {
        float time = (float)(System.currentTimeMillis()-reloadDelay-InGame.deltaPauseTime())/1000;
        dots = (int)((time / 0.7f) / 0.2f);
        if(reloading && time > 0.7f) {
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
    @Override
    public boolean canSwap() {
        return !reloading && System.currentTimeMillis()  - InGame.deltaPauseTime()- delay > delayTime/2 && !secondShotReady;
    }
}
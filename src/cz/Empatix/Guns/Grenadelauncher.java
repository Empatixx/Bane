package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Grenadelauncher extends Weapon {
    // map push when player shoot
    // audio
    private final int soundShoot;
    private final int soundEmptyShoot;
    private final int soundReload;

    private int dots;

    private ArrayList<Grenadebullet> bullets;

    Grenadelauncher(TileMap tm){
        super(tm);
        mindamage = 4;
        maxdamage = 7;
        inaccuracy = 0.7f;
        maxAmmo = 24;
        maxMagazineAmmo = 6;
        type = 4;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = AudioManager.loadSound("guns\\grenadelaunchershoot.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\grenadelauncherreload.ogg");

        source.setPitch(0.75f);

        weaponHud = new Image("Textures\\grenadelauncher.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\rocket-ammo.tga",new Vector3f(1830,975,0),2f);

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("grenadelauncher","upgrades");
        if(numUpgrades >= 1){
            maxAmmo+=4;
            currentAmmo=maxAmmo;
        }
        if(numUpgrades >= 2){
            maxMagazineAmmo+=3;
            currentMagazineAmmo=maxMagazineAmmo;
        }
        if(numUpgrades >= 3){
            maxdamage+=2;
            mindamage+=2;
        }
        if(numUpgrades >= 4){
            criticalHits=true;
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
        if(isShooting()) {
            if (currentMagazineAmmo != 0) {
                if (reloading) return;
                // delta - time between shoots
                long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
                if (delta > 450) {
                    source.play(soundShoot);
                    double inaccuracy = 0;

                    delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                    Grenadebullet bullet = new Grenadebullet(tm, x, y, inaccuracy,30);
                    int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                    if(criticalHits){
                        if(Math.random() > 0.9){
                            damage*=2;
                            bullet.setCritical(true);
                        }
                    }
                    bullet.setDamage(damage);
                    bullet.setPosition(px, py);
                    bullets.add(bullet);
                    currentMagazineAmmo--;
                    GunsManager.bulletShooted++;

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
    public void draw() {
        if(reloading){
            StringBuilder builder = new StringBuilder();
            for(int i = 0;i<=dots;i++) builder.append(".");
            TextRender.renderText(builder.toString(),new Vector3f(1825,985,0),6,new Vector3f(0.886f,0.6f,0.458f));;

        } else {
            TextRender.renderText(currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1760,985,0),2,new Vector3f(0.886f,0.6f,0.458f));
        }
        weaponHud.draw();
        weaponAmmo.draw();
    }

    @Override
    public void drawAmmo() {
        for(Grenadebullet bullet:bullets){
            bullet.draw();
        }
    }

    @Override
    public void update() {
        float time = (float)(System.currentTimeMillis()-reloadDelay- InGame.deltaPauseTime())/1000;
        dots = (int)((time / 2.2f) / 0.2f);
        if(reloading && time > 2.2f) {

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
        A: for(Grenadebullet bullet:bullets){
            for(Enemy enemy:enemies){
                if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
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
                        continue A;
                    }
                }
            }
        }
    }
}
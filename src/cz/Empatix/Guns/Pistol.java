package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Pistol extends Weapon {
    // audio
    private final int[] soundShoot;
    private final int soundEmptyShoot;
    private final int soundReload;


    private ArrayList<Bullet> bullets;

    Pistol(TileMap tm){
        super(tm);
        mindamage = 1;
        maxdamage = 1;
        inaccuracy = 0.4f;
        maxAmmo = 120;
        maxMagazineAmmo = 7;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = new int[2];
        soundShoot[0] = AudioManager.loadSound("guns\\shootpistol_1.ogg");
        soundShoot[1] = AudioManager.loadSound("guns\\shootpistol_2.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadpistol.ogg");

    }

    @Override
    public void reload() {
        if (!reloading && currentAmmo != 0 && currentMagazineAmmo != maxMagazineAmmo){
            reloadDelay = System.currentTimeMillis() - InGame.deltaPauseTime();
            reloadsource.play(soundReload);
            reloading = true;
        }
    }

    @Override
    public void shot(float x,float y,float px,float py) {
        if (currentMagazineAmmo != 0) {
            if (reloading) return;
            // delta - time between shoots
            // InGame.deltaPauseTime(); returns delayed time because of pause time
            long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
            System.out.println("delta"+delta);
            if (delta > 250){
                double inaccuracy = 0;
                if (delta < 400){
                    inaccuracy = 0.055 * 400/delta * (Random.nextInt(2)*2-1);
                }
                delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                Bullet bullet = new Bullet(tm,x,y,inaccuracy);
                bullet.setPosition(px,py);
                bullets.add(bullet);
                currentMagazineAmmo--;
                source.play(soundShoot[cz.Empatix.Java.Random.nextInt(2)]);
            }
        } else if (currentAmmo != 0){
            reload();
        } else {
            source.play(soundEmptyShoot);
        }
    }

    @Override
    public void drawAmmo(Camera c) {
        for (Bullet bullet : bullets) {
            bullet.draw(c);
        }
    }

    @Override
    public void draw(Camera c) {
        if(reloading){
            TextRender.renderText(c,"Reloading...",new Vector3f(1650,1000,0),3,new Vector3f(1.0f,1.0f,1.0f));

        } else {
            TextRender.renderText(c,currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1650,1000,0),3,new Vector3f(1.0f,1.0f,1.0f));
        }
    }

    @Override
    public void update() {
        if(reloading && (float)(System.currentTimeMillis()-reloadDelay-InGame.deltaPauseTime())/1000 > 0.7f) {

            if (currentAmmo - maxMagazineAmmo < 0) {
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
        for(Bullet bullet:bullets){
            for(Enemy enemy:enemies){
                if (bullet.intersects(enemy) && !bullet.isHit()) {
                    enemy.hit(1);
                    bullet.playEnemyHit();
                    bullet.setHit();
                }
            }
        }
    }
}
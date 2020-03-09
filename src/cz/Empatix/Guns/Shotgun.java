package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Shotgun extends Weapon {
    // audio
    private final int soundShoot;
    private final int soundEmptyShoot;
    private final int soundReload;


    private ArrayList<Bullet> bullets;

    Shotgun(TileMap tm){
        super(tm);
        mindamage = 1;
        maxdamage = 1;
        inaccuracy = 0.7f;
        maxAmmo = 18;
        maxMagazineAmmo = 2;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootshotgun.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadshotgun.ogg");

    }

    @Override
    public void reload() {
        if (!reloading && currentAmmo != 0 && currentMagazineAmmo != maxMagazineAmmo){
            reloadDelay = System.currentTimeMillis();
            reloadsource.play(soundReload);
            reloading = true;
        }
    }

    @Override
    public void shot(float x,float y,float px,float py) {
        if (currentMagazineAmmo != 0) {
            if (reloading) return;
            // delta - time between shoots
            long delta = System.currentTimeMillis() - delay;
            if (delta > 450){
                source.play(soundShoot);
                for(int i = 0; i < 6;i++){
                    int coef = i % 2 == 1 ? -1 : 1;

                    int xAccuracy = (int)(12*Math.pow(i,1.2)) * coef;
                    int yAccuracy = (int)(12*Math.pow(i,1.2)) * coef;

                    Bullet bullet = new Bullet(tm,x+yAccuracy,y+xAccuracy);
                    bullet.setPosition(px,py);
                    bullets.add(bullet);
                }
                currentMagazineAmmo--;
                delay = System.currentTimeMillis();

            }
        } else if (currentAmmo != 0){
            reload();
        } else {
            source.play(soundEmptyShoot);
        }
    }

    @Override
    public void draw(Camera c) {
        for(Bullet bullet:bullets){
            bullet.draw(c);
        }
        if(reloading){
            TextRender.renderText(c,"Reloading...",new Vector3f(1650,1000,0),3,new Vector3f(1.0f,1.0f,1.0f));

        } else {
            TextRender.renderText(c,currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1650,1000,0),3,new Vector3f(1.0f,1.0f,1.0f));
        }
    }

    @Override
    public void update() {
        for(int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
            if(bullets.get(i).shouldRemove()) {
                bullets.remove(i);
                i--;
            }
        }
        if(reloading && (float)(System.currentTimeMillis()-reloadDelay)/1000 > 0.7f) {

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

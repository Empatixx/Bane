package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Shotgun extends Weapon {
    // map push when player shoot
    private int push;
    private double pushX;
    private double pushY;
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
            long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
            if (delta > 450){
                source.play(soundShoot);
                for(int i = 0; i < 4;i++){
                    int coef = (i+1) % 2 == 1 ? -1 : 1;

                    double inaccuracy = 0.035 * i * coef;

                    Bullet bullet = new Bullet(tm,x,y,inaccuracy);
                    bullet.setPosition(px,py);
                    bullets.add(bullet);
                }
                currentMagazineAmmo--;
                delay = System.currentTimeMillis() - InGame.deltaPauseTime();

                double atan = Math.atan2(y,x);
                push = 60;
                pushX = Math.cos(atan);
                pushY = Math.sin(atan);

            }
        } else if (currentAmmo != 0){
            reload();
        } else {
            source.play(soundEmptyShoot);
        }
    }

    @Override
    public void draw(Camera c) {
        if(reloading){
            TextRender.renderText(c,"Reloading...",new Vector3f(1740,985,0),2,new Vector3f(0.886f,0.6f,0.458f));

        } else {
            TextRender.renderText(c,currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1740,985,0),2,new Vector3f(0.886f,0.6f,0.458f));
        }
    }

    @Override
    public void drawAmmo(Camera c) {
        for(Bullet bullet:bullets){
            bullet.draw(c);
        }
    }

    @Override
    public void update() {
        if(reloading && (float)(System.currentTimeMillis()-reloadDelay- InGame.deltaPauseTime())/1000 > 0.7f) {

            if (currentAmmo - maxMagazineAmmo < 0) {
                currentMagazineAmmo = currentAmmo;
                currentAmmo = 0;
            } else {
                currentAmmo -= maxMagazineAmmo - currentMagazineAmmo;
                currentMagazineAmmo = maxMagazineAmmo;
            }
            reloading = false;
        }
        if (push > 0) push-=5;
        if (push < 0) push+=5;
        push = -push;
        tm.setPosition(tm.getX()+push*pushX,tm.getY()+push*pushY);
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
                if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead()) {
                    enemy.hit(1);
                    bullet.playEnemyHit();
                    bullet.setHit();
                }
            }
        }
    }

}

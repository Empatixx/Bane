package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Render.Graphics.Framebuffer;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Postprocessing.GaussianBlurAmmo;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Sniperrifle extends Weapon {
    // audio
    private final int soundShoot;
    private final int soundEmptyShoot;
    private final int soundReload;

    private int dots;

    private boolean firstTickLaser;
    private Vector2f projectileVector;
    private Vector2f endProjectíle;
    private Vector2f startProjectíle;

    private ArrayList<Bullet> bullets;

    private GaussianBlurAmmo gaussianBlurAmmo;
    private Framebuffer framebuffer;

    Sniperrifle(TileMap tm){
        super(tm);
        mindamage = 4;
        maxdamage = 4;
        inaccuracy = 0f;
        maxAmmo = 30;
        maxMagazineAmmo = 4;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootrevolver.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadpistol.ogg");

        weaponHud = new Image("Textures\\sniperrifle.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1810,975,0),1f);

        projectileVector = new Vector2f();
        endProjectíle = new Vector2f();
        startProjectíle = new Vector2f();

        framebuffer = new Framebuffer();
        gaussianBlurAmmo = new GaussianBlurAmmo("shaders\\ammoblur");


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
                // InGame.deltaPauseTime(); returns delayed time because of pause time
                long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
                gaussianBlurAmmo.setValue(10f);
                if (delta > 450) {
                    delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                    currentMagazineAmmo--;
                    GunsManager.bulletShooted++;
                    source.play(soundShoot);

                    projectileVector.x = x;
                    projectileVector.y = y;

                    startProjectíle.x = endProjectíle.x = px;
                    startProjectíle.y = endProjectíle.y = py;


                    int tileSize = tm.getTileSize();

                    for(int i = 0;i<500;i++){
                        endProjectíle.x = startProjectíle.x+i/100f*projectileVector.x;
                        if(tm.getType((int)(endProjectíle.y/tileSize),(int)(endProjectíle.x/tileSize)) == Tile.BLOCKED){
                            break;
                        }
                        endProjectíle.y = startProjectíle.y+i/100f*projectileVector.y;
                        if(tm.getType((int)(endProjectíle.y/tileSize),(int)(endProjectíle.x/tileSize)) == Tile.BLOCKED){
                            break;
                        }
                    }

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
        for(int i = 1;i<4;i++){
            glLineWidth(1f);
            glColor4f(1f,0.6f,0.1f,0.334f);
            glBegin(GL_LINES);
            glVertex2f(startProjectíle.x+tm.getX(),startProjectíle.y+tm.getY());
            glVertex2f(endProjectíle.x+tm.getX(),endProjectíle.y+tm.getY());
            glEnd();
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
        for(Bullet bullet:bullets){
            for(Enemy enemy:enemies){
                if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
                    enemy.hit(bullet.getDamage());
                    bullet.playEnemyHit();
                    bullet.setHit();
                    GunsManager.hitBullets++;
                }
            }
        }
    }
}
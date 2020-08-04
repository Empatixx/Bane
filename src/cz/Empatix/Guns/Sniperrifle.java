package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Text.TextRender;
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
    private Vector2f startLaser;
    private Vector2f endLaser;

    private ArrayList<Bullet> bullets;

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

        startLaser = new Vector2f();
        endLaser = new Vector2f();
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
                if (delta > 450) {
                    delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                    currentMagazineAmmo--;
                    GunsManager.bulletShooted++;
                    source.play(soundShoot);

                    startLaser.x = px;
                    startLaser.y = py;

                    endLaser.x = x+px;
                    endLaser.y = y+py;
                    int tileSize = tm.getTileSize();
                    Vector2f temp = new Vector2f(endLaser.x,endLaser.y);
                    while(true){
                        if(tm.getType((int)endLaser.y/tileSize,(int)endLaser.x/tileSize) == Tile.BLOCKED){
                            if(px < temp.x) {
                                endLaser.x = ((int)temp.x/tileSize) * tileSize;
                            } else {
                                endLaser.x = ((int)temp.x/tileSize +1) * tileSize;
                            }
                            if(py < endLaser.y) {
                                endLaser.y = ((int)temp.y/tileSize) * tileSize;
                            } else {
                                endLaser.y = ((int)temp.y/tileSize + 1) * tileSize;
                            }
                            break;
                        }
                        double atan = Math.atan2(y,x);
                        // 30 - speed of bullet
                        float tileSizeX = (float)(Math.cos(atan) * tileSize);
                        float tileSizeY = (float)(Math.sin(atan) * tileSize);
                        if(x-960 < 0){
                            temp.x+=tileSizeX;
                        }else {
                            temp.x-=tileSizeX;
                        }
                        if(tm.getType((int)endLaser.y/tileSize,(int)temp.x/tileSize) == Tile.BLOCKED){
                            if(px < temp.x) {
                                endLaser.x = ((int)temp.x/tileSize) * tileSize;
                            } else {
                                endLaser.x = ((int)temp.x/tileSize +1) * tileSize;
                            }
                            break;
                        }
                        if(y-540 < 0){
                            temp.y+=tileSizeY;
                        }else {
                            temp.y-=tileSizeY;
                        }
                        if(tm.getType((int)temp.y/tileSize,(int)endLaser.x/tileSize) == Tile.BLOCKED){
                            if(py < temp.y) {
                                endLaser.y = ((int)temp.y/tileSize) * tileSize;
                            } else {
                                endLaser.y = ((int)temp.y/tileSize + 1) * tileSize;
                            }
                            break;
                        }
                        endLaser.x = temp.x();
                        endLaser.y = temp.y();
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
        glLineWidth(3f);
        glBegin(GL_LINES);
        glVertex2f(startLaser.x+tm.getX(),startLaser.y+tm.getY());
        glVertex2f(endLaser.x+tm.getX(),endLaser.y+tm.getY());
        glEnd();
    }

    @Override
    public void draw() {
        if(reloading){
            StringBuilder builder = new StringBuilder();
            for(int i = 0;i<=dots;i++) builder.append(".");
            TextRender.renderText(builder.toString(),new Vector3f(1820,985,0),6,new Vector3f(0.886f,0.6f,0.458f));

        } else {
            TextRender.renderText(currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1750,985,0),2,new Vector3f(0.886f,0.6f,0.458f));
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
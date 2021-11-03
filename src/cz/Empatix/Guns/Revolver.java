package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Revolver extends Weapon {
    public static void load(){
        Loader.loadImage("Textures\\revolver.tga");
        Loader.loadImage("Textures\\pistol_bullet.tga");
    }
    // map push when player shoot
    private int push;
    private double pushX;
    private double pushY;
    // audio
    private int soundShoot;
    private int soundEmptyShoot;
    private int soundReload;

    private int dots;

    private ArrayList<Bullet> bullets;

    private boolean increasedCritDamage;
    private boolean nextCritChanceEnabled;
    private float nextCritChance;

    Revolver(TileMap tm, Player player, GunsManager gunsManager){
        super(tm,player,gunsManager);
        source.setVolume(0.20f);
        mindamage = 4;
        maxdamage = 6;
        inaccuracy = 0.8f;
        maxAmmo = 60;
        maxMagazineAmmo = 6;
        delayTime = 450;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootrevolver.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadpistol.ogg");
        reloadsource.setPitch(0.75f);
        source.setPitch(1.2f);

        weaponHud = new Image("Textures\\revolver.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1810,975,0),1f);
        int numUpgrades = GameStateManager.getDb().getValueUpgrade("revolver","upgrades");

        increasedCritDamage = false;

        if(numUpgrades >= 1){
            criticalHits = true;
        }
        if(numUpgrades >= 2){
            maxdamage+=2;
        }
        if(numUpgrades >= 3){
            nextCritChanceEnabled = true;
        }
        if(numUpgrades >= 4){
            increasedCritDamage=true;
        }
    }
    public Revolver(TileMap tm, Player player){
        super(tm,player);
        mindamage = 4;
        maxdamage = 6;
        inaccuracy = 0.8f;
        maxAmmo = 60;
        maxMagazineAmmo = 6;
        delayTime = 450;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();
    }
    @Override
    public void reload() {
        if (!reloading && currentAmmo != 0 && currentMagazineAmmo != maxMagazineAmmo){
            reloadDelay = System.currentTimeMillis() - InGame.deltaPauseTime();
            if(!tm.isServerSide())reloadsource.play(soundReload);
            reloading = true;

            dots = 0;
        }
    }

    @Override
    public void shoot(float x, float y, float px, float py) {
        if(isShooting()) {
            if (currentMagazineAmmo != 0) {
                if (reloading) return;
                // delta - time between shoots
                // InGame.deltaPauseTime(); returns delayed time because of pause time
                long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
                if (delta > delayTime) {
                    double inaccuracy = 0;
                    if (delta < 600) {
                        inaccuracy = 0.055 * 600 / delta * (Random.nextInt(2) * 2 - 1);
                    }
                    delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                    Bullet bullet = new Bullet(tm, x, y, inaccuracy,40);
                    bullet.setPosition(px, py);
                    int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                    if(criticalHits){
                        if(Math.random() > 0.9-nextCritChance){
                            damage*=increasedCritDamage ? 3 : 2;
                            nextCritChance=0;
                            bullet.setCritical(true);
                        } else if(nextCritChanceEnabled){
                            nextCritChance+=0.1f;
                        }
                    }
                    bullet.setDamage(damage);
                    bullets.add(bullet);
                    currentMagazineAmmo--;
                    GunsManager.bulletShooted++;
                    source.play(soundShoot);

                    double atan = Math.atan2(y, x);
                    push = 80;
                    pushX = Math.cos(atan);
                    pushY = Math.sin(atan);

                }
            } else if (currentAmmo != 0) {
                reload();
            } else {
                source.play(soundEmptyShoot);
                outOfAmmo();
            }
            setShooting(false);

        }
    }

    @Override
    public void shoot(float x, float y, float px, float py, String username) {
        if(MultiplayerManager.multiplayer && !tm.isServerSide()){
            if(isShooting()) {
                if (currentMagazineAmmo != 0) {
                    if (reloading) return;
                } else if (currentAmmo != 0) {
                    reload();
                } else {
                    source.play(soundEmptyShoot);
                    outOfAmmo();
                }
                setShooting(false);
            }
        } else {
            if(isShooting()) {
                if (currentMagazineAmmo != 0) {
                    if (reloading) return;
                    // delta - time between shoots
                    // InGame.deltaPauseTime(); returns delayed time because of pause time
                    long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
                    if (delta > delayTime) {
                        double inaccuracy = 0;
                        if (delta < 600) {
                            inaccuracy = 0.055 * 600 / delta * (Random.nextInt(2) * 2 - 1);
                        }
                        delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                        Bullet bullet = new Bullet(tm, x, y, inaccuracy,40);
                        bullet.setPosition(px, py);
                        int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                        if(criticalHits){
                            if(Math.random() > 0.9-nextCritChance){
                                damage*=increasedCritDamage ? 3 : 2;
                                nextCritChance=0;
                                bullet.setCritical(true);
                            } else if(nextCritChanceEnabled){
                                nextCritChance+=0.1f;
                            }
                        }
                        bullet.setDamage(damage);
                        bullets.add(bullet);
                        currentMagazineAmmo--;
                        GunsManager.bulletShooted++;
                        sendAddBulletPacket(bullet,x,y,px,py,username);

                        double atan = Math.atan2(y, x);
                        push = 80;
                        pushX = Math.cos(atan);
                        pushY = Math.sin(atan);

                    }
                } else if (currentAmmo != 0) {
                    reload();
                }
                setShooting(false);

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
            textRender.draw(builder.toString(),new Vector3f(1700,985,0),6,new Vector3f(0.886f,0.6f,0.458f));

        } else {
            textRender.draw(currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1700,985,0),2,new Vector3f(0.886f,0.6f,0.458f));
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
        if(!tm.isServerSide()){
            if (push > 0) push-=5;
            if (push < 0) push+=5;
            push = -push;
            tm.setPosition(tm.getX()+push*pushX,tm.getY()+push*pushY);
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
        checkCollisionsBullets(enemies,bullets);
    }

    @Override
    public void loadSave() {
        super.loadSave();

        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootrevolver.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadpistol.ogg");
        reloadsource.setPitch(0.75f);
        source.setPitch(1.2f);

        weaponHud = new Image("Textures\\revolver.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1810,975,0),1f);
        for(Bullet bullet : bullets){
            bullet.loadSave();
        }
    }
    @Override
    public void handleBulletPacket(Network.AddBullet response) {
        Bullet bullet = new Bullet(tm, response.id);
        bullet.setPosition(response.px, response.py);
        bullet.setCritical(response.critical);
        bullet.setDamage(response.damage);
        bullets.add(bullet);
    }

    @Override
    public void handleBulletMovePacket(Network.MoveBullet moveBullet) {
        for(Bullet b : bullets){
            if(b.getId() == moveBullet.id){
                b.setPosition(moveBullet.x, moveBullet.y);
            }
        }
    }
    @Override
    public void handleHitBulletPacket(Network.HitBullet hitBullet) {
        for(Bullet b : bullets){
            if(b.getId() == hitBullet.id){
                b.setHit(hitBullet.type);
                if(hitBullet.type == Bullet.TypeHit.ENEMY){
                    EnemyManager em = EnemyManager.getInstance();
                    Enemy e = em.handleHitEnemyPacket(hitBullet.idHit,b.getDamage());
                    showDamageIndicator(b.getDamage(),b.isCritical(),e);
                } else if (hitBullet.type == Bullet.TypeHit.ROOMOBJECT){
                    ArrayList<RoomObject> roomObjects = tm.getRoomMapObjects();
                    for(RoomObject obj : roomObjects){
                        if(obj.getId() == hitBullet.idHit){
                            if(obj instanceof DestroyableObject){
                                ((DestroyableObject) obj).setHit(b.getDamage());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void shootSound(Network.AddBullet response) {
        source.play(soundShoot);
        double atan = Math.atan2(response.y, response.x);
        push = 80;
        pushX = Math.cos(atan);
        pushY = Math.sin(atan);
    }
}
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
import cz.Empatix.Multiplayer.GunsManagerMP;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class M4 extends Weapon {
    public static void load(){
        Loader.loadImage("Textures\\M4.tga");
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
    private int bonusShots;
    private float lastX;
    private float lastY;

    private ArrayList<Bullet> bullets;
    private int speedBullet;

    private long timeShootSound;


   M4(TileMap tm, Player player, GunsManager gunsManager){
        super(tm,player,gunsManager);
        source.setVolume(0.15f);
        mindamage = 2;
        maxdamage = 2;
        inaccuracy = 0.5f;
        maxAmmo = 250;
        maxMagazineAmmo = 32;
        delayTime = 750;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootM4.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadM4.ogg");

        weaponHud = new Image("Textures\\M4.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1830,975,0),1f);

        speedBullet = 30;

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("m4","upgrades");
        if(numUpgrades >= 1){
            maxMagazineAmmo+=4;
            currentMagazineAmmo = maxMagazineAmmo;
        }
        if(numUpgrades >= 2){
            speedBullet = 36;
        }
        if(numUpgrades >= 3){
            maxdamage++;
        }
        if(numUpgrades >= 4){
            maxdamage++;
            mindamage++;
        }
    }

    public M4(TileMap tm, Player player){
        super(tm,player);
        mindamage = 2;
        maxdamage = 2;
        inaccuracy = 0.5f;
        maxAmmo = 250;
        maxMagazineAmmo = 32;
        delayTime = 750;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();

        speedBullet = 30;

    }
    // resetting stats of gun of new owner of gun
    @Override
    public void restat(int idPlayer, boolean fullAmmo) {
        mindamage = 2;
        maxdamage = 2;
        inaccuracy = 0.5f;
        maxAmmo = 250;
        maxMagazineAmmo = 32;
        delayTime = 750;

        GunsManagerMP gunsManagerMP = GunsManagerMP.getInstance();
        int numUpgrades = gunsManagerMP.getNumUpgrades(idPlayer, "M4");
        if(numUpgrades >= 1){
            maxMagazineAmmo+=4;
            currentMagazineAmmo = maxMagazineAmmo;
        }
        if(numUpgrades >= 2){
            speedBullet = 36;
        }
        if(numUpgrades >= 3){
            maxdamage++;
        }
        if(numUpgrades >= 4){
            maxdamage++;
            mindamage++;
        }
        if(!fullAmmo){
            if(currentAmmo > maxAmmo) currentAmmo = maxAmmo;
            if(currentMagazineAmmo > maxMagazineAmmo) currentMagazineAmmo = maxMagazineAmmo;
        }
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
        long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
        if(bonusShots > 0 && delta > 250-bonusShots*62.5 && delta < 550){
            double inaccuracy = 0;
            Bullet bullet = new Bullet(tm, lastX, lastY, inaccuracy,speedBullet);
            bullet.setPosition(px, py);
            int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
            bullet.setDamage(damage);
            bullets.add(bullet);
            GunsManager.bulletShooted++;
            bonusShots--;
            currentMagazineAmmo--;

            push += 35;
        }
        if(isShooting()) {
            if (currentMagazineAmmo != 0) {
                if (reloading) return;
                // delta - time between shoots
                // InGame.deltaPauseTime(); returns delayed time because of pause time
                if (delta > delayTime) {
                    double inaccuracy = 0;
                    delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                    lastX = x;
                    lastY = y;
                    Bullet bullet = new Bullet(tm, x, y, inaccuracy,speedBullet);
                    bullet.setPosition(px, py);
                    int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                    bullet.setDamage(damage);
                    bullets.add(bullet);
                    currentMagazineAmmo--;
                    GunsManager.bulletShooted++;
                    source.play(soundShoot);
                    for(int i = 0;i<3 && currentMagazineAmmo-i != 0;i++){
                        bonusShots++;
                    }
                    double atan = Math.atan2(y, x);
                    push = 30;
                    pushX = Math.cos(atan);
                    pushY = Math.sin(atan);
                }
            } else if (currentAmmo != 0) {
                reload();
            } else {
                source.play(soundEmptyShoot);
                outOfAmmo();
            }

        }
    }

    @Override
    public void shoot(float x, float y, float px, float py, int idPlayer) {
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
            }
        } else {
            long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
            if(bonusShots > 0 && delta > 250-bonusShots*62.5 && delta < 550){
                double inaccuracy = 0;
                Bullet bullet = new Bullet(tm, lastX, lastY, inaccuracy,speedBullet);
                bullet.setPosition(px, py);
                int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                bullet.setDamage(damage);
                bullet.setOwner(idPlayer);
                bullets.add(bullet);
                bonusShots--;
                currentMagazineAmmo--;
                sendAddBulletPacket(bullet,x,y,px,py,idPlayer);
                push += 35;
            }
            if(isShooting()) {
                if (currentMagazineAmmo != 0) {
                    if (reloading) return;
                    // delta - time between shoots
                    // InGame.deltaPauseTime(); returns delayed time because of pause time
                    if (delta > delayTime) {
                        double inaccuracy = 0;
                        delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                        lastX = x;
                        lastY = y;
                        Bullet bullet = new Bullet(tm, x, y, inaccuracy,speedBullet);
                        bullet.setPosition(px, py);
                        int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                        bullet.setDamage(damage);
                        bullet.setOwner(idPlayer);
                        bullets.add(bullet);
                        currentMagazineAmmo--;
                        sendAddBulletPacket(bullet,x,y,px,py,idPlayer);
                        for(int i = 0;i<3 && currentMagazineAmmo-i != 0;i++){
                            bonusShots++;
                        }
                        double atan = Math.atan2(y, x);
                        push = 30;
                        pushX = Math.cos(atan);
                        pushY = Math.sin(atan);
                    }
                } else if (currentAmmo != 0) {
                    reload();
                }

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
            textRender.draw(builder.toString(),new Vector3f(1710,985,0),6,new Vector3f(0.886f,0.6f,0.458f));

        } else {
            textRender.draw(currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1710,985,0),2,new Vector3f(0.886f,0.6f,0.458f));
        }
        weaponHud.draw();
        weaponAmmo.draw();

    }

    @Override
    public void update() {
        float time = (float)(System.currentTimeMillis()-reloadDelay-InGame.deltaPauseTime())/1000;
        dots = (int)((time / 1.3f) / 0.2f);
        if(reloading && time > 1.3f) {
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
    public boolean canSwap() {
        return !reloading && System.currentTimeMillis() - InGame.deltaPauseTime() - delay > delayTime/2 && bonusShots <= 0;
    }

    @Override
    public void loadSave() {
        super.loadSave();

        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootM4.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadM4.ogg");

        weaponHud = new Image("Textures\\M4.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1830,975,0),1f);
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
                    ArrayList<RoomObject> objects = tm.getRoomByCoords(b.getX(),b.getY()).getMapObjects();
                    for(RoomObject obj : objects){
                        if(obj.getId() == hitBullet.idHit) {
                            if (obj instanceof DestroyableObject) {
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
        if(System.currentTimeMillis() - timeShootSound > 650) {
            timeShootSound = System.currentTimeMillis();
            source.play(soundShoot);
            double atan = Math.atan2(response.y, response.x);
            push = 30;
            pushX = Math.cos(atan);
            pushY = Math.sin(atan);
        } else {
            push+=35;
        }
   }
}

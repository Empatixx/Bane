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

public class Pistol extends Weapon {
    public static void load(){
        Loader.loadImage("Textures\\pistol.tga");
        Loader.loadImage("Textures\\pistol_bullet.tga");
    }
    // map push when player shoot
    private int push;
    private double pushX;
    private double pushY;

    // audio
    private int[] soundShoot;
    private int soundEmptyShoot;
    private int soundReload;

    private int dots;

    private ArrayList<Bullet> bullets;

    private boolean doubleShots;
    private boolean secondShotReady;
    private float lastX;
    private float lastY;

    Pistol(TileMap tm, Player player){
        super(tm,player);
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
    public Pistol(TileMap tm, Player[] player){
        super(tm,player);
        mindamage = 1;
        maxdamage = 3;
        inaccuracy = 0.8f;
        maxAmmo = 120;
        maxMagazineAmmo = 71;
        delayTime = 250;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();
    }

    // resetting stats of gun of new owner of gun
    @Override
    public void restat(int idPlayer, boolean fullAmmo) {
        mindamage = 1;
        maxdamage = 3;
        inaccuracy = 0.8f;
        maxAmmo = 120;
        maxMagazineAmmo = 7;
        delayTime = 250;

        GunsManagerMP gunsManagerMP = GunsManagerMP.getInstance();
        int numUpgrades = gunsManagerMP.getNumUpgrades(idPlayer, "Pistol");
        if(numUpgrades >= 1){
            maxMagazineAmmo+=2;
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
        if(!fullAmmo){
            if(currentAmmo > maxAmmo) currentAmmo = maxAmmo;
            if(currentMagazineAmmo > maxMagazineAmmo) currentMagazineAmmo = maxMagazineAmmo;
        } else {
            currentAmmo = maxAmmo;
            currentMagazineAmmo = maxMagazineAmmo;
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

    // SINGLEPLAYER
    @Override
    public void shoot(float x, float y, float px, float py) {
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
            setShooting(false);
        }
    }
    // MULTIPLAYER
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
                setShooting(false);

            }
        } else {
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
                bullet.setOwner(idPlayer);

                bullets.add(bullet);
                currentMagazineAmmo--;
                secondShotReady=false;
                sendAddBulletPacket(bullet,x,y,px,py,idPlayer,false);
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
                        bullet.setOwner(idPlayer);

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

                        sendAddBulletPacket(bullet,x,y,px,py,idPlayer,true);

                        lastX = x;
                        lastY = y;
                        if(currentMagazineAmmo > 0 && doubleShots) secondShotReady = true;

                        double atan = Math.atan2(y, x);
                        push = 30;
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
    public boolean canSwap() {
        return !reloading && System.currentTimeMillis()  - InGame.deltaPauseTime()- delay > delayTime/2 && !secondShotReady;
    }

    @Override
    public void handleAddBulletPacket(Network.AddBullet response) {
        if(response.makeSound) {
            source.play(soundShoot[cz.Empatix.Java.Random.nextInt(2)]);
            if(response.idPlayer == MultiplayerManager.getInstance().getIdConnection()){
                double atan = Math.atan2(response.y, response.x);
                push = 30;
                pushX = Math.cos(atan);
                pushY = Math.sin(atan);
            }
        }
        Bullet bullet = new Bullet(tm, response.id);
        bullet.setPosition(response.px, response.py);
        bullet.setCritical(response.critical);
        bullet.setDamage(response.damage);
        bullets.add(bullet);
    }

    @Override
    public void handleMoveBulletPacket(Network.MoveBullet moveBullet) {
        for(Bullet b : bullets){
            if(b.id== moveBullet.id){
                b.setPosition(moveBullet.x, moveBullet.y);
            }
        }
    }
    @Override
    public void handleHitBulletPacket(Network.HitBullet hitBullet) {
        for(Bullet b : bullets){
            if(b.id == hitBullet.id && !b.isHit()){
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
}
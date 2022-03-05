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

public class Luger extends Weapon {
    public static void load(){
        Loader.loadImage("Textures\\lahti.tga");
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

    private int bonusShots;
    private float chanceBonusShots;
    private boolean bonusShotsAntiConsume; // bonus shots do not consume amm
    private int lastDamage;
    private boolean lastDamageCrit;

    Luger(TileMap tm, Player player, GunsManager gunsManager){
        super(tm,player,gunsManager);
        mindamage = 1;
        maxdamage = 2;
        inaccuracy = 0.8f;
        maxAmmo = 120;
        maxMagazineAmmo = 9;
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
        reloadsource.setPitch(1.3f);

        weaponHud = new Image("Textures\\lahti.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1810,975,0),1f);

        bonusShots = 0;
        chanceBonusShots = 0.2f;

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("luger","upgrades");
        if(numUpgrades >= 1){
            maxdamage++;
            mindamage++;
        }
        if(numUpgrades >= 2){
            chanceBonusShots+=0.2f;
        }
        if(numUpgrades >= 3){
            bonusShotsAntiConsume = true;
        }
        if(numUpgrades >= 4){
            criticalHits = true;
        }
    }

    public Luger(TileMap tm, Player player){
        super(tm,player);
        mindamage = 1;
        maxdamage = 2;
        inaccuracy = 0.8f;
        maxAmmo = 120;
        maxMagazineAmmo = 9;
        delayTime = 250;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();

        bonusShots = 0;
        chanceBonusShots = 0.2f;
    }
    // resetting stats of gun of new owner of gun
    @Override
    public void restat(String username, boolean fullAmmo) {
        mindamage = 1;
        maxdamage = 2;
        inaccuracy = 0.8f;
        maxAmmo = 120;
        maxMagazineAmmo = 9;
        delayTime = 250;

        GunsManagerMP gunsManagerMP = GunsManagerMP.getInstance();
        int numUpgrades = gunsManagerMP.getNumUpgrades(username, "Luger");
        if(numUpgrades >= 1){
            maxdamage++;
            mindamage++;
        }
        if(numUpgrades >= 2){
            chanceBonusShots+=0.2f;
        }
        if(numUpgrades >= 3){
            bonusShotsAntiConsume = true;
        }
        if(numUpgrades >= 4){
            criticalHits = true;
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
        if(bonusShots > 0 && delta > delayTime-bonusShots*16.6){
            double inaccuracy = 0;
            inaccuracy = 0.055 * delta / (delay-bonusShots*16.6) * (Random.nextInt(2) * 2 - 1);
            Bullet bullet = new Bullet(tm, x, y, inaccuracy,30);
            bullet.setPosition(px, py);
            bullet.setDamage(lastDamage);
            bullet.setCritical(lastDamageCrit);
            bullets.add(bullet);
            GunsManager.bulletShooted++;
            source.setPitch(1.1f+0.15f*bonusShots);
            source.play(soundShoot[Random.nextInt(2)]);

            bonusShots--;
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
                    lastDamageCrit = false;
                    if(criticalHits){
                        if(Math.random() > 0.9){
                            damage*=2;
                            bullet.setCritical(true);
                            lastDamageCrit = true;
                        }
                    }
                    lastDamage = damage;
                    bullet.setDamage(damage);
                    bullets.add(bullet);
                    currentMagazineAmmo--;
                    GunsManager.bulletShooted++;
                    source.setPitch(1.1f);
                    source.play(soundShoot[Random.nextInt(2)]);
                    while(Math.random() > 1-chanceBonusShots && currentMagazineAmmo != 0 && bonusShots < 9){
                        bonusShots++;
                        if(!bonusShotsAntiConsume)currentMagazineAmmo--;
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
            long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
            if(isShooting()) {
                if (currentMagazineAmmo != 0) {
                    if (reloading) return;
                    // delta - time between shoots
                    // InGame.deltaPauseTime(); returns delayed time because of pause time
                    if(bonusShots > 0 && delta > delayTime-bonusShots*16.6){
                        double inaccuracy = 0;
                        inaccuracy = 0.055 * delta / (delay-bonusShots*16.6) * (Random.nextInt(2) * 2 - 1);
                        Bullet bullet = new Bullet(tm, x, y, inaccuracy,30);
                        bullet.setPosition(px, py);
                        bullet.setDamage(lastDamage);
                        bullet.setCritical(lastDamageCrit);
                        bullet.setOwner(username);
                        bullets.add(bullet);
                        bonusShots--;
                        sendAddBulletPacket(bullet,x,y,px,py,username);
                    }
                    if (delta > delayTime) {
                        double inaccuracy = 0;
                        if (delta < 400) {
                            inaccuracy = 0.055 * 400 / delta * (Random.nextInt(2) * 2 - 1);
                        }
                        delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                        Bullet bullet = new Bullet(tm, x, y, inaccuracy,30);
                        bullet.setPosition(px, py);
                        int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                        lastDamageCrit = false;
                        if(criticalHits){
                            if(Math.random() > 0.9){
                                damage*=2;
                                bullet.setCritical(true);
                                lastDamageCrit = true;
                            }
                        }
                        lastDamage = damage;
                        bullet.setDamage(damage);
                        bullet.setOwner(username);
                        bullets.add(bullet);
                        currentMagazineAmmo--;
                        sendAddBulletPacket(bullet,x,y,px,py,username);
                        while(Math.random() > 1-chanceBonusShots && currentMagazineAmmo != 0 && bonusShots < 9){
                            bonusShots++;
                            if(!bonusShotsAntiConsume)currentMagazineAmmo--;
                        }
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
        return !reloading && System.currentTimeMillis() - InGame.deltaPauseTime() - delay > delayTime/2 && bonusShots <= 0;
    }

    @Override
    public void loadSave() {
        super.loadSave();

        // shooting
        soundShoot = new int[2];
        soundShoot[0] = AudioManager.loadSound("guns\\shootpistol_1.ogg");
        soundShoot[1] = AudioManager.loadSound("guns\\shootpistol_2.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadpistol.ogg");
        reloadsource.setPitch(1.3f);

        weaponHud = new Image("Textures\\lahti.tga",new Vector3f(1600,975,0),2f);
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
        source.play(soundShoot[cz.Empatix.Java.Random.nextInt(2)]);
        double atan = Math.atan2(response.y, response.x);
        push = 30;
        pushX = Math.cos(atan);
        pushY = Math.sin(atan);
    }
}
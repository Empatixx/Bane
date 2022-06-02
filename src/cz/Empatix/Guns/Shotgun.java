package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.RoomObjects.DestroyableObject;
import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Multiplayer.GunsManagerMP;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import cz.Empatix.Utility.Random;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Shotgun extends Weapon {
    public static void load(){
        Loader.loadImage("Textures\\shotgun.tga");
        Loader.loadImage("Textures\\shotgun_bullet.tga");
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

    Shotgun(TileMap tm, Player player){
        super(tm, player);
        mindamage = 2;
        maxdamage = 3;
        inaccuracy = 0.7f;
        maxAmmo = 48;
        maxMagazineAmmo = 6;
        type = 3;
        delayTime = 500;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootshotgun.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadshotgun.ogg");

        weaponHud = new Image("Textures\\shotgun.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\shotgun_bullet.tga",new Vector3f(1810,975,0),1f);


        int numUpgrades = GameStateManager.getDb().getValueUpgrade("shotgun","upgrades");
        if(numUpgrades >= 1){
            maxAmmo += 6;
        }
        if(numUpgrades >= 2){
            maxMagazineAmmo++;
            currentMagazineAmmo = maxMagazineAmmo;
        }
        if(numUpgrades >= 3){
            maxdamage++;
        }
        if(numUpgrades >= 4){
            delayTime = (int)(delayTime*0.80f);
        }

    }
    public Shotgun(TileMap tm, Player[] player){
        super(tm, player);
        mindamage = 2;
        maxdamage = 3;
        inaccuracy = 0.7f;
        maxAmmo = 48;
        maxMagazineAmmo = 6;
        type = 3;
        delayTime = 500;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        bullets = new ArrayList<>();

    }
    // resetting stats of gun of new owner of gun
    @Override
    public void restat(int idPlayer, boolean fullAmmo) {
        mindamage = 2;
        maxdamage = 3;
        inaccuracy = 0.7f;
        maxAmmo = 48;
        maxMagazineAmmo = 6;
        delayTime = 500;
        GunsManagerMP gunsManagerMP = GunsManagerMP.getInstance();
        int numUpgrades = gunsManagerMP.getNumUpgrades(idPlayer, "Shotgun");
        if(numUpgrades >= 1){
            maxAmmo += 6;
        }
        if(numUpgrades >= 2){
            maxMagazineAmmo++;
        }
        if(numUpgrades >= 3){
            maxdamage++;
        }
        if(numUpgrades >= 4){
            delayTime = (int)(delayTime*0.80f);
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

    @Override
    public void shoot(float x, float y, float px, float py) {
        if(isShooting()) {
            if (currentMagazineAmmo != 0) {
                if (reloading) return;
                // delta - time between shoots
                long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
                if (delta > delayTime) {
                    source.play(soundShoot);
                    for (int i = 0; i < 4; ) {
                        double inaccuracy = 0.055 * i;

                        Bullet bullet = new Bullet(tm, x, y, inaccuracy,1800);
                        bullet.setPosition(px, py);
                        bullets.add(bullet);
                        int damage;
                        if(i <= 1){
                            damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                        } else {
                            damage = Random.nextInt(maxdamage-mindamage) + mindamage-1;
                        }
                        bullet.setDamage(damage);
                        if (i >= 0) i++;
                        else i--;
                        i = -i;
                        GunsManager.bulletShooted++;
                    }
                    currentMagazineAmmo--;
                    delay = System.currentTimeMillis() - InGame.deltaPauseTime();

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
            if(isShooting()) {
                if (currentMagazineAmmo != 0) {
                    if (reloading) return;
                    // delta - time between shoots
                    long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
                    if (delta > delayTime) {
                        boolean first = false;
                        for (int i = 0; i < 4; ) {
                            double inaccuracy = 0.055 * i;

                            Bullet bullet = new Bullet(tm, x, y, inaccuracy,1800);
                            bullet.setPosition(px, py);
                            bullet.setOwner(idPlayer);
                            bullets.add(bullet);
                            int damage;
                            if(i <= 1){
                                damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                            } else {
                                damage = Random.nextInt(maxdamage-mindamage) + mindamage-1;
                            }
                            bullet.setDamage(damage);
                            if (i >= 0) i++;
                            else i--;
                            i = -i;
                            sendAddBulletPacket(bullet,x,y,px,py,idPlayer,first);
                            if(!first) first = true;
                        }
                        currentMagazineAmmo--;
                        delay = System.currentTimeMillis() - InGame.deltaPauseTime();

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
    public void draw() {
        if(reloading){
            StringBuilder builder = new StringBuilder();
            for(int i = 0;i<=dots;i++) builder.append(".");
            textRender.draw(builder.toString(),new Vector3f(1715,985,0),6,new Vector3f(0.886f,0.6f,0.458f));

        } else {
            textRender.draw(currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1715,985,0),2,new Vector3f(0.886f,0.6f,0.458f));
        }
        weaponHud.draw();
        weaponAmmo.draw();
    }

    @Override
    public void drawAmmo() {
        for(Bullet bullet:bullets){
            bullet.draw();
        }
    }

    @Override
    public void update() {
        float time = (float)(System.currentTimeMillis()-reloadDelay- InGame.deltaPauseTime())/1000;
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
            tm.setPosition(tm.getX()+push*pushX,tm.getY()+push*pushY,true);
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
    public void handleAddBulletPacket(Network.AddBullet response) {
        if(response.makeSound) {
            source.play(soundShoot);
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
    public boolean handleMoveBulletPacket(Network.MoveBullet moveBullet) {
        for(Bullet b : bullets){
            if(b.getId() == moveBullet.id){
                b.addInterpolationPosition(moveBullet);
                return true;
            }
        }
        return false;
    }
    @Override
    public void handleHitBulletPacket(Network.HitBullet hitBullet) {
        for(Bullet b : bullets){
            if(b.getId() == hitBullet.id && !b.isHit()){
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
                            break;
                        }
                    }
                }
                break;
            }
        }
    }
}

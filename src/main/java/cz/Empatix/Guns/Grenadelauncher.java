package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.RoomObjects.DestroyableObject;
import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Multiplayer.GunsManagerMP;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import cz.Empatix.Utility.Random;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Grenadelauncher extends Weapon {
    public static void load(){
        Loader.loadImage("Textures\\grenadelauncher.tga");
        Loader.loadImage("Textures\\rocket-ammo.tga");
    }
    // map push when player shoot
    // audio
    private int soundShoot;
    private int soundEmptyShoot;
    private int soundReload;

    private int dots;

    private ArrayList<Grenadebullet> bullets;

    Grenadelauncher(TileMap tm, Player player){
        super(tm,player);
        mindamage = 4;
        maxdamage = 7;
        inaccuracy = 0.7f;
        maxAmmo = 36;
        maxMagazineAmmo = 6;
        type = 4;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = AudioManager.loadSound("guns\\grenadelaunchershoot.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\grenadelauncherreload.ogg");

        source.setPitch(0.75f);

        weaponHud = new Image("Textures\\grenadelauncher.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\rocket-ammo.tga",new Vector3f(1830,975,0),2f);

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("grenadelauncher","upgrades");
        if(numUpgrades >= 1){
            maxAmmo+=8;
            currentAmmo=maxAmmo;
        }
        if(numUpgrades >= 2){
            maxMagazineAmmo+=3;
            currentMagazineAmmo=maxMagazineAmmo;
        }
        if(numUpgrades >= 3){
            maxdamage+=2;
            mindamage+=2;
        }
        if(numUpgrades >= 4){
            criticalHits=true;
        }
    }
    public Grenadelauncher(TileMap tm, Player[] player){
        super(tm,player);
        mindamage = 4;
        maxdamage = 7;
        inaccuracy = 0.7f;
        maxAmmo = 36;
        maxMagazineAmmo = 6;
        type = 4;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        bullets = new ArrayList<>();
    }
    // resetting stats of gun of new owner of gun
    @Override
    public void restat(int idPlayer, boolean fullAmmo) {
        mindamage = 4;
        maxdamage = 7;
        inaccuracy = 0.7f;
        maxAmmo = 36;
        maxMagazineAmmo = 6;
        GunsManagerMP gunsManagerMP = GunsManagerMP.getInstance();
        int numUpgrades = gunsManagerMP.getNumUpgrades(idPlayer, "Grenade Launcher");
        if(numUpgrades >= 1){
            maxAmmo+=8;
        }
        if(numUpgrades >= 2){
            maxMagazineAmmo+=3;
        }
        if(numUpgrades >= 3){
            maxdamage+=2;
            mindamage+=2;
        }
        if(numUpgrades >= 4){
            criticalHits=true;
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
        if(isShooting()) {
            if (currentMagazineAmmo != 0) {
                if (reloading) return;
                // delta - time between shoots
                long delta = System.currentTimeMillis() - delay - InGame.deltaPauseTime();
                if (delta > 450) {
                    source.play(soundShoot);
                    double inaccuracy = 0;

                    delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                    Grenadebullet bullet = new Grenadebullet(tm, x, y, inaccuracy,1500);
                    int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                    if(criticalHits){
                        if(Math.random() > 0.9){
                            damage*=2;
                            bullet.setCritical(true);
                        }
                    }
                    bullet.setDamage(damage);
                    bullet.setPosition(px, py);
                    bullets.add(bullet);
                    currentMagazineAmmo--;
                    GunsManager.bulletShooted++;

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
                    if (delta > 550) {
                        double inaccuracy = 0;
                        delay = System.currentTimeMillis() - InGame.deltaPauseTime();
                        Grenadebullet bullet = new Grenadebullet(tm, x, y, inaccuracy,1500);
                        int damage = Random.nextInt(maxdamage+1-mindamage) + mindamage;
                        if(criticalHits){
                            if(Math.random() > 0.9){
                                damage*=2;
                                bullet.setCritical(true);
                            }
                        }
                        bullet.setDamage(damage);
                        bullet.setPosition(px, py);
                        bullets.add(bullet);
                        sendAddBulletPacket(bullet,x,y,px,py,idPlayer,true);
                        currentMagazineAmmo--;
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
            textRender.draw(builder.toString(),new Vector3f(1730,985,0),6,new Vector3f(0.886f,0.6f,0.458f));

        } else {
            textRender.draw(currentMagazineAmmo+"/"+currentAmmo,new Vector3f(1730,985,0),2,new Vector3f(0.886f,0.6f,0.458f));
        }
        weaponHud.draw();
        weaponAmmo.draw();
    }

    @Override
    public void drawAmmo() {
        for(Grenadebullet bullet:bullets){
            bullet.draw();
        }
    }

    @Override
    public void update() {
        float time = (float)(System.currentTimeMillis()-reloadDelay- InGame.deltaPauseTime())/1000;
        dots = (int)((time / 2.2f) / 0.2f);
        if(reloading && time > 2.2f) {

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
        A: for(Grenadebullet bullet:bullets){
            for(Enemy enemy:enemies){
                if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
                    bullet.setHit();
                    GunsManager.hitBullets++;
                    continue A;
                }
            }
            ArrayList<RoomObject>[] objectsArray = tm.getRoomMapObjects();
            for(ArrayList<RoomObject> objects : objectsArray){
                if(objects == null) continue;
                for(RoomObject object: objects){
                    if(object instanceof DestroyableObject) {
                        if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                            bullet.setHit();
                            continue A;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleAddBulletPacket(Network.AddBullet response) {
        if(response.makeSound) {
            source.play(soundShoot);
        }
        Grenadebullet bullet = new Grenadebullet(tm, response.id);
        bullet.setPosition(response.px, response.py);
        bullet.setCritical(response.critical);
        bullet.setDamage(response.damage);
        bullets.add(bullet);
    }
    @Override
    public boolean handleMoveBulletPacket(Network.MoveBullet moveBullet) {
        for(Grenadebullet b : bullets){
            if(b.getId() == moveBullet.id && !b.isHit()){
                b.setPosition(moveBullet.x, moveBullet.y);
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleHitBulletPacket(Network.HitBullet hitBullet) {
         for(Grenadebullet b : bullets){
            if(b.id == hitBullet.id && !b.isHit()){
                b.setHit();
                break;
            }
         }
    }
}

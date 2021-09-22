package cz.Empatix.Guns;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Damageindicator.DamageIndicator;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Submachine extends Weapon{
    public static void load(){
        Loader.loadImage("Textures\\submachine.tga");
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
    private boolean chanceToNotConsumeAmmo;

    Submachine(TileMap tm, Player player, GunsManager gunsManager){
        super(tm,player,gunsManager);
        source.setVolume(0.15f);
        mindamage = 1;
        maxdamage = 2;
        inaccuracy = 0.5f;
        maxAmmo = 200;
        maxMagazineAmmo = 20;
        delayTime = 150;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
        type = 1;
        bullets = new ArrayList<>();
        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootsubmachine.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadpistol.ogg");
        reloadsource.setPitch(1.5f);

        weaponHud = new Image("Textures\\submachine.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1830,975,0),1f);

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("uzi","upgrades");
        if(numUpgrades >= 1){
            delayTime = 105;
        }
        if(numUpgrades >= 2){
            maxdamage++;
        }
        if(numUpgrades >= 3){
            chanceToNotConsumeAmmo = true;
        }
        if(numUpgrades >= 4){
            criticalHits = true;
        }
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
                if (delta > delayTime) {
                    double inaccuracy = 0;
                    if (delta < 400) {
                        inaccuracy = (Math.random() * 0.155) * (Random.nextInt(2) * 2 - 1);
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
                    if(MultiplayerManager.multiplayer){
                        Network.AddBullet addBullet = new Network.AddBullet();
                        addBullet.x = x;
                        addBullet.y = y;
                        addBullet.px = px;
                        addBullet.py = py;
                        addBullet.inaccuracy = (float)inaccuracy;
                        addBullet.speed = 30;
                        addBullet.damage = damage;
                        addBullet.critical = bullet.isCritical();

                        addBullet.indexWeapon = gunsManager.getIndexOfCurrentWeapon();

                        Client client = MultiplayerManager.getInstance().client.getClient();
                        client.sendTCP(addBullet);
                    }
                    if(chanceToNotConsumeAmmo){
                        if(Math.random() <= 0.8){
                            currentMagazineAmmo--;
                        }
                    } else {
                        currentMagazineAmmo--;
                    }
                    source.play(soundShoot);
                    GunsManager.bulletShooted++;

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
        ArrayList<RoomObject> objects = tm.getRoomMapObjects();
        A: for(Bullet bullet:bullets){
            for(Enemy enemy:enemies){
                if(bullet.intersects(enemy) && enemy.canReflect()){
                    Vector3f speed = bullet.getSpeed();
                    speed.x = -speed.x;
                    speed.y = -speed.y;
                    bullet.setFriendlyFire(true);
                    continue;
                }
                if(bullet.isFriendlyFire()){
                    if(bullet.intersects(player) && !bullet.isHit() && !player.isDead() && !player.isFlinching()){
                        player.hit(bullet.getDamage());
                        bullet.setHit();
                        GunsManager.hitBullets++;
                    }
                }
                else if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
                    enemy.hit(bullet.getDamage());
                    int cwidth = enemy.getCwidth();
                    int cheight = enemy.getCheight();
                    int x = -cwidth/4+Random.nextInt(cwidth/2);
                    if(bullet.isCritical()){
                        DamageIndicator.addCriticalDamageShow(bullet.getDamage(),(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                                ,new Vector2f(-x/25f,-1f));
                    } else {
                        DamageIndicator.addDamageShow(bullet.getDamage(),(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                                ,new Vector2f(-x/25f,-1f));
                    }
                    bullet.playEnemyHit();
                    bullet.setHit();
                    GunsManager.hitBullets++;
                    continue A;
                }
            }
            for(RoomObject object: objects){
                if(object instanceof DestroyableObject) {
                    if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                        bullet.playEnemyHit();
                        bullet.setHit();
                        ((DestroyableObject) object).setHit(bullet.getDamage());
                        continue A;
                    }
                }
            }
        }
    }

    @Override
    public void loadSave() {
        super.loadSave();

        // shooting
        soundShoot = AudioManager.loadSound("guns\\shootsubmachine.ogg");
        // shooting without ammo
        soundEmptyShoot = AudioManager.loadSound("guns\\emptyshoot.ogg");
        soundReload = AudioManager.loadSound("guns\\reloadpistol.ogg");
        reloadsource.setPitch(1.5f);

        weaponHud = new Image("Textures\\submachine.tga",new Vector3f(1600,975,0),2f);
        weaponAmmo = new Image("Textures\\pistol_bullet.tga",new Vector3f(1830,975,0),1f);
        for(Bullet bullet : bullets){
            bullet.loadSave();
        }
    }
    @Override
    public void handleBulletPacket(Network.AddBullet addBullet) {
        Bullet bullet = new Bullet(tm,addBullet.x,addBullet.y,addBullet.inaccuracy,addBullet.speed);
        bullet.setCritical(addBullet.critical);
        bullet.setDamage(addBullet.damage);
        bullet.setPosition(addBullet.px, addBullet.py);

        bullets.add(bullet);
    }
}

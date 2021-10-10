package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Random;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.Damageindicator.DamageIndicator;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public abstract class Weapon{
    protected String name;
    private boolean shooting;
    //ammo vars
    protected final TileMap tm;
    protected final Player player;
    protected GunsManager gunsManager;
    // dmg
    protected int mindamage;
    protected int maxdamage;

    protected float inaccuracy;
    protected long delay; // between shoots
    protected int delayTime; // between shoots
    protected boolean criticalHits;

    protected long reloadDelay;
    protected boolean reloading;

    protected int currentAmmo;
    protected int maxAmmo;
    protected int currentMagazineAmmo;
    protected int maxMagazineAmmo;

    boolean alreadyDropped;

    protected Source source;
    protected Source reloadsource;

    transient Image weaponHud;
    transient Image weaponAmmo;

    TextRender textRender;

    private long alertCooldown;


    /*
    0 - MELEE
    1 - PISTOL
    2 - SUBMACHINE
    3 - SHOTGUN
    4 - SPECIAL
     */
    protected int type;

    Weapon(TileMap tm, Player player, GunsManager gunsManager) {
        this.tm = tm;
        this.gunsManager = gunsManager;
        this.player = player;
        source = AudioManager.createSource(Source.EFFECTS, 0.35f);
        reloadsource = AudioManager.createSource(Source.EFFECTS, 0.35f);

        textRender = new TextRender();
    }

    public Weapon(TileMap tm, Player player) {
        this.tm = tm;
        this.player = player;
    }

    // SINGLEPLAYER
    public abstract void shoot(float x, float y, float px, float py);
    // MULTIPLAYER
    public abstract void shoot(float x, float y, float px, float py, String username);

    public abstract void reload();


    public void setMindamage(int mindamage) {
        this.mindamage = mindamage;
    }

    public void setMaxdamage(int maxdamage) {
        this.maxdamage = maxdamage;
    }

    public void setType(int type) {
        this.type = type;
    }

    public abstract void draw();

    public abstract void update();

    public abstract void drawAmmo();

    public abstract void updateAmmo();

    public abstract void checkCollisions(ArrayList<Enemy> enemies);

    public boolean isReloading() {
        return reloading;
    }

    public int getType() {
        return type;
    }

    public void addAmmo(int amountprocent) {
        currentAmmo += Math.ceil((float) (amountprocent) / 100 * maxAmmo);
        if (currentAmmo > maxAmmo) currentAmmo = maxAmmo;
    }

    public boolean isShooting() {
        return shooting;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public Image getWeaponHud() {
        return weaponHud;
    }

    public boolean isFullAmmo() {
        return maxAmmo == currentAmmo;
    }

    public boolean hasAlreadyDropped() {
        return alreadyDropped;
    }

    public void drop() {
        alreadyDropped = true;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
    }

    public void despawn() {
        alreadyDropped = false;
    }

    public boolean canSwap() {
        return !reloading && System.currentTimeMillis() - InGame.deltaPauseTime() - delay > delayTime / 2;
    }

    public void loadSave() {
        source = AudioManager.createSource(Source.EFFECTS, 0.35f);
        reloadsource = AudioManager.createSource(Source.EFFECTS, 0.35f);

        textRender = new TextRender();
    }

    public void outOfAmmo() {
        if (System.currentTimeMillis() - alertCooldown > 2000) {
            AlertManager.add(AlertManager.WARNING, "You're out of ammo!");
            alertCooldown = System.currentTimeMillis();
        }
    }

    public abstract void handleBulletPacket(Network.AddBullet response);

    public abstract void handleBulletMovePacket(Network.MoveBullet moveBullet);

    public abstract void handleHitBulletPacket(Network.HitBullet hitBullet);

    public void handleWeaponInfoPacket(Network.WeaponInfo weaponInfo){
        currentAmmo = weaponInfo.currentAmmo;
        currentMagazineAmmo = weaponInfo.currentMagazineAmmo;
    }

    public void showDamageIndicator(int damage, boolean critical, Enemy enemy){
        int cwidth = enemy.getCwidth();
        int cheight = enemy.getCheight();
        int x = -cwidth/4+ Random.nextInt(cwidth/2);
        if(critical){
            DamageIndicator.addCriticalDamageShow(damage,(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                    ,new Vector2f(-x/25f,-1f));
        } else {
            DamageIndicator.addDamageShow(damage,(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                    ,new Vector2f(-x/25f,-1f));
        }
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public int getCurrentMagazineAmmo() {
        return currentMagazineAmmo;
    }

    public void checkCollisionsBullets(ArrayList<Enemy> enemies, ArrayList<Bullet> bullets){
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
                        bullet.setHit(Bullet.TypeHit.PLAYER);
                        GunsManager.hitBullets++;
                    }
                }
                else if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
                    enemy.hit(bullet.getDamage());
                    if(!tm.isServerSide()){
                        showDamageIndicator(bullet.getDamage(),bullet.isCritical(),enemy);
                    }
                    bullet.setHit(Bullet.TypeHit.ENEMY, enemy.getId());
                    GunsManager.hitBullets++;
                    continue A;
                }
            }
            for(RoomObject object: objects){
                if(object instanceof DestroyableObject) {
                    if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                        bullet.setHit(Bullet.TypeHit.ROOMOBJECT);
                        ((DestroyableObject) object).setHit(bullet.getDamage());
                        continue A;
                    }
                }
            }
        }
    }
    public abstract void shootSound();
}
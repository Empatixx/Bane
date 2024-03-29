package cz.Empatix.Guns;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Buffs.BuffManager;
import cz.Empatix.Buffs.BuffManagerMP;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.RoomObjects.DestroyableObject;
import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Multiplayer.GunsManagerMP;
import cz.Empatix.Multiplayer.MPStatistics;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.Damageindicator.CombatIndicator;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public abstract class Weapon{
    protected String name;
    private boolean shooting;
    //ammo vars
    protected final TileMap tm;
    protected final Player player[];
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

    Weapon(TileMap tm, Player player) {
        this.tm = tm;
        this.player = new Player[]{player};
        source = AudioManager.createSource(Source.EFFECTS, 0.35f);
        reloadsource = AudioManager.createSource(Source.EFFECTS, 0.35f);

        textRender = new TextRender();
    }

    public Weapon(TileMap tm, Player[] player) {
        this.tm = tm;
        this.player = player;
    }

    // SINGLEPLAYER
    public abstract void shoot(float x, float y, float px, float py);
    // MULTIPLAYER
    public abstract void shoot(float x, float y, float px, float py, int idPlayer);

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

    public void addAmmo(int amountpercent) {
        currentAmmo += Math.ceil((float) (amountpercent) / 100 * maxAmmo);
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

    public void outOfAmmo() {
        if (System.currentTimeMillis() - alertCooldown > 2000) {
            AlertManager.add(AlertManager.WARNING, "You're out of ammo!");
            alertCooldown = System.currentTimeMillis();
        }
    }

    public abstract void handleAddBulletPacket(Network.AddBullet response);

    public abstract boolean handleMoveBulletPacket(Network.MoveBullet moveBullet);

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
            CombatIndicator.addCriticalDamageShow(damage,(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                    ,new Vector2f(-x/10f,-30f));
        } else {
            CombatIndicator.addDamageShow(damage,(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                    ,new Vector2f(-x/10f,-30f));
        }
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public int getCurrentMagazineAmmo() {
        return currentMagazineAmmo;
    }

    public void checkCollisionsBullets(ArrayList<Enemy> enemies, ArrayList<Bullet> bullets){
        A: for(Bullet bullet:bullets){
            for(Enemy enemy:enemies){
                if(bullet.intersects(enemy) && enemy.canReflect()){
                    Vector2f acceleration = bullet.getAcceleration();
                    Vector3f speed = bullet.getSpeed();
                    acceleration.x = -acceleration.x;
                    acceleration.y = -acceleration.y;
                    speed.x = -speed.x;
                    speed.y = -speed.y;
                    bullet.setFriendlyFire(true);
                    continue A;
                }
                if(bullet.isFriendlyFire()){
                    for(Player p : player){
                        if(p == null) continue;
                        if(bullet.intersects(p) && !bullet.isHit() && !p.isDead() && !p.isFlinching()){
                            p.hit(bullet.getDamage());
                            bullet.setHit(Bullet.TypeHit.PLAYER);
                            GunsManager.hitBullets++;
                        }
                    }
                }
                else if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
                    enemy.hit(bullet.getDamage());
                    if(enemy.isDead() && tm.isServerSide()){
                        MultiplayerManager mpManager = MultiplayerManager.getInstance();
                        // increasing statistic of killed enemies by player
                        MPStatistics mpStatistics = mpManager.server.getMpStatistics();
                        int owner = bullet.getPlayerOwner();
                        if(owner != 0) mpStatistics.addEnemiesKill(bullet.getPlayerOwner());
                    }
                    if(!tm.isServerSide()){
                        showDamageIndicator(bullet.getDamage(),bullet.isCritical(),enemy);
                    }
                    bullet.setHit(Bullet.TypeHit.ENEMY, enemy.getId());
                    GunsManager.hitBullets++;
                    continue A;
                }
            }
            ArrayList<RoomObject>[] objectsArray = tm.getRoomMapObjects();
            for(ArrayList<RoomObject> objects : objectsArray) {
                if (objects == null) continue;
                for(RoomObject object: objects){
                    if(object instanceof DestroyableObject) {
                        if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                            bullet.setHit(Bullet.TypeHit.ROOMOBJECT,object.getId());
                            ((DestroyableObject) object).setHit(bullet.getDamage());
                            continue A;
                        }
                    }
                }
            }
        }
    }
    public void sendAddBulletPacket(Bullet bullet, float x, float y, float px, float py, int idPlayer, boolean makeSound){
        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        // increasing statistic of shooted bullets so we can calculate accuracy
        MPStatistics mpStatistics = mpManager.server.getMpStatistics();
        mpStatistics.addBulletShoot(idPlayer);
        // sending new bullet as packet
        Network.AddBullet response = new Network.AddBullet();
        mpManager.server.requestACK(response,response.idPacket);
        response.x = x;
        response.y = y;
        response.px = px;
        response.py = py;
        response.critical = bullet.isCritical();
        response.speed = 30;
        response.damage = (byte)bullet.getDamage();
        response.id = bullet.getId();
        response.idPlayer = idPlayer;
        response.slot = (byte)GunsManagerMP.getInstance().getWeaponSlot(this);
        response.makeSound = makeSound;
        Server server = mpManager.server.getServer();
        server.sendToAllUDP(response);
    }
    public void sendAddBulletPacket(Grenadebullet bullet, float x, float y, float px, float py, int idPlayer, boolean makeSound){
        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        // increasing statistic of shooted bullets so we can calculate accuracy
        MPStatistics mpStatistics = mpManager.server.getMpStatistics();
        mpStatistics.addBulletShoot(idPlayer);
        // sending new bullet as packet
        Network.AddBullet response = new Network.AddBullet();
        mpManager.server.requestACK(response,response.idPacket);
        response.x = x;
        response.y = y;
        response.px = px;
        response.py = py;
        response.critical = bullet.isCritical();
        response.speed = 30;
        response.damage = (byte)bullet.getDamage();
        response.id = bullet.getId();
        response.idPlayer = idPlayer;
        response.slot = (byte)GunsManagerMP.getInstance().getWeaponSlot(this);
        response.makeSound = makeSound;
        Server server = mpManager.server.getServer();
        server.sendToAllUDP(response);
    }
    public abstract void restat(int idPlayer, boolean fullAmmo);

    public boolean criticalHit(float gunCritChance){ // singleplayer
        if(criticalHits){
            BuffManager buffManager = BuffManager.getInstance();
            float totalCritChance = buffManager.getCriticalChance(gunCritChance);
            if(Math.random() > 1 - totalCritChance){
                return true;
            }
        }
        return false;
    }
    public boolean criticalHit(float gunCritChance, int idPlayer){ // multiplayer
        if(criticalHits){
            BuffManagerMP buffManager = BuffManagerMP.getInstance();
            float totalCritChance = buffManager.getCriticalChance(gunCritChance,idPlayer);
            if(Math.random() > 1 - totalCritChance){
                return true;
            }
        }
        return false;
    }
}
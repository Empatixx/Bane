package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Weapon implements Serializable {
    protected String name;
    private boolean shooting;
    //ammo vars
    protected final TileMap tm;
    protected final Player player;
    protected final GunsManager gunsManager;
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

    transient protected Source source;
    transient protected Source reloadsource;

    transient Image weaponHud;
    transient Image weaponAmmo;

    transient TextRender textRender;

    private long alertCooldown;


    /*
    0 - MELEE
    1 - PISTOL
    2 - SUBMACHINE
    3 - SHOTGUN
    4 - SPECIAL
     */
    protected int type;

    Weapon(TileMap tm, Player player, GunsManager gunsManager){
        this.tm = tm;
        this.gunsManager = gunsManager;
        this.player = player;
        source = AudioManager.createSource(Source.EFFECTS,0.35f);
        reloadsource = AudioManager.createSource(Source.EFFECTS,0.35f);

        textRender = new TextRender();
    }


    public abstract void shot(float x,float y,float px,float py);
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
    public void addAmmo(int amountprocent){
        currentAmmo+=Math.ceil((float)(amountprocent)/100*maxAmmo);
        if(currentAmmo > maxAmmo) currentAmmo = maxAmmo;
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
    public boolean isFullAmmo(){ return maxAmmo == currentAmmo;}

    public boolean hasAlreadyDropped() {
        return alreadyDropped;
    }
    public void drop(){
        alreadyDropped = true;
        currentAmmo = maxAmmo;
        currentMagazineAmmo = maxMagazineAmmo;
    }
    public void despawn(){
        alreadyDropped = false;
    }

    public boolean canSwap(){
        return !reloading && System.currentTimeMillis() - InGame.deltaPauseTime() - delay > delayTime/2;
    }
    public void loadSave(){
        source = AudioManager.createSource(Source.EFFECTS,0.35f);
        reloadsource = AudioManager.createSource(Source.EFFECTS,0.35f);

        textRender = new TextRender();
    }
    public void outOfAmmo(){
        if(System.currentTimeMillis()  - alertCooldown  > 2000){
            AlertManager.add(AlertManager.WARNING,"You're out of ammo!");
            alertCooldown = System.currentTimeMillis();
        }
    }
    public abstract void handleBulletPacket(Network.AddBullet addBullet);

}

package cz.Empatix.Guns;

import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;

public abstract class Weapon {
    private boolean shooting;
    //ammo vars
    protected final TileMap tm;
    // dmg
    protected int mindamage;
    protected int maxdamage;

    protected float inaccuracy;
    protected long delay; // between shoots

    protected long reloadDelay;
    protected boolean reloading;

    protected int currentAmmo;
    protected int maxAmmo;
    protected int currentMagazineAmmo;
    protected int maxMagazineAmmo;

    protected Source source;
    protected Source reloadsource;

    Image weaponHud;
    Image weaponAmmo;


    /*
    0 - MELEE
    1 - PISTOL
    2 - SUBMACHINE
    3 - SHOTGUN
    4 - SPECIAL
     */
    protected int type;

    Weapon(TileMap tm){
        this.tm = tm;
        source = new Source(Source.EFFECTS,0.35f);
        reloadsource = new Source(Source.EFFECTS,0.35f);
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
    public void addAmmo(int amount){
        currentAmmo+=amount;
        if(currentAmmo > maxAmmo) currentAmmo = maxAmmo;
    }

    public boolean isShooting() {
        return shooting;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }
}

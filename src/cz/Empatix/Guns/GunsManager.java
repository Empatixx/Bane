package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class GunsManager {
    public static int bulletShooted;
    public static int hitBullets;

    private static ArrayList<Weapon> weapons;

    private final static int FIRSTSLOT = 0;
    private final static int SECONDARYSLOT = 1;
    private int currentslot;

    private Weapon[] equipedweapons;
    private final int soundSwitchingGun;
    private final Source source;
    private long switchDelay;

    private Weapon current;

    private Image weaponBorder_hud;


    public GunsManager(TileMap tileMap){
        weapons = new ArrayList<>();
        weapons.add(new Pistol(tileMap));
        weapons.add(new Shotgun(tileMap));
        weapons.add(new Submachine(tileMap));
        weapons.add(new Revolver(tileMap));


        weaponBorder_hud = new Image("Textures\\weapon_hud.tga",new Vector3f(1675,975,0),2.6f);


        soundSwitchingGun = AudioManager.loadSound("guns\\switchgun.ogg");
        source = new Source(Source.EFFECTS,0.35f);

        equipedweapons = new Weapon[2];

        equipedweapons[0] = weapons.get(0);
        //equipedweapons[1] = weapons.get(1);

        current = equipedweapons[FIRSTSLOT];
        currentslot = FIRSTSLOT;

        bulletShooted = 0;
        hitBullets = 0;
    }
    public void shot(float x,float y,float px,float py){
        if(current == null) return;
        current.shot(x,y,px,py);
    }
    public void reload(){
        if(current == null)return;
        current.reload();
    }
    public void update(){
        for(Weapon weapon : weapons){
            weapon.updateAmmo();
        }
        if(current == null) return;
        current.update();
    }
    public void draw(){
        for(Weapon weapon : weapons){
            weapon.drawAmmo();
        }
    }
    public void drawHud(){
        if(current != null) {
            current.draw();
        }

        weaponBorder_hud.draw();
    }

    private void setCurrentWeapon(Weapon current, int slot) {
        if(System.currentTimeMillis()-InGame.deltaPauseTime()-switchDelay < 500) return;
        if(this.current == null && current == null && slot == currentslot) return;
        if(this.current != null){
            if(this.current == current || this.current.isReloading()) return;
        }
        currentslot = slot;
        switchDelay = System.currentTimeMillis()-InGame.deltaPauseTime();
        this.current = current;
        stopShooting();
        source.play(soundSwitchingGun);
    }
    public void checkCollisions(ArrayList<Enemy> enemies){
        for(Weapon weapon:weapons){
            weapon.checkCollisions(enemies);
        }
    }

    public void keyPressed(int k, int x,int y) {
        switch (k) {
            case GLFW.GLFW_KEY_Q: {
                if(current != null){
                    stopShooting();
                    ItemManager.dropPlayerWeapon(current, x,y);
                }
                current = null;
                equipedweapons[currentslot] = null;
                break;
            }
            case GLFW.GLFW_KEY_1: {
                setCurrentWeapon(equipedweapons[FIRSTSLOT],FIRSTSLOT);
                break;
            }
            case GLFW.GLFW_KEY_2: {
                setCurrentWeapon(equipedweapons[SECONDARYSLOT],SECONDARYSLOT);
                break;
            }
        }
    }
    public void stopShooting(){
        if(current == null) return;
        current.setShooting(false);
    }
    public void startShooting(){
        if(current == null) return;
        current.setShooting(true);
    }
    public boolean addAmmo(int amount, int type) {
        // first check main gun in hand
        if(current != null){
            if(current.getType() == type){
                if(current.isFullAmmo()) return false;
                current.addAmmo(amount);
                return true;
            }
        }
        // check all guns in inventory
        for (int i = 0; i < 2; i++) {
            Weapon weapon = equipedweapons[i];
            if (weapon == null) continue;
            if (weapon.getType() == type) {
                if(weapon.isFullAmmo()) return false;
                weapon.addAmmo(amount);
                return true;
            }
        }
        return false;
    }
    public void changeGun(int x, int y, Weapon weapon){
        source.play(soundSwitchingGun);
        stopShooting();
        // check player's currentslot
        if(equipedweapons[currentslot] == null){
            if(currentslot==currentslot) current = weapon;
            equipedweapons[currentslot] = weapon;
            return;
        }
        // check player's all slots
        for(int i = 0;i<2;i++){
            if(equipedweapons[i] == null){
                if(i==currentslot) current = weapon;
                equipedweapons[i] = weapon;
                return;
            }
        }
        // if player's slots are already filled
        ItemManager.dropPlayerWeapon(current,x,y);
        equipedweapons[currentslot] = weapon;
        current=weapon;
    }
    public static void dropGun(int x, int y, Vector2f speed){
        ItemManager.dropWeapon(weapons.get(1+Random.nextInt(3)),x,y,speed);
    }
    public void changeGunScroll(){
        setCurrentWeapon(weapons.get(currentslot == FIRSTSLOT ? SECONDARYSLOT : FIRSTSLOT),currentslot == FIRSTSLOT ? SECONDARYSLOT : FIRSTSLOT);
    }
}

package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.ArrayList;

public class GunsManager implements Serializable {
    public static void load(){
        Loader.loadImage("Textures\\weapon_hud.tga");
        Bullet.load();
        Grenadebullet.load();

        Luger.load();
        Grenadelauncher.load();
        M4.load();
        Pistol.load();
        Revolver.load();
        Shotgun.load();
        Submachine.load();
        Thompson.load();
    }
    private static GunsManager gunsManager;
    public static void init(GunsManager gunsManager
    ){
        GunsManager.gunsManager = gunsManager;
    }
    public static GunsManager getInstance(){ return gunsManager;}

    public static int bulletShooted;
    public static int hitBullets;

    private ArrayList<Weapon> weapons;

    private final static int FIRSTSLOT = 0;
    private final static int SECONDARYSLOT = 1;

    private int currentslot;
    private Weapon current;


    private Weapon[] equipedweapons;
    private int soundSwitchingGun;
    transient private Source source;
    private long switchDelay;

    transient private Image weaponBorder_hud;


    public GunsManager(TileMap tileMap, Player p){
        weapons = new ArrayList<>();
        weapons.add(new Pistol(tileMap,p));
        weapons.add(new Shotgun(tileMap,p));
        weapons.add(new Submachine(tileMap,p));
        weapons.add(new Revolver(tileMap,p));
        weapons.add(new Grenadelauncher(tileMap,p));
        weapons.add(new Luger(tileMap,p));
        weapons.add(new M4(tileMap,p));
        weapons.add(new Thompson(tileMap,p));


        weaponBorder_hud = new Image("Textures\\weapon_hud.tga",new Vector3f(1675,975,0),2.6f);


        soundSwitchingGun = AudioManager.loadSound("guns\\switchgun.ogg");
        source = AudioManager.createSource(Source.EFFECTS,0.35f);

        equipedweapons = new Weapon[2];

        equipedweapons[0] = weapons.get(0);

        current = equipedweapons[FIRSTSLOT];
        currentslot = FIRSTSLOT;

        bulletShooted = 0;
        hitBullets = 0;
    }
    public void loadSave(){
        weaponBorder_hud = new Image("Textures\\weapon_hud.tga",new Vector3f(1675,975,0),2.6f);

        soundSwitchingGun = AudioManager.loadSound("guns\\switchgun.ogg");
        source = AudioManager.createSource(Source.EFFECTS,0.35f);
        for(Weapon weapon: weapons){
            weapon.loadSave();
        }
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
        // when slot is same as current
        if(currentslot == slot){
            return;
        }
        // when current gun is not realoding
        if(this.current == null){
            currentslot = slot;
            switchDelay = System.currentTimeMillis()-InGame.deltaPauseTime();
            this.current = current;
            stopShooting();
            source.play(soundSwitchingGun);
        }
        else if(this.current.canSwap()){
            currentslot = slot;
            switchDelay = System.currentTimeMillis()-InGame.deltaPauseTime();
            this.current = current;
            stopShooting();
            source.play(soundSwitchingGun);
        }
    }
    public void checkCollisions(ArrayList<Enemy> enemies){
        for(Weapon weapon:weapons){
            weapon.checkCollisions(enemies);
        }
    }

    public void keyPressed(int k, int x,int y) {
        if(k == ControlSettings.getValue(ControlSettings.WEAPON_DROP)){
            if(current != null){
                stopShooting();
                ItemManager itemManager = ItemManager.getInstance();
                itemManager.dropPlayerWeapon(current, x,y);
            }
            current = null;
            equipedweapons[currentslot] = null;
        } else if (k == ControlSettings.getValue(ControlSettings.WEAPON_SLOT1)){
            setCurrentWeapon(equipedweapons[FIRSTSLOT],FIRSTSLOT);
        } else if (k == ControlSettings.getValue(ControlSettings.WEAPON_SLOT2)){
            setCurrentWeapon(equipedweapons[SECONDARYSLOT],SECONDARYSLOT);
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
    public boolean addAmmo(int amountprocent, int type) {
        // first check main gun in hand
        if(current != null){
            if(current.getType() == type){
                if(current.isFullAmmo()) return false;
                current.addAmmo(amountprocent);
                return true;
            }
        }
        // check all guns in inventory
        for (int i = 0; i < 2; i++) {
            Weapon weapon = equipedweapons[i];
            if (weapon == null) continue;
            if (weapon.getType() == type) {
                if(weapon.isFullAmmo()) return false;
                weapon.addAmmo(amountprocent);
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
            equipedweapons[currentslot] = weapon;
            current = weapon;
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
        ItemManager itemManager = ItemManager.getInstance();
        itemManager.dropPlayerWeapon(current,x,y);
        equipedweapons[currentslot] = weapon;
        current=weapon;
    }
    public Weapon randomGun(){
        Weapon weapon = weapons.get(1+Random.nextInt(weapons.size()-1));
        while(weapon.hasAlreadyDropped()){
            weapon = weapons.get(1+Random.nextInt(weapons.size()-1));
        }
        return weapon;
    }
    public Weapon getWeapon(int index){
        return weapons.get(index);
    }
    public void changeGunScroll(){
        int slot = currentslot;
        slot++;
        if(slot > 1) slot = 0;
        setCurrentWeapon(equipedweapons[slot],slot);
    }
    public int[] getWeaponTypes(){
        int[] types = new int[2];
        if(equipedweapons[0] != null) types[0] = equipedweapons[0].getType();
        else types[0] = -1;
        if(equipedweapons[1] != null) types[1] = equipedweapons[1].getType();
        else types[1] = -1;
        return types;
    }

    public int getCurrentslot() {
        return currentslot;
    }
}

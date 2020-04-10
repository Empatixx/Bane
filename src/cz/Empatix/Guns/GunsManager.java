package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class GunsManager {
    private final ArrayList<Weapon> weapons;

    private final static int FIRSTSLOT = 0;
    private final static int SECONDARYSLOT = 1;

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


        weaponBorder_hud = new Image("Textures\\weapon_hud.tga",new Vector3f(1675,975,0),2.6f);


        soundSwitchingGun = AudioManager.loadSound("guns\\switchgun.ogg");
        source = new Source(Source.EFFECTS,0.35f);

        equipedweapons = new Weapon[2];

        equipedweapons[0] = weapons.get(0);
        equipedweapons[1] = weapons.get(1);

        current = equipedweapons[FIRSTSLOT];
    }
    public void shot(float x,float y,float px,float py){
        current.shot(x,y,px,py);
    }
    public void reload(){
        current.reload();
    }
    public void update(){
        for(Weapon weapon : weapons){
            weapon.updateAmmo();
        }
        current.update();
    }
    public void draw(){
        for(Weapon weapon : weapons){
            weapon.drawAmmo();
        }
    }
    public void drawHud(){
        current.draw();

        weaponBorder_hud.draw();
    }

    private void setCurrentWeapon(Weapon current) {
        if(System.currentTimeMillis()- InGame.deltaPauseTime()-switchDelay < 500 || this.current == current || this.current.isReloading()) return;
        switchDelay = System.currentTimeMillis();
        this.current = current;
        source.play(soundSwitchingGun);
    }
    public void checkCollisions(ArrayList<Enemy> enemies){
        for(Weapon weapon:weapons){
            weapon.checkCollisions(enemies);
        }
    }

    public void keyPressed(int k) {
        switch (k) {
            case GLFW.GLFW_KEY_1: {
                setCurrentWeapon(equipedweapons[FIRSTSLOT]);
                break;
            }
            case GLFW.GLFW_KEY_2: {
                setCurrentWeapon(equipedweapons[SECONDARYSLOT]);
                break;
            }
        }
    }
    public void stopShooting(){
        current.setShooting(false);
    }
    public void startShooting(){
        current.setShooting(true);
    }
    public void addAmmo(int amount, int type){
        if(equipedweapons[0].getType() == type){
            equipedweapons[0].addAmmo(amount);
        } else if (equipedweapons[1].getType() == type) {
            equipedweapons[1].addAmmo(amount);
        }
    }
}

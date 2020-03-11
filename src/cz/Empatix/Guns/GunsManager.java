package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.TileMap;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class GunsManager {
    private final ArrayList<Weapon> weapons;

    private final static int PISTOL = 0;
    private final static int SHOTGUN = 1;


    private final int soundSwitchingGun;
    private final Source source;
    private long switchDelay;

    private int current;

    public GunsManager(TileMap tileMap){
        current = PISTOL;
        weapons = new ArrayList<>();
        weapons.add(new Pistol(tileMap));
        weapons.add(new Shotgun(tileMap));

        soundSwitchingGun = AudioManager.loadSound("guns\\switchgun.ogg");
        source = new Source(Source.EFFECTS,0.35f);

    }
    public void shot(float x,float y,float px,float py){
        weapons.get(current).shot(x,y,px,py);
    }
    public void reload(){
        weapons.get(current).reload();
    }
    public void update(){
        for(Weapon weapon : weapons){
            weapon.updateAmmo();
        }
        weapons.get(current).update();
    }
    public void draw(Camera c){
        for(Weapon weapon : weapons){
            weapon.drawAmmo(c);
        }
        weapons.get(current).draw(c);
    }

    private void setCurrentWeapon(int current) {
        if(System.currentTimeMillis()-switchDelay < 300 || this.current == current || weapons.get(this.current).isReloading()) return;
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
                setCurrentWeapon(PISTOL);
                break;
            }
            case GLFW.GLFW_KEY_2: {
                setCurrentWeapon(SHOTGUN);
                break;
            }
        }
    }
}

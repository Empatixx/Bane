package cz.Empatix.Guns;

import cz.Empatix.Entity.Enemy;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;

public class GunsManager {
    private final ArrayList<Weapon> weapons;
    private final static int PISTOL = 0;
    private final static int SHOTGUN = 1;

    private int current;

    public GunsManager(TileMap tileMap){
        current = 0;
        weapons = new ArrayList<>();
        weapons.add(new Pistol(tileMap));
    }
    public void shot(float x,float y,float px,float py){
        weapons.get(current).shot(x,y,px,py);
    }
    public void reload(){
        weapons.get(current).reload();
    }
    public void update(){
        weapons.get(current).update();
    }
    public void draw(Camera c){
        weapons.get(current).draw(c);
    }

    public void setCurrentWeapon(int current) {
        this.current = current;
    }
    public void checkCollisions(ArrayList<Enemy> enemies){
        weapons.get(current).checkCollisions(enemies);
    }
}

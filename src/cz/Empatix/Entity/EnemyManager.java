package cz.Empatix.Entity;

import cz.Empatix.Entity.Enemies.Bat;
import cz.Empatix.Entity.Enemies.Rat;
import cz.Empatix.Entity.Enemies.Slime;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;

public class EnemyManager {
    private static ArrayList<Enemy> enemies;

    private static Player player;
    private static TileMap tileMap;

    public EnemyManager(Player p, TileMap tm){


        player = p;
        tileMap = tm;

        enemies = new ArrayList<>();
    }



    public void update(){
        // updating enemies
        for(int i = 0;i < enemies.size();i++){
            enemies.get(i).update();
            // checking if enemy is dead
            if (enemies.get(i).isDead()){
                enemies.remove(i);
                i--;
            }
        }
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public void draw(Camera camera){
        for(Enemy e : enemies){
            e.draw(camera);
        }
    }
    public static void addSlime(float x, float y){
        Enemy slime = new Slime(tileMap,player);
        slime.setPosition(x,y);
        enemies.add(slime);
    }
    public static void addBat(float x, float y){
        Enemy bat = new Bat(tileMap,player);
        bat.setPosition(x,y);
        enemies.add(bat);
    }
    public static void addRat(float x, float y){
        Enemy bat = new Rat(tileMap,player);
        bat.setPosition(x,y);
        enemies.add(bat);
    }
}

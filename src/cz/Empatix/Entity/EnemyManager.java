package cz.Empatix.Entity;

import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class EnemyManager {
    public static int enemiesKilled;
    private int chanceDrop;

    private static ArrayList<Enemy> enemies;

    private static ArrayList<String> enemiesList;

    private static Player player;
    private static TileMap tileMap;

    public EnemyManager(Player p, TileMap tm){
        player = p;
        tileMap = tm;

        enemies = new ArrayList<>();
        enemiesList = new ArrayList<>(3);

        enemiesList.add("Slime");
        enemiesList.add("Rat");
        enemiesList.add("Bat");

        enemiesKilled = 0;
    }



    public void update(){
        // updating enemies
        for(int i = 0;i < enemies.size();i++){
            Enemy enemy = enemies.get(i);
            enemy.update();
            // checking if enemy is dead && should drop item
            if(enemy.canDropItem()){
                chanceDrop++;
                enemy.setItemDropped();
                int chance = Random.nextInt(5);
                if(chanceDrop+chance > 3){
                    ItemManager.createDrop(enemy.getx(),enemy.gety());
                    chanceDrop=0;
                }
            }
            // checking if enemy is dead && should be removed from memory
            if (enemy.shouldRemove()){
                enemies.remove(i);
                i--;
                enemiesKilled++;
            }
        }
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public void draw(){
        for(Enemy e : enemies){
            e.draw();
        }
    }
    private static int getRandom(int lower, int upper) {
        return cz.Empatix.Java.Random.nextInt((upper - lower) + 1) + lower;
    }
    public static void addEnemy(int xMin,int xMax, int yMin,int yMax){
        int enemyType = cz.Empatix.Java.Random.nextInt(enemiesList.size());
        Enemy instance = null;
        try{
            String enemy = "cz.Empatix.Entity.Enemies."+enemiesList.get(enemyType);
            Class<?> clazz = Class.forName(enemy);
            Constructor<?> constructor = clazz.getConstructor(TileMap.class,Player.class);
            instance = (Enemy)constructor.newInstance(tileMap,player);
        } catch (Exception e){
            e.printStackTrace();
        }

        int x = getRandom(xMin,xMax);
        int y = getRandom(yMin,yMax);

        int cwidth = instance.getCwidth()/2;
        int cheight = instance.getCheight()/2;

        int tileSize = tileMap.getTileSize();


        int leftTile = (x - cwidth / 2) / tileSize;
        int rightTile = (x + cwidth / 2 - 1) / tileSize;
        int topTile = (y - cheight / 2) / tileSize;
        int bottomTile = (y + cheight / 2 - 1) / tileSize;


        // getting type of tile
        int tl = tileMap.getType(topTile, leftTile);
        int tr = tileMap.getType(topTile, rightTile);
        int bl = tileMap.getType(bottomTile, leftTile);
        int br = tileMap.getType(bottomTile, rightTile);

        while(tl == Tile.BLOCKED || tr == Tile.BLOCKED || bl == Tile.BLOCKED || br == Tile.BLOCKED)
        {

            x = getRandom(xMin,xMax);
            y = getRandom(yMin,yMax);

            leftTile = (x - cwidth / 2) / tileSize;
            rightTile = (x + cwidth / 2 - 1) / tileSize;
            topTile = (y - cheight / 2) / tileSize;
            bottomTile = (y + cheight / 2 - 1) / tileSize;


            // getting type of tile
            tl = tileMap.getType(topTile, leftTile);
            tr = tileMap.getType(topTile, rightTile);
            bl = tileMap.getType(bottomTile, leftTile);
            br = tileMap.getType(bottomTile, rightTile);
        }

        instance.setPosition(x,y);
        enemies.add(instance);
    }
}

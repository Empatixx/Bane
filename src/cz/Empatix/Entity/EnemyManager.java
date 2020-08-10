package cz.Empatix.Entity;

import cz.Empatix.Entity.Enemies.ArcaneMage;
import cz.Empatix.Entity.Enemies.KingSlime;
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
        enemiesList = new ArrayList<>(5);


        enemiesList.add("Slime");
        enemiesList.add("Rat");
        enemiesList.add("Bat");
        enemiesList.add("Demoneye");
        enemiesList.add("Ghost");

        enemiesKilled = 0;
    }

    public static boolean areEnemiesDead(){
        for(Enemy e:enemies){
            if(!e.isDead()){
                return false;
            }
        }
        return true;
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
                    ItemManager.createDrop(enemy.getX(),enemy.getY());
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

    public static ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public void draw(){
        for(Enemy e : enemies){
            e.draw();
        }
    }
    // not affected by lightning system
    public void drawHud(){
        for(Enemy e : enemies){
            if(e instanceof KingSlime) {
                ((KingSlime)e).drawHud();
            } else if(e instanceof ArcaneMage) {
                ((ArcaneMage) e).drawHud();
            }
        }
    }
    private static int getRandom(int lower, int upper) {
        return cz.Empatix.Java.Random.nextInt((upper - lower) + 1) + lower;
    }
    public static void spawnBoss(int x,int y){
        int randombosses = 1;
        if(tileMap.getFloor() >= 1){
            //randombosses++;
        }
        int typeboss = Random.nextInt(randombosses);
        if(typeboss == 0){
            KingSlime slime = new KingSlime(tileMap,player);
            slime.setPosition(x,y);
            enemies.add(slime);
        } else {
            ArcaneMage arcaneMage= new ArcaneMage(tileMap,player);
            arcaneMage.setPosition(x,y);
            enemies.add(arcaneMage);
        }
    }
    public static void addEnemy(int xMin,int xMax, int yMin,int yMax){
        int defaultsize = 3;
        if(tileMap.getFloor() >= 1){
            defaultsize+=2;
        }
        int enemyType = cz.Empatix.Java.Random.nextInt(defaultsize);
        Enemy instance = null;
        try{
            String enemy = "cz.Empatix.Entity.Enemies."+enemiesList.get(enemyType);
            Class<?> clazz = Class.forName(enemy);
            Constructor<?> constructor = clazz.getConstructor(TileMap.class,Player.class);
            instance = (Enemy)constructor.newInstance(tileMap,player);
        } catch (Exception e){
            e.printStackTrace();
        }
        int tileSize = tileMap.getTileSize();

        int x = getRandom(xMin+tileSize,xMax-tileSize);
        int y = getRandom(yMin+tileSize,yMax-tileSize);

        int cwidth = instance.getCwidth()/2;
        int cheight = instance.getCheight()/2;



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
        instance.checkTileMapCollision();
        instance.setPosition(instance.temp.x,instance.temp.y);
        enemies.add(instance);
    }
    public void addEnemy(int x, int y, String type,int count) {
        boolean canContinue = false;
        type = type.substring(0,1).toUpperCase() + type.substring(1).toLowerCase();
        for (String enemy : enemiesList) {
            if (enemy.equals(type)) {
                canContinue = true;
            }
        }
        if (!canContinue) return;
        for (int i = 0; i < count; i++) {

            Enemy instance = null;
            try {
                String enemy = "cz.Empatix.Entity.Enemies." + type;
                Class<?> clazz = Class.forName(enemy);
                Constructor<?> constructor = clazz.getConstructor(TileMap.class, Player.class);
                instance = (Enemy) constructor.newInstance(tileMap, player);
            } catch (Exception e) {
                e.printStackTrace();
            }

            instance.setPosition(x, y);
            instance.checkTileMapCollision();
            instance.setPosition(instance.temp.x, instance.temp.y);
            enemies.add(instance);
        }
    }
}

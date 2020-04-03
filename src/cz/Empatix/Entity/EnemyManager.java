package cz.Empatix.Entity;

import cz.Empatix.Entity.Enemies.Bat;
import cz.Empatix.Entity.Enemies.Rat;
import cz.Empatix.Entity.Enemies.Skeleton;
import cz.Empatix.Entity.Enemies.Slime;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Tile;
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
            if (enemies.get(i).shouldRemove()){
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
    public static void addSlime(int xMin,int xMax, int yMin,int yMax){
        Enemy slime = new Slime(tileMap,player);
        int x = getRandom(xMin,xMax);
        int y = getRandom(yMin,yMax);

        int cwidth = slime.getCwidth()/2;
        int cheight = slime.getCheight()/2;

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

        slime.setPosition(x,y);
        enemies.add(slime);
    }
    public static void addBat(int xMin,int xMax, int yMin,int yMax){
        Enemy bat = new Bat(tileMap,player);
        int x = getRandom(xMin,xMax);
        int y = getRandom(yMin,yMax);

        int cwidth = bat.getCwidth()/2;
        int cheight = bat.getCheight()/2;

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

        bat.setPosition(x,y);
        enemies.add(bat);
    }
    public static void addRat(int xMin,int xMax, int yMin,int yMax){
        Enemy rat = new Rat(tileMap,player);
        int x = getRandom(xMin,xMax);
        int y = getRandom(yMin,yMax);

        int cwidth = rat.getCwidth();
        int cheight = rat.getCheight();


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

        rat.setPosition(x,y);
        enemies.add(rat);
    }
    public static void addSkeleton(int xMin,int xMax, int yMin,int yMax){
        Enemy skeleton = new Skeleton(tileMap,player);

        int x = getRandom(xMin,xMax);
        int y = getRandom(yMin,yMax);

        int cwidth = skeleton.getCwidth()/2;
        int cheight = skeleton.getCheight()/2;

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

        skeleton.setPosition(x,y);
        enemies.add(skeleton);
    }
    private static int getRandom(int lower, int upper) {
        return cz.Empatix.Java.Random.nextInt((upper - lower) + 1) + lower;
    }
    public static void addEnemy(int xMin,int xMax, int yMin,int yMax){
        int enemyType = cz.Empatix.Java.Random.nextInt(3);
        if(enemyType == 0){
            EnemyManager.addBat(xMin,xMax,yMin,yMax);
        } else if(enemyType == 1){
            EnemyManager.addSlime(xMin,xMax,yMin,yMax);
        } else {
            EnemyManager.addRat(xMin,xMax,yMin,yMax);
        }
    }
}

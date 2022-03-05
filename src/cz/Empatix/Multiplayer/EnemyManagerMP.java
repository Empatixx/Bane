package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Enemies.*;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Room;
import cz.Empatix.Render.RoomObjects.PathWall;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class EnemyManagerMP {
    private static EnemyManagerMP enemyManager;

    public static EnemyManagerMP getInstance() {
        return enemyManager;
    }

    public static int enemiesKilled;
    private int chanceDrop;

    private ArrayList<Enemy> enemies;

    private ArrayList<String> enemiesList;

    private Player[] player;
    private TileMap tileMap;

    public EnemyManagerMP(Player[] p, TileMap tm) {
        EnemyManagerMP.enemyManager = this;

        player = p;
        tileMap = tm;

        enemies = new ArrayList<>();
        enemiesList = new ArrayList<>(5);


        enemiesList.add("slime");
        enemiesList.add("rat");
        enemiesList.add("bat");
        enemiesList.add("demoneye");
        enemiesList.add("ghost");
        enemiesList.add("snake");
        enemiesList.add("redslime");
        enemiesList.add("eyebat");

        enemiesKilled = 0;
    }

    public void loadSave() {
        for (Enemy e : enemies) {
            e.loadSave();
        }
    }

    public boolean areEnemiesDead() {
        for (Enemy e : enemies) {
            if (!e.isDead()) {
                return false;
            }
        }
        return true;
    }

    public void clear() { enemies.clear();
    }


    public void update() {
        // updating enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.update();
            // checking if enemy is dead && should drop item
            if (enemy.canDropItem()) {
                chanceDrop++;
                enemy.setItemDropped();
                int chance = Random.nextInt(5);
                if (chanceDrop + chance > 3) {
                    ItemManagerMP itemManager = ItemManagerMP.getInstance();
                    itemManager.createDrop(enemy.getX(), enemy.getY());
                    chanceDrop = 0;
                }
            }
            // checking if enemy is dead && should be removed from memory
            if (enemy.shouldRemove()) {
                enemies.remove(i);
                i--;
                enemiesKilled++;
            }
        }
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    private static int getRandom(int lower, int upper) {
        return cz.Empatix.Java.Random.nextInt((upper - lower) + 1) + lower;
    }

    public void spawnBoss(int x, int y) {
        int randombosses = 1;
        if (tileMap.getFloor() >= 1) {
            randombosses++;
        }
        int typeboss = Random.nextInt(randombosses);
        if (typeboss == 0) {
            KingSlime slime = new KingSlime(tileMap, player);
            slime.setPosition(x, y);
            enemies.add(slime);

            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Network.AddEnemy addEnemy= new Network.AddEnemy();
            addEnemy.type = "kingslime";
            addEnemy.x = x;
            addEnemy.y = y;
            addEnemy.id = slime.id;

            Server server = mpManager.server.getServer();
            server.sendToAllUDP(addEnemy);
        } else {
            Golem golem = new Golem(tileMap, player);
            golem.setPosition(x, y);
            enemies.add(golem);

            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Network.AddEnemy addEnemy= new Network.AddEnemy();
            addEnemy.type = "golem";
            addEnemy.x = x;
            addEnemy.y = y;
            addEnemy.id = golem.id;

            Server server = mpManager.server.getServer();
            server.sendToAllUDP(addEnemy);
        }
    }

    // only for server side - sending packet to all clients
    public void addEnemy(int xMin, int xMax, int yMin, int yMax) {
        int defaultsize = 3;
        if (tileMap.getFloor() >= 1) {
            defaultsize += 3;
        }
        if (tileMap.getFloor() >= 2) {
            defaultsize += 2;
        }
        int enemyType = cz.Empatix.Java.Random.nextInt(defaultsize);
        Enemy instance = null;
        String enemy = enemiesList.get(enemyType);
        if (MultiplayerManager.multiplayer) {
            switch (enemy) {
                case "slime": {
                    instance = new Slime(tileMap, player);
                    break;
                }
                case "rat": {
                    instance = new Rat(tileMap, player);
                    break;
                }
                case "bat": {
                    instance = new Bat(tileMap, player);
                    break;
                }
                case "demoneye": {
                    instance = new Demoneye(tileMap, player);
                    break;
                }
                case "ghost": {
                    instance = new Ghost(tileMap, player);
                    break;
                }
                case "snake": {
                    instance = new Snake(tileMap, player);
                    break;
                }
                case "redslime": {
                    instance = new RedSlime(tileMap, player);
                    break;
                }
                case "eyebat": {
                    instance = new EyeBat(tileMap, player);
                    break;
                }
            }
        }

        int tileSize = tileMap.getTileSize();

        int x;
        int y;

        int cwidth = instance.getCwidth();
        int cheight = instance.getCheight();


        // tiles
        int leftTile;
        int rightTile;
        int topTile;
        int bottomTile;


        // getting type of tile
        int tl;
        int tr;
        int bl;
        int br;

        boolean loop;
        boolean ROCollision;
        ArrayList<RoomObject> roomObjs = tileMap.getRoomByCoords(xMin+(xMax-xMin)/2,yMin+(yMax-yMin)/2).getMapObjects();
        do {
            x = getRandom(xMin, xMax);
            y = getRandom(yMin, yMax);

            leftTile = (x - cwidth / 2) / tileSize;
            rightTile = (x + cwidth / 2 - 1) / tileSize;
            topTile = (y - cheight / 2) / tileSize;
            bottomTile = (y + cheight / 2 - 1) / tileSize;


            // getting type of tile
            tl = tileMap.getType(topTile, leftTile);
            tr = tileMap.getType(topTile, rightTile);
            bl = tileMap.getType(bottomTile, leftTile);
            br = tileMap.getType(bottomTile, rightTile);

            loop = (tl == Tile.BLOCKED || tr == Tile.BLOCKED || bl == Tile.BLOCKED || br == Tile.BLOCKED);
            instance.setPosition(x, y);

            ROCollision = false;
            for(RoomObject object : roomObjs){
                if(object.intersects(instance)){
                    if( object.collision || object instanceof PathWall) ROCollision  = true;
                }
            }
        } while (loop || ROCollision);

        instance.setPosition(x, y);
        enemies.add(instance);
        MultiplayerManager mpManager = MultiplayerManager.getInstance();

        Network.AddEnemy addEnemy = new Network.AddEnemy();
        addEnemy.type = enemy;
        addEnemy.x = x;
        addEnemy.y = y;
        addEnemy.id = instance.id;

        Server server = mpManager.server.getServer();
        server.sendToAllUDP(addEnemy);
    }

    public boolean areEnemiesDeadInCoords(int xMin, int xMax, int yMin, int yMax) {
        for(Enemy e : enemies){
            if(e.isDead()) continue;
            Vector3f position = e.getPosition();
            if(position.x > xMin && position.x < xMax && position.y > yMin && position.y < yMax) return false;
        }
        return true;
    }

    public void clearEnemiesInRoom(Room room) {
        int xMax,xMin,yMax,yMin;
        xMax = room.getxMax();
        xMin = room.getxMin();
        yMax = room.getyMax();
        yMin = room.getyMin();
        for(Enemy e : enemies){
            if(e.isDead()) continue;
            Vector3f position = e.getPosition();
            if(position.x > xMin && position.x < xMax && position.y > yMin && position.y < yMax){
                e.setDead();
                Network.RemoveEnemy rEnemy = new Network.RemoveEnemy();
                rEnemy.id = e.id;
                Server server = MultiplayerManager.getInstance().server.getServer();
                server.sendToAllUDP(rEnemy);
            }
        }

    }
}

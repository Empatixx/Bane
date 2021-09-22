package cz.Empatix.Entity;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.Entity.Enemies.*;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Random;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EnemyManager {
    private static EnemyManager enemyManager;
    public static void init(EnemyManager enemyManager
    ){
        EnemyManager.enemyManager = enemyManager;
    }
    public static EnemyManager getInstance(){return enemyManager;}
    public static int enemiesKilled;
    private int chanceDrop;

    private ArrayList<Enemy> enemies;

    private ArrayList<String> enemiesList;

    private Player player[];
    private TileMap tileMap;

    private ArrayList<Network.AddEnemy> queueAddEnemyPackets;
    private Lock lock;

    // singleplayer
    public EnemyManager(Player p, TileMap tm){
        player = new Player[1];
        player[0] = p;
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

        if(MultiplayerManager.multiplayer){
            queueAddEnemyPackets = new ArrayList<>();
            lock = new ReentrantLock();
        }
    }

    // multiplayer
    public EnemyManager(Player[] p, TileMap tm){
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

        if(MultiplayerManager.multiplayer){
            queueAddEnemyPackets = new ArrayList<>();
            lock = new ReentrantLock();
        }
    }

    public void loadSave(){
        for(Enemy e:enemies){
            e.loadSave();
        }
    }
    public boolean areEnemiesDead(){
        for(Enemy e:enemies){
            if(!e.isDead()){
                return false;
            }
        }
        return true;
    }
    public void clear(){
        enemies.clear();
    }



    public void update(){
        if(MultiplayerManager.multiplayer){
            try{
                lock.lock();
                for(Network.AddEnemy addEnemy : queueAddEnemyPackets){
                    addEnemy(addEnemy);
                }
                if(!queueAddEnemyPackets.isEmpty()){
                    queueAddEnemyPackets.clear();
                }
            } finally {
                lock.unlock();
            }
        }
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
                    ItemManager itemManager = ItemManager.getInstance();
                    itemManager.createDrop(enemy.getX(),enemy.getY());
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
    public void updateOnlyAnimations(){
        for(int i = 0;i < enemies.size();i++) {
            Enemy enemy = enemies.get(i);
            enemy.animation.update();
        }
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public void draw(){
        ArrayList<Enemy> enemiesleft = (ArrayList<Enemy>) enemies.clone();
        for(int i = 0;i<enemies.size();i++){
            Enemy drawnEnemy = null;
            for(Enemy enemy : enemiesleft){
                if(drawnEnemy == null) drawnEnemy = enemy;
                else if(drawnEnemy.getY() > enemy.getY()) drawnEnemy = enemy;
            }
            drawnEnemy.draw();
            enemiesleft.remove(drawnEnemy);
        }
    }
    public void drawShadow(){
        ArrayList<Enemy> enemiesleft = (ArrayList<Enemy>) enemies.clone();
        for(int i = 0;i<enemies.size();i++){
            Enemy drawnEnemy = null;
            for(Enemy enemy : enemiesleft){
                if(drawnEnemy == null) drawnEnemy = enemy;
                else if(drawnEnemy.getY() > enemy.getY()) drawnEnemy = enemy;
            }
            drawnEnemy.drawShadow();
            enemiesleft.remove(drawnEnemy);
        }
    }
    // not affected by lightning system
    public void drawHud(){
        for(Enemy e : enemies){
            if(e instanceof KingSlime) {
                ((KingSlime)e).drawHud();
            } else if(e instanceof ArcaneMage) {
                ((ArcaneMage) e).drawHud();
            } else if(e instanceof Golem) {
                ((Golem) e).drawHud();
            }
        }
    }
    private static int getRandom(int lower, int upper) {
        return cz.Empatix.Java.Random.nextInt((upper - lower) + 1) + lower;
    }
    public void spawnBoss(int x,int y){
        int randombosses = 1;
        if(tileMap.getFloor() >= 1){
            randombosses++;
        }
        int typeboss = Random.nextInt(randombosses);
        if(typeboss == 0){
            KingSlime slime = new KingSlime(tileMap,player);
            slime.setPosition(x,y);
            enemies.add(slime);
        } else {
            Golem golem = new Golem(tileMap,player);
            golem.setPosition(x,y);
            enemies.add(golem);
        }
    }
    public void addEnemy(int xMin,int xMax, int yMin,int yMax){
        int defaultsize = 3;
        if(tileMap.getFloor() >= 1){
            defaultsize+=3;
        }
        if(tileMap.getFloor() >= 2){
            defaultsize+=2;
        }
        int enemyType = cz.Empatix.Java.Random.nextInt(defaultsize);
        Enemy instance = null;
        String enemy = enemiesList.get(enemyType);
        if(MultiplayerManager.multiplayer) {
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
        } else {
            //singleplayer
            switch (enemy) {
                case "slime": {
                    instance = new Slime(tileMap, player[0]);
                    break;
                }
                case "rat": {
                    instance = new Rat(tileMap, player[0]);
                    break;
                }
                case "bat": {
                    instance = new Bat(tileMap, player[0]);
                    break;
                }
                case "demoneye": {
                    instance = new Demoneye(tileMap, player[0]);
                    break;
                }
                case "ghost": {
                    instance = new Ghost(tileMap, player[0]);
                    break;
                }
                case "snake": {
                    instance = new Snake(tileMap, player[0]);
                    break;
                }
                case "redslime": {
                    instance = new RedSlime(tileMap, player[0]);
                    break;
                }
                case "eyebat": {
                    instance = new EyeBat(tileMap, player[0]);
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
        do
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

            loop = (tl == Tile.BLOCKED || tr == Tile.BLOCKED || bl == Tile.BLOCKED || br == Tile.BLOCKED);
        }while(loop);

        instance.setPosition(x,y);
        enemies.add(instance);

        // multiplayer packet of new enemy
        if(MultiplayerManager.multiplayer){
            MultiplayerManager mpManager = MultiplayerManager.getInstance();

            Network.AddEnemy addEnemy = new Network.AddEnemy();
            addEnemy.type = enemy;
            addEnemy.x = x;
            addEnemy.y = y;
            addEnemy.id = instance.idEnemy;

            Client client = mpManager.client.getClient();
            client.sendTCP(addEnemy);
        }
    }
    public void addEnemy(String enemy){
        Enemy instance;
        switch (enemy){
            case "slime":{
                instance = new Slime(tileMap,player);
                break;
            }
            case "rat":{
                instance = new Rat(tileMap,player);
                break;
            }
            case "bat":{
                instance = new Bat(tileMap,player);
                break;
            }
            case "demoneye":{
                instance = new Demoneye(tileMap,player);
                break;
            }
            case "ghost":{
                instance = new Ghost(tileMap,player);
                break;
            }
            case "golem":{
                instance = new Golem(tileMap,player);
                break;
            }
            case "kingslime":{
                instance = new KingSlime(tileMap,player);
                break;
            }
            case "snake":{
                instance = new Snake(tileMap,player);
                break;
            }
            case "redslime":{
                instance = new RedSlime(tileMap,player);
                break;
            }
            case "eyebat":{
                instance = new EyeBat(tileMap,player);
                break;
            }
            default:{
                return;
            }
        }
        instance.setPosition(player[0].getX(),player[0].getY());
        enemies.add(instance);
    }
    public void addEnemy(Network.AddEnemy addEnemy){
        Enemy instance;
        switch (addEnemy.type){
            case "slime":{
                instance = new Slime(tileMap,player);
                break;
            }
            case "rat":{
                instance = new Rat(tileMap,player);
                break;
            }
            case "bat":{
                instance = new Bat(tileMap,player);
                break;
            }
            case "demoneye":{
                instance = new Demoneye(tileMap,player);
                break;
            }
            case "ghost":{
                instance = new Ghost(tileMap,player);
                break;
            }
            case "golem":{
                instance = new Golem(tileMap,player);
                break;
            }
            case "kingslime":{
                instance = new KingSlime(tileMap,player);
                break;
            }
            case "snake":{
                instance = new Snake(tileMap,player);
                break;
            }
            case "redslime":{
                instance = new RedSlime(tileMap,player);
                break;
            }
            case "eyebat":{
                instance = new EyeBat(tileMap,player);
                break;
            }
            default:{
                return;
            }
        }
        instance.setPosition(addEnemy.x, addEnemy.y);
        instance.idEnemy = addEnemy.id;
        enemies.add(instance);
    }
    public void addEnemyPacket(Network.AddEnemy addEnemy){
        try{
            lock.lock();
            queueAddEnemyPackets.add(addEnemy);
        } finally {
            lock.unlock();
        }
    }
}

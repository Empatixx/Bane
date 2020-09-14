package cz.Empatix.Render;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.Shopkeeper;
import cz.Empatix.Render.Hud.Minimap.MMRoom;
import cz.Empatix.Render.RoomObjects.*;
import cz.Empatix.Render.RoomObjects.ProgressRoom.Portal;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Room {
    // if player has entered room yet
    private boolean entered;
    private MMRoom minimapRoom;

    // indicator for setting map
    private final int id;
    private final int x;
    private final int y;

    // info about room
    private int numCols;
    private int numRows;
    private final int type;

    // types of rooms
    public final static int Starter = 0;
    public final static int Classic = 1;
    public final static int Loot = 2;
    public final static int Shop = 3;
    public final static int Boss = 4;
    public final static int Progress = 5;

    // orientation about paths
    private boolean bottom;
    private boolean top;
    private boolean left;
    private boolean right;

    // tiles of room
    private byte[][] roomMap;

    // corners of room
    private int yMin;
    private int yMax;
    private int xMin;
    private int xMax;

    private final RoomPath[] roomPaths;

    private final ArrayList<RoomObject> mapObjects;


    Room(int type, int id, int x, int y){
        entered = false;

        this.id = id;

        this.x = x;
        this.y = y;

        this.type = type;

        roomPaths = new RoomPath[4];

        mapObjects = new ArrayList<>();

    }

    void loadMap(){
        if(type == Classic) {
            try {
                InputStream in = getClass().getResourceAsStream("/Map/currentmap" + (new Random().nextInt(2) + 1) + ".map");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in)
                );

                numCols = Integer.parseInt(br.readLine());
                numRows = Integer.parseInt(br.readLine());

                roomMap = new byte[numRows][numCols];

                String delims = "\\s+";
                for (int row = 0; row < numRows; row++) {
                    String line = br.readLine();
                    String[] tokens = line.split(delims);
                    for (int col = 0; col < numCols; col++) {
                        roomMap[row][col] = Byte.parseByte(tokens[col]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (type == Loot){
            try {
                InputStream in = getClass().getResourceAsStream("/Map/lootroom.map");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in)
                );

                numCols = Integer.parseInt(br.readLine());
                numRows = Integer.parseInt(br.readLine());

                roomMap = new byte[numRows][numCols];

                String delims = "\\s+";
                for (int row = 0; row < numRows; row++) {
                    String line = br.readLine();
                    String[] tokens = line.split(delims);
                    for (int col = 0; col < numCols; col++) {
                        roomMap[row][col] = Byte.parseByte(tokens[col]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (type == Shop){
            try {
                InputStream in = getClass().getResourceAsStream("/Map/shoproom.map");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in)
                );

                numCols = Integer.parseInt(br.readLine());
                numRows = Integer.parseInt(br.readLine());

                roomMap = new byte[numRows][numCols];

                String delims = "\\s+";
                for (int row = 0; row < numRows; row++) {
                    String line = br.readLine();
                    String[] tokens = line.split(delims);
                    for (int col = 0; col < numCols; col++) {
                        roomMap[row][col] = Byte.parseByte(tokens[col]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(type == Starter || type == Boss) {
            try {
                InputStream in = getClass().getResourceAsStream("/Map/currentmap2.map");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in)
                );

                numCols = Integer.parseInt(br.readLine());
                numRows = Integer.parseInt(br.readLine());

                roomMap = new byte[numRows][numCols];

                String delims = "\\s+";
                for (int row = 0; row < numRows; row++) {
                    String line = br.readLine();
                    String[] tokens = line.split(delims);
                    for (int col = 0; col < numCols; col++) {
                        roomMap[row][col] = Byte.parseByte(tokens[col]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(type == Progress){
            try {
                InputStream in = getClass().getResourceAsStream("/Map/progressroom.map");
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in)
                );

                numCols = Integer.parseInt(br.readLine());
                numRows = Integer.parseInt(br.readLine());

                roomMap = new byte[numRows][numCols];

                String delims = "\\s+";
                for (int row = 0; row < numRows; row++) {
                    String line = br.readLine();
                    String[] tokens = line.split(delims);
                    for (int col = 0; col < numCols; col++) {
                        roomMap[row][col] = Byte.parseByte(tokens[col]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getType() {
        return type;
    }

    byte[][] getRoomMap() {
        return roomMap;
    }
    int getNumCols() {
        return numCols;
    }

    int getNumRows() {
        return numRows;
    }

    int getId(){ return id;}

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    void setTop(boolean top) {
        minimapRoom.setTop(top);
        this.top = top;
    }

    void setLeft(boolean left) {
        minimapRoom.setLeft(left);
        this.left = left;
    }

    void setBottom(boolean bottom) {
        minimapRoom.setBottom(bottom);
        this.bottom = bottom;
    }

    void setRight(boolean right) {
        minimapRoom.setRight(right);
        this.right = right;
    }

    void setCorners(int xMin,int xMax,int yMin, int yMax){
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.yMin = yMin;
    }

    int getyMin() { return yMin; }

    int getxMax() { return xMax; }

    int getxMin() { return xMin; }

    int getyMax() { return yMax; }

    boolean hasEntered(){ return entered; }

    void entered(){
        entered = true;
        if (type == Room.Classic){

            int maxMobs = cz.Empatix.Java.Random.nextInt(4) + 2;


            for (int i = 0; i < maxMobs;i++){

                EnemyManager.addEnemy(xMin,xMax,yMin,yMax);
            }

            lockRoom(true);
        } else if (type == Room.Boss){

            int y=yMin + (yMax - yMin) / 2;
            int x=xMin + (xMax - xMin) / 2;
            EnemyManager.spawnBoss(x,y);

            AudioManager.playSoundtrack(Soundtrack.BOSS);

            lockRoom(true);
        }
    }
    private void lockRoom(boolean b){
        for(RoomObject obj:mapObjects){
            if(obj instanceof PathWall) obj.collision=b;
        }
    }


    boolean isRight() {
        return right;
    }

    boolean isBottom() {
        return bottom;
    }

    boolean isLeft() {
        return left;
    }

    boolean isTop() {
        return top;
    }

    void unload(){
        roomMap = null;
    }

    RoomPath[] getRoomPaths() {
        return roomPaths;
    }
    void setTopRoomPath(RoomPath roomPath){
        roomPaths[0] = roomPath;
    }
    void setBottomRoomPath(RoomPath roomPath){
        roomPaths[1] = roomPath;
    }
    void setLeftRoomPath(RoomPath roomPath){
        roomPaths[2] = roomPath;
    }
    void setRightRoomPath(RoomPath roomPath){
        roomPaths[3] = roomPath;
    }

    public void preDrawObjects(){
        for(RoomObject object : mapObjects){
            if(object.isPreDraw()){
                object.draw();
            }
        }
    }
    public void drawObjects(TileMap tm){
        for(RoomObject object : mapObjects){
            if(!object.isPreDraw())object.draw();
        }
        if(type == Starter && tm.getFloor() == 0) {
            int y = yMin + (yMax - yMin) / 2;
            int x = xMin + (xMax - xMin) / 2;
            float time = (float) Math.sin(System.currentTimeMillis() % 2000 / 600f) + (1 - (float) Math.cos((System.currentTimeMillis() % 2000 / 600f) + 0.5f));
            TextRender.renderMapText("WASD - Movement", new Vector3f(x, y, 0), 2,
                    new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
            TextRender.renderMapText("Mouse click - shoot", new Vector3f(x, y + 50, 0), 2,
                    new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
            TextRender.renderMapText("1 and 2 - weapon slots", new Vector3f(x, y + 100, 0), 2,
                    new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
            TextRender.renderMapText("E/Q - pickup/drop gun", new Vector3f(x, y + 150, 0), 2,
                    new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
        }
    }
    public void updateObjects(){
        for(int i = 0;i<mapObjects.size();i++){
            RoomObject object = mapObjects.get(i);
            if(object.shouldRemove()){
                mapObjects.remove(i);
                if(object instanceof DestroyableObject){
                    if(((DestroyableObject) object).isDestroyed()){
                        if(((DestroyableObject) object).hasDrop() && Math.random() > 0.6){
                            ItemManager.createDrop(object.getX(),object.getY());
                        }
                    }
                }
                i--;
                continue;
            }
            object.update();
        }
        if(type == Boss || type == Classic) {
            if (EnemyManager.areEnemiesDead()) {
                lockRoom(false);
            }
        }
    }
    public void createObjects(TileMap tm) {
        if(type == Classic){
            int num = cz.Empatix.Java.Random.nextInt(3);

            // spikes
            for(int i = 0;i<num;i++){
                int tileSize = tm.getTileSize();

                int xMinTile = xMin/tileSize + 2;
                int yMinTile = yMin/tileSize + 2;

                int xMaxTile = xMax/tileSize - 2;
                int yMaxTile = yMax/tileSize - 2;

                int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;

                boolean done = false;
                while(!done){
                    boolean collision = false;

                    x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                    y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
                    A: for(int j = -1;j<2;j++){
                        for(int k = -1;k<2;k++){
                            if(tm.getType(y+j,x+k) == Tile.BLOCKED){
                                collision=true;
                                break A;
                            }
                        }
                    }
                    if(!collision) done = true;
                }
                tm.addSpike(x*tileSize+tileSize/2,y*tileSize+tileSize/2,this);
            }
            // flags
            num = cz.Empatix.Java.Random.nextInt(3);
            for(int i = 0;i<num;i++) {
                int tileSize = tm.getTileSize();

                int xMinTile = xMin/tileSize + 1;
                int yMinTile = yMin/tileSize + 1;

                int xMaxTile = xMax/tileSize - 1;
                int yMaxTile = yMax/tileSize - 1;

                int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
                while(tm.getType(y-1,x) != Tile.BLOCKED || tm.getType(y,x) == Tile.BLOCKED){
                    x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                    y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
                }
                Flag flag = new Flag(tm);
                flag.setPosition(x*tileSize+tileSize/2,y*tileSize-tileSize/2);
                this.addObject(flag);
            }
            if(Math.random() > 0.5){
                int tileSize = tm.getTileSize();

                int xMinTile = xMin/tileSize + 1;
                int yMinTile = yMin/tileSize + 1;

                int xMaxTile = xMax/tileSize - 1;
                int yMaxTile = yMax/tileSize - 1;

                int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
                while(tm.getType(y,x) == Tile.BLOCKED){
                    x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                    y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
                }
                Barrel barrel = new Barrel(tm);
                barrel.setPosition(x*tileSize+tileSize/2,y*tileSize+tileSize/2);
                this.addObject(barrel);
            }
        }
        if (type == Loot) {
            Chest chest = new Chest(tm);
            chest.setPosition(xMin + (float) (xMax - xMin) / 2, yMin + (float) (yMax - yMin) / 2);
            mapObjects.add(chest);
        }
        if(type == Shop){
            for(int i = 1;i<=3;i++){
                ShopTable table = new ShopTable(tm);
                table.setPosition(xMin+ (float) (xMax - xMin) / 4 * i,yMin + (float) (yMax - yMin) / 2);
                table.createItem();
                mapObjects.add(table);

                if(i == 2){
                    Shopkeeper shopkeeper = new Shopkeeper(tm);
                    shopkeeper.setPosition(xMin+ (float) (xMax - xMin) / 4 * i,yMin + (float) (yMax - yMin) / 2 - 300);
                    this.addObject(shopkeeper);
                }
            }
        }
        if(type == Boss){
            int tileSize = tm.getTileSize();
            for(int i = 4;i<6;i++){
                for(int j = 4;j<6;j++){
                    if(i == 4 && j == 4) continue;
                    Barrel barrel = new Barrel(tm);
                    barrel.setPosition(xMin + i*tileSize, yMin + j*tileSize);
                    mapObjects.add(barrel);

                    barrel = new Barrel(tm);
                    barrel.setPosition(xMax - i*tileSize, yMin + j*tileSize);
                    mapObjects.add(barrel);

                    barrel = new Barrel(tm);
                    barrel.setPosition(xMax - i*tileSize, yMax - j*tileSize);
                    mapObjects.add(barrel);

                    barrel = new Barrel(tm);
                    barrel.setPosition(xMin + i*tileSize, yMax - j*tileSize);
                    mapObjects.add(barrel);
                }
            }
        }
        if(type == Progress){
            int tileSize = tm.getTileSize();
            Portal portal = new Portal(tm);
            portal.setPosition((float) (numCols*tileSize) / 2,  2*tileSize);
            mapObjects.add(portal);
            // flags
            Flag flag = new Flag(tm);
            flag.setPosition(12*tileSize+tileSize/2,3*tileSize-tileSize/2);
            this.addObject(flag);
            flag = new Flag(tm);
            flag.setPosition(16*tileSize+tileSize/2,3*tileSize-tileSize/2);
            this.addObject(flag);
            // barrels
            for(int i = 13;i<16;i++){
                Barrel barrel = new Barrel(tm);
                barrel.setPosition(i*tileSize+tileSize/2,7*tileSize+tileSize/2);
                barrel.setMoveable(false);
                this.addObject(barrel);
            }
        }

    }


    public ArrayList<RoomObject> getMapObjects(){return mapObjects;}

    public void addWall(TileMap tm, float x, float y,int dir){

        PathWall roomPath = new PathWall(tm);
        roomPath.setDirection(dir);
        roomPath.setPosition(x,y);
        mapObjects.add(roomPath);
    }
    public void addObject(RoomObject obj){
        mapObjects.add(obj);
    }

    public void setMinimapRoom(MMRoom minimapRoom) {
        this.minimapRoom = minimapRoom;
    }
    public void showRoomOnMinimap() {
        minimapRoom.setDiscovered(true);
    }

    public MMRoom getMinimapRoom() {
        return minimapRoom;
    }
    public void keyPressed(int k, Player p){
        if(k == GLFW.GLFW_KEY_E){
            for(RoomObject object : mapObjects){
                if(object.intersects(p)){
                    object.keyPress();
                }
            }
        }
    }
}

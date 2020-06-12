package cz.Empatix.Render;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.EnemyManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Room {
    // if player has entered room yet
    private boolean entered;

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

    public boolean[] getPaths(){
        // 0 - TOP
        // 1 - BOTTOM
        // 2 - LEFT
        // 3 - RIGHT
        boolean[] paths = new boolean[4];
        paths[0] = top;
        paths[1] = bottom;
        paths[2] = left;
        paths[3] = right;
        return paths;
    }

    void setTop(boolean top) {
        this.top = top;
    }

    void setLeft(boolean left) {
        this.left = left;
    }

    void setBottom(boolean bottom) {
        this.bottom = bottom;
    }

    void setRight(boolean right) {
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

    public void drawObjects(){
        for(RoomObject object : mapObjects){
            object.draw();
        }
    }
    public void updateObjects(){
        for(int i = 0;i<mapObjects.size();i++){
            RoomObject object = mapObjects.get(i);
            if(object instanceof Chest) {
                if(((Chest)object).shouldRemove()){
                    mapObjects.remove(i);
                    i--;
                    continue;
                }
            }
            object.update();
        }
        if(EnemyManager.areEnemiesDead()){
            lockRoom(false);
        }
    }
    public void createObjects(TileMap tm){
        if(type == Room.Loot){
            Chest chest = new Chest(tm);
            chest.setPosition(xMin + (float) (xMax - xMin) / 2,yMin + (float) (yMax - yMin) / 2);
            mapObjects.add(chest);
        }
    }


    public ArrayList<RoomObject> getMapObjects(){return mapObjects;}

    public void addWall(TileMap tm, float x, float y,int dir){
        System.out.println("X "+x);
        System.out.println("Y "+y);

        PathWall roomPath = new PathWall(tm);
        roomPath.setDirection(dir);
        roomPath.setPosition(x,y);
        mapObjects.add(roomPath);
    }
    public void addObject(RoomObject obj){
        mapObjects.add(obj);
    }
}

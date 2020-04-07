package cz.Empatix.Render;

import cz.Empatix.Entity.EnemyManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public final static int Classic = 0;
    public final static int Loot = 1;
    public final static int Shop = 2;
    public final static int Boss = 3;
    public final static int Path = 4;


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

    private int tileSize;

    private RoomPath[] roomPaths;

    Room(int type, int id, int x, int y, int tileSize){
        entered = false;

        this.id = id;

        this.x = x;
        this.y = y;

        this.type = type;

        this.tileSize = tileSize;

        roomPaths = new RoomPath[4];

    }

    void loadMap(){
        try {
            InputStream in = getClass().getResourceAsStream("/Map/currentmap"+(new Random().nextInt(2)+1)+".map");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(in)
            );

            numCols = Integer.parseInt(br.readLine());
            numRows = Integer.parseInt(br.readLine());

            roomMap = new byte[numRows][numCols];

            String delims = "\\s+";
            for(int row = 0; row < numRows; row++) {
                String line = br.readLine();
                String[] tokens = line.split(delims);
                for(int col = 0; col < numCols; col++) {
                    roomMap[row][col] = Byte.parseByte(tokens[col]);
                }
            }
            // converting
/*
            for (int col = 0;col < numCols;col++){
                for (int row = 0; row < numRows;row++){

                    int currentTile = collisionMap[row][col];
                    int bottomTile;
                    int topTile;
                    int rightTile;
                    int leftTile;

                    int x;

                    // TOP
                    x = row-1;
                    if (x >= 0){ topTile = collisionMap[x][col]; }
                    else { topTile = 0; }

                    // BOTTOM
                    x = row+1;
                    if (x <numRows){ bottomTile = collisionMap[x][col]; }
                    else { bottomTile = 0; }

                    // RIGHT
                    x = col+1;
                    if (x < numCols){ rightTile = collisionMap[row][x]; }
                    else { rightTile = 0; }

                    // LEFT
                    x = col-1;
                    if (x >= 0){ leftTile = collisionMap[row][x]; }
                    else { leftTile = 0; }
                    if (currentTile == 1){
                        if (topTile == 1 && bottomTile == 1){
                            roomMap[row][col] = 22;
                        } else if (topTile == 1){
                            if (leftTile == 1){
                                roomMap[row][col] = 33;
                            } else if (rightTile == 1){
                                roomMap[row][col] = 28;
                            }
                        } else if (bottomTile == 1){
                            if (leftTile == 1){
                                roomMap[row][col] = 25;
                            } else if (rightTile == 1){
                                roomMap[row][col] = 20;
                            }
                        }
                    }
                }
            }
*/
        }
        catch(Exception e) {
            e.printStackTrace();
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

    int countPaths(){
        int i = 0;
        if (bottom) i++;
        if (top) i++;
        if (left) i++;
        if (right) i++;
        return i;
    }

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
}

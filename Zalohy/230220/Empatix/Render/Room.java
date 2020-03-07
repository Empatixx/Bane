package cz.Empatix.Render;

import cz.Empatix.Entity.Enemies.Slime;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.InGame;

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

    // orientation about paths
    private boolean bottom;
    private boolean top;
    private boolean left;
    private boolean right;

    // tiles of room
    private int[][] roomMap;

    // corners of room
    private int yMin;
    private int yMax;
    private int xMin;
    private int xMax;

    private InGame gs;


    Room(int type, int id, int x    , int y, InGame gameState){
        entered = false;

        this.id = id;

        this.x = x;
        this.y = y;

        this.type = type;

        this.gs = gameState;

    }

    void loadMap(){
        try {
            InputStream in = getClass().getResourceAsStream("/Map/test"+(new Random().nextInt(1)+1)+".map");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(in)
            );

            numCols = Integer.parseInt(br.readLine());
            numRows = Integer.parseInt(br.readLine());

            roomMap = new int[numRows][numCols];

            String delims = "\\s+";
            for(int row = 0; row < numRows; row++) {
                String line = br.readLine();
                String[] tokens = line.split(delims);
                for(int col = 0; col < numCols; col++) {
                    char letter = tokens[col].charAt(0);
                    if (Character.isLetter(letter)){
                        if (letter == 'R' && (!right)) {
                            roomMap[row][col] = 25;
                        } else if (letter == 'L' && (!left)) {
                            roomMap[row][col] = 26;
                        } else if (letter == 'T' && (!top)) {
                            roomMap[row][col] = 21;
                        } else if (letter == 'B' && (!bottom)) {
                            roomMap[row][col] = 30;
                        } else {
                            roomMap[row][col] = 6;
                        }
                    }  else {
                        roomMap[row][col] = Integer.parseInt(tokens[col]);
                    }
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

    int[][] getRoomMap() {
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

            TileMap tm = gs.getTileMap();
            Player p = gs.getPlayer();

            int tileSize = tm.getTileSize();
            int maxMobs = cz.Empatix.Java.Random.nextInt(3) + 1;


            for (int i = 0; i < maxMobs;i++){

                int x = getRandom(xMin+tileSize*2,xMax-tileSize*2);
                int y = getRandom(yMin+tileSize*2,yMax-tileSize*2);
                Slime slime = new Slime(tm,p);
                slime.setPosition(x,y);

                gs.addHostile(slime);
            }
        }
    }
    private int getRandom(int lower, int upper) {
        return cz.Empatix.Java.Random.nextInt((upper - lower) + 1) + lower;
    }
}

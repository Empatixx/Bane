package cz.Empatix.Render;

import cz.Empatix.Main.Game;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Room {

    private int numCols;
    private int numRows;
    private final int type;

    public final static int Classic = 0;
    public final static int Loot = 1;
    public final static int Shop = 2;
    public final static int Boss = 3;

    // directions
    private boolean bottom;
    private boolean top;
    private boolean left;
    private boolean right;

    private int[][] roomMap;

    public Room(int type){

        this.type = type;

        try {

            InputStream in = getClass().getResourceAsStream("/Map/test.map");
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
                    roomMap[row][col] = Integer.parseInt(tokens[col]);
                }
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    public int getType() {
        return type;
    }

    public int[][] getRoomMap() {
        return roomMap;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getNumRows() {
        return numRows;
    }
}

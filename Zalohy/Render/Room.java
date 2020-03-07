package cz.Empatix.Render;

public class Room {

    private final int xmax;
    private final int ymax;
    private final int type;

    public final static int Classic = 0;
    public final static int Loot = 1;
    public final static int Shop = 2;
    public final static int Boss = 3;








    public Room(int xmax, int ymax, int type){

        this.xmax = xmax;
        this.ymax = ymax;

        this.type = type;


    }


    public int getType() {
        return type;
    }

    public int getXmax() {
        return xmax;
    }

    public int getYmax() {
        return ymax;
    }
}

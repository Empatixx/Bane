package Render;

public class Room {
    // room vars
    private int id;
    private int type;

    private int tilesX;
    private int tilesY;

    // room types
    public static final int STARTROOM = 0;
    public static final int BOSS = 1;
    public static final int SHOP = 2;
    public static final int LOOTROOM = 3;
    public static final int CLASSIC = 4;



    public Room(int id, int type) {
        this.id = id;
        this.type = type;
    }

    public int getId() { return id; }
    public int getType() { return type; }

}

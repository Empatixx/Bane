package cz.Empatix.Render.Hud.Minimap;

public class MMRoom {
    private int type;
    private int x;
    private int y;
    public boolean discovered;

    // rooms between this room has paths
    private final MMRoom[] sideRooms;

    public MMRoom(int type, int x, int y){
        this.x = x;
        this.y = y;
        this.type = type;
        discovered = false;
        sideRooms = new MMRoom[4];
    }
    public void addSideRoom(MMRoom room,int index){
        sideRooms[index] = room;
    }

    int getType() {
        return type;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
        for(MMRoom room:sideRooms){
            if(room != null) room.discovered = true;
        }
    }
}

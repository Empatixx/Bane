package cz.Empatix.Render.Hud.Minimap;

import java.io.Serializable;

public class MMRoom implements Serializable {
    private int type;
    private int x;
    private int y;
    public boolean discovered;

    private boolean right;
    private boolean left;
    private boolean top;
    private boolean bottom;

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

    public boolean isBottom() {
        return bottom;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isTop() {
        return top;
    }

    public void setBottom(boolean bottom) {
        this.bottom = bottom;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public MMRoom[] getSideRooms() {
        return sideRooms;
    }
}

package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.MapObject;
import cz.Empatix.Render.TileMap;

public abstract class RoomObject extends MapObject {
    public boolean moveable;
    public boolean collision;
    public boolean preDraw;
    public float speedMoveBoost;
    public boolean remove;
    public RoomObject(TileMap tm){
        super(tm);
        remove = false;
    }
    public abstract void touchEvent();
    public abstract void update();

    public abstract void keyPress();

    public boolean isPreDraw() {
        return preDraw;
    }

    public float getSpeedMoveBoost() {
        return speedMoveBoost;
    }
    public boolean shouldRemove(){return remove;}

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }
}

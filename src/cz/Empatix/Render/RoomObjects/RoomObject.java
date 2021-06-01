package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.MapObject;
import cz.Empatix.Render.TileMap;

import java.io.Serializable;

public abstract class RoomObject extends MapObject implements Serializable {
    public boolean moveable;
    public boolean collision;
    public boolean preDraw;

    public boolean remove;
    public boolean behindCollision;

    public float maxMovement;
    public RoomObject(TileMap tm){
        super(tm);
        remove = false;
        behindCollision=false;

        maxMovement = 1f;
    }
    public abstract void touchEvent();
    public abstract void update();

    public abstract void keyPress();

    public boolean isPreDraw() {
        return preDraw;
    }

    public boolean shouldRemove(){return remove;}

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
    }

    public void delete(){remove = true;}

    public boolean isBehindCollision(){return behindCollision;}

    public abstract void loadSave();

    public float getMaxMovement() {
        return maxMovement;
    }
}

package cz.Empatix.Render;

import cz.Empatix.Entity.MapObject;

public abstract class RoomObject extends MapObject {
    public boolean moveable;
    public boolean collision;
    public RoomObject(TileMap tm){
        super(tm);
    }
    public abstract void touchEvent();
    public abstract void update();
}

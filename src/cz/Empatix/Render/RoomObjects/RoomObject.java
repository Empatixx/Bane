package cz.Empatix.Render.RoomObjects;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.TileMap;

public abstract class RoomObject extends MapObject{
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
    public abstract void touchEvent(MapObject o);
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

    public void sendMovePacket(){
        if(tileMap.isServerSide()){
            Network.MoveRoomObject moveRoomObject = new Network.MoveRoomObject();
            moveRoomObject.id = getId();
            moveRoomObject.x = position.x;
            moveRoomObject.y = position.y;
            Server server = MultiplayerManager.getInstance().server.getServer();
            server.sendToAllUDP(moveRoomObject);
        }
    }


}

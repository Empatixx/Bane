package cz.Empatix.Entity.ItemDrops;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.TileMap;

public abstract class ItemDrop extends MapObject {
    int amount;

    public static final int PISTOLAMMO = 1;
    public static final int SHOTGUNAMMO = 3;
    public static final int SUBMACHINE = 2;
    public static final int EXPLOSIVEAMMO = 4;

    public static final int HP = 0;
    public static final int ARTEFACT = 8;
    public static final int ARMOR = 7;
    public static final int GUN = 6;
    public static final int COIN = 5;
    public static final int AMMOBOX = 9;
    public static final int STATUPGRADE = 10;

    public int type;
    public boolean pickedUp;

    public boolean shop;
    public int price;

    public boolean canDespawn;
    public long liveTime;

    public ItemDrop(TileMap tm){
        super(tm);
        shop = false;

        stopAcceleration = 1.5f;
        movementVelocity = 550;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }
    public void update() {
        if(!MultiplayerManager.multiplayer || tileMap.isServerSide()){
            checkRoomObjectsCollision();
            checkTileMapCollision();
            setPosition(temp.x, temp.y);
            getMovementSpeed();
            if(tileMap.isServerSide()){
                if(shop) return; // shop items are static located
                Network.MoveDropItem moveDropItem = new Network.MoveDropItem();
                moveDropItem.id = id;
                moveDropItem.x = position.x;
                moveDropItem.y = position.y;
                Server server = MultiplayerManager.getInstance().server.getServer();
                server.sendToAllUDP(moveDropItem);
            }
        }
    }

    public void remove(){
        if(tileMap.isServerSide()) return;
        light.remove();
    }
    public int getAmount() {
        return amount;
    }

    public void setShop(int price){
        this.price = price;
        canDespawn=false;
        shop = true;
    }
    public void shopBuy(){
        shop = false;
    }

    public int getPrice() {
        return price;
    }

    public boolean isShop() {
        return shop;
    }

    public void preventDespawn(){
        canDespawn = false;
    }

}

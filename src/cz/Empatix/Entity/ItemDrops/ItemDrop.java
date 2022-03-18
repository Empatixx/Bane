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

    private static int idGen = 0;
    private int idDrop;

    public ItemDrop(TileMap tm){
        super(tm);
        shop = false;

        if(tm.isServerSide()){
            idDrop = idGen++;
        }
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
                Network.MoveDropItem moveDropItem = new Network.MoveDropItem();
                moveDropItem.id = idDrop;
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
    private void getMovementSpeed() {
        if (speed.x < 0){
            speed.x += stopSpeed;
            if (speed.x > 0) speed.x = 0;
        } else if (speed.x > 0){
            speed.x -= stopSpeed;
            if (speed.x < 0) speed.x = 0;
        }
        if (speed.y < 0){
            speed.y += stopSpeed;
            if (speed.y > 0) speed.y = 0;
        } else if (speed.y > 0){
            speed.y -= stopSpeed;
            if (speed.y < 0) speed.y = 0;

        }

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

    public int getId() {
        return idDrop;
    }

    public void setId(int id) {
        this.idDrop = id;
    }
}

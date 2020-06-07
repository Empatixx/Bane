package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.Entity.MapObject;
import cz.Empatix.Render.TileMap;

public abstract class ItemDrop extends MapObject {
    int amount;

    static final int PISTOLAMMO = 1;
    static final int SHOTGUNAMMO = 3;
    static final int SUBMACHINE = 2;

    static final int HP = 0;
    static final int ARMOR = 4;
    static final int GUN = 6;
    static final int COIN = 5;

    int type;
    boolean pickedUp;

    boolean canDespawn;
    long liveTime;

    public ItemDrop(TileMap tm){
        super(tm);
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }
    public void update() { }

    public void remove(){
        pickedUp = true;
        light.remove();
    }

    public int getAmount() {
        return amount;
    }
}
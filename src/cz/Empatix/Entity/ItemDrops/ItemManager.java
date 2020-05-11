package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Guns.Weapon;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;

public class ItemManager {
    private static ArrayList<ItemDrop> itemDrops;
    private static TileMap tm;
    private static GunsManager gm;
    private static Player player;


    private final int pickupSound;
    private Source source;

    public ItemManager(TileMap tm, GunsManager gm, Player player){
        this.tm = tm;
        this.gm = gm;
        this.player = player;
        itemDrops = new ArrayList<>();
        pickupSound =  AudioManager.loadSound("pickup.ogg");
        source = new Source(Source.EFFECTS,0.35f);

    }
    public static void createDrop(float x, float y){
        int drops = 4;
        if(player.getHealth() == player.getMaxHealth()){
            drops--;
        }
        int random = cz.Empatix.Java.Random.nextInt(drops);
        if(random == 0){
            ItemDrop drop = new PistolAmmo(tm);
            drop.setPosition(x,y);
            itemDrops.add(drop);

        } else if (random == 1) {
            ItemDrop drop = new ShotgunAmmo(tm);
            drop.setPosition(x,y);
            itemDrops.add(drop);
        } else if(random == 3){
            ItemDrop drop = new HealingPot(tm);
            drop.setPosition(x,y);
            itemDrops.add(drop);
        } else {
            ItemDrop drop = new Coin(tm);
            drop.setPosition(x,y);
            itemDrops.add(drop);
        }
    }
    public void update(){
        for(int i = 0;i<itemDrops.size();i++){
            ItemDrop drop = itemDrops.get(i);
            drop.update();
            if(itemDrops.get(i).isPickedUp()){
                itemDrops.get(i).remove();
                itemDrops.remove(i);
                i--;
            }
        }
        float distance = -1;
        ItemDrop selectedDrop = null;
        for(ItemDrop drop : itemDrops){
            if(drop.type == ItemDrop.GUN){
                ((WeaponDrop)drop).setCanPick(false);
            }
            if(drop.intersects(player)){
                int type = drop.type;
                if(type == ItemDrop.PISTOLAMMO || type == ItemDrop.SHOTGUNAMMO || type == ItemDrop.SUBMACHINE){
                    boolean done = gm.addAmmo(drop.getAmount(),type);
                    if(done){
                        drop.pickedUp = true;
                        source.play(pickupSound);
                    }
                } else if(type == ItemDrop.HP){
                    if(player.getHealth() != player.getMaxHealth()){
                        player.addHealth(2);
                        drop.pickedUp = true;
                        source.play(pickupSound);
                    }
                } else if(type == ItemDrop.COIN){
                    player.addCoins(1);
                    drop.pickedUp = true;
                    source.play(pickupSound);
                } else if(type == ItemDrop.GUN){
                    float newDist = (float)((WeaponDrop)drop).distance(player.getX(),player.getY());
                    if(distance > newDist || distance == -1) {
                        distance = newDist;
                        selectedDrop = drop;
                    }
                }

            }
        }
        if(selectedDrop != null) {
            ((WeaponDrop)selectedDrop).setCanPick(true);
        }
    }
    public void draw(){
        for(ItemDrop drop : itemDrops){
            drop.draw();
        }
    }
    public static void dropWeapon(Weapon weapon, int x,int y){
        WeaponDrop drop = new WeaponDrop(tm,weapon,x, y);
        drop.setPosition(player.getX(),player.getY());
        itemDrops.add(drop);
    }
    public void pickUpGun(int x, int y){
        float distance = -1;
        ItemDrop selectedDrop = null;
        for(ItemDrop drop : itemDrops) {
            if (drop.type == ItemDrop.GUN) {
                if(((WeaponDrop)drop).isCanPick()){
                    float newDist = (float)((WeaponDrop)drop).distance(player.getX(),player.getY());
                    if(distance > newDist || distance == -1){
                        distance = newDist;
                        selectedDrop = drop;
                    }
                }
            }
        }
        if(selectedDrop != null){
            gm.changeGun(x,y,((WeaponDrop)selectedDrop).getWeapon());
            selectedDrop.pickedUp = true;
        }
    }
}

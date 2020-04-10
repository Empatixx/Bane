package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.GunsManager;
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
    public void update(float x, float y){
        for(int i = 0;i<itemDrops.size();i++){
            ItemDrop drop = itemDrops.get(i);
            drop.update();
            if(itemDrops.get(i).isPickedUp()){
                itemDrops.get(i).remove();
                itemDrops.remove(i);
                i--;
            }
        }
        for(ItemDrop drop : itemDrops){
            if(drop.intersectsPlayer(x,y)){
                drop.pickedUp = true;

                int type = drop.type;
                if(type == ItemDrop.PISTOLAMMO || type == ItemDrop.SHOTGUNAMMO || type == ItemDrop.SUBMACHINE){

                    gm.addAmmo(drop.getAmount(),type);
                } else if(type == ItemDrop.HP){
                    player.addHealth(2);
                } else if(type == ItemDrop.COIN){
                    player.addCoins(1);
                }

                source.play(pickupSound);
            }
        }
    }
    public void draw(){
        for(ItemDrop drop : itemDrops){
            drop.draw();
        }
    }
}

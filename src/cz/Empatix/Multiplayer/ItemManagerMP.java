package cz.Empatix.Multiplayer;

import cz.Empatix.Entity.ItemDrops.*;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.Weapon;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;

import java.util.ArrayList;

public class ItemManagerMP {
    private ArrayList<ItemDrop> itemDrops;
    private TileMap tm;
    private GunsManagerMP gm;
    private ArtefactManagerMP am;
    private Player player[];

    private int totalCoins;

    private boolean showShopHud;
    private ItemDrop shopItem;

    private long alertCooldown;

    public static ItemManagerMP itemManagerMP;

    public static ItemManagerMP getInstance() {
        return itemManagerMP;
    }

    public ItemManagerMP(TileMap tm, GunsManagerMP gm, ArtefactManagerMP am, Player[] player) {
        this.tm = tm;
        this.gm = gm;
        this.am = am;
        this.player = player;
        itemDrops = new ArrayList<>();

        totalCoins = 0;

        itemManagerMP = this;
    }
    public void clear() {
        for (ItemDrop i : itemDrops) {
            if (i instanceof WeaponDrop) {
                ((WeaponDrop) i).despawn();
            }
            if (i instanceof ArtefactDrop) {
                ((ArtefactDrop) i).despawn();
            }
            i.remove();
        }
        itemDrops.clear();
    }

    public void createShopDrop(float x, float y) {
        int drops = 6;
        int random = cz.Empatix.Java.Random.nextInt(drops);

        ItemDrop drop;
        if (random == 0) {
            drop = new PistolAmmo(tm);
            drop.setPosition(x, y);
            drop.setAmount(60);
            itemDrops.add(drop);
        } else if (random == 3) {
            drop = new ShotgunAmmo(tm);
            drop.setPosition(x, y);
            drop.setAmount(60);
            itemDrops.add(drop);
        } else if (random == 4) {
            drop = new ArmorPot(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        } else if (random == 2) {
            drop = new HealingPot(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        } else if (random == 5) {
            drop = new ExplosiveAmmo(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        } else {
            Weapon weapon = gm.randomGun();
            weapon.drop();
            drop = new WeaponDrop(tm, weapon);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        }
        if(drop instanceof WeaponDrop){
            drop.setShop(Random.nextInt(5+tm.getFloor()*2)
                    +5+tm.getFloor()*2);
        } else {
            drop.setShop(Random.nextInt(3+tm.getFloor()) + 2+tm.getFloor()*2);
        }
    }

    public ItemDrop createDrop(float x, float y) {
        int drops = 3;
        int randomIndexPlayer;
        do{
            randomIndexPlayer = Random.nextInt(player.length);
        } while(player[randomIndexPlayer] == null);

        if (player[randomIndexPlayer].getHealth() == player[randomIndexPlayer].getMaxHealth()) {
            drops--;
        }
        int random = cz.Empatix.Java.Random.nextInt(drops);

        int[] weaponTypes = gm.getWeaponTypes(((PlayerMP)player[randomIndexPlayer]).getUsername());
        ItemDrop drop = null;
        if (random == 0) {
            int numWeapons = 0;
            for(int type : weaponTypes) {
                if(type != -1){
                    numWeapons++;
                }
            }
            if(numWeapons == 1){
                for(int type : weaponTypes) {
                    if(type != -1){
                        if (type == ItemDrop.PISTOLAMMO) {
                            drop = new PistolAmmo(tm);
                            drop.setPosition(x, y);
                            itemDrops.add(drop);
                        } else if (type == ItemDrop.EXPLOSIVEAMMO){
                            drop = new ExplosiveAmmo(tm);
                            drop.setPosition(x, y);
                            itemDrops.add(drop);
                        } else{
                            drop = new ShotgunAmmo(tm);
                            drop.setPosition(x, y);
                            itemDrops.add(drop);
                        }
                    }
                }
            } else {
                int type = weaponTypes[Random.nextInt(numWeapons)];
                while(type == -1) {
                    type = weaponTypes[Random.nextInt(numWeapons)];
                }
                if (type == ItemDrop.PISTOLAMMO) {
                    drop = new PistolAmmo(tm);
                    drop.setPosition(x, y);
                    itemDrops.add(drop);
                } else if (type == ItemDrop.EXPLOSIVEAMMO){
                    drop = new ExplosiveAmmo(tm);
                    drop.setPosition(x, y);
                    itemDrops.add(drop);
                } else {
                    drop = new ShotgunAmmo(tm);
                    drop.setPosition(x, y);
                    itemDrops.add(drop);
                }
            }
        } else if (random == 2) {
            drop = new HealingPot(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        } else {
            drop = new Coin(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        }
        return drop;
    }

    public void update() {
        for (int i = 0; i < itemDrops.size(); i++) {
            ItemDrop drop = itemDrops.get(i);
            drop.update();
            if (itemDrops.get(i).isPickedUp()) {
                itemDrops.get(i).remove();
                itemDrops.remove(i);
                i--;
            }
        }
        float distance = -1;
        ItemDrop selectedDrop = null;
        showShopHud = false;
        for (ItemDrop drop : itemDrops) {
            if (drop.type == ItemDrop.GUN) {
                ((WeaponDrop) drop).setCanPick(false);
            }
            if (drop.type == ItemDrop.ARTEFACT) {
                ((ArtefactDrop) drop).setCanPick(false);
            }
            for(int i = 0;i<player.length;i++){
                if(player[i] != null){
                    if (drop.intersects(player[i])) {
                        int type = drop.type;
                        if (drop.isShop()) {
                            showShopHud = true;
                            shopItem = drop;
                        } else {
                            if (type == ItemDrop.PISTOLAMMO || type == ItemDrop.SHOTGUNAMMO || type == ItemDrop.EXPLOSIVEAMMO) {
                                boolean done = gm.addAmmo(drop.getAmount(), type, ((PlayerMP)player[i]).getUsername());
                                if (done) {
                                    drop.pickedUp = true;
                                }
                            } else if (type == ItemDrop.HP) {
                                if (player[i].getHealth() != player[i].getMaxHealth()) {
                                    player[i].addHealth(2);
                                    drop.pickedUp = true;
                                }
                            } else if (type == ItemDrop.COIN) {
                                player[i].addCoins(drop.getAmount());
                                totalCoins+=drop.getAmount();
                                drop.pickedUp = true;
                            } else if (type == ItemDrop.GUN) {
                                float newDist = (float) ((WeaponDrop) drop).distance(player[i].getX(), player[i].getY());
                                if (distance > newDist || distance == -1) {
                                    distance = newDist;
                                    selectedDrop = drop;
                                }
                            } else if (type == ItemDrop.ARTEFACT) {
                                float newDist = (float) ((ArtefactDrop) drop).distance(player[i].getX(), player[i].getY());
                                if (distance > newDist || distance == -1) {
                                    distance = newDist;
                                    selectedDrop = drop;
                                }
                            } else if (type == ItemDrop.ARMOR) {
                                if (player[i].getArmor() != player[i].getMaxArmor()) {
                                    player[i].addArmor(2);
                                    drop.pickedUp = true;
                                }
                            }
                        }

                    }
                }
            }
        }
        if (selectedDrop != null) {
            if(selectedDrop instanceof WeaponDrop){
                ((WeaponDrop) selectedDrop).setCanPick(true);
            } else if(selectedDrop instanceof ArtefactDrop){
                ((ArtefactDrop) selectedDrop).setCanPick(true);
            }
        }
    }

    public void dropWeapon(int x, int y, Vector2f speed) {
        Weapon weapon = gm.randomGun();
        weapon.drop();
        WeaponDrop drop = new WeaponDrop(tm, weapon);
        drop.setSpeed(speed.x, speed.y);
        drop.setPosition(x, y);
        itemDrops.add(drop);
    }
    public void dropWeapon(Weapon weapon, int x, int y, Vector2f speed) {
        weapon.drop();
        WeaponDrop drop = new WeaponDrop(tm, weapon);
        drop.setSpeed(speed.x, speed.y);
        drop.setPosition(x, y);
        itemDrops.add(drop);
    }
    public void dropArtefact(int x, int y) {
        //TODO:
        //ArtefactDrop drop = new ArtefactDrop(tm, am.randomArtefact());
        //drop.setPosition(x, y);
        //itemDrops.add(drop);
    }
    public void dropPlayerArtefact(Artefact artefact, int x, int y, String username) {
        ArtefactDrop drop = new ArtefactDrop(tm, artefact,x,y);
        // todo: fix for mp
        drop.setPosition((int)player[0].getX(), (int)player[0].getY()+30);
        itemDrops.add(drop);
    }
    public void createDrop(float x, float y, Vector2f speed, String username) {
        int random = cz.Empatix.Java.Random.nextInt(5);
        ItemDrop drop;
        int[] weaponTypes = gm.getWeaponTypes(username);
        if (random == 0) {
            int numWeapons = 0;
            for(int type : weaponTypes) {
                if(type != -1){
                    numWeapons++;
                }
            }
            if(numWeapons == 1){
                for(int type : weaponTypes) {
                    if(type != -1){
                        if (type == ItemDrop.PISTOLAMMO) {
                            drop = new PistolAmmo(tm);
                            drop.setPosition(x, y);
                            drop.setSpeed(speed.x, speed.y);
                            itemDrops.add(drop);
                        } else if (type == ItemDrop.EXPLOSIVEAMMO){
                            drop = new ExplosiveAmmo(tm);
                            drop.setPosition(x, y);
                            drop.setSpeed(speed.x, speed.y);
                            itemDrops.add(drop);
                        } else{
                            drop = new ShotgunAmmo(tm);
                            drop.setPosition(x, y);
                            drop.setSpeed(speed.x, speed.y);
                            itemDrops.add(drop);
                        }
                    }
                }
            } else {
                int type = weaponTypes[Random.nextInt(numWeapons)];
                while(type == -1) {
                    type = weaponTypes[Random.nextInt(numWeapons)];
                }
                if (type == ItemDrop.PISTOLAMMO) {
                    drop = new PistolAmmo(tm);
                    drop.setPosition(x, y);
                    drop.setSpeed(speed.x, speed.y);
                    itemDrops.add(drop);
                } else if (type == ItemDrop.EXPLOSIVEAMMO){
                    drop = new ExplosiveAmmo(tm);
                    drop.setPosition(x, y);
                    drop.setSpeed(speed.x, speed.y);
                    itemDrops.add(drop);
                } else {
                    drop = new ShotgunAmmo(tm);
                    drop.setPosition(x, y);
                    drop.setSpeed(speed.x, speed.y);
                    itemDrops.add(drop);
                }
            }
        } else if (random == 1) {
            drop = new ShotgunAmmo(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        } else if (random == 3) {
            drop = new HealingPot(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        } else if (random == 4) {
            drop = new ExplosiveAmmo(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        } else {
            drop = new Coin(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        }
    }

    public void dropPlayerWeapon(Weapon weapon, int x, int y) {
        WeaponDrop drop = new WeaponDrop(tm, weapon, x, y);
        //todo: fix for mp
        drop.setPosition((int) player[0].getX(), (int) player[0].getY()+30);
        itemDrops.add(drop);
    }

    /**
     *
     * @param x - location of player X + X of mouse + tilemap X
     * @param y - location of player Y + Y of mouse + tilemap Y
     */

    public boolean keyPressed(int k, int x, int y, String username) {
        if(k == ControlSettings.getValue(ControlSettings.OBJECT_INTERACT)){
            // picking gun from ground
            float distance = -1;
            ItemDrop selectedDrop = null;
            for (ItemDrop drop : itemDrops) {
                if (drop.type == ItemDrop.GUN){
                    if (((WeaponDrop) drop).isCanPick()){
                        float newDist = (float) ((WeaponDrop) drop).distance(player[0].getX(), player[0].getY());
                        if (distance > newDist || distance == -1){
                            distance = newDist;
                            selectedDrop = drop;
                        }
                    }
                }
                if (drop.type == ItemDrop.ARTEFACT) {
                    if (((ArtefactDrop) drop).isCanPick()) {
                        float newDist = (float) ((ArtefactDrop) drop).distance(player[0].getX(), player[0].getY());
                        if (distance > newDist || distance == -1) {
                            distance = newDist;
                            selectedDrop = drop;
                        }
                    }
                }
            }
            if (selectedDrop != null) {
                if(selectedDrop instanceof WeaponDrop){
                    gm.changeGun(x, y, ((WeaponDrop) selectedDrop).getWeapon(),username);
                    selectedDrop.pickedUp = true;
                } else {
                    //TODO:
                    //am.setCurrentArtefact(((ArtefactDrop) selectedDrop).getArtefact(),x,y);
                    selectedDrop.pickedUp = true;
                }
                return true;
            }
            if(showShopHud){
                // buying item from shop
                if(shopItem instanceof WeaponDrop){
                    if(player[0].getCoins() >= shopItem.getPrice()) {
                        player[0].removeCoins(shopItem.getPrice());
                        gm.changeGun(x, y, ((WeaponDrop) shopItem).getWeapon(),username);
                        shopItem.pickedUp = true;
                    } else {
                        if(System.currentTimeMillis() - alertCooldown > 2000){
                            alertCooldown = System.currentTimeMillis();
                            //TODO:
                            //AlertManager.add(AlertManager.WARNING,"You don't have enough coins");
                        }
                    }
                } else {
                    if(player[0].getCoins() >= shopItem.getPrice()) {
                        player[0].removeCoins(shopItem.getPrice());
                        shopItem.shopBuy();
                    } else {
                        if(System.currentTimeMillis() - alertCooldown > 2000){
                            alertCooldown = System.currentTimeMillis();
                            //TODO:
                            //AlertManager.add(AlertManager.WARNING,"You don't have enough coins");
                        }
                    }
                }
            }
        }
        return false;
    }
    public int getTotalCoins(){
        return totalCoins;
    }

    public void addItemDrop(ItemDrop itemdrop){
        itemDrops.add(itemdrop);
    }
}

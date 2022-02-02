package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.ItemDrops.*;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Guns.Weapon;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemManagerMP {
    private ArrayList<ItemDrop> itemDrops;
    private TileMap tm;
    private GunsManagerMP gm;
    private ArtefactManagerMP am;
    private PlayerMP[] player;

    private int totalCoins;

    private ItemDrop[] shopItem;
    private List<Network.ObjectInteract> objectInteractPackets;

    private long[] alertCooldown;

    public static ItemManagerMP itemManagerMP;

    public static ItemManagerMP getInstance() {
        return itemManagerMP;
    }

    public ItemManagerMP(TileMap tm, GunsManagerMP gm, ArtefactManagerMP am, PlayerMP[] player) {
        this.tm = tm;
        this.gm = gm;
        this.am = am;
        this.player = player;
        itemDrops = new ArrayList<>();
        shopItem = new ItemDrop[player.length];
        alertCooldown = new long[player.length];
        objectInteractPackets = Collections.synchronizedList(new ArrayList<>());

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

    public void createShopDrop(float x, float y, int idObject) {
        int drops = 6;
        int random = cz.Empatix.Java.Random.nextInt(drops);
        ItemDrop drop;

        int weaponSlot = -1;

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
        } else  {
            Weapon weapon = gm.randomGun();
            weapon.drop();
            drop = new WeaponDrop(tm, weapon);
            drop.setPosition(x, y);
            itemDrops.add(drop);
            weaponSlot = gm.getWeaponSlot(weapon);
        }
        int price;
        if(drop instanceof WeaponDrop){
            price = Random.nextInt(5+tm.getFloor()*2) +5+tm.getFloor()*2;
            drop.setShop(price);
        } else {
            price = Random.nextInt(3+tm.getFloor()) + 2+tm.getFloor()*2;
            drop.setShop(price);
        }
        Network.ShopDropitem shopPacket = new Network.ShopDropitem();
        shopPacket.id = drop.getId();
        shopPacket.idObject = idObject;
        shopPacket.type = random;
        shopPacket.price = drop.getPrice();
        shopPacket.weaponSlot = weaponSlot;
        Server server = MultiplayerManager.getInstance().server.getServer();
        server.sendToAllTCP(shopPacket);
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

        int[] weaponTypes = gm.getWeaponTypes(player[randomIndexPlayer].getUsername());
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
        Server server = MultiplayerManager.getInstance().server.getServer();
        Network.DropItem dropItem = new Network.DropItem();
        dropItem.type = random;
        dropItem.id = drop.getId();
        dropItem.x = (int)x;
        dropItem.y = (int)y;
        dropItem.amount = drop.getAmount();
        server.sendToAllTCP(dropItem);
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
            if (drop.type == ItemDrop.GUN) {
                ((WeaponDrop) drop).setCanPick(false);
            }
            if (drop.type == ItemDrop.ARTEFACT) {
                ((ArtefactDrop) drop).setCanPick(false);
            }
        }
        for(int i = 0;i<player.length;i++){
            ItemDrop selectedDrop = null;
            shopItem[i] = null;
            float distance = -1;
            PlayerMP player = this.player[i];
            for (ItemDrop drop : itemDrops) {
                if (player != null) {
                    if (drop.intersects(player)) {
                        int type = drop.type;
                        if (drop.isShop()) {
                            if (drop.intersects(this.player[i])) {
                                shopItem[i] = drop;
                            }
                        } else  {
                            if (type == ItemDrop.PISTOLAMMO || type == ItemDrop.SHOTGUNAMMO || type == ItemDrop.EXPLOSIVEAMMO) {
                                boolean done = gm.addAmmo(drop.getAmount(), type, player.getUsername());
                                if (done) {
                                    drop.pickedUp = true;
                                    Server server = MultiplayerManager.getInstance().server.getServer();
                                    Network.RemoveItem item = new Network.RemoveItem();
                                    item.id = drop.getId();
                                    server.sendToAllTCP(item);
                                }
                            } else if (type == ItemDrop.HP) {
                                if (player.getHealth() != player.getMaxHealth()) {
                                    player.addHealth(2);
                                    drop.pickedUp = true;
                                    Server server = MultiplayerManager.getInstance().server.getServer();
                                    Network.RemoveItem item = new Network.RemoveItem();
                                    item.id = drop.getId();
                                    server.sendToAllTCP(item);
                                }
                            } else if (type == ItemDrop.COIN) {
                                player.addCoins(drop.getAmount());
                                totalCoins += drop.getAmount();
                                drop.pickedUp = true;
                                Server server = MultiplayerManager.getInstance().server.getServer();
                                Network.RemoveItem item = new Network.RemoveItem();
                                item.id = drop.getId();
                                server.sendToAllTCP(item);
                            } else if (type == ItemDrop.GUN) {
                                float newDist = (float) ((WeaponDrop) drop).distance(player.getX(), player.getY());
                                if (distance > newDist || distance == -1) {
                                    distance = newDist;
                                    selectedDrop = drop;
                                }
                            } else if (type == ItemDrop.ARTEFACT) {
                                float newDist = (float) ((ArtefactDrop) drop).distance(player.getX(), player.getY());
                                if (distance > newDist || distance == -1) {
                                    distance = newDist;
                                    selectedDrop = drop;
                                }
                            } else if (type == ItemDrop.ARMOR) {
                                if (player.getArmor() != player.getMaxArmor()) {
                                    player.addArmor(2);
                                    drop.pickedUp = true;
                                    Server server = MultiplayerManager.getInstance().server.getServer();
                                    Network.RemoveItem item = new Network.RemoveItem();
                                    item.id = drop.getId();
                                    server.sendToAllTCP(item);
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
        for(Network.ObjectInteract objectInteract : objectInteractPackets){
            pickup(objectInteract);
        }
        objectInteractPackets.clear();
    }

    public void dropWeapon(int x, int y, Vector2f speed) {
        Weapon weapon = gm.randomGun();
        weapon.drop();
        WeaponDrop drop = new WeaponDrop(tm, weapon);
        drop.setSpeed(speed.x, speed.y);
        drop.setPosition(x, y);
        itemDrops.add(drop);

        // adding weapon drop to all players
        Network.DropWeapon dropWeapon = new Network.DropWeapon();
        dropWeapon.id = drop.getId();
        dropWeapon.x = x;
        dropWeapon.y = y;
        dropWeapon.slot = gm.getWeaponSlot(weapon);

        Server server = MultiplayerManager.getInstance().server.getServer();
        server.sendToAllTCP(dropWeapon);
    }
    public void dropWeapon(Weapon weapon, int x, int y, Vector2f speed) {
        weapon.drop();
        WeaponDrop drop = new WeaponDrop(tm, weapon);
        drop.setSpeed(speed.x, speed.y);
        drop.setPosition(x, y);
        itemDrops.add(drop);
    }

    /**
     * Dropping artefact by chests etc.
     * @param x - Y position of drop
     * @param y - X position of drop
     */
    public void dropArtefact(int x, int y) {
        Artefact artefact = am.randomArtefact();
        ArtefactDrop drop = new ArtefactDrop(tm, artefact);
        drop.setPosition(x, y);

        Network.DropArtefact dropArtefact = new Network.DropArtefact();
        dropArtefact.id = drop.getId();
        dropArtefact.x = x;
        dropArtefact.y = y;
        dropArtefact.slot = am.getArtefactSlot(artefact);
        Server server = MultiplayerManager.getInstance().server.getServer();
        server.sendToAllTCP(dropArtefact);

        itemDrops.add(drop);
    }

    /**
     * Dropping artefact by changing artefact of player
     * @param artefact - artefact that we want to drop
     * @param dx - X direction of drop
     * @param dy - Y direction of drop
     * @param username -
     */
    public void dropPlayerArtefact(Artefact artefact, int dx, int dy, String username) {
        for(PlayerMP p : player){
            if(p == null) continue;
            if(p.getUsername().equalsIgnoreCase(username)){
                ArtefactDrop drop = new ArtefactDrop(tm, artefact,dx,dy);
                int x = (int)p.getX();
                int y = (int)p.getY()+30;
                drop.setPosition((int)p.getX(), (int)p.getY()+30);
                Network.DropArtefact dropArtefact = new Network.DropArtefact();
                dropArtefact.slot = am.getArtefactSlot(artefact);
                dropArtefact.x = x;
                dropArtefact.y = y;
                dropArtefact.dx = dx;
                dropArtefact.dy = dy;
                dropArtefact.username = username;
                dropArtefact.id = drop.getId();
                itemDrops.add(drop);

                Server server = MultiplayerManager.getInstance().server.getServer();
                server.sendToAllTCP(dropArtefact);
                break;
            }
        }
    }
    public void createDrop(float x, float y, Vector2f speed) {
        int drops = 3;
        int randomIndexPlayer;
        do{
            randomIndexPlayer = Random.nextInt(player.length);
        } while(player[randomIndexPlayer] == null);

        if (player[randomIndexPlayer].getHealth() == player[randomIndexPlayer].getMaxHealth()) {
            drops--;
        }
        int random = cz.Empatix.Java.Random.nextInt(drops);
        int[] weaponTypes = gm.getWeaponTypes(player[randomIndexPlayer].getUsername());

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
        } else if (random == 2) {
            drop = new HealingPot(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        } else {
            drop = new Coin(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        }
        Server server = MultiplayerManager.getInstance().server.getServer();
        Network.DropItem dropItem = new Network.DropItem();
        dropItem.type = random;
        dropItem.id = drop.getId();
        dropItem.x = (int)x;
        dropItem.y = (int)y;
        dropItem.amount = drop.getAmount();
        server.sendToAllTCP(dropItem);
    }

    public void dropPlayerWeapon(Weapon weapon, int x, int y, int slot, String username) {
        WeaponDrop drop = new WeaponDrop(tm, weapon, x, y);
        for(PlayerMP p : player){
            if(p != null){
                if(p.getUsername().equalsIgnoreCase(username)){
                    int px = (int) p.getX();
                    int py = (int) p.getY()+30;
                    drop.setPosition((int) p.getX(), (int) p.getY()+30);

                    // adding weapon drop to all players
                    Network.DropWeapon dropWeapon = new Network.DropWeapon();
                    dropWeapon.id = drop.getId();
                    dropWeapon.x = px;
                    dropWeapon.y = py;
                    dropWeapon.slot = slot;

                    Server server = MultiplayerManager.getInstance().server.getServer();
                    server.sendToAllTCP(dropWeapon);

                    itemDrops.add(drop);
                }
            }
        }
    }

    public int getTotalCoins(){
        return totalCoins;
    }

    public void addItemDrop(ItemDrop itemdrop){
        itemDrops.add(itemdrop);
    }

    public void pickup(Network.ObjectInteract pickup) {
        int x = pickup.x;
        int y = pickup.y;
        for(int i = 0;i< player.length;i++){
            PlayerMP player = this.player[i];
            if(!pickup.username.equalsIgnoreCase(player.getUsername())) continue;
            // picking gun from ground
            float distance = -1;
            ItemDrop selectedDrop = null;
            for (ItemDrop drop : itemDrops) {
                if (drop instanceof WeaponDrop){
                    if (((WeaponDrop) drop).isCanPick()){
                        float newDist = (float) ((WeaponDrop) drop).distance(player.getX(), player.getY());
                        if (distance > newDist || distance == -1){
                            distance = newDist;
                            selectedDrop = drop;
                        }
                    }
                }
                if (drop instanceof ArtefactDrop) {
                    if (((ArtefactDrop) drop).isCanPick()) {
                        float newDist = (float) ((ArtefactDrop) drop).distance(player.getX(), player.getY());
                        if (distance > newDist || distance == -1) {
                            distance = newDist;
                            selectedDrop = drop;
                        }
                    }
                }
            }
            if (selectedDrop != null) {
                if(selectedDrop instanceof WeaponDrop){
                    gm.changeGun(x, y, ((WeaponDrop) selectedDrop).getWeapon(),player.getUsername());
                    selectedDrop.pickedUp = true;
                    pickup.sucessful = true;
                    pickup.id = selectedDrop.getId();
                    Server server = MultiplayerManager.getInstance().server.getServer();
                    server.sendToAllTCP(pickup);
                } else {
                    am.setCurrentArtefact(((ArtefactDrop) selectedDrop).getArtefact(),x,y, pickup.username);
                    selectedDrop.pickedUp = true;
                    pickup.sucessful = true;
                    pickup.id = selectedDrop.getId();
                    Server server = MultiplayerManager.getInstance().server.getServer();
                    server.sendToAllTCP(pickup);
                    selectedDrop.pickedUp = true;
                }
            }
            if(shopItem[i] != null){
                Server server = MultiplayerManager.getInstance().server.getServer();
                // buying item from shop
                if(shopItem[i] instanceof WeaponDrop) {
                    if (player.getCoins() >= shopItem[i].getPrice()) {
                        player.removeCoins(shopItem[i].getPrice());
                        gm.changeGun(x, y, ((WeaponDrop) shopItem[i]).getWeapon(), player.getUsername());
                        shopItem[i].pickedUp = true;
                        pickup.sucessful = true;
                        pickup.id = shopItem[i].getId();
                        server.sendToAllTCP(pickup);
                    } else {
                        if(System.currentTimeMillis() - alertCooldown[i] > 2000){
                            alertCooldown[i]  = System.currentTimeMillis();
                            Network.Alert alert = new Network.Alert();
                            alert.text = "You don't have enough coins";
                            alert.type = AlertManager.WARNING;
                            alert.username = pickup.username;
                            server.sendToAllUDP(alert);
                        }
                    }
                } else {
                    if (player.getCoins() >= shopItem[i].getPrice()) {
                        player.removeCoins(shopItem[i].getPrice());
                        shopItem[i].shopBuy();
                        pickup.sucessful = true;
                        pickup.id = shopItem[i].getId();
                        server.sendToAllTCP(pickup);
                    } else {
                        if (System.currentTimeMillis() - alertCooldown[i]  > 2000) {
                            alertCooldown[i]  = System.currentTimeMillis();
                            Network.Alert alert = new Network.Alert();
                            alert.text = "You don't have enough coins";
                            alert.type = AlertManager.WARNING;
                            alert.username = pickup.username;
                            server.sendToAllUDP(alert);
                        }
                    }
                }
            }
        }
    }
    public void handleObjectInteractPacket(Network.ObjectInteract objectInteract){
        objectInteractPackets.add(objectInteract);
    }
}

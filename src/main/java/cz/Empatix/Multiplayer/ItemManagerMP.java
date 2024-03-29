package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.ItemDrops.*;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Guns.Weapon;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Random;
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
    private List<Network.DropInteract> dropInteractPackets;

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
        dropInteractPackets = Collections.synchronizedList(new ArrayList<>());

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
        int random = Random.nextInt(drops);
        ItemDrop drop;
        int slot = -1;

        if (random == 0) {
            drop = new AmmoBox(tm);
            drop.setPosition(x, y-30);
            drop.setAmount(60);
            itemDrops.add(drop);
        } else if (random == 1) {
            drop = new ArmorPot(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        } else if (random == 2) {
            drop = new HealingPot(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        } else if (random == 3) {
            Artefact artefact = am.randomShopArtefact();
            if (artefact == null) {
                Weapon weapon = gm.randomGun();
                weapon.drop();
                drop = new WeaponDrop(tm, weapon);
                drop.setPosition(x, y);
                itemDrops.add(drop);
                slot = gm.getWeaponSlot(weapon);
                random = 5;
            } else {
                drop = new ArtefactDrop(tm, artefact);
                drop.setPosition(x, y);
                itemDrops.add(drop);
                slot = am.getArtefactSlot(artefact);
            }
        }else if (random == 4) {
            int subType = Random.nextInt(2);
            drop = new StatUpgradeDrop(tm, (byte)subType);
            drop.setPosition(x, y-15);
            itemDrops.add(drop);
        } else {
            Weapon weapon = gm.randomGun();
            weapon.drop();
            drop = new WeaponDrop(tm, weapon);
            drop.setPosition(x, y);
            itemDrops.add(drop);
            slot = gm.getWeaponSlot(weapon);
        }
        int price;
        if(drop instanceof WeaponDrop || drop instanceof StatUpgradeDrop){
            price = Random.nextInt(5+tm.getFloor()*2) +5+tm.getFloor()*2;;
        }
        else if(drop instanceof ArtefactDrop){
            price = Random.nextInt(7+tm.getFloor()*2) +7+tm.getFloor()*2;
        } else {
            price = Random.nextInt(3+tm.getFloor()) + 2+tm.getFloor()*2;
        }
        if(tm.isActiveAffix(TileMap.INFLATION)){
            price *= 2f;
        }
        drop.setShop(price);
        Network.ShopDropitem shopPacket = new Network.ShopDropitem();
        shopPacket.id = drop.getId();
        shopPacket.idObject = idObject;
        shopPacket.type = (byte)random;
        shopPacket.price = (short) drop.getPrice();
        shopPacket.objectSlot = (byte)slot;
        if(drop instanceof StatUpgradeDrop) shopPacket.subType = ((StatUpgradeDrop) drop).getUpgradeType();
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
        int random = Random.nextInt(drops);

        ItemDrop drop = null;
        if (random == 0) {
            drop = dropAmmo(x,y,randomIndexPlayer);
        } else if (random == 2) {
            if(tm.isActiveAffix(TileMap.LOWHPPOTS)){
                int tries = 0;
                do{
                    random = Random.nextInt(3);
                    tries++;
                } while (random == 2 && tries <= 2);
                if(random == 0){
                    drop = dropAmmo(x,y,randomIndexPlayer);
                } else if (random != 2) {
                    drop = new Coin(tm);
                    drop.setPosition(x, y);
                    itemDrops.add(drop);
                }
            } else {
                drop = new HealingPot(tm);
                drop.setPosition(x, y);
                itemDrops.add(drop);
            }
        } else {
            drop = new Coin(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        }
        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        Server server = mpManager.server.getServer();
        Network.DropItem dropItem = new Network.DropItem();
        mpManager.server.requestACK(dropItem,dropItem.idPacket);
        dropItem.type = (byte)random;
        dropItem.id = drop.getId();
        dropItem.x = (int)x;
        dropItem.y = (int)y;
        dropItem.amount = (byte)drop.getAmount();
        dropItem.despawn = drop.canDespawn;
        server.sendToAllUDP(dropItem);
        return drop;
    }
    private ItemDrop dropAmmo(float x, float y, int pIndex){
        int[] weaponTypes = gm.getWeaponTypes(player[pIndex].getIdConnection());
        ItemDrop drop = null;
        int numWeapons = 0;
        for(int type : weaponTypes) {
            if(type != -1){
                numWeapons++;
            }
        }
        if(numWeapons <= 0){
            drop = new Coin(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        }
        else if(numWeapons == 1){
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
                    if (drop.intersects(player) && !player.isDead()) {
                        int type = drop.type;
                        if (drop.isShop()) {
                            if (drop.intersects(this.player[i])) {
                                shopItem[i] = drop;
                            }
                        } else  {
                            if (type == ItemDrop.PISTOLAMMO || type == ItemDrop.SHOTGUNAMMO || type == ItemDrop.EXPLOSIVEAMMO) {
                                boolean done = gm.addAmmo(drop.getAmount(), type, player.getIdConnection());
                                if (done) {
                                    drop.pickedUp = true;
                                }
                            } else if (type == ItemDrop.AMMOBOX) {
                                int[] ammotypes = new int[]{1,3,4};
                                for(int atype : ammotypes){
                                    boolean picked = gm.addAmmo(drop.getAmount(), atype,player.getIdConnection());
                                    if(picked) drop.pickedUp = true;
                                }
                            } else if (type == ItemDrop.HP) {
                                if (player.getHealth() != player.getMaxHealth()) {
                                    player.addHealth(2);
                                    drop.pickedUp = true;
                                }
                            } else if (type == ItemDrop.COIN) {
                                player.addCoins(drop.getAmount());
                                totalCoins += drop.getAmount();
                                drop.pickedUp = true;
                            } else if (type == ItemDrop.GUN) {
                                float newDist = (float) ((WeaponDrop) drop).distance(player.getX(), player.getY());
                                if (distance > newDist || distance == -1) {
                                    distance = newDist;
                                    selectedDrop = drop;
                                }
                            } else if (type == ItemDrop.STATUPGRADE) {
                                byte subType = ((StatUpgradeDrop)drop).getUpgradeType();
                                if(subType == StatUpgradeDrop.HEALTH){
                                    player.setMaxHealth(player.getMaxHealth()+1);
                                    player.addHealth(1);
                                    drop.pickedUp = true;
                                } else {
                                    player.setMaxArmor(player.getMaxArmor()+2);
                                    player.addArmor(2);
                                    drop.pickedUp = true;
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
                                }
                            }
                            if(drop.pickedUp){
                                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                                Server server = mpManager.server.getServer();
                                Network.RemoveItem item = new Network.RemoveItem();
                                mpManager.server.requestACK(item,item.idPacket);
                                item.id = drop.getId();
                                server.sendToAllUDP(item);
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
    }

    public void dropWeapon(int x, int y) {
        Weapon weapon = gm.randomGun();
        weapon.drop();
        WeaponDrop drop = new WeaponDrop(tm, weapon);
        drop.setPosition(x, y);
        itemDrops.add(drop);

        // adding weapon drop to all players
        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        Network.DropWeapon dropWeapon = new Network.DropWeapon();
        mpManager.server.requestACK(dropWeapon,dropWeapon.idPacket);
        dropWeapon.id = drop.getId();
        dropWeapon.x = x;
        dropWeapon.y = y;
        dropWeapon.slot = (byte)gm.getWeaponSlot(weapon);

        Server server = mpManager.server.getServer();
        server.sendToAllUDP(dropWeapon);
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

        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        Network.DropArtefact dropArtefact = new Network.DropArtefact();
        mpManager.server.requestACK(dropArtefact,dropArtefact.idPacket);
        dropArtefact.id = drop.getId();
        dropArtefact.x = x;
        dropArtefact.y = y;
        dropArtefact.slot = (byte)am.getArtefactSlot(artefact);
        Server server = mpManager.server.getServer();
        server.sendToAllUDP(dropArtefact);

        itemDrops.add(drop);
    }

    /**
     * Dropping artefact by changing artefact of player
     * @param artefact - artefact that we want to drop
     * @param dx - X direction of drop
     * @param dy - Y direction of drop
     * @param idPlayer - id connection of player
     */
    public void dropPlayerArtefact(Artefact artefact, int dx, int dy, int idPlayer) {
        for(PlayerMP p : player){
            if(p == null) continue;
            if(p.getIdConnection() == idPlayer){
                ArtefactDrop drop = new ArtefactDrop(tm, artefact,dx,dy);
                int x = (int)p.getX();
                int y = (int)p.getY()+30;
                drop.setPosition((int)p.getX(), (int)p.getY()+30);
                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                Network.DropArtefact dropArtefact = new Network.DropArtefact();
                mpManager.server.requestACK(dropArtefact,dropArtefact.idPacket);
                dropArtefact.slot = (byte)am.getArtefactSlot(artefact);
                dropArtefact.x = x;
                dropArtefact.y = y;
                dropArtefact.dx = (byte)dx;
                dropArtefact.dy = (byte)dy;
                dropArtefact.idPlayer = idPlayer;
                dropArtefact.id = drop.getId();
                itemDrops.add(drop);

                Server server = mpManager.server.getServer();
                server.sendToAllUDP(dropArtefact);
                break;
            }
        }
    }
    public void createDrop(float x, float y, Vector2f acceleration) {
        ItemDrop drop = createDrop(x,y);
        drop.move(acceleration,drop.getMovementVelocity());
    }

    public void dropPlayerWeapon(Weapon weapon, int x, int y, int slot, int idPlayer) {
        WeaponDrop drop = new WeaponDrop(tm, weapon, x, y);
        for(PlayerMP p : player){
            if(p != null){
                if(p.getIdConnection() == idPlayer){
                    int px = (int) p.getX();
                    int py = (int) p.getY()+30;
                    drop.setPosition((int) p.getX(), (int) p.getY()+30);

                    // adding weapon drop to all players
                    MultiplayerManager mpManager = MultiplayerManager.getInstance();
                    Network.DropWeapon dropWeapon = new Network.DropWeapon();
                    mpManager.server.requestACK(dropWeapon,dropWeapon.idPacket);
                    dropWeapon.id = drop.getId();
                    dropWeapon.x = px;
                    dropWeapon.y = py;
                    dropWeapon.slot = (byte)slot;

                    Server server = mpManager.server.getServer();
                    server.sendToAllUDP(dropWeapon);

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
        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        Network.DropItem dropItem = new Network.DropItem();
        mpManager.server.requestACK(dropItem,dropItem.idPacket);
        dropItem.type = (byte)itemdrop.type;
        dropItem.id = itemdrop.getId();
        dropItem.x = (int)itemdrop.getX();
        dropItem.y = (int)itemdrop.getY();
        dropItem.despawn = itemdrop.canDespawn;
        dropItem.amount = (byte)itemdrop.getAmount();
        Server server = mpManager.server.getServer();
        server.sendToAllUDP(dropItem);
    }

    public boolean pickup(Network.DropInteract pickup) {
        int x = pickup.x;
        int y = pickup.y;
        for(int i = 0;i< player.length;i++){
            PlayerMP player = this.player[i];
            if(pickup.idPlayer != player.getIdConnection() || player.isDead()) continue;
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
                    gm.changeGun(x, y, ((WeaponDrop) selectedDrop).getWeapon(),player.getIdConnection());
                    selectedDrop.pickedUp = true;
                    pickup.sucessful = true;
                    pickup.id = selectedDrop.getId();
                    Server server = MultiplayerManager.getInstance().server.getServer();
                    server.sendToAllUDP(pickup);
                } else {
                    am.setCurrentArtefact(((ArtefactDrop) selectedDrop).getArtefact(),x,y, pickup.idPlayer);
                    selectedDrop.pickedUp = true;
                    pickup.sucessful = true;
                    pickup.id = selectedDrop.getId();
                    Server server = MultiplayerManager.getInstance().server.getServer();
                    server.sendToAllUDP(pickup);
                    selectedDrop.pickedUp = true;
                }
                return true;
            }
            if(shopItem[i] != null){
                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                Server server = mpManager.server.getServer();
                // buying item from shop
                if(shopItem[i] instanceof WeaponDrop) {
                    if (player.getCoins() >= shopItem[i].getPrice()) {
                        player.removeCoins(shopItem[i].getPrice());
                        gm.changeGun(x, y, ((WeaponDrop) shopItem[i]).getWeapon(), player.getIdConnection());
                        shopItem[i].pickedUp = true;
                        pickup.sucessful = true;
                        pickup.id = shopItem[i].getId();
                        server.sendToAllUDP(pickup);
                    } else {
                        if (System.currentTimeMillis() - alertCooldown[i] > 2000) {
                            alertCooldown[i] = System.currentTimeMillis();
                            Network.Alert alert = new Network.Alert();
                            mpManager.server.requestACK(alert, alert.idPacket);
                            alert.text = "You don't have enough coins";
                            alert.warning = true;
                            alert.idPlayer = pickup.idPlayer;
                            server.sendToUDP(pickup.idPlayer, alert);
                        }
                    }
                } else if(shopItem[i] instanceof ArtefactDrop) {
                    if (player.getCoins() >= shopItem[i].getPrice()) {
                        player.removeCoins(shopItem[i].getPrice());
                        am.setCurrentArtefact(((ArtefactDrop) shopItem[i]).getArtefact(),x,y, player.getIdConnection());
                        shopItem[i].pickedUp = true;
                        pickup.sucessful = true;
                        pickup.id = shopItem[i].getId();
                        server.sendToAllUDP(pickup);
                    } else {
                        if (System.currentTimeMillis() - alertCooldown[i] > 2000) {
                            alertCooldown[i] = System.currentTimeMillis();
                            Network.Alert alert = new Network.Alert();
                            mpManager.server.requestACK(alert, alert.idPacket);
                            alert.text = "You don't have enough coins";
                            alert.warning = true;
                            alert.idPlayer = pickup.idPlayer;
                            server.sendToUDP(pickup.idPlayer, alert);
                        }
                    }
                } else {
                    if (player.getCoins() >= shopItem[i].getPrice()) {
                        player.removeCoins(shopItem[i].getPrice());
                        shopItem[i].shopBuy();
                        pickup.sucessful = true;
                        pickup.id = shopItem[i].getId();
                        server.sendToAllUDP(pickup);
                    } else {
                        if (System.currentTimeMillis() - alertCooldown[i]  > 2000) {
                            alertCooldown[i]  = System.currentTimeMillis();
                            Network.Alert alert = new Network.Alert();
                            mpManager.server.requestACK(alert,alert.idPacket);
                            alert.text = "You don't have enough coins";
                            alert.warning = true;
                            alert.idPlayer = pickup.idPlayer;
                            server.sendToUDP(pickup.idPlayer, alert);
                        }
                    }
                }
            }
        }
        return false;
    }
    public void handleDropInteractPacket(Network.DropInteract dropInteract){
        dropInteractPackets.add(dropInteract);
    }

    public InteractionAcknowledge[] checkDropInteractions() {
        InteractionAcknowledge[] interactionAcknowledges = new InteractionAcknowledge[player.length];
        for(int i = 0;i< player.length;i++){
            PlayerMP p = player[i];
            if(p == null) interactionAcknowledges[i] = null;
            else interactionAcknowledges[i] = new InteractionAcknowledge(p.getIdConnection());
        }
        for(Network.DropInteract dropInteract : dropInteractPackets){
            boolean interact = pickup(dropInteract);
            if(interact){
                for(InteractionAcknowledge acknowledge : interactionAcknowledges) {
                    acknowledge.pickup(dropInteract.idPlayer);
                }
            }
        }
        dropInteractPackets.clear();
        return interactionAcknowledges;
    }

    public static class InteractionAcknowledge{
        private final int idPlayer;
        private boolean interact;
        private InteractionAcknowledge(int idPlayer){
            this.idPlayer = idPlayer;
            this.interact = false;
        }
        private void pickup(int idPlayer){
            if(this.idPlayer == idPlayer){
                interact = true;
            }
        }
        public boolean isThisAckOfPlayer(int idPlayer){
            return this.idPlayer == idPlayer;
        }

        public boolean didInteract() {
            return interact;
        }
    }
}

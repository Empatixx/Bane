package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.ItemDrops.Artefacts.ArtefactManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Guns.Weapon;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PacketHolder;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class ItemManager {
    private static ItemManager itemManager;
    public static void init(ItemManager itemManager
    ){
        ItemManager.itemManager = itemManager;
    }
    public static ItemManager getInstance(){return itemManager;}
    public static void load(){
        Loader.loadImage("Textures\\shophud.tga");
        ArmorPot.load();
        Coin.load();
        ExplosiveAmmo.load();
        HealingPot.load();
        ShotgunAmmo.load();
        PistolAmmo.load();
        AmmoBox.load();
        StatUpgradeDrop.load();
    }
    private ArrayList<ItemDrop> itemDrops;
    private TileMap tm;
    private GunsManager gm;
    private ArtefactManager am;
    private Player player;


    private  int pickupSound;
    private int pickupCoinSound;
    private Source source;
    private Source buysource;

    private Image shopHud;
    private boolean showShopHud;
    private ItemDrop shopItem;
    private int soundShopBuy;

    private int totalCoins;

    private TextRender[] textRender;

    private long alertCooldown;

    public ItemManager(TileMap tm, GunsManager gm,ArtefactManager am, Player player) {
        this.tm = tm;
        this.gm = gm;
        this.am = am;
        this.player = player;
        itemDrops = new ArrayList<>();
        pickupSound = AudioManager.loadSound("pickup.ogg");
        pickupCoinSound = AudioManager.loadSound("coin.ogg");

        source = AudioManager.createSource(Source.EFFECTS, 0.35f);
        buysource = AudioManager.createSource(Source.EFFECTS, 0.35f);

        shopHud = new Image("Textures\\shophud.tga", new Vector3f(0, 0, 0), 3f);
        showShopHud = false;
        soundShopBuy = AudioManager.loadSound("buy.ogg");

        totalCoins = 0;

        textRender = new TextRender[2];
        for(int i=0;i<2;i++) textRender[i] = new TextRender();

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

    // singeplayer shop item
    public void createShopDrop(float x, float y) {
        int drops = 5;
        int random = Random.nextInt(drops);

        ItemDrop drop;
        if (random == 0) {
            drop = new AmmoBox(tm);
            drop.setAmount(60);
            drop.setPosition(x, y-30);
            drop.setShop(Random.nextInt(3+tm.getFloor()) + 2+tm.getFloor()*2);
            itemDrops.add(drop);
            return;
        } else if (random == 1) {
            drop = new ArmorPot(tm);
        } else if (random == 2) {
            drop = new HealingPot(tm);
        }else if (random == 3) {
            drop = new StatUpgradeDrop(tm, (byte) Random.nextInt(2));
            drop.setPosition(x, y-15);
            drop.setShop(Random.nextInt(5+tm.getFloor()*2) + 5+tm.getFloor()*2);
            itemDrops.add(drop);
            return;
        } else {
            Weapon weapon = gm.randomGun();
            weapon.drop();
            drop = new WeaponDrop(tm, weapon);
        }
        if(drop instanceof WeaponDrop){
            drop.setShop(Random.nextInt(5+tm.getFloor()*2) + 5+tm.getFloor()*2);
        } else {
            drop.setShop(Random.nextInt(3+tm.getFloor()) + 2+tm.getFloor()*2);
        }
        drop.setPosition(x, y);
        itemDrops.add(drop);

    }

    public ItemDrop createDrop(float x, float y) {
        int drops = 3;
        if (player.getHealth() == player.getMaxHealth()) {
            drops--;
        }
        int random = cz.Empatix.Java.Random.nextInt(drops);

        int[] weaponTypes = gm.getWeaponTypes();
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
                        } else if (type == ItemDrop.EXPLOSIVEAMMO){
                            drop = new ExplosiveAmmo(tm);
                            drop.setPosition(x, y);
                        } else{
                            drop = new ShotgunAmmo(tm);
                            drop.setPosition(x, y);
                        }
                        itemDrops.add(drop);

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
                } else if (type == ItemDrop.EXPLOSIVEAMMO){
                    drop = new ExplosiveAmmo(tm);
                    drop.setPosition(x, y);
                } else {
                    drop = new ShotgunAmmo(tm);
                    drop.setPosition(x, y);
                }
                itemDrops.add(drop);
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
        if(MultiplayerManager.multiplayer){
            PacketHolder packetHolder = MultiplayerManager.getInstance().packetHolder;
            for(Object dropItem : packetHolder.get(PacketHolder.DROPITEM)){
                createDrop((Network.DropItem) dropItem);
            }
            for(Object dropWeapon : packetHolder.get(PacketHolder.DROPWEAPON)){
                dropWeapon((Network.DropWeapon) dropWeapon);
            }
            for(Object dropArtefact : packetHolder.get(PacketHolder.DROPARTEFACT)){
                dropArtefact((Network.DropArtefact) dropArtefact);
            }
            for(Object dropArtefact : packetHolder.get(PacketHolder.MOVEITEM)){
                handleMoveDropItemPacket((Network.MoveDropItem) dropArtefact);
            }
            for(Object removeItem : packetHolder.get(PacketHolder.REMOVEITEM)){
                for(ItemDrop drop:itemDrops){
                    if(drop.getId() == ((Network.RemoveItem)(removeItem)).id){
                        if(drop instanceof Coin){
                            source.play(pickupCoinSound);
                        } else {
                            source.play(pickupSound);
                        }
                        drop.pickedUp = true;
                        break;
                    }
                }
            }
            for(Object o : packetHolder.get(PacketHolder.OBJECTINTERACT)){
                Network.DropInteract dropInteract = (Network.DropInteract) o;
                for(ItemDrop drop:itemDrops){
                    if(dropInteract.sucessful && drop.getId() == dropInteract.id){
                        // if player is the one that interacted with drop
                        if(drop instanceof WeaponDrop) {
                            drop.pickedUp = true;
                            if(MultiplayerManager.getInstance().getIdConnection() == dropInteract.idPlayer){
                                Weapon weapon = ((WeaponDrop) drop).getWeapon();
                                gm.changeGun(weapon);
                                if (drop.isShop()) {
                                    drop.shopBuy();
                                    buysource.play(soundShopBuy);
                                }
                            }
                        } else if(drop instanceof ArtefactDrop){
                            drop.pickedUp = true;
                            if(MultiplayerManager.getInstance().getIdConnection() == dropInteract.idPlayer){
                                Artefact artefact = ((ArtefactDrop)drop).getArtefact();
                                am.setCurrentArtefact(artefact);
                                if(drop.isShop()) {
                                    drop.shopBuy();
                                    buysource.play(soundShopBuy);
                                }
                            }
                        } else if (drop.isShop()) {
                            drop.shopBuy();
                            buysource.play(soundShopBuy);
                        }
                    }
                }
            }
        }
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
            else if (drop.type == ItemDrop.ARTEFACT) {
                ((ArtefactDrop) drop).setCanPick(false);
            }
            if (drop.intersects(player) && !player.isDead()) {
                int type = drop.type;
                if (drop.isShop()) {
                    showShopHud = true;
                    shopHud.setPosition(new Vector3f(drop.getX() + tm.getX(), drop.getY() + tm.getY() - 125, 0));
                    shopItem = drop;
                } else {
                    if(MultiplayerManager.multiplayer){
                        if(type == ItemDrop.PISTOLAMMO || type == ItemDrop.SHOTGUNAMMO || type == ItemDrop.EXPLOSIVEAMMO || type == ItemDrop.COIN ||
                        type == ItemDrop.ARMOR || type == ItemDrop.STATUPGRADE || type == ItemDrop.HP) continue;
                    }
                    if (type == ItemDrop.PISTOLAMMO || type == ItemDrop.SHOTGUNAMMO || type == ItemDrop.EXPLOSIVEAMMO) {
                        boolean done = gm.addAmmo(drop.getAmount(), type);
                        if (done) {
                            drop.pickedUp = true;
                            source.play(pickupSound);
                        }
                    } else if (type == ItemDrop.AMMOBOX) {
                        int[] ammotypes = new int[]{1,3,4};
                        for(int atype : ammotypes){
                            gm.addAmmo(drop.getAmount(), atype);
                        }
                        drop.pickedUp = true;
                        source.play(pickupSound);
                    } else if (type == ItemDrop.HP) {
                        if (player.getHealth() != player.getMaxHealth()) {
                            player.addHealth(2);
                            drop.pickedUp = true;
                            source.play(pickupSound);
                        }
                    } else if (type == ItemDrop.STATUPGRADE) {
                        byte subType = ((StatUpgradeDrop)drop).getUpgradeType();
                        if(subType == StatUpgradeDrop.HEALTH){
                            player.setMaxHealth(player.getMaxHealth()+1);
                            player.addHealth(1);
                            drop.pickedUp = true;
                            source.play(pickupSound);
                        } else {
                            player.setMaxArmor(player.getMaxArmor()+2);
                            player.addArmor(2);
                            drop.pickedUp = true;
                            source.play(pickupSound);
                        }
                    } else if (type == ItemDrop.COIN) {
                        player.addCoins(drop.getAmount());
                        totalCoins+=drop.getAmount();
                        drop.pickedUp = true;
                        source.play(pickupCoinSound);
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
                            source.play(pickupSound);
                        }
                    }
                }

            }
        }
        if(MultiplayerManager.multiplayer) if(((PlayerMP)player).isGhost()) selectedDrop = null;
        if (selectedDrop != null) {
            if(selectedDrop instanceof WeaponDrop){
                ((WeaponDrop) selectedDrop).setCanPick(true);
            } else if(selectedDrop instanceof ArtefactDrop){
                ((ArtefactDrop) selectedDrop).setCanPick(true);
            }
        }
    }


    public void draw() {
        for (ItemDrop drop : itemDrops) {
            drop.draw();
        }
        if (showShopHud){
            shopHud.draw();

            // 240 - WIDTH OF TABLE SHOP - object

            textRender[0].drawMap("" + shopItem.getPrice(), new Vector3f(
                            TextRender.getHorizontalCenter((int) (shopItem.getX()-shopHud.getWidth()/2),(int) (shopItem.getX()+shopHud.getWidth()/2),""+shopItem.getPrice(),2),
                            shopItem.getY() - 110,
                            0),
                    3,
                    new Vector3f(0.986f, 0.7f, 0.458f));

            float time = (float) Math.sin(System.currentTimeMillis() % 2000 / 600f) + (1 - (float) Math.cos((System.currentTimeMillis() % 2000 / 600f) + 0.5f));
            textRender[1].drawMap("Press E to buy", new Vector3f(
                            TextRender.getHorizontalCenter((int) (shopItem.getX()-120),(int) (shopItem.getX()+120),"Press E to buy",2),
                            shopItem.getY() + 140,
                            0),
                    2,
                    new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
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
        ArtefactDrop drop = new ArtefactDrop(tm, am.randomArtefact());
        drop.setPosition(x, y);
        itemDrops.add(drop);
    }
    private void dropArtefact(Network.DropArtefact dropArtefact) {
        ArtefactDrop drop;
        if(dropArtefact.idPlayer != 0){
            if(MultiplayerManager.getInstance().getIdConnection() == dropArtefact.idPlayer) am.setCurrentArtefact(null);
            drop = new ArtefactDrop(tm, am.getArtefact(dropArtefact.slot),dropArtefact.dx,dropArtefact.dy);
        } else {
            drop = new ArtefactDrop(tm, am.getArtefact(dropArtefact.slot));
        }
        drop.setPosition(dropArtefact.x, dropArtefact.y);
        drop.setId(dropArtefact.id);
        itemDrops.add(drop);
    }
    public void dropPlayerArtefact(Artefact artefact, int x, int y) {
        ArtefactDrop drop = new ArtefactDrop(tm, artefact,x,y);
        drop.setPosition((int)player.getX(), (int)player.getY()+30);
        itemDrops.add(drop);
    }
    public void createDrop(float x, float y, Vector2f speed) {
        int random = cz.Empatix.Java.Random.nextInt(3);
        ItemDrop drop;
        int[] weaponTypes = gm.getWeaponTypes();
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
                        } else if (type == ItemDrop.EXPLOSIVEAMMO){
                            drop = new ExplosiveAmmo(tm);
                        } else{
                            drop = new ShotgunAmmo(tm);
                        }
                        drop.setPosition(x, y);
                        drop.setSpeed(speed.x, speed.y);
                        itemDrops.add(drop);
                    }
                }
            } else {
                int type = weaponTypes[Random.nextInt(numWeapons)];
                while(type == -1) {
                    type = weaponTypes[Random.nextInt(numWeapons)];
                }
                if (type == ItemDrop.PISTOLAMMO) {
                    drop = new PistolAmmo(tm);
                } else if (type == ItemDrop.EXPLOSIVEAMMO){
                    drop = new ExplosiveAmmo(tm);
                } else {
                    drop = new ShotgunAmmo(tm);
                }
                drop.setPosition(x, y);
                drop.setSpeed(speed.x, speed.y);
                itemDrops.add(drop);
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
    }

    public void dropWeapon(Weapon weapon, int x, int y) {
        WeaponDrop drop = new WeaponDrop(tm, weapon, x, y);
        drop.setPosition((int) player.getX(), (int) player.getY()+30);
        itemDrops.add(drop);
    }
    public void dropWeapon(Network.DropWeapon dropWeapon) {
        WeaponDrop drop = new WeaponDrop(tm, gm.getWeapon(dropWeapon.slot), 0, 0);
        drop.setPosition(dropWeapon.x, dropWeapon.y);
        drop.setId(dropWeapon.id);
        itemDrops.add(drop);
    }

    /**
     *
     * @param x - location of player X + X of mouse + tilemap X
     * @param y - location of player Y + Y of mouse + tilemap Y
     */

    public boolean keyPressed(int k, int x, int y) {
        if(k == ControlSettings.getValue(ControlSettings.OBJECT_INTERACT)){
            // picking gun from ground
            float distance = -1;
            ItemDrop selectedDrop = null;
            for (ItemDrop drop : itemDrops) {
                if (drop.type == ItemDrop.GUN){
                    if (((WeaponDrop) drop).isCanPick()){
                        float newDist = (float) ((WeaponDrop) drop).distance(player.getX(), player.getY());
                        if (distance > newDist || distance == -1){
                            distance = newDist;
                            selectedDrop = drop;
                        }
                    }
                }
                if (drop.type == ItemDrop.ARTEFACT) {
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
                    gm.changeGun(x, y, ((WeaponDrop) selectedDrop).getWeapon());
                    selectedDrop.pickedUp = true;
                } else {
                    am.setCurrentArtefact(((ArtefactDrop) selectedDrop).getArtefact(),x,y);
                    selectedDrop.pickedUp = true;
                }
                return true;
            }
            if(showShopHud){
                // buying item from shop
                if(shopItem instanceof WeaponDrop){
                    if(player.getCoins() >= shopItem.getPrice()) {
                        buysource.play(soundShopBuy);
                        player.removeCoins(shopItem.getPrice());
                        gm.changeGun(x, y, ((WeaponDrop) shopItem).getWeapon());
                        shopItem.pickedUp = true;
                    } else {
                        if(System.currentTimeMillis() - alertCooldown > 2000){
                            alertCooldown = System.currentTimeMillis();
                            AlertManager.add(AlertManager.WARNING,"You don't have enough coins");
                        }
                    }
                } else {
                    if(player.getCoins() >= shopItem.getPrice()) {
                        buysource.play(soundShopBuy);
                        player.removeCoins(shopItem.getPrice());
                        shopItem.shopBuy();
                    } else {
                        if(System.currentTimeMillis() - alertCooldown > 2000){
                            alertCooldown = System.currentTimeMillis();
                            AlertManager.add(AlertManager.WARNING,"You don't have enough coins");
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

    public void createDrop(Network.DropItem item) {
        int x = item.x;
        int y = item.y;
        int random = item.type;

        int[] weaponTypes = gm.getWeaponTypes();
        ItemDrop drop = null;
        if (random == 0) {
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
                        } else if (type == ItemDrop.EXPLOSIVEAMMO){
                            drop = new ExplosiveAmmo(tm);
                            drop.setPosition(x, y);
                        } else{
                            drop = new ShotgunAmmo(tm);
                            drop.setPosition(x, y);
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
                } else if (type == ItemDrop.EXPLOSIVEAMMO){
                    drop = new ExplosiveAmmo(tm);
                    drop.setPosition(x, y);
                } else {
                    drop = new ShotgunAmmo(tm);
                    drop.setPosition(x, y);
                }
            }
        } else if (random == 2) {
            drop = new HealingPot(tm);
            drop.setPosition(x, y);
        } else {
            drop = new Coin(tm);
            drop.setPosition(x, y);
        }
        drop.setId(item.id);
        drop.setAmount(item.amount);
        drop.canDespawn = item.despawn;
        itemDrops.add(drop);
    }

    public void handleMoveDropItemPacket(Network.MoveDropItem dropItem) {
        for(ItemDrop drop: itemDrops){
            if(drop.getId() == dropItem.id){
                drop.setPosition(dropItem.x, dropItem.y);
            }
        }
    }

    // multiplayer shop item
    public void createShopDrop(Network.ShopDropitem packet, float x, float y) {
        int random = packet.type;

        ItemDrop drop;
        if (random == 0) {
            drop = new AmmoBox(tm);
            drop.setAmount(60);
            drop.setPosition(x, y);
        } else if (random == 1) {
            drop = new ArmorPot(tm);
            drop.setPosition(x, y);
        } else if (random == 2) {
            drop = new HealingPot(tm);
            drop.setPosition(x, y);
        } else if (random == 3) {
            // converting indexing from server to client ones
            Artefact artefact = am.getArtefact(packet.objectSlot);
            artefact.dropped = true;
            drop = new ArtefactDrop(tm, artefact);
            drop.setPosition(x, y);
        }else if (random == 4) {
            drop = new StatUpgradeDrop(tm, packet.subType);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        } else {
            // converting indexing from server to client ones
            int totalPlayers = MultiplayerManager.getInstance().client.getTotalPlayers();
            int index = packet.objectSlot -(totalPlayers-1);
            if(index < 0) index = 0;
            packet.objectSlot = (byte)index;

            Weapon weapon = gm.getWeapon(packet.objectSlot);
            weapon.drop();
            drop = new WeaponDrop(tm, weapon);
            drop.setPosition(x, y);
        }
        drop.setShop(packet.price);
        drop.setAmount(packet.amount);
        drop.setId(packet.id);
        itemDrops.add(drop);
    }
}

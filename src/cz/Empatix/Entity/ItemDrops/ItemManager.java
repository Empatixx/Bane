package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.ItemDrops.Artefacts.ArtefactManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Guns.Weapon;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class ItemManager {
    public static void load(){
        Loader.loadImage("Textures\\shophud.tga");
        ArmorPot.load();
        Coin.load();
        ExplosiveAmmo.load();
        HealingPot.load();
        ShotgunAmmo.load();
        PistolAmmo.load();
    }
    private static ArrayList<ItemDrop> itemDrops;
    private static TileMap tm;
    private static GunsManager gm;
    private static ArtefactManager am;
    private static Player player;


    private final int pickupSound;
    private final int pickupCoinSound;
    private final Source source;
    private final Source buysource;

    private Image shopHud;
    private boolean showShopHud;
    private ItemDrop shopItem;
    private final int soundShopBuy;

    private int totalCoins;

    private TextRender[] textRender;

    public ItemManager(TileMap tm, GunsManager gm,ArtefactManager am, Player player) {
        ItemManager.tm = tm;
        ItemManager.gm = gm;
        ItemManager.am = am;
        ItemManager.player = player;
        itemDrops = new ArrayList<>();
        pickupSound = AudioManager.loadSound("pickup.ogg");
        pickupCoinSound = AudioManager.loadSound("coin.ogg");

        source = AudioManager.createSource(Source.EFFECTS, 0.35f);
        buysource = AudioManager.createSource(Source.EFFECTS, 0.35f);

        shopHud = new Image("Textures\\shophud.tga", new Vector3f(0, 0, 0), 3.5f);
        showShopHud = false;
        soundShopBuy = AudioManager.loadSound("buy.ogg");

        totalCoins = 0;

        textRender = new TextRender[2];
        for(int i=0;i<2;i++) textRender[i] = new TextRender();
    }

    public static void clear() {
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

    public static void createShopDrop(float x, float y) {
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
            Weapon weapon = GunsManager.randomGun();
            weapon.drop();
            drop = new WeaponDrop(tm, weapon);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        }
        if(drop instanceof WeaponDrop){
            drop.setShop(Random.nextInt(5+tm.getFloor()*2)
                    +5+tm.getFloor());
        } else {
            drop.setShop(Random.nextInt(3+tm.getFloor()) + 2+tm.getFloor());
        }
    }

    public static void createDrop(float x, float y) {
        int drops = 3;
        if (player.getHealth() == player.getMaxHealth()) {
            drops--;
        }
        int random = cz.Empatix.Java.Random.nextInt(drops);

        int[] weaponTypes = gm.getWeaponTypes();
        ItemDrop drop;
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
            if (drop.intersects(player)) {
                int type = drop.type;
                if (drop.isShop()) {
                    showShopHud = true;
                    shopHud.setPosition(new Vector3f(drop.getX() + tm.getX(), drop.getY() + tm.getY() - 125, 0));
                    shopItem = drop;
                } else {
                    if (type == ItemDrop.PISTOLAMMO || type == ItemDrop.SHOTGUNAMMO || type == ItemDrop.EXPLOSIVEAMMO) {
                        boolean done = gm.addAmmo(drop.getAmount(), type);
                        if (done) {
                            drop.pickedUp = true;
                            source.play(pickupSound);
                        }
                    } else if (type == ItemDrop.HP) {
                        if (player.getHealth() != player.getMaxHealth()) {
                            player.addHealth(2);
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

            textRender[0].drawMap("" + shopItem.getPrice(), new Vector3f(
                            shopItem.getX() + shopHud.getWidth() + 3,
                            shopItem.getY() - 110,
                            0),
                    3,
                    new Vector3f(0.986f, 0.7f, 0.458f));

            float time = (float) Math.sin(System.currentTimeMillis() % 2000 / 600f) + (1 - (float) Math.cos((System.currentTimeMillis() % 2000 / 600f) + 0.5f));
            textRender[1].drawMap("Press E to buy", new Vector3f(
                            shopItem.getX() - 65,
                            shopItem.getY() + 140,
                            0),
                    2,
                    new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
        }
    }

    public static void dropWeapon(int x, int y, Vector2f speed) {
        Weapon weapon = GunsManager.randomGun();
        weapon.drop();
        WeaponDrop drop = new WeaponDrop(tm, weapon);
        drop.setSpeed(speed.x, speed.y);
        drop.setPosition(x, y);
        itemDrops.add(drop);
    }
    public static void dropWeapon(Weapon weapon, int x, int y, Vector2f speed) {
        weapon.drop();
        WeaponDrop drop = new WeaponDrop(tm, weapon);
        drop.setSpeed(speed.x, speed.y);
        drop.setPosition(x, y);
        itemDrops.add(drop);
    }
    public static void dropArtefact(int x, int y) {
        ArtefactDrop drop = new ArtefactDrop(tm, ArtefactManager.randomArtefact());
        drop.setPosition(x, y);
        itemDrops.add(drop);
    }
    public static void dropArtefact(Artefact artefact, int x, int y) {
        ArtefactDrop drop = new ArtefactDrop(tm, artefact);
        drop.setPosition(x, y);
        itemDrops.add(drop);
    }
    public static void createDrop(float x, float y, Vector2f speed) {
        int random = cz.Empatix.Java.Random.nextInt(5);
        if (random == 0) {
            ItemDrop drop = new PistolAmmo(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);

        } else if (random == 1) {
            ItemDrop drop = new ShotgunAmmo(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        } else if (random == 3) {
            ItemDrop drop = new HealingPot(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        } else if (random == 4) {
            ItemDrop drop = new ExplosiveAmmo(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        } else {
            ItemDrop drop = new Coin(tm);
            drop.setPosition(x, y);
            drop.setSpeed(speed.x, speed.y);
            itemDrops.add(drop);
        }
    }

    public static void dropPlayerWeapon(Weapon weapon, int x, int y) {
        WeaponDrop drop = new WeaponDrop(tm, weapon, x, y);
        drop.setPosition((int) player.getX(), (int) player.getY());
        itemDrops.add(drop);
    }

    /**
     *
     * @param x - location of player X + X of mouse + tilemap X
     * @param y - location of player Y + Y of mouse + tilemap Y
     */

    public void keyPressed(int k, int x, int y) {
        if(k == GLFW.GLFW_KEY_E){
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
                } else if (selectedDrop instanceof ArtefactDrop){
                    // TODO: sebrani artefaktu itemu
                    am.setCurrentArtefact(((ArtefactDrop) selectedDrop).getArtefact());
                    selectedDrop.pickedUp = true;
                }
            }
            if(showShopHud){
                // buying item from shop
                if(shopItem instanceof WeaponDrop){
                    if(player.getCoins() >= shopItem.getPrice()) {
                        buysource.play(soundShopBuy);
                        player.removeCoins(shopItem.getPrice());
                        gm.changeGun(x, y, ((WeaponDrop) shopItem).getWeapon());
                        shopItem.pickedUp = true;
                    }
                } else {
                    if(player.getCoins() >= shopItem.getPrice()) {
                        buysource.play(soundShopBuy);
                        player.removeCoins(shopItem.getPrice());
                        shopItem.shopBuy();
                    }
                }
            }

        }
    }
    public int getTotalCoins(){
        return totalCoins;
    }
}

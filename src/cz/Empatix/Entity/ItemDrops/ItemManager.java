package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Guns.Weapon;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class ItemManager {
    private static ArrayList<ItemDrop> itemDrops;
    private static TileMap tm;
    private static GunsManager gm;
    private static Player player;


    private final int pickupSound;
    private final int pickupCoinSound;
    private final Source source;

    private Image shopHud;
    private boolean showShopHud;
    private ItemDrop shopItem;
    private final int soundShopBuy;

    public ItemManager(TileMap tm, GunsManager gm, Player player) {
        ItemManager.tm = tm;
        ItemManager.gm = gm;
        ItemManager.player = player;
        itemDrops = new ArrayList<>();
        pickupSound = AudioManager.loadSound("pickup.ogg");
        pickupCoinSound = AudioManager.loadSound("coin.ogg");

        source = AudioManager.createSource(Source.EFFECTS, 0.35f);

        shopHud = new Image("Textures\\shophud.tga", new Vector3f(0, 0, 0), 3.5f);
        showShopHud = false;
        soundShopBuy = AudioManager.loadSound("buy.ogg");

    }

    public static void clear() {
        for (ItemDrop i : itemDrops) {
            if (i instanceof WeaponDrop) {
                ((WeaponDrop) i).despawn();
            }
            i.remove();
        }
        itemDrops.clear();
    }

    public static void createShopDrop(float x, float y) {
        int drops = 5;
        int random = cz.Empatix.Java.Random.nextInt(drops);

        ItemDrop drop;
        if (random == 0) {
            drop = new PistolAmmo(tm);
            drop.setPosition(x, y);
            drop.setAmount(70);
            itemDrops.add(drop);
        } else if (random == 3) {
            drop = new ShotgunAmmo(tm);
            drop.setPosition(x, y);
            drop.setAmount(20);
            itemDrops.add(drop);
        } else if (random == 4) {
            drop = new ArmorPot(tm);
            drop.setPosition(x, y);
            itemDrops.add(drop);
        } else if (random == 2) {
            drop = new HealingPot(tm);
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
            drop.setShop(Random.nextInt(5)+5);
        } else {
            drop.setShop(Random.nextInt(3) + 2);
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
                        } else {
                            drop = new ShotgunAmmo(tm);
                            drop.setPosition(x, y);
                            itemDrops.add(drop);
                        }
                    }
                }
            } else {
                int type = Random.nextInt(numWeapons);
                while(type == -1) {
                    type = Random.nextInt(numWeapons);
                }
                if (type == ItemDrop.PISTOLAMMO) {
                    drop = new PistolAmmo(tm);
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
            if (drop.intersects(player)) {
                int type = drop.type;
                if (drop.isShop()) {
                    showShopHud = true;
                    shopHud.setPosition(new Vector3f(drop.getx() + tm.getX(), drop.gety() + tm.getY() - 125, 0));
                    shopItem = drop;
                } else {
                    if (type == ItemDrop.PISTOLAMMO || type == ItemDrop.SHOTGUNAMMO || type == ItemDrop.SUBMACHINE) {
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
                        drop.pickedUp = true;
                        source.play(pickupCoinSound);
                    } else if (type == ItemDrop.GUN) {
                        float newDist = (float) ((WeaponDrop) drop).distance(player.getX(), player.getY());
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
            ((WeaponDrop) selectedDrop).setCanPick(true);
        }
    }

    public void draw() {
        for (ItemDrop drop : itemDrops) {
            drop.draw();
        }
        if (showShopHud) {
            shopHud.draw();

            TextRender.renderText("" + shopItem.getPrice(), new Vector3f(
                            shopItem.getx() + tm.getX() + shopHud.getWidth() + 3,
                            shopItem.gety() + tm.getY() - 110,
                            0),
                    3,
                    new Vector3f(0.986f, 0.7f, 0.458f));
        }
    }

    public static void dropWeapon(Weapon weapon, int x, int y, Vector2f speed) {
        WeaponDrop drop = new WeaponDrop(tm, weapon);
        drop.setSpeed(speed.x, speed.y);
        drop.setPosition(x, y);
        itemDrops.add(drop);
    }

    public static void createDrop(float x, float y, Vector2f speed) {
        int random = cz.Empatix.Java.Random.nextInt(4);
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
                if (drop.type == ItemDrop.GUN) {
                    if (((WeaponDrop) drop).isCanPick()) {
                        float newDist = (float) ((WeaponDrop) drop).distance(player.getX(), player.getY());
                        if (distance > newDist || distance == -1) {
                            distance = newDist;
                            selectedDrop = drop;
                        }
                    }
                }
            }
            if (selectedDrop != null) {
                gm.changeGun(x, y, ((WeaponDrop) selectedDrop).getWeapon());
                selectedDrop.pickedUp = true;
            }
            if(showShopHud){
                // buying item from shop
                if(shopItem instanceof WeaponDrop){
                    if(player.getCoins() >= shopItem.getPrice()) {
                        source.play(soundShopBuy);
                        player.removeCoins(shopItem.getPrice());
                        gm.changeGun(x, y, ((WeaponDrop) shopItem).getWeapon());
                        shopItem.pickedUp = true;
                    }
                } else {
                    if(player.getCoins() >= shopItem.getPrice()) {
                        source.play(soundShopBuy);
                        player.removeCoins(shopItem.getPrice());
                        shopItem.shopBuy();
                    }
                }
            }

        }
    }
}

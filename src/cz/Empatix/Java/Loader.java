package cz.Empatix.Java;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Entity.Enemies.*;
import cz.Empatix.Entity.Enemies.Projectiles.KingSlimebullet;
import cz.Empatix.Entity.Enemies.Projectiles.RedSlimebullet;
import cz.Empatix.Entity.Enemies.Projectiles.Slimebullet;
import cz.Empatix.Entity.ItemDrops.Artefacts.ArtefactManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.ProgressNPC;
import cz.Empatix.Entity.Shopkeeper;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.MenuState;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Render.Alerts.Alert;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Hud.*;
import cz.Empatix.Render.Hud.Minimap.MiniMap;
import cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades.UpgradeBar;
import cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades.UpgradeSideBar;
import cz.Empatix.Render.Hud.ProgressNPC.UpgradeMenu;
import cz.Empatix.Render.RoomObjects.*;
import cz.Empatix.Render.RoomObjects.ProgressRoom.Bookshelf;
import cz.Empatix.Render.RoomObjects.ProgressRoom.Portal;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.lwjgl.stb.STBImage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Loader {
    private static int maxTextures;
    private static int currentTextures;

    private final static HashMap<String, ByteBufferImage> images = new HashMap<>();
    private static Queue<String> queue = new LinkedList<>();

    private static boolean done;
    private static boolean doneDatabase;
    private static boolean doneAudio;

    public static void init(){
        //text render init
        loadTextEngine();

        done = false;
        doneDatabase = false;
        doneAudio = false;
        new Thread(() -> {
            loadUp();
            maxTextures = queue.size();
            currentTextures = 0;
            while(!queue.isEmpty()){
                currentTextures++;
                String file = queue.poll();
                if(images.containsKey(file)) continue;
                ByteBufferImage byteBufferImage = new ByteBufferImage();
                byteBufferImage.decodeImage(file);
                images.put(file,byteBufferImage);
            }
            done = true;
        }).start();
        new Thread(() -> {
            GameStateManager.loadDatabase();
            doneDatabase = true;
        }).start();
        new Thread(() -> {
            AudioManager.init();
            AudioManager.setListenerData(0,0);
            doneAudio = true;
        }).start();
    }
    public static String rendering(){
        if(!done){
            return "Loading textures "+currentTextures+"/"+maxTextures;
        } else if (!doneDatabase) {
            return "Loading database...";
        } else {
            return "Loading audio...";
        }
    }
    public static boolean isDone(){
        return done && doneDatabase && doneAudio;
    }

    public static ByteBufferImage getImage(String file){
        return images.get(file);
    }
    public static void loadImage(String file){
        queue.add(file);
    }

    private static void loadUp(){
        // ENEMIES
        Player.load();
        Slime.load();
        Bat.load();
        Demoneye.load();
        Ghost.load();
        KingSlime.load();
        Rat.load();
        Slime.load();
        Slimebullet.load();
        KingSlimebullet.load();
        RedSlimebullet.load();
        MapObject.load();
        Golem.load();
        Snake.load();
        RedSlime.load();
        EyeBat.load();
        // ENTITIES
        Shopkeeper.load();
        ProgressNPC.load();
        // itemdrops
        ItemManager.load();
        ArtefactManager.load();
        // GUNS
        GunsManager.load();
        // HUD
        MiniMap.load();
        UpgradeBar.load();
        UpgradeSideBar.load();
        ArmorBar.load();
        CheckBox.load();
        HealthBar.load();
        MenuBar.load();
        SliderBar.load();
        UpgradeMenu.load();
        Alert.load();
        // others
        Portal.load();
        ArrowTrap.load();
        Barrel.load();
        Pot.load();
        Bones.load();
        Chest.load();
        Flag.load();
        Flamethrower.load();
        Ladder.load();
        PathWall.load();
        ShopTable.load();
        Spike.load();
        Torch.load();
        TileMap.load();
        Bookshelf.load();
        // states
        InGame.load();
        MenuState.load();

    }
    public static void unload(){
        images.forEach((k,v) -> STBImage.stbi_image_free(v.getBuffer()));
    }
    private static void loadTextEngine(){
        TextRender.load();
        String file = queue.poll();
        ByteBufferImage byteBufferImage = new ByteBufferImage();
        byteBufferImage.decodeImage(file);
        images.put(file,byteBufferImage);
        TextRender.init();
    }
}

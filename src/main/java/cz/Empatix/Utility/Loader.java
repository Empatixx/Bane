package cz.Empatix.Utility;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Buffs.BuffManager;
import cz.Empatix.Entity.Enemies.*;
import cz.Empatix.Entity.Enemies.Projectiles.KingSlimebullet;
import cz.Empatix.Entity.Enemies.Projectiles.RedSlimebullet;
import cz.Empatix.Entity.Enemies.Projectiles.Slimebullet;
import cz.Empatix.Entity.ItemDrops.Artefacts.ArtefactManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.ProgressRoom.MultiplayerNPC;
import cz.Empatix.Entity.ProgressRoom.ProgressNPC;
import cz.Empatix.Entity.RoomObjects.*;
import cz.Empatix.Entity.RoomObjects.ProgressRoom.Armorstand;
import cz.Empatix.Entity.RoomObjects.ProgressRoom.Bookshelf;
import cz.Empatix.Entity.RoomObjects.ProgressRoom.Portal;
import cz.Empatix.Entity.Shopkeeper;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.MenuState;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Render.Alerts.Alert;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Hud.*;
import cz.Empatix.Render.Hud.Minimap.MiniMap;
import cz.Empatix.Render.Hud.MultiplayerNPC.MultiplayerMenu;
import cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades.UpgradeBar;
import cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades.UpgradeSideBar;
import cz.Empatix.Render.Hud.ProgressNPC.UpgradeMenu;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Loader {
    private final static HashMap<String, ByteBufferImage> images = new HashMap<>();
    private static Queue<String> queue = new LinkedList<>();

    private static boolean done;
    private static boolean doneDatabase;
    private static boolean doneAudio;

    private static float percentLoading = 0;

    public static void init(){
        //text render init
        loadTextEngine();

        done = false;
        doneDatabase = false;
        doneAudio = false;
        new Thread(() -> {
            loadUp();
            int maxTextures = queue.size();
            while(!queue.isEmpty()){
                String file = queue.poll();
                if(images.containsKey(file)) continue;
                ByteBufferImage byteBufferImage = new ByteBufferImage();
                byteBufferImage.decodeImage(file);
                images.put(file,byteBufferImage);
                addPercentLoad(0.5f/maxTextures);

            }
            done = true;
        }).start();
        new Thread(() -> {
            GameStateManager.loadDatabase();
            addPercentLoad(0.25f);
            doneDatabase = true;
        }).start();
        new Thread(() -> {
            AudioManager.init();
            AudioManager.setListenerData(0,0);
            addPercentLoad(0.25f);
            doneAudio = true;
        }).start();
    }
    public static String rendering(){
        if(!isDone()){
            return "Loading "+String.format("%d%%",(int)(percentLoading*100));
        } else {
            return "";
        }
    }
    private static synchronized void addPercentLoad(float x){
        percentLoading+=x;
    }
    public static boolean isDone(){
        return done && doneDatabase && doneAudio;
    }

    public static ByteBufferImage getImage(String file){
        return images.get(file.toLowerCase());
    }
    public static void loadImage(String file){
        queue.add(file.toLowerCase());
    }

    private static void loadUp(){
        File mainFolder = new File("Textures");
        listTextures(mainFolder);
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
    private static void listTextures(File folder){
        for(File file : Objects.requireNonNull(folder.listFiles())){
            if(file.isDirectory()){
                listTextures(file);
            } else {
                if(getFileExtension(file.getName()).equalsIgnoreCase("tga")
                 || getFileExtension(file.getName()).equalsIgnoreCase("png")){ // is tga/png file
                    queue.add(file.getPath().toLowerCase());
                }
            }
        }
    }
    public static String getFileExtension(String fileName) {
        char ch;
        int len;
        if(fileName==null ||
                (len = fileName.length())==0 ||
                (ch = fileName.charAt(len-1))=='/' || ch=='\\' || //in the case of a directory
                ch=='.' ) //in the case of . or ..
            return "";
        int dotInd = fileName.lastIndexOf('.'),
                sepInd = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if( dotInd<=sepInd )
            return "";
        else
            return fileName.substring(dotInd+1).toLowerCase();
    }
}

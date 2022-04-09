package cz.Empatix.Utility;

import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Console {
    public boolean enabled;
    StringBuilder stringbuilder;
    private boolean dot;
    private long time;

    private int[] keys;
    private boolean[] used;
    private long writeDelay;
    private long globalDelay;

    private GunsManager gunsManager;
    private Player p;
    private ItemManager itemManager;
    private EnemyManager em;

    private TextRender textRender;

    public Console(GunsManager gunsManager, Player p, ItemManager itemManager, EnemyManager em){
        stringbuilder = new StringBuilder();
        time = System.currentTimeMillis();
        dot = true;
        this.gunsManager = gunsManager;
        this.em = em;
        this.p = p;
        this.itemManager = itemManager;

        keys = new int[10];
        used = new boolean[10];

        textRender = new TextRender();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if(!enabled){
            stringbuilder.setLength(0);
        }
    }
    public void addChar(char c){
        stringbuilder.append(c);
    }
    public void keyReleased(int k){
        for(int i = 0; i< keys.length; i++){
            if(k == keys[i]) used[i] = false;
        }
    }
    public void keyPressed(int k){
        if(enabled) {
            if (k == GLFW.GLFW_KEY_ENTER) {
                String cmd = stringbuilder.toString();
                String[] args = cmd.split(" ");
                switch (args[0]) {
                    case "sethealth": {
                        if (args.length < 2) {
                            break;
                        }
                        p.setHealth(Integer.parseInt(args[1]));
                        break;
                    }
                    case "setcoins": {
                        if (args.length < 2) {
                            break;
                        }
                        p.setCoins(Integer.parseInt(args[1]));
                        break;
                    }
                    case "fillammo": {
                        for (int i = 0; i < 5; i++) {
                            gunsManager.addAmmo(100, i);
                        }
                        break;
                    }
                    case "spawnenemy": {
                        if (args.length < 2) {
                            break;
                        }
                        em.addEnemy(args[1]);
                        break;
                    }
                    case "dropgun": {
                        if (args.length < 2) {
                            break;
                        }
                        switch (args[1]) {
                            case "pistol": {
                                ItemManager itemManager = ItemManager.getInstance();
                                itemManager.dropWeapon(gunsManager.getWeapon(0), (int) p.getX(), (int) p.getY(), new Vector2f(0, 0));
                                break;
                            }
                            case "shotgun": {
                                ItemManager itemManager = ItemManager.getInstance();
                                itemManager.dropWeapon(gunsManager.getWeapon(1), (int) p.getX(), (int) p.getY(), new Vector2f(0, 0));
                                break;
                            }
                            case "uzi": {
                                ItemManager itemManager = ItemManager.getInstance();
                                itemManager.dropWeapon(gunsManager.getWeapon(2), (int) p.getX(), (int) p.getY(), new Vector2f(0, 0));
                                break;
                            }
                            case "thompson": {
                                ItemManager itemManager = ItemManager.getInstance();
                                itemManager.dropWeapon(gunsManager.getWeapon(7), (int) p.getX(), (int) p.getY(), new Vector2f(0, 0));
                                break;
                            }
                            case "revolver": {
                                ItemManager itemManager = ItemManager.getInstance();
                                itemManager.dropWeapon(gunsManager.getWeapon(3), (int) p.getX(), (int) p.getY(), new Vector2f(0, 0));
                                break;
                            }
                            case "grenadelauncher": {
                                ItemManager itemManager = ItemManager.getInstance();
                                itemManager.dropWeapon(gunsManager.getWeapon(4), (int) p.getX(), (int) p.getY(), new Vector2f(0, 0));
                                break;
                            }
                            case "luger": {
                                ItemManager itemManager = ItemManager.getInstance();
                                itemManager.dropWeapon(gunsManager.getWeapon(5), (int) p.getX(), (int) p.getY(), new Vector2f(0, 0));
                                break;
                            }
                            case "m4": {
                                ItemManager itemManager = ItemManager.getInstance();
                                itemManager.dropWeapon(gunsManager.getWeapon(6), (int) p.getX(), (int) p.getY(), new Vector2f(0, 0));
                                break;
                            }
                            case "modernshotgun": {
                                ItemManager itemManager = ItemManager.getInstance();
                                itemManager.dropWeapon(gunsManager.getWeapon(8), (int) p.getX(), (int) p.getY(), new Vector2f(0, 0));
                                break;
                            }
                        }
                        break;
                    }
                }
                stringbuilder.setLength(0);
            }
            for(int i = 0; i< keys.length; i++){
                if(!used[i]){
                    keys[i] = k;
                    used[i] = true;
                    if ((k >= '0' && k <= '9') || (k >= 'a' && k <= 'z') || (k >= 'A' && k <= 'Z') || k == ' ') {
                        if (stringbuilder.length() <= 50) addChar(Character.toLowerCase((char) k));
                    }
                    if(k == GLFW.GLFW_KEY_BACKSPACE){
                        if (stringbuilder.length() >= 1) stringbuilder.setLength(stringbuilder.length() - 1);
                    }
                    globalDelay = System.currentTimeMillis();
                    return;
                }
            }
        }
    }
    public void draw(){
        if(enabled) {
            if (dot) {
                textRender.draw(stringbuilder.toString() + "/", new Vector3f(50, 50, 0), 2, new Vector3f(1f, 1f, 1f));
            } else {
                textRender.draw(stringbuilder.toString(), new Vector3f(50, 50, 0), 2, new Vector3f(1f, 1f, 1f));
            }
        }
    }
    public void update(){
        if(enabled) {
            if (System.currentTimeMillis() - time - InGame.deltaPauseTime() > 250) {
                time = System.currentTimeMillis() - InGame.deltaPauseTime();
                dot = !dot;
            }
            if(System.currentTimeMillis() - writeDelay > 70 && System.currentTimeMillis() - globalDelay > 400){
                writeDelay = System.currentTimeMillis();
                for(int i = 0; i< keys.length; i++){
                    if(keys[i] == GLFW.GLFW_KEY_BACKSPACE && used[i]){
                        if (stringbuilder.length() >= 1) stringbuilder.setLength(stringbuilder.length() - 1);
                        return;
                    }
                }
                for(int i = 0;i<keys.length;i++){
                    if(used[i]){
                        int k = keys[i];
                        if ((k >= '0' && k <= '9') || (k >= 'a' && k <= 'z') || (k >= 'A' && k <= 'Z') || k == ' ') {
                            if (stringbuilder.length() <= 50) addChar(Character.toLowerCase((char) k));
                        }
                    }
                }
            }
        }
    }
}

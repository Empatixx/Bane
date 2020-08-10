package cz.Empatix.Utility;

import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.InGame;
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

    private GunsManager gunsManager;
    private Player p;
    private ItemManager itemManager;
    private EnemyManager em;

    public Console(GunsManager gunsManager, Player p, ItemManager itemManager, EnemyManager em){
        stringbuilder = new StringBuilder();
        time = System.currentTimeMillis();
        dot = true;
        this.gunsManager = gunsManager;
        this.em = em;
        this.p = p;
        this.itemManager = itemManager;
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
    public void keyPressed(int k){
        if(enabled) {
            if ((k >= '0' && k <= '9') || (k >= 'a' && k <= 'z') || (k >= 'A' && k <= 'Z') || k == ' ') {
                if (stringbuilder.length() <= 50) addChar(Character.toLowerCase((char) k));
            }
            if (k == GLFW.GLFW_KEY_BACKSPACE) {
                if (stringbuilder.length() >= 1) stringbuilder.setLength(stringbuilder.length() - 1);
            }
            if (k == GLFW.GLFW_KEY_ENTER) {
                String cmd = stringbuilder.toString();
                String[] args = cmd.split(" ");
                switch (args[0]){
                    case "sethealth":{
                        if(args.length < 2){
                            break;
                        }
                        p.setHealth(Integer.parseInt(args[1]));
                        break;
                    }
                    case "setcoins":{
                        if(args.length < 2){
                            break;
                        }
                        p.setCoins(Integer.parseInt(args[1]));
                        break;
                    }
                    case "fillammo":{
                        for(int i = 0;i<5;i++){
                            gunsManager.addAmmo(100,i);
                        }
                        break;
                    }
                    case "spawnenemy":{
                        if(args.length < 3){
                            break;
                        }
                        em.addEnemy((int)p.getX(),(int)p.getY(),args[1],Integer.parseInt(args[2]));
                        break;
                    }
                    case "dropgun":{
                        if(args.length < 2){
                            break;
                        }
                        switch (args[1]){
                            case "pistol":{
                                ItemManager.dropWeapon(gunsManager.getWeapon(0),(int)p.getX(),(int)p.getY(), new Vector2f(0,0));
                                break;
                            }
                            case "shotgun":{
                                ItemManager.dropWeapon(gunsManager.getWeapon(1),(int)p.getX(),(int)p.getY(), new Vector2f(0,0));
                                break;
                            }
                            case "uzi":{
                                ItemManager.dropWeapon(gunsManager.getWeapon(2),(int)p.getX(),(int)p.getY(), new Vector2f(0,0));
                                break;
                            }
                            case "revolver":{
                                ItemManager.dropWeapon(gunsManager.getWeapon(3),(int)p.getX(),(int)p.getY(), new Vector2f(0,0));
                                break;
                            }
                            case "grenadelauncher":{
                                ItemManager.dropWeapon(gunsManager.getWeapon(4),(int)p.getX(),(int)p.getY(), new Vector2f(0,0));
                                break;
                            }
                            case "luger":{
                                ItemManager.dropWeapon(gunsManager.getWeapon(5),(int)p.getX(),(int)p.getY(), new Vector2f(0,0));
                                break;
                            }
                            case "m4":{
                                ItemManager.dropWeapon(gunsManager.getWeapon(6),(int)p.getX(),(int)p.getY(), new Vector2f(0,0));
                                break;
                            }
                        }
                        break;
                    }
                }
                stringbuilder.setLength(0);
            }
        }
    }
    public void draw(){
        if(enabled) {
            if (dot) {
                TextRender.renderText(stringbuilder.toString() + "/", new Vector3f(50, 50, 0), 2, new Vector3f(1f, 1f, 1f));
            } else {
                TextRender.renderText(stringbuilder.toString(), new Vector3f(50, 50, 0), 2, new Vector3f(1f, 1f, 1f));
            }
        }
    }
    public void update(){
        if(enabled) {
            if (System.currentTimeMillis() - time - InGame.deltaPauseTime() > 250) {
                time = System.currentTimeMillis() - InGame.deltaPauseTime();
                dot = !dot;
            }
        }
    }
}

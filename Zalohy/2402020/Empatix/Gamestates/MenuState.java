package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Hud.MenuBar;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

import java.awt.*;

public class MenuState extends GameState{
    // main menu
    private final static int BEGIN = 0;
    private final static int SETTINGS = 1;
    private final static int EXIT = 2;
    // settings menu
    private final static int GRAPHICS = 3;
    private final static int AUDIO = 4;
    private final static int CONTROLS = 5;
    private final static int SETTINGSEXIT = 6;



    private MenuBar[] huds;
    private MenuBar[] settingsHuds;

    private Background bg;
    private Background settingsBg;

    private float mouseX;
    private float mouseY;

    private boolean settings;
    private int selectedSettings;

    MenuState(GameStateManager gsm, Camera c){
        super(c);
        this.gsm = gsm;
        this.
        init();

    }


    @Override
    public void draw() {
        bg.draw();
        if (settings){
            settingsBg.draw();
            for (int i = 0; i < settingsHuds.length;i++){
                settingsHuds[i].draw();
            }
            TextRender.renderText(camera,"Graphics",new Vector3f(550,300,0),4,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText(camera,"Audio",new Vector3f(940,300,0),4,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText(camera,"Controls",new Vector3f(1260,300,0),4,new Vector3f(0.874f,0.443f,0.149f));

            if(selectedSettings == GRAPHICS){
                TextRender.renderText(camera,"Resolution",new Vector3f(460,450,0),3,new Vector3f(0.874f,0.443f,0.149f));
            }


        } else {
            for(int i = 0; i < huds.length; i++) {
                huds[i].draw();
            }
        }
    }

    @Override
    public void init() {

        bg = new Background("Textures\\Menu\\bg.png",camera);
        settingsHuds = new MenuBar[4];
        huds = new MenuBar[3];
        // the main menu
        MenuBar bar = new MenuBar("Textures\\Menu\\menu_bar_start.tga",new Vector3f(960,150,0),2,camera,200,100);
        bar.setType(BEGIN);
        huds[0] = bar;

        bar = new MenuBar("Textures\\Menu\\menu_bar_settings.tga",new Vector3f(960,450,0),2,camera,200,100);
        bar.setType(SETTINGS);
        huds[1] = bar;

        bar = new MenuBar("Textures\\Menu\\menu_bar_exit.tga",new Vector3f(960,750,0),2,camera,200,100);
        bar.setType(EXIT);
        huds[2] = bar;

        AudioManager.playSoundtrack(Soundtrack.MENU);

        settingsBg = new Background("Textures\\Menu\\settings.tga",camera);

        String defaultBar = "Textures\\Menu\\menu_bar.tga";

        bar = new MenuBar(defaultBar,new Vector3f(610,275,0),1.5f,camera,200,100);
        bar.setType(GRAPHICS);
        bar.setClick(true);
        settingsHuds[0] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(960,275,0),1.5f,camera,200,100);
        bar.setType(AUDIO);
        settingsHuds[1] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(1310,275,0),1.5f,camera,200,100);
        bar.setType(CONTROLS);
        settingsHuds[2] = bar;

        bar = new MenuBar("Textures\\Menu\\settings_exit.tga",new Vector3f(1558,220,0),2,camera,25,25);
        bar.setType(SETTINGSEXIT);
        settingsHuds[3] = bar;


        settingsBg.setDimensions(1280,720);
        selectedSettings = GRAPHICS;
    }

    @Override
    public void keyPressed(int k) {

    }
    @Override
    public void keyReleased(int k) {

    }
    @Override
    public void mouseReleased(int button) {
        if (settings){
            for (int i = 0; i < settingsHuds.length; i++) {
                MenuBar bar = settingsHuds[i];
                if (bar.intersects(mouseX, mouseY)) {
                    int type = bar.getType();
                    if(type == SETTINGSEXIT){
                        settings = false;
                    } else {
                        bar.setClick(true);
                        selectedSettings = type;
                        for (int j = 0; j < settingsHuds.length; j++) {
                            MenuBar bar2 = settingsHuds[i];
                            if (bar2.getType() != type){
                                bar2.setClick(false);
                            }
                        }
                    }
                }
            }
        } else {

            for (int i = 0; i < huds.length; i++) {
                if (huds[i].intersects(mouseX, mouseY)) {
                    int type = huds[i].getType();
                    if (type == BEGIN) {
                        gsm.setState(GameStateManager.INGAME);
                    } else if (type == SETTINGS) {
                        settings = true;
                    } else if (type == EXIT) {
                        Game.stopGame();
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(int button) {

    }

    @Override
    public void update() {
        final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        mouseX = mouseLoc.x;
        mouseY = mouseLoc.y;
        if(settings){
            for (int j = 0;j < settingsHuds.length;j++){
                MenuBar bar = settingsHuds[j];
                if (bar.intersects(mouseX, mouseY)) {
                    bar.setClick(true);
                } else {
                    if (bar.getType() == selectedSettings) continue;
                    bar.setClick(false);
                }
            }
        } else {
            for(int i = 0;i < huds.length;i++) {
                MenuBar bar = huds[i];
                if (bar.intersects(mouseX, mouseY)) {
                    bar.setClick(true);
                } else {
                    bar.setClick(false);
                }
            }
        }
    }
}

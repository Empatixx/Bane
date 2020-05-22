package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Hud.MenuBar;
import cz.Empatix.Render.Hud.SliderBar;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

import java.awt.*;

import static cz.Empatix.Main.Game.ARROW;
import static cz.Empatix.Main.Game.setCursor;

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

    // settings graphics
    private final static int LOWERRESOLUTION = 7;
    private final static int HIGHERRESOLUTION = 8;
    // settings audio
    private final static int OVERALL = 0;
    private final static int EFFECTS = 1;
    private final static int MUSIC = 2;


    // main menu
    private MenuBar[] huds;
    private Image title;

    private MenuBar[] settingsHuds;
    //   graphics
    private MenuBar[] graphicsHuds;
    // audio
    private SliderBar[] audioSliders;



    private Background bg;
    private Background settingsBg;

    private float mouseX;
    private float mouseY;

    private boolean settings;
    private int selectedSettings;

    private Source source;
    private int soundMenuClick;

    MenuState(GameStateManager gsm){
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
            TextRender.renderText("Graphics",new Vector3f(550,300,0),4,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText("Audio",new Vector3f(940,300,0),4,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText("Controls",new Vector3f(1260,300,0),4,new Vector3f(0.874f,0.443f,0.149f));

            if(selectedSettings == GRAPHICS){
                TextRender.renderText("Resolution:",new Vector3f(460,450,0),3,new Vector3f(0.874f,0.443f,0.149f));
                TextRender.renderText(Settings.WIDTH+"x"+Settings.HEIGHT,new Vector3f(1160,450,0),3,new Vector3f(0.874f,0.443f,0.149f));
                for(int j = 0;j < graphicsHuds.length;j++){
                    graphicsHuds[j].draw();
                }
            } else if (selectedSettings == AUDIO){
                TextRender.renderText("Overall:",new Vector3f(460,450,0),3,new Vector3f(0.874f,0.443f,0.149f));
                TextRender.renderText("Sounds:",new Vector3f(460,550,0),3,new Vector3f(0.874f,0.443f,0.149f));
                TextRender.renderText("Music:",new Vector3f(460,650,0),3,new Vector3f(0.874f,0.443f,0.149f));
                for(SliderBar hud:audioSliders){
                    hud.draw();
                }
            } else if (selectedSettings == CONTROLS){
                TextRender.renderText("Coming soon...",new Vector3f(760,450,0),5,new Vector3f(0.874f,0.443f,0.149f));

            }


        } else {
            for(int i = 0; i < huds.length; i++) {
                huds[i].draw();
            }
            title.draw();
        }
    }

    @Override
    public void init() {
        bg = new Background("Textures\\Menu\\bg.png");
        settingsHuds = new MenuBar[4];
        huds = new MenuBar[3];
        graphicsHuds = new MenuBar[2];
        audioSliders = new SliderBar[3];
        title = new Image("Textures\\Menu\\logo.tga",new Vector3f(960,150,0),7);
        // the main menu
        MenuBar bar = new MenuBar("Textures\\Menu\\menu_bar_start.tga",new Vector3f(960,400,0),1.8f,200,100,true);
        bar.setType(BEGIN);
        huds[0] = bar;

        bar = new MenuBar("Textures\\Menu\\menu_bar_settings.tga",new Vector3f(960,625,0),1.8f,200,100,true);
        bar.setType(SETTINGS);
        huds[1] = bar;

        bar = new MenuBar("Textures\\Menu\\menu_bar_exit.tga",new Vector3f(960,850,0),1.8f,200,100,true);
        bar.setType(EXIT);
        huds[2] = bar;

        AudioManager.playSoundtrack(Soundtrack.MENU);


        // settings main menu
        settingsBg = new Background("Textures\\Menu\\settings.tga");

        String defaultBar = "Textures\\Menu\\menu_bar.tga";
        bar = new MenuBar(defaultBar,new Vector3f(610,275,0),1.5f,200,100,true);
        bar.setType(GRAPHICS);
        bar.setClick(true);
        settingsHuds[0] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(960,275,0),1.5f,200,100,true);
        bar.setType(AUDIO);
        settingsHuds[1] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(1310,275,0),1.5f,200,100,true);
        bar.setType(CONTROLS);
        settingsHuds[2] = bar;


        // graphics menu
        bar = new MenuBar("Textures\\Menu\\settings_exit.tga",new Vector3f(1558,220,0),2,25,25,true);
        bar.setType(SETTINGSEXIT);
        settingsHuds[3] = bar;

        bar = new MenuBar("Textures\\Menu\\arrow_left.tga",new Vector3f(1020,430,0),4,15,15,true);
        bar.setType(LOWERRESOLUTION);
        graphicsHuds[0] = bar;

        bar = new MenuBar("Textures\\Menu\\arrow_right.tga",new Vector3f(1420,430,0),4,15,15,true);
        bar.setType(HIGHERRESOLUTION);
        graphicsHuds[1] = bar;


        SliderBar sliderBar = new SliderBar("Textures\\Menu\\volume_slider",new Vector3f(1100,430,0),4);
        sliderBar.setValue(Settings.OVERALL);
        sliderBar.setType(OVERALL);
        audioSliders[0] = sliderBar;

        sliderBar = new SliderBar("Textures\\Menu\\volume_slider",new Vector3f(1100,530,0),4);
        sliderBar.setValue(Settings.EFFECTS);
        sliderBar.setType(EFFECTS);
        audioSliders[1] = sliderBar;

        sliderBar = new SliderBar("Textures\\Menu\\volume_slider",new Vector3f(1100,630,0),4);
        sliderBar.setValue(Settings.MUSIC);
        sliderBar.setType(MUSIC);
        audioSliders[2] = sliderBar;

        settingsBg.setDimensions(1280,720);
        selectedSettings = GRAPHICS;


        // sounds
        source = new Source(Source.EFFECTS,1f);
        soundMenuClick = AudioManager.loadSound("menuclick.ogg");

        setCursor(ARROW);
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
                    if(selectedSettings != type) {
                        source.play(soundMenuClick);
                    }
                    if(type == SETTINGSEXIT) {
                        settings = false;
                    } else {
                        selectedSettings = type;
                    }
                }
            }
            if (selectedSettings == GRAPHICS) {
                for(MenuBar hud : graphicsHuds){
                    int type = hud.getType();
                    if(type == LOWERRESOLUTION){
                        if (hud.intersects(mouseX,mouseY)){
                            source.play(soundMenuClick);
                            Settings.lowerResolution();
                        }
                    }
                    else if(type == HIGHERRESOLUTION){
                        if (hud.intersects(mouseX,mouseY)){
                            source.play(soundMenuClick);
                            Settings.higherResolution();
                        }
                    }
                }
            } else if (selectedSettings == AUDIO){
                for(SliderBar sliderBar: audioSliders) {
                    sliderBar.unlock();
                }
            }
        } else {

            for (int i = 0; i < huds.length; i++) {
                if (huds[i].intersects(mouseX, mouseY)) {
                    source.play(soundMenuClick);

                    int type = huds[i].getType();
                    source.play(soundMenuClick);
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

        if(selectedSettings == AUDIO){
            for(SliderBar sliderBar: audioSliders) {
                if (sliderBar.intersects(mouseX, mouseY)) {
                    sliderBar.setLocked(true);
                }
            }
        }

    }

    @Override
    public void update() {
        final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        mouseX = mouseLoc.x * Settings.scaleMouseX();
        mouseY = mouseLoc.y * Settings.scaleMouseY();

        AudioManager.update();

        if(settings){
            // main settings huds
            for (int j = 0;j < settingsHuds.length;j++){
                MenuBar bar = settingsHuds[j];
                if (bar.intersects(mouseX, mouseY)) {
                    bar.setClick(true);
                } else {
                    if (bar.getType() == selectedSettings) continue;
                    bar.setClick(false);
                }
            }
            // graphics huds
            if (selectedSettings == GRAPHICS) {
                for(MenuBar hud : graphicsHuds){
                    if(hud.intersects(mouseX,mouseY)){
                        hud.setClick(true);
                    } else {
                        hud.setClick(false);
                    }
                }
            // audio sliders / huds
            } else if (selectedSettings == AUDIO){
                for(SliderBar sliderBar : audioSliders){
                    if(sliderBar.isLocked()){
                        sliderBar.update(mouseX);
                        int type = sliderBar.getType();
                        if(type == EFFECTS){
                            Settings.EFFECTS = sliderBar.getValue();
                        } else if (type == OVERALL) {
                            Settings.OVERALL = sliderBar.getValue();
                        } else if (type == MUSIC) {
                            Settings.MUSIC = sliderBar.getValue();
                        }
                    }
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

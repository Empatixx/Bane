package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Hud.CheckBox;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Hud.MenuBar;
import cz.Empatix.Render.Hud.SliderBar;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

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
    private final static int LIGHTNING = 9;
    private final static int VSYNC = 10;
    private final static int BRIGHTNESS = 11;
    private final static int CONFIRMCHANGES = 12;
    private final static int RESETCHANGES = 13;

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
    private CheckBox[] checkBoxes;
    private SliderBar[] graphicsSliders;
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

    private TextRender[] textRender;

    MenuState(GameStateManager gsm){
        this.gsm = gsm;
        textRender = new TextRender[26];
        for(int i = 0;i<26;i++) textRender[i] = new TextRender();
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
            textRender[0].draw("Graphics",new Vector3f(550,300,0),4,new Vector3f(0.874f,0.443f,0.149f));
            textRender[1].draw("Audio",new Vector3f(940,300,0),4,new Vector3f(0.874f,0.443f,0.149f));
            textRender[2].draw("Controls",new Vector3f(1260,300,0),4,new Vector3f(0.874f,0.443f,0.149f));

            if(selectedSettings == GRAPHICS){
                for(int j = 0;j < graphicsHuds.length;j++){
                    graphicsHuds[j].draw();
                }

                textRender[3].draw("Resolution:",new Vector3f(460,450,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[4].draw(Settings.preWIDTH+"x"+Settings.preHEIGHT,new Vector3f(1160,450,0),3,new Vector3f(0.874f,0.443f,0.149f));

                textRender[5].draw("Lightning:",new Vector3f(460,525,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[6].draw("V-Sync:",new Vector3f(460,600,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[7].draw("Brightness:",new Vector3f(460,675,0),3,new Vector3f(0.874f,0.443f,0.149f));

                textRender[8].draw("Reset",new Vector3f(1495,855,0),2,new Vector3f(0.874f,0.443f,0.149f));
                textRender[9].draw("Confirm",new Vector3f(1320,855,0),2,new Vector3f(0.874f,0.443f,0.149f));

                for(CheckBox box : checkBoxes){
                    box.draw();
                }
                for(SliderBar hud : graphicsSliders){
                    if(hud.isLocked()){
                        Vector3f pos = hud.getPos();
                        float value = hud.getValue();
                        float b = 0f,r,g;
                        if (value <= 0.5f){
                            r = 1.f;
                            g = 0.f + 2 * value;
                        } else{
                            r = 2 * (1-value);
                            g = 1f;
                        }
                        textRender[10].draw((int)(value*100)+"%",new Vector3f(pos.x(),pos.y()-25,pos.z()),2, new Vector3f(r,g,b));
                    }
                    hud.draw();
                }
            } else if (selectedSettings == AUDIO){
                textRender[10].draw("Overall:",new Vector3f(460,450,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[11].draw("Sounds:",new Vector3f(460,550,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[12].draw("Music:",new Vector3f(460,650,0),3,new Vector3f(0.874f,0.443f,0.149f));


                for(SliderBar hud:audioSliders){
                    if(hud.isLocked()){
                        Vector3f pos = hud.getPos();
                        float value = hud.getValue();
                        float b = 0f,r,g;
                        if (value <= 0.5f){
                            r = 1.f;
                            g = 0.f + 2 * value;
                        } else{
                            r = 2 * (1-value);
                            g = 1f;
                        }
                        textRender[13].draw((int)(value*100)+"%",new Vector3f(pos.x(),pos.y()-25,pos.z()),2, new Vector3f(r,g,b));
                    }
                    hud.draw();
                }
            } else if (selectedSettings == CONTROLS){
                textRender[14].draw("Move Up",new Vector3f(460,450,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[15].draw("Move Down",new Vector3f(460,500,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[16].draw("Move left",new Vector3f(460,550,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[17].draw("Move right",new Vector3f(460,600,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[18].draw("Object interact",new Vector3f(460,650,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[19].draw("Weapon drop",new Vector3f(460,700,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[20].draw("Shoot",new Vector3f(460,750,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[21].draw("Weapon slot 1",new Vector3f(460,800,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[23].draw("Weapon slot 2",new Vector3f(460,850,0),3,new Vector3f(0.874f,0.443f,0.149f));


                textRender[24].draw("Reload",new Vector3f(1060,450,0),3,new Vector3f(0.874f,0.443f,0.149f));
                textRender[25].draw("Big map",new Vector3f(1060,500,0),3,new Vector3f(0.874f,0.443f,0.149f));

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

        graphicsHuds = new MenuBar[4];
        checkBoxes = new CheckBox[2];
        graphicsSliders = new SliderBar[1];

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
        settingsBg.setDimensions(1280,720);


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

        bar = new MenuBar(defaultBar,new Vector3f(1505,845,0),0.75f,200,100,true);
        bar.setType(RESETCHANGES);
        bar.setClick(true);
        graphicsHuds[2] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(1345,845,0),0.75f,200,100,true);
        bar.setType(CONFIRMCHANGES);
        bar.setClick(true);
        graphicsHuds[3] = bar;


        CheckBox checkbox = new CheckBox("Textures\\Menu\\checkbox.tga", new Vector3f(1220,505,0),1,64,64);
        checkbox.setType(LIGHTNING);
        checkbox.setSelected(Settings.preLIGHTNING);
        checkBoxes[0] = checkbox;

        checkbox = new CheckBox("Textures\\Menu\\checkbox.tga", new Vector3f(1220,580,0),1,64,64);
        checkbox.setType(VSYNC);
        checkbox.setSelected(Settings.preVSYNC);
        checkBoxes[1] = checkbox;


        SliderBar sliderBar = new SliderBar("Textures\\Menu\\volume_slider",new Vector3f(1225,670,0),3);
        sliderBar.setValue(Settings.preBRIGHTNESS);
        sliderBar.setType(BRIGHTNESS);
        graphicsSliders[0] = sliderBar;


        sliderBar = new SliderBar("Textures\\Menu\\volume_slider",new Vector3f(1100,430,0),4);
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

        selectedSettings = GRAPHICS;


        // sounds
        source = AudioManager.createSource(Source.EFFECTS,1f);
        soundMenuClick = AudioManager.loadSound("menuclick.ogg");

        setCursor(ARROW);
    }
    @Override
    public void mouseScroll(double x, double y){
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
            if (selectedSettings == GRAPHICS){
                for (SliderBar sliderBar : graphicsSliders) {
                    sliderBar.unlock();
                }
            } else if (selectedSettings == AUDIO){
                for (SliderBar sliderBar : audioSliders) {
                    sliderBar.unlock();
                }
            }
        }

    }

    @Override
    public void mousePressed(int button) {
        if(settings){
            if(selectedSettings == AUDIO){
                for(SliderBar sliderBar: audioSliders) {
                    if (sliderBar.intersects(mouseX, mouseY)) {
                        sliderBar.setLocked(true);
                    }
                }
            } else if(selectedSettings == GRAPHICS){
                for(SliderBar sliderBar: graphicsSliders) {
                    if (sliderBar.intersects(mouseX, mouseY)) {
                        sliderBar.setLocked(true);
                    }
                }
            }
            for (int i = 0; i < settingsHuds.length; i++) {
                MenuBar bar = settingsHuds[i];
                if (bar.intersects(mouseX, mouseY)) {
                    int type = bar.getType();
                    if(selectedSettings != type) {
                        source.play(soundMenuClick);
                    }
                    if(type == SETTINGSEXIT) {
                        settings = false;
                        Settings.cancelChanges();

                        checkBoxes[0].setSelected(Settings.LIGHTNING);
                        checkBoxes[1].setSelected(Settings.VSYNC);

                        graphicsSliders[0].setValue(Settings.BRIGHTNESS);

                    } else {
                        selectedSettings = type;
                    }
                }
            }
            if (selectedSettings == GRAPHICS){
                for (MenuBar hud : graphicsHuds) {
                    int type = hud.getType();
                    if (type == LOWERRESOLUTION){
                        if (hud.intersects(mouseX, mouseY)){
                            source.play(soundMenuClick);
                            Settings.lowerResolution();
                            break;
                        }
                    } else if (type == HIGHERRESOLUTION){
                        if (hud.intersects(mouseX, mouseY)){
                            source.play(soundMenuClick);
                            Settings.higherResolution();
                            break;
                        }
                    } else if (type == CONFIRMCHANGES){
                        if (hud.intersects(mouseX, mouseY)){
                            Settings.confirmChanges();
                        }
                    } else if (type == RESETCHANGES){
                        if(hud.intersects(mouseX,mouseY)){
                            Settings.reset();
                            checkBoxes[0].setSelected(Settings.LIGHTNING);
                            checkBoxes[1].setSelected(Settings.VSYNC);

                            graphicsSliders[0].setValue(Settings.BRIGHTNESS);
                           }
                    }
                }
                for (CheckBox box : checkBoxes) {
                    if (box.intersects(mouseX, mouseY)){
                        box.setSelected(!box.isSelected());
                        break;
                    }
                }
            }

        } else {

            for (int i = 0; i < huds.length; i++) {
                if (huds[i].intersects(mouseX, mouseY)) {
                    source.play(soundMenuClick);

                    int type = huds[i].getType();
                    source.play(soundMenuClick);
                    if (type == BEGIN) {
                        gsm.setState(GameStateManager.PROGRESSROOM);
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
    public void update() {
        mouseX = gsm.getMouseX();
        mouseY = gsm.getMouseY();

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
                for(SliderBar sliderBar : graphicsSliders){
                    if(sliderBar.isLocked()){
                        sliderBar.update(mouseX,mouseY);
                        int type = sliderBar.getType();
                        if(type == BRIGHTNESS) {
                            Settings.preBRIGHTNESS = sliderBar.getValue();
                        }
                    }
                }
                Settings.preLIGHTNING = checkBoxes[0].isSelected();
                Settings.preVSYNC = checkBoxes[1].isSelected();

                // audio sliders / huds
            } else if (selectedSettings == AUDIO){
                for(SliderBar sliderBar : audioSliders){
                    if(sliderBar.isLocked()){
                        sliderBar.update(mouseX,mouseY);
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

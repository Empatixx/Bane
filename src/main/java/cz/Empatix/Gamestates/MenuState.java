package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Main.DiscordRP;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Hud.*;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Utility.Loader;
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
    private final static int CONTRAST = 14;
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

    // controls
    private SliderBar controlSlider;
    private ControlSettings controlSettings;

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
        textRender = new TextRender[35];
        for(int i = 0;i<textRender.length;i++) textRender[i] = new TextRender();
        init();
    }


    @Override
    public void draw() {
        bg.draw();
        if (settings) {
            settingsBg.draw();
            for (int i = 0; i < settingsHuds.length; i++) {
                settingsHuds[i].draw();
            }
            textRender[0].draw("Graphics", new Vector3f(TextRender.getHorizontalCenter(465, 755, "Graphics", 4), 300, 0), 4, new Vector3f(0.874f, 0.443f, 0.149f));
            textRender[1].draw("Audio", new Vector3f(TextRender.getHorizontalCenter(815, 1105, "Audio", 4), 300, 0), 4, new Vector3f(0.874f, 0.443f, 0.149f));
            textRender[2].draw("Controls", new Vector3f(TextRender.getHorizontalCenter(1165, 1455, "Controls", 4), 300, 0), 4, new Vector3f(0.874f, 0.443f, 0.149f));

            if (selectedSettings == GRAPHICS) {
                for (int j = 0; j < graphicsHuds.length; j++) {
                    graphicsHuds[j].draw();
                }

                textRender[3].draw("Resolution:", new Vector3f(460, 450, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));
                textRender[4].draw(Settings.preWIDTH + "x" + Settings.preHEIGHT, new Vector3f(TextRender.getHorizontalCenter(1045, 1395, Settings.preWIDTH + "x" + Settings.preHEIGHT, 3), 450, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));

                textRender[5].draw("Lightning:", new Vector3f(460, 525, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));
                textRender[6].draw("V-Sync:", new Vector3f(460, 600, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));
                textRender[7].draw("Brightness:", new Vector3f(460, 675, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));
                //textRender[26].draw("Contrast:",new Vector3f(460,750,0),3,new Vector3f(0.874f,0.443f,0.149f));

                textRender[8].draw("Confirm", new Vector3f(TextRender.getHorizontalCenter(1275, 1415, "Confirm", 2), 855, 0), 2, new Vector3f(0.874f, 0.443f, 0.149f));
                textRender[9].draw("Reset", new Vector3f(TextRender.getHorizontalCenter(1435, 1575, "Reset", 2), 855, 0), 2, new Vector3f(0.874f, 0.443f, 0.149f));

                for (CheckBox box : checkBoxes) {
                    box.draw();
                }
                for (SliderBar hud : graphicsSliders) {
                    if (hud.isLocked()) {
                        Vector3f pos = hud.getPos();
                        float value = hud.getValue();
                        float b = 0f, r, g;
                        if (value <= 0.5f) {
                            r = 1.f;
                            g = 0.f + 2 * value;
                        } else {
                            r = 2 * (1 - value);
                            g = 1f;
                        }
                        String text = (int) (value * 100) + "%";
                        float centerX = TextRender.getHorizontalCenter((int)pos.x(),(int)pos.x(),text,2);
                        textRender[11].draw(text, new Vector3f(centerX, pos.y() - 25, pos.z()), 2, new Vector3f(r, g, b));
                    }
                    hud.draw();
                }
            } else if (selectedSettings == AUDIO) {
                textRender[11].draw("Overall:", new Vector3f(460, 450, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));
                textRender[12].draw("Sounds:", new Vector3f(460, 550, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));
                textRender[13].draw("Music:", new Vector3f(460, 650, 0), 3, new Vector3f(0.874f, 0.443f, 0.149f));


                for (SliderBar hud : audioSliders) {
                    if (hud.isLocked()) {
                        Vector3f pos = hud.getPos();
                        float value = hud.getValue();
                        float b = 0f, r, g;
                        if (value <= 0.5f) {
                            r = 1.f;
                            g = 0.f + 2 * value;
                        } else {
                            r = 2 * (1 - value);
                            g = 1f;
                        }
                        textRender[14].draw((int) (value * 100) + "%", new Vector3f(pos.x(), pos.y() - 25, pos.z()), 2, new Vector3f(r, g, b));
                    }
                    hud.draw();
                }
            } else if (selectedSettings == CONTROLS) {
                controlSettings.draw();
            }


        } else {
            for (MenuBar hud : huds) {
                hud.draw();
            }
            textRender[26].draw("Play",new Vector3f(TextRender.getHorizontalCenter(780,1150,"Play",5),415,0),5,new Vector3f(0.874f,0.443f,0.149f));
            textRender[27].draw("Settings",new Vector3f(TextRender.getHorizontalCenter(780,1150,"Settings",5),640,0),5,new Vector3f(0.874f,0.443f,0.149f));
            textRender[28].draw("Exit",new Vector3f(TextRender.getHorizontalCenter(780,1150,"Exit",5),865,0),5,new Vector3f(0.874f,0.443f,0.149f));
            title.draw();
        }
    }

    @Override
    public void init() {
        DiscordRP.getInstance().update("Idle","Main menu");

        bg = new Background("Textures\\Menu\\bg.png");
        settingsHuds = new MenuBar[4];
        huds = new MenuBar[3];

        graphicsHuds = new MenuBar[4];
        checkBoxes = new CheckBox[2];
        graphicsSliders = new SliderBar[1];

        audioSliders = new SliderBar[3];
        title = new Image("Textures\\Menu\\logo.tga",new Vector3f(960,150,0),7);
        // main menu
        MenuBar bar = new MenuBar("Textures\\Menu\\menu_bar.tga",new Vector3f(960,400,0),1.8f,200,100,true);
        bar.setType(BEGIN);
        huds[0] = bar;
        bar = new MenuBar("Textures\\Menu\\menu_bar.tga",new Vector3f(960,625,0),1.8f,200,100,true);
        bar.setType(SETTINGS);
        huds[1] = bar;
        bar = new MenuBar("Textures\\Menu\\menu_bar.tga",new Vector3f(960,850,0),1.8f,200,100,true);
        bar.setType(EXIT);
        huds[2] = bar;

        InputBar inputBar;
        inputBar = new InputBar("Textures\\Menu\\input_bar.tga",new Vector3f(960,400,0),1.8f,300,100,"Your name:");
        inputBar.setType(0);

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


        CheckBox checkbox = new CheckBox(new Vector3f(1220,505,0),1,64,64);
        checkbox.setType(LIGHTNING);
        checkbox.setSelected(Settings.preLIGHTNING);
        checkBoxes[0] = checkbox;

        checkbox = new CheckBox(new Vector3f(1220,580,0),1,64,64);
        checkbox.setType(VSYNC);
        checkbox.setSelected(Settings.preVSYNC);
        checkBoxes[1] = checkbox;


        SliderBar sliderBar = new SliderBar(new Vector3f(1225,670,0),3);
        sliderBar.setValue(Settings.preBRIGHTNESS);
        sliderBar.setType(BRIGHTNESS);
        graphicsSliders[0] = sliderBar;

        sliderBar = new SliderBar(new Vector3f(1100,430,0),4);
        sliderBar.setValue(Settings.OVERALL);
        sliderBar.setType(OVERALL);
        audioSliders[0] = sliderBar;

        sliderBar = new SliderBar(new Vector3f(1100,530,0),4);
        sliderBar.setValue(Settings.EFFECTS);
        sliderBar.setType(EFFECTS);
        audioSliders[1] = sliderBar;

        sliderBar = new SliderBar(new Vector3f(1100,630,0),4);
        sliderBar.setValue(Settings.MUSIC);
        sliderBar.setType(MUSIC);
        audioSliders[2] = sliderBar;

        selectedSettings = GRAPHICS;

        controlSlider = new SliderBar(new Vector3f(1560f,630,0),3f);

        controlSettings = new ControlSettings(controlSlider);

        // sounds
        source = AudioManager.createSource(Source.EFFECTS,1f);
        soundMenuClick = AudioManager.loadSound("menuclick.ogg");

        setCursor(ARROW);
    }
    @Override
    public void mouseScroll(double x, double y){
        if(settings && selectedSettings == CONTROLS){
            controlSettings.mouseScroll(x,y);
        }
    }
    @Override
    public void keyPressed(int k) {
    }
    @Override
    public void keyReleased(int k) {
        controlSettings.keyReleased(k);
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
            } else if (selectedSettings == CONTROLS){
                controlSettings.mouseReleased(mouseX,mouseY);
                controlSlider.unlock();
            }
        }
    }

    @Override
    public void mousePressed(int button) {
        if(settings) {
            for (MenuBar bar : settingsHuds) {
                if (bar.intersects(mouseX, mouseY)) {
                    int type = bar.getType();
                    if (selectedSettings != type) {
                        source.play(soundMenuClick);
                    }
                    if (type == SETTINGSEXIT) {
                        settings = false;
                        Settings.cancelChanges();
                        if (selectedSettings == CONTROLS) controlSettings.cancel();

                        checkBoxes[0].setSelected(Settings.LIGHTNING);
                        checkBoxes[1].setSelected(Settings.VSYNC);

                        graphicsSliders[0].setValue(Settings.BRIGHTNESS);
                        break;
                    } else {
                        selectedSettings = type;
                        break;
                    }
                }
            }
            if (selectedSettings == AUDIO) {
                for (SliderBar sliderBar : audioSliders) {
                    if (sliderBar.intersects(mouseX, mouseY)) {
                        sliderBar.setLocked(true);
                        break;
                    }
                }
            } else if (selectedSettings == GRAPHICS) {
                for (SliderBar sliderBar : graphicsSliders) {
                    if (sliderBar.intersects(mouseX, mouseY)) {
                        sliderBar.setLocked(true);
                        break;
                    }
                }
                for (MenuBar hud : graphicsHuds) {
                    int type = hud.getType();
                    if(hud.intersects(mouseX,mouseY)){
                        source.play(soundMenuClick);
                        if(type == LOWERRESOLUTION){
                            Settings.lowerResolution();
                            break;
                        } else if(type == HIGHERRESOLUTION){
                            Settings.higherResolution();
                            break;
                        } else if (type == CONFIRMCHANGES){
                            Settings.confirmChanges();
                            break;
                        } else if (type == RESETCHANGES) {
                            Settings.reset();
                            checkBoxes[0].setSelected(Settings.LIGHTNING);
                            checkBoxes[1].setSelected(Settings.VSYNC);

                            graphicsSliders[0].setValue(Settings.BRIGHTNESS);
                            break;
                        }
                    }
                }
                for (CheckBox box : checkBoxes) {
                    if (box.intersects(mouseX, mouseY)) {
                        box.setSelected(!box.isSelected());
                        break;
                    }
                }
            } else if (selectedSettings == CONTROLS) {
                controlSettings.mousePressed(mouseX, mouseY, button);
                if (controlSlider.intersects(mouseX, mouseY)) {
                    controlSlider.setLocked(true);
                }
            }
        } else {

            for (MenuBar hud : huds) {
                if (hud.intersects(mouseX, mouseY)) {
                    int type = hud.getType();
                    source.play(soundMenuClick);
                    if (type == BEGIN) {
                        gsm.setState(GameStateManager.PROGRESSROOM);
                        break;
                    } else if (type == SETTINGS) {
                        settings = true;
                        break;
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

        if(settings) {
            // main settings huds
            for (MenuBar bar : settingsHuds) {
                if (bar.intersects(mouseX, mouseY)) {
                    bar.setClick(true);
                } else {
                    if (bar.getType() == selectedSettings) continue;
                    bar.setClick(false);
                }
            }
            // graphics huds
            if (selectedSettings == GRAPHICS) {
                for (MenuBar hud : graphicsHuds) {
                    if (hud.intersects(mouseX, mouseY)) {
                        hud.setClick(true);
                    } else {
                        hud.setClick(false);
                    }
                }
                for (SliderBar sliderBar : graphicsSliders) {
                    if (sliderBar.isLocked()) {
                        sliderBar.update(mouseX, mouseY);
                        int type = sliderBar.getType();
                        if (type == BRIGHTNESS) {
                            Settings.preBRIGHTNESS = sliderBar.getValue();
                        }
                        if (type == CONTRAST) {
                            //Settings.CONTRAST = sliderBar.getValue();
                        }
                    }
                }
                Settings.preLIGHTNING = checkBoxes[0].isSelected();
                Settings.preVSYNC = checkBoxes[1].isSelected();

                // audio sliders / huds
            } else if (selectedSettings == AUDIO) {
                for (SliderBar sliderBar : audioSliders) {
                    if (sliderBar.isLocked()) {
                        sliderBar.update(mouseX, mouseY);
                        int type = sliderBar.getType();
                        if (type == EFFECTS) {
                            Settings.EFFECTS = sliderBar.getValue();
                        } else if (type == OVERALL) {
                            Settings.OVERALL = sliderBar.getValue();
                        } else if (type == MUSIC) {
                            Settings.MUSIC = sliderBar.getValue();
                        }
                    }
                }
            } else if (selectedSettings == CONTROLS) {
                controlSettings.update(mouseX, mouseY);
            }
        } else {
            for (MenuBar bar : huds) {
                if (bar.intersects(mouseX, mouseY)) {
                    bar.setClick(true);
                } else {
                    bar.setClick(false);
                }
            }
        }
    }
}

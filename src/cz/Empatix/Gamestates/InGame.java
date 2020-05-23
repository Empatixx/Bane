package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Framebuffer;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Hud.MenuBar;
import cz.Empatix.Render.Hud.*;
import cz.Empatix.Render.Postprocessing.Fade;
import cz.Empatix.Render.Postprocessing.GaussianBlur;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

import static cz.Empatix.Main.Game.ARROW;
import static cz.Empatix.Main.Game.setCursor;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;


public class InGame extends GameState {
    private boolean pause;

    //
    // Main game
    //
    // game state manager
    private Player player;
    private Image skullPlayerdead;

    private TileMap tileMap;

    private GunsManager gunsManager;

    private float mouseX;
    private float mouseY;

    // ingame huds
    private HealthBar healthBar;
    private ArmorBar armorBar;
    private MiniMap miniMap;
    private cz.Empatix.Render.Hud.Image coin;

    private EnemyManager enemyManager;

    // post processing
    private Framebuffer objectsFramebuffer;
    private Framebuffer pauseBlurFramebuffer;
    private Framebuffer fadeFramebuffer;
    private Fade fade;
    private GaussianBlur gaussianBlur;
    private LightManager lightManager;



    private ItemManager itemManager;
    //
    // Paused game
    //
    private Background pauseBackground;
    private MenuBar[] pauseBars;
    // bars types
    private final static int PAUSEEXIT = 2;
    private final static int PAUSESETTINGS = 1;
    private final static int PAUSERESUME = 0;

    private int soundMenuClick;
    private Source source;

    // pause time deltas
    private static long pauseTimeStarted;
    private static long pauseTimeEnded;



    InGame(GameStateManager gsm){
        this.gsm = gsm;
    }

    @Override
    void mouseReleased(int button) {
        if(pause){
            for(MenuBar bar : pauseBars){
                if(bar.intersects(mouseX,mouseY)){
                    int type = bar.getType();
                    source.play(soundMenuClick);
                    if(type == PAUSEEXIT){
                        player.cleanUp();
                        gsm.setState(GameStateManager.MENU);
                    } else if (type == PAUSERESUME){
                        pause = false;
                        gunsManager.stopShooting();
                        setCursor(Game.CROSSHAIR);
                    } else{
                        // TODO: save menu
                    }
                }
            }
        } else{
            gunsManager.stopShooting();
        }
    }

    @Override
    void mousePressed(int button) {
        gunsManager.startShooting();
    }

    @Override
    void keyReleased(int k) {
        if(player.isDead()){
            if(k == GLFW_KEY_SPACE){
                float time = (System.currentTimeMillis()-player.getDeathTime());
                if(time > 5500){
                    gsm.setState(GameStateManager.MENU);
                    glfwSetInputMode(Game.window,GLFW_CURSOR,GLFW_CURSOR_NORMAL);
                }
            }
            return;
        }
        if (k == GLFW_KEY_ESCAPE){
            pause = !pause;
            if(pause){
                setCursor(ARROW);
                pauseTimeStarted = System.currentTimeMillis();
            } else {
                setCursor(Game.CROSSHAIR);
                pauseTimeEnded += System.currentTimeMillis() - pauseTimeStarted;
            }
            gunsManager.stopShooting();
        }
        player.keyReleased(k);

        if(pause) return;

        if (k == 'R'){
            gunsManager.reload();
        }
    }

    @Override
    void keyPressed(int k) {
        if(player.isDead()) return;
        if(pause) return;
        float px = player.getX();
        float py = player.getY();
        float mx = tileMap.getX();
        float my = tileMap.getY();
        gunsManager.keyPressed(k,(int)(mouseX-mx-px),(int)(mouseY-my-py));

        player.keyPressed(k);

        if(k == GLFW.GLFW_KEY_E){
            itemManager.pickUpGun((int)(mouseX-mx-px),(int)(mouseY-my-py));
        }

        if (k == GLFW.GLFW_KEY_F1){
            Game.displayCollisions = !Game.displayCollisions;
        }
    }

    @Override
    void init() {
        pause = false;

        objectsFramebuffer = new Framebuffer();
        pauseBlurFramebuffer = new Framebuffer();
        fadeFramebuffer = new Framebuffer();
        lightManager = new LightManager();
        fade = new Fade("shaders\\fade");
        gaussianBlur = new GaussianBlur("shaders\\blur");


        setCursor(Game.CROSSHAIR);

        // Tile map
        tileMap = new TileMap(64);
        tileMap.loadTiles("Textures\\tileset64.tga");
        tileMap.loadMap();
        tileMap.setTween(0.10);

        // player
        player = new Player(tileMap);
        player.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());


        //bg = new Background("/testing.jpg/");

        // weapons
        gunsManager = new GunsManager(tileMap);
        // items drops
        itemManager = new ItemManager(tileMap,gunsManager,player);


        //health bar
        healthBar = new HealthBar("Textures\\healthBar",new Vector3f(250,125,0),5);
        //armor bar
        armorBar = new ArmorBar("Textures\\armorbar",new Vector3f(275,175,0),3);
        //minimap
        miniMap = new MiniMap();
        // coin
        coin = new Image("Textures\\coin.tga",new Vector3f(75,1000,0),1.5f);

        //audio
        AudioManager.playSoundtrack(Soundtrack.IDLE);

        enemyManager = new EnemyManager(player,tileMap);



        // pause menu
        pauseBackground = new Background("Textures\\Menu\\pausemenu.tga");
        pauseBackground.setDimensions(500,800);
        pauseBars = new MenuBar[3];

        String defaultBar = "Textures\\Menu\\menu_bar.tga";
        MenuBar bar;
        bar = new MenuBar(defaultBar,new Vector3f(960,455,0),1.70f,200,100,true);
        bar.setType(PAUSERESUME);
        pauseBars[0] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(960,645,0),1.70f,200,100,true);
        bar.setType(PAUSESETTINGS);
        pauseBars[1] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(960,835,0),1.70f,200,100,true);
        bar.setType(PAUSEEXIT);
        pauseBars[2] = bar;

        source = new Source(Source.EFFECTS,0.35f);
        soundMenuClick = AudioManager.loadSound("menuclick.ogg");


        gunsManager.dropGun((int)player.getX(),(int)player.getY());

        skullPlayerdead = new Image("Textures\\skull.tga",new Vector3f(960,540,0),1f);
        skullPlayerdead.setAlpha(0f);

    }

    @Override
    void draw() {

        objectsFramebuffer.bindFBO();
        // clear framebuffer
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        tileMap.draw();

        itemManager.draw();

        player.draw();

        // draw enemies
        enemyManager.draw();

        gunsManager.draw();

        objectsFramebuffer.unbindFBO();
        if(player.isDead()){
            fadeFramebuffer.bindFBO();
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT);
        }
        else if(pause){
            pauseBlurFramebuffer.bindFBO();
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT);
        }


        lightManager.draw(objectsFramebuffer);

        if (Game.displayCollisions){
            TextRender.renderText("X: "+(int)player.getX(),new Vector3f(1650,200,0),3,new Vector3f(1.0f,1.0f,1.0f));
            TextRender.renderText("Y: "+(int)player.getY(),new Vector3f(1650,300,0),3,new Vector3f(1.0f,1.0f,1.0f));
        }

        gunsManager.drawHud();

        player.drawVignette();

        healthBar.draw();
        armorBar.draw();
        //miniMap.draw(); //TODO: dodelat mapu
        coin.draw();
        TextRender.renderText(""+player.getCoins(),new Vector3f(170,1019,0),3,new Vector3f(1.0f,0.847f,0.0f));
        if(player.isDead()){
            fadeFramebuffer.unbindFBO();
            fade.draw(fadeFramebuffer);
            if(player.isDead()){
                skullPlayerdead.draw();
            }
            float time = (System.currentTimeMillis()-player.getDeathTime());
            if(time > 3800){
                TextRender.renderText("GAME OVER",new Vector3f( 800,340,0),5,new Vector3f((time-3800)/2000,(time-3800)/2000,(time-3800)/2000));
                glLineWidth(3f);
                glBegin(GL_LINES);
                float first = 960-(time-3800)/2.5f;
                float secondary = 960+(time-3800)/2.5f;
                if(first<480) first=480;
                if(secondary>1440) secondary=1440;
                glVertex2f(secondary, 380);
                glVertex2f(first, 380);
                glEnd();
            }
            if(time > 5500){
                if(System.currentTimeMillis() / 500 % 2 == 0) {
                    TextRender.renderText("Press space to continue...",new Vector3f(1500,1000,0),2,new Vector3f(1f,1f,1f));

                }
            }

        }
        else if(pause){
            pauseBlurFramebuffer.unbindFBO();
            gaussianBlur.draw(pauseBlurFramebuffer);
            pauseBackground.draw();
            for(MenuBar bar: pauseBars){
                bar.draw();
            }
            TextRender.renderText("Pause",new Vector3f(925,300,0),7,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText("Resume",new Vector3f(905,465,0),4,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText("Save",new Vector3f(955,655,0),4,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText("Exit",new Vector3f(975,845,0),4,new Vector3f(0.874f,0.443f,0.149f));

        }

    }



    @Override
    void update() {
        if(player.isDead()){
            enemyManager.update();
            fade.update();
            player.update();
            float time = (System.currentTimeMillis()-player.getDeathTime());
            if(time > 2000){
                Vector3f pos = skullPlayerdead.getPos();
                float y = pos.y() + (140-pos.y()) * time/40000;
                Vector3f newpos = new Vector3f(pos.x(),y,0);
                skullPlayerdead.setPosition(newpos);
            }
            skullPlayerdead.setAlpha(time/4500f);
            return;
        }
        // loc of mouse
        final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        mouseX = mouseLoc.x* Settings.scaleMouseX();
        mouseY = mouseLoc.y*Settings.scaleMouseY();

        // mouse location-moving direction of mouse of tilemap
        tileMap.setPosition(
                tileMap.getX()-(mouseX-960)/30,
                tileMap.getY()-(mouseY- 540)/30);

        // updating player
        // updating tilemap by player position
        tileMap.setPosition(
                Camera.getWIDTH() / 2f - player.getX(),
                Camera.getHEIGHT() / 2f - player.getY()
        );

        if(pause){
            for(MenuBar hud:pauseBars){
                hud.setClick(false);
                if(hud.intersects(mouseX,mouseY)){
                    hud.setClick(true);
                }
            }
        } else {

            ArrayList<Enemy> enemies = enemyManager.getEnemies();

            player.update();

            // updating if player entered some another room
            tileMap.updateCurrentRoom(
                    (int) player.getX(),
                    (int) player.getY()
            );

            // updating bullets(ammo)
            float px = player.getX();
            float py = player.getY();
            float mx = tileMap.getX();
            float my = tileMap.getY();
            gunsManager.shot(mouseX-mx-px,
                    mouseY-my-py,
                    px,
                    py);
            gunsManager.update();
            // updating if player shoots any enemies
            enemyManager.update();

            // check bullet collision with enemies
            gunsManager.checkCollisions(enemies);
            player.checkCollision(enemies);

            healthBar.update(player.getHealth(), player.getMaxHealth());
            armorBar.update(player.getArmor(),player.getMaxArmor());

            itemManager.update();
        }

        gaussianBlur.update(pause);
        lightManager.update();

    }

    /**
     *
     * @return returns time delta in ms
     */
    public static long deltaPauseTime(){
        return pauseTimeEnded ;
    }

}

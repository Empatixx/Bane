package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Graphics.Framebuffer;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Hud.HealthBar;
import cz.Empatix.Render.Hud.MenuBar;
import cz.Empatix.Render.Lightning.LightManager;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.opengl.GL11.*;


public class InGame extends GameState {
    private boolean pause;

    //
    // Main game
    //
    // game state manager
    private Player player;

    private TileMap tileMap;

    private GunsManager gunsManager;

    private float mouseX;
    private float mouseY;

    private HealthBar healthBar;

    private EnemyManager enemyManager;

    private Framebuffer objectsFramebuffer;

    private LightManager lightManager;
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





    InGame(GameStateManager gsm,Camera c){
        super(c);
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
                        gsm.setState(GameStateManager.MENU);
                    } else if (type == PAUSERESUME){
                        pause = false;
                    } else{
                        // TODO: save menu
                    }
                }
            }
        } else {
            float px = player.getX();
            float py = player.getY();
            float mx = tileMap.getX();
            float my = tileMap.getY();
            gunsManager.shot(mouseX-mx-px,
                    mouseY-my-py,
                    px,
                    py);
        }
    }

    @Override
    void mousePressed(int button) {
    }

    @Override
    void keyReleased(int k) {

        if (k == GLFW_KEY_ESCAPE){
            pause = !pause;
            if(pause){
                Game.setCursor(Game.ARROW);
            } else {
                Game.setCursor(Game.CROSSHAIR);
            }
        }
        player.keyReleased(k);

        if(pause) return;

        if (k == 'R'){
            gunsManager.reload();
        }
    }

    @Override
    void keyPressed(int k) {

        gunsManager.keyPressed(k);

        player.keyPressed(k);

        if (k == GLFW.GLFW_KEY_F1){
            Game.displayCollisions = !Game.displayCollisions;
        }
    }

    @Override
    void init() {
        pause = false;

        objectsFramebuffer = new Framebuffer();
        lightManager = new LightManager();

        Game.setCursor(Game.CROSSHAIR);

        // Tile map
        tileMap = new TileMap(64, camera);
        tileMap.loadTiles("Textures\\tileset64.tga");
        tileMap.loadMap();
        tileMap.setTween(0.10);
        // player
        player = new Player(tileMap, camera);
        player.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());

        //bg = new Background("/testing.jpg/");

        // weapons
        gunsManager = new GunsManager(tileMap);


        //health bar
        healthBar = new HealthBar("Textures\\healthBar",new Vector3f(250,125,0),5,camera);

        //audio
        AudioManager.playSoundtrack(Soundtrack.IDLE);

        enemyManager = new EnemyManager(player,tileMap);



        // pause menu
        pauseBackground = new Background("Textures\\Menu\\pausemenu.tga",camera);
        pauseBackground.setDimensions(500,800);
        pauseBars = new MenuBar[3];

        String defaultBar = "Textures\\Menu\\menu_bar.tga";
        MenuBar bar;
        bar = new MenuBar(defaultBar,new Vector3f(960,455,0),1.70f,camera,200,100,true);
        bar.setType(PAUSERESUME);
        pauseBars[0] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(960,645,0),1.70f,camera,200,100,true);
        bar.setType(PAUSESETTINGS);
        pauseBars[1] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(960,835,0),1.70f,camera,200,100,true);
        bar.setType(PAUSEEXIT);
        pauseBars[2] = bar;

        source = new Source(Source.EFFECTS,0.35f);
        soundMenuClick = AudioManager.loadSound("menuclick.ogg");
    }

    @Override
    void draw() {
        //bg.draw(g);

        objectsFramebuffer.bindFBO();
        // clear framebuffer
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        tileMap.draw();


        player.draw(camera);

        // draw enemies
        enemyManager.draw(camera);

        gunsManager.draw(camera);

        objectsFramebuffer.unbindFBO();

        lightManager.draw(objectsFramebuffer);

        if (Game.displayCollisions){
            TextRender.renderText(camera,"X: "+(int)player.getX(),new Vector3f(1650,200,0),3,new Vector3f(1.0f,1.0f,1.0f));
            TextRender.renderText(camera,"Y: "+(int)player.getY(),new Vector3f(1650,300,0),3,new Vector3f(1.0f,1.0f,1.0f));
        }

        gunsManager.drawHud(camera);

        player.drawVignette();

        healthBar.draw();


        if(pause){
            pauseBackground.draw();
            for(MenuBar bar: pauseBars){
                bar.draw();
            }
            TextRender.renderText(camera,"Pause",new Vector3f(925,300,0),7,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText(camera,"Resume",new Vector3f(905,465,0),4,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText(camera,"Save",new Vector3f(955,655,0),4,new Vector3f(0.874f,0.443f,0.149f));
            TextRender.renderText(camera,"Exit",new Vector3f(975,845,0),4,new Vector3f(0.874f,0.443f,0.149f));

        }

    }



    @Override
    void update() {
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
            lightManager.update();

            // updating if player entered some another room
            tileMap.updateCurrentRoom(
                    (int) player.getX(),
                    (int) player.getY()
            );

            // updating bullets(ammo)
            gunsManager.update();
            // updating if player shoots any enemies
            enemyManager.update();

            // check bullet collision with enemies
            gunsManager.checkCollisions(enemies);
            player.checkCollision(enemies);

            healthBar.update(player.getHealth(), player.getMaxHealth());
        }

    }

}

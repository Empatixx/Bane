package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Graphics.Framebuffer;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Hud.HealthBar;
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

    InGame(GameStateManager gsm,Camera c){
        super(c);
        this.gsm = gsm;
    }

    @Override
    void mouseReleased(int button) {
        if(pause) return;


        float px = player.getX();
        float py = player.getY();
        float mx = tileMap.getX();
        float my = tileMap.getY();
        gunsManager.shot(mouseX-mx-px,
                mouseY-my-py,
                px,
                py);
    }

    @Override
    void mousePressed(int button) {
    }

    @Override
    void keyReleased(int k) {
        if (k == GLFW_KEY_ESCAPE){
            pause = !pause;
        }

        if(pause) return;


        player.keyReleased(k);
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



    }



    @Override
    void update() {
        if(pause) return;
        ArrayList<Enemy> enemies = enemyManager.getEnemies();

        // loc of mouse
        final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        mouseX = mouseLoc.x* Settings.scaleMouseX();
        mouseY = mouseLoc.y*Settings.scaleMouseY();

        player.update();
        lightManager.update();

        // updating if player entered some another room
        tileMap.updateCurrentRoom(
                (int)player.getX(),
                (int)player.getY()
        );

        // updating player
        // updating tilemap by player position
        tileMap.setPosition(
                Camera.getWIDTH() / 2f - player.getX(),
                Camera.getHEIGHT() / 2f - player.getY()
        );

        // mouse location-moving direction of mouse of tilemap
        tileMap.setPosition(
                tileMap.getX()-(mouseX-960)/30,
                tileMap.getY()-(mouseY- 540)/30);


        // updating bullets(ammo)
        gunsManager.update();
        // updating if player shoots any enemies
        enemyManager.update();

        // check bullet collision with enemies
        gunsManager.checkCollisions(enemies);
        player.checkCollision(enemies);

        healthBar.update(player.getHealth(),player.getMaxHealth());

    }

}

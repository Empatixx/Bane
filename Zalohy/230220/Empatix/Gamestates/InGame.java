package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Hud.HealthBar;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;


public class InGame extends GameState {
    // game state manager
    private Player player;

    //private Background bg;

    private TileMap tileMap;


    private ArrayList<Enemy> enemies;

    private GunsManager gunsManager;

    private float mouseX;
    private float mouseY;

    private HealthBar healthBar;

    InGame(GameStateManager gsm,Camera c){
        super(c);
        this.gsm = gsm;
        //init();
    }

    @Override
    void mouseReleased(int button) {
        float px = player.getX();
        float py = player.getY();
        float mx = tileMap.getx();
        float my = tileMap.gety();
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
        player.keyReleased(k);
        if (k == 'R'){
            gunsManager.reload();
        }
    }

    @Override
    void keyPressed(int k) {
        player.keyPressed(k);
        if (k == GLFW.GLFW_KEY_F1){
            Game.displayCollisions = !Game.displayCollisions;
        }
    }

    @Override
    void init() {
        // Tile map
        tileMap = new TileMap(64, this, camera);
        tileMap.loadTiles("Textures\\tileset64.tga");
        tileMap.loadMap();
        //tileMap.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());
        tileMap.setTween(0.10);
        // player
        player = new Player(tileMap, camera);
        player.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());

        //bg = new Background("/testing.jpg/");

        enemies = new ArrayList<>();

        // weapons
        gunsManager = new GunsManager(tileMap);


        //health bar
        healthBar = new HealthBar("Textures\\healthBar",new Vector3f(250,125,0),5,camera);

        /*int buffer = AudioManager.loadSound("sound.ogg");
        Source source = new Source();
        source.setVolume(0.5f);dddd
        source.setLooping(true);
        source.play(buffer);*/

        //audio
        AudioManager.playSoundtrack(Soundtrack.IDLE);
    }

    @Override
    void draw() {
        //bg.draw(g);

        tileMap.draw();


        player.draw(camera);

        // draw enemies
        for(Enemy e : enemies){
            e.draw(camera);
        }

        gunsManager.draw(camera);
        if (Game.displayCollisions){
            TextRender.renderText(camera,"X: "+(int)player.getX(),new Vector3f(1650,200,0),3,new Vector3f(1.0f,1.0f,1.0f));
            TextRender.renderText(camera,"Y: "+(int)player.getY(),new Vector3f(1650,300,0),3,new Vector3f(1.0f,1.0f,1.0f));
        }


        healthBar.draw();


    }



    @Override
    void update() {

        // loc of mouse
        final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        mouseX = mouseLoc.x;
        mouseY = mouseLoc.y;

        player.update();


        // updating if player entered some another room
        tileMap.updateCurrentRoom(
                (int)player.getX(),
                (int)player.getY()
        );

        // updating player
        // updating tilemap by player position
        tileMap.setPosition(
                Game.WIDTH / 2f - player.getX(),
                Game.HEIGHT / 2f - player.getY()
        );

        // mouse location-moving direction of mouse of tilemap
        tileMap.setPosition(
                tileMap.getx()-(mouseX-960)/30,
                tileMap.gety()-(mouseY- 540)/30);


        // updating bullets(ammo)
        gunsManager.update();
        // updating if player shoots any enemies
        // check bullet collision with enemies
        gunsManager.checkCollisions(enemies);
        player.checkCollision(enemies);

        // updating enemies
        for(int i = 0;i < enemies.size();i++){
            enemies.get(i).update();
            // checking if enemy is dead
            if (enemies.get(i).isDead()){
                enemies.remove(i);
                i--;
            }
        }
        healthBar.update(player.getHealth(),player.getMaxHealth());

    }
    public void addHostile(Enemy e){
        enemies.add(e);
    }

    public TileMap getTileMap() { return tileMap; }

    public Player getPlayer() { return player; }

}

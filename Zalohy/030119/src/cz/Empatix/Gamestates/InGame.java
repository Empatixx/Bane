package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.TileMap;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

public class InGame extends GameState {
    // game state manager
    private Player player;

    //private Background bg;

    private TileMap tileMap;


    private ArrayList<Enemy> enemies;

    private Camera camera;

    InGame(){
        init();
    }

    @Override
    void mouseReleased(int button) {
        player.setShooting(false);
    }

    @Override
    void mousePressed(int button) {
        player.setShooting(true);
    }

    @Override
    void keyReleased(int k) {
        player.keyReleased(k);
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
        camera = new Camera(1920,1080);
        // Tile map
        tileMap = new TileMap(64, this, camera);
        tileMap.loadTiles("Textures\\tileset64.tga");
        tileMap.loadMap();
        tileMap.setPosition(0, 0);
        tileMap.setTween(0.10);

        // player
        player = new Player(tileMap);
        player.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());

        //bg = new Background("/testing.jpg/");

        enemies = new ArrayList<>();

        AudioManager.init();
        AudioManager.setListenerData(0,0);

        AudioManager.playSoundtrack(Soundtrack.IDLE);

        /*int buffer = AudioManager.loadSound("sound.ogg");
        Source source = new Source();
        source.setVolume(0.5f);
        source.setLooping(true);
        source.play(buffer);*/


    }

    @Override
    void draw() {
        //bg.draw(g);

        tileMap.draw();

        player.draw(camera);

        //Font titleFont = new Font("Century Gothic",Font.PLAIN,12);

        // draw enemies
        for(Enemy e : enemies){
            e.draw(camera);
        }
    }



    @Override
    void update() {
        // loc of mouse
        final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        final float mouseX = mouseLoc.x;
        final float mouseY = mouseLoc.y;

        player.update(mouseX,mouseY);


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
                tileMap.getx()-(mouseX - 960)/30,
                tileMap.gety()-(mouseY - 540)/30);


        // updating if player shoots any enemies
        player.checkAttack(enemies);

        // updating enemies
        for(int i = 0;i < enemies.size();i++){
            enemies.get(i).update();
            // checking if enemy is dead
            if (enemies.get(i).isDead()){
                enemies.remove(i);
                i--;
            }
        }
    }
    public void addHostile(Enemy e){
        enemies.add(e);
    }

    public TileMap getTileMap() { return tileMap; }

    public Player getPlayer() { return player; }
}

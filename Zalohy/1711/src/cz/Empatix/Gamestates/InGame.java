package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.TileMap;

import java.awt.*;
import java.util.ArrayList;

public class InGame extends GameState {
    // game state manager
    private Player player;

    //private Background bg;

    private TileMap tileMap;


    private ArrayList<Enemy> enemies;

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

    }

    @Override
    void init() {
        // Tile map
        tileMap = new TileMap(64, this);
        tileMap.loadTiles("/Tilesets/tileset64.png");
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
        source.play(buffer);
        */
    }

    @Override
    void draw() {
        //bg.draw(g);

        tileMap.draw();

        player.draw();

        //Font titleFont = new Font("Century Gothic",Font.PLAIN,12);

        // draw enemies
        for(Enemy e : enemies){
            e.draw();
        }
    }



    @Override
    void update() {
        // loc of mouse
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        double mouseX = mouseLoc.x;
        double mouseY = mouseLoc.y;

        player.update(mouseX,mouseY);


        // updating if player entered some another room
        tileMap.updateCurrentRoom(
                (int)player.getX(),
                (int)player.getY()
        );

        // updating player
        // updating tilemap by player position
        tileMap.setPosition(
                Game.WIDTH / 2 - player.getX(),
                Game.HEIGHT / 2 - player.getY()
        );
        // mouse location-moving direction of mouse of tilemap
        tileMap.setPosition(
                tileMap.getx()-(mouseX - 960)/30,
                tileMap.gety()-(mouseY - 540)/30);


        // updating if player shooted any enemies
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

package Gamestates;

import Entity.Enemies.Slime;
import Entity.Enemy;
import Entity.Player;
import Main.Gamepanel;
import Render.Background;
import Render.TileMap;

import java.awt.*;
import java.util.ArrayList;

import static Main.Gamepanel.mouseX;
import static Main.Gamepanel.mouseY;

public class InGame extends GameState {

    // game state manager
    public static Player player;


    private Background bg;


    private TileMap tileMap;


    private ArrayList<Enemy> enemies;


    InGame(GameStateManager gsm){
        this.gsm = gsm;
        init();
    }

    @Override
    public void mouseReleased(int button) {
        player.setShooting(false);
    }

    @Override
    public void mousePressed(int button) {
        player.setShooting(true);
    }

    @Override
    public void keyReleased(int k) {
        player.keyReleased(k);
    }

    @Override
    public void keyPressed(int k) {
        player.keyPressed(k);

    }

    @Override
    public void init() {
        // Tile map
        tileMap = new TileMap(64);
        tileMap.loadTiles("/Tilesets/tileset64.png");
        tileMap.loadMap("/Map/test.map");
        tileMap.setPosition(0, 0);
        tileMap.setTween(0.1);

        // player
        player = new Player(tileMap);
        player.setPosition(160,90);

        bg = new Background("/testing.jpg/");

        enemies = new ArrayList<>();
        Slime slime;
        slime = new Slime(tileMap, player);
        slime.setPosition(100,100);
        enemies.add(slime);
    }

    @Override
    public void draw(Graphics2D g) {
        bg.draw(g);

        tileMap.draw(g);

        player.draw(g);

        Color titleColor = new Color(0,0,0);
        Font titleFont = new Font("Century Gothic",Font.PLAIN,12);
        g.setColor(titleColor);
        g.setFont(titleFont);
        g.drawString("X: "+(int)player.getX(),10,10);
        g.drawString("Y: "+(int)player.getY(),10,20);
        g.drawString("FPS: "+ Gamepanel.FPS,10,50);

        // draw enemies
        for(int i = 0;i < enemies.size();i++){
            enemies.get(i).draw(g);
        }
    }



    @Override
    public void update() {
        player.update();
        // updating player
        // updating tilemap by player position
        tileMap.setPosition(
                Gamepanel.WIDTH / 2 - player.getX(),
                Gamepanel.HEIGHT / 2 - player.getY()
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
}

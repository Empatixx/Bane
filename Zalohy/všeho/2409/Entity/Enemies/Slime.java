package Entity.Enemies;

import Entity.Animation;
import Entity.Enemy;
import Entity.Player;
import Main.Gamepanel;
import Render.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Slime extends Enemy{
    private BufferedImage[] sprites;

    public Slime(TileMap tm, Player player) {

        super(tm,player);

        moveSpeed = 0.3;
        maxSpeed = 0.3;

        width = 64;
        height = 48;
        cwidth = 60;
        cheight = 46;

        health = maxHealth = 15;
        damage = 1;

        //dx = 1;
        type = melee;
        facingRight = true;

        collisionY = 12;

        // load sprites
        try {
            BufferedImage spritesheet;
            if (!Gamepanel.devMode){
                spritesheet = ImageIO.read(new File("Textures\\Sprites\\Enemies\\slime64.png"));
            } else {
                spritesheet = ImageIO.read(
                        getClass().getResourceAsStream(
                                "/Sprites/slime64.png"
                        )
                );
            }
            sprites = new BufferedImage[4];
            for(int i = 0; i < sprites.length; i++) {
                sprites[i] = spritesheet.getSubimage(
                        i * width,
                        0,
                        width,
                        height
                );
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        animation = new Animation();
        animation.setFrames(sprites);
        animation.setDelay(175);
    }

    private void getNextPosition() {

        // movement
        if(left) {
            dx -= moveSpeed;
            if(dx < -maxSpeed) {
                dx = -maxSpeed;
            }
        }
        else if(right) {
            dx += moveSpeed;
            if(dx > maxSpeed) {
                dx = maxSpeed;
            }
        }
        if(down) {
            dy += moveSpeed;
            if (dy > maxSpeed){
                dy = maxSpeed;
            }
        } else if (up){
            dy -= moveSpeed;
            if (dy < -maxSpeed){
                dy = -maxSpeed;
            }
        } else {
            if (dy < 0){
                dy += stopSpeed;
                if (dy > 0) dy = 0;
            } else if (dy > 0){
                dy -= stopSpeed;
                if (dy < 0) dy = 0;
            }
        }
    }

    public void update() {
        // ENEMY AI
        EnemyAI();

        // update position
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        // if it hits a wall, go other direction
        /*if(right && dx == 0) {
            right = false;
            left = true;
        }
        else if(left && dx == 0) {
            right = true;
            left = false;
        }*/

        // update animation
        animation.update();

        if (right) facingRight = true;
        if (left) facingRight = false;
    }

    public void draw(Graphics2D g) {

        //if(notOnScreen()) return;

        setMapPosition();

        super.draw(g);

    }

}


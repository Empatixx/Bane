package cz.Empatix.Entity;

import cz.Empatix.Main.Game;
import cz.Empatix.Render.ByteBufferImage;
import cz.Empatix.Render.TileMap;
import org.lwjgl.stb.STBImage;

import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;

public class Player extends MapObject {
    private boolean shooting;
    private boolean dead;


    // STUFF

    private int health;
    private int maxHealth;
    private int energy;
    private int maxEnergy;


    private static final int IDLE = 0;
    private static final int SIDE = 1;
    private static final int DOWN = 2;
    private static final int UP = 3;

    private ArrayList<Sprite[]> sprites;

    private ArrayList<Bullet> Bullets;

    public Player(TileMap tm) {
        super(tm);
        width = 32;
        height = 76;
        // spritesheet
        spriteSheetCols = 6;
        spriteSheetRows = 4;

        // COLLISION WIDTH/HEIGHT
        cwidth = 64;
        cheight = 64;

        moveSpeed = 0.34;
        maxSpeed = 5.92;
        stopSpeed = 0.8;

        Bullets = new ArrayList<>();

        health = maxHealth = 5;
        energy = maxEnergy = 100;

        shooting = false;
        dead = false;
        flinching = false;
        facingRight = true;

        final int[] numFrames = {
                6, 6, 6, 6
        };
        ByteBufferImage decoder = new ByteBufferImage();
        ByteBuffer spritesheet = decoder.decodeImage("Textures\\Sprites\\Player\\player64.tga");
        sprites = new ArrayList<>();

        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width*spriteSheetCols, height*spriteSheetRows, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheet);

        for(int i = 0; i < spriteSheetRows; i++) {

            Sprite[] images = new Sprite[numFrames[i]];

            for(int j = 0; j < numFrames[i]; j++) {

                float[] texCoords =
                        {
                                (float)j/spriteSheetCols,(float)i/spriteSheetRows,

                                (float)j/spriteSheetCols,(1f+i)/spriteSheetRows,

                                (1f+j)/spriteSheetCols,(1f+i)/spriteSheetRows,

                                (1f+j)/spriteSheetCols,(float)i/spriteSheetRows
                        };
                Sprite sprite = new Sprite(texCoords,id);

                images[j] = sprite;

            }

            sprites.add(images);

        }
        // free memory of stb alloc
        STBImage.stbi_image_free(spritesheet);


        currentAction = IDLE;

        animation = new Animation();
        animation.setFrames(sprites.get(IDLE));
        animation.setDelay(100);

    }

    public void checkAttack(ArrayList<Enemy> enemies){
        // check bullet collision with enemies
        for (int j = 0;j<enemies.size();j++){
            Enemy currentEnemy = enemies.get(j);
            for(int i = 0;i<Bullets.size();i++){
                if(Bullets.get(i).intersects(currentEnemy)) {
                    if (Bullets.get(i).isHit()) continue;
                    currentEnemy.hit(1);
                    Bullets.get(i).setHit();
                }
            }
            // check player X enemy collision
            if (intersects(currentEnemy)){
                hit(currentEnemy.getDamage());
            }
        }
    }

    public void update(double mx, double my) {
        // check if player is not dead
        // TODO: gamestate for dying
        if (health < 0){
            dead = true;
        }

        getMovementSpeed();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (right) {
            if (currentAction != SIDE) {
                currentAction = SIDE;
                animation.setFrames(sprites.get(SIDE));
                animation.setDelay(75);
            }
        } else if (left){
            if (currentAction != SIDE) {
                currentAction = SIDE;
                animation.setFrames(sprites.get(SIDE));
                animation.setDelay(75);
            }
        } else if (up) {
            if (currentAction != UP) {
                currentAction = UP;
                animation.setFrames(sprites.get(UP));
                animation.setDelay(50);
            }
        } else if (down) {
            if (currentAction != DOWN) {
                currentAction = DOWN;
                animation.setFrames(sprites.get(DOWN));
                animation.setDelay(75);
            }
        } else {
            if (currentAction != IDLE) {
                currentAction = IDLE;
                animation.setFrames(sprites.get(IDLE));
                animation.setDelay(100);
            }
        }

        // next sprite of player
        animation.update();

        // direction of player
        if (right) facingRight = true;
        if (left) facingRight = false;


        if (shooting) {

            shooting = false;
            mx = mx/2-xmap;
            my = my/2-ymap;
            Bullet fb = new Bullet(tileMap,
                    mx-x,
                    my-y);
            fb.setPosition(x, y);
            Bullets.add(fb);
        }

        for(int i = 0; i < Bullets.size(); i++) {
            Bullets.get(i).update();
            if(Bullets.get(i).shouldRemove()) {
                Bullets.remove(i);
                i--;
            }
        }

        //  NESMRTELNOST PO DOSTANI HITU
        if (flinching){
            if ((System.nanoTime()-flinchingTimer)/ 1000000000 > 2) {
                flinching = false;
            }
        }
    }

    private void getMovementSpeed() {
        // MAKING CHARACTER MOVE
        if (right){
            dx += moveSpeed;
            if (dx > maxSpeed){
                dx = maxSpeed;
            }
        }
        else if (left){
            dx -= moveSpeed;
            if (dx < -maxSpeed){
                dx = -maxSpeed;
            }
        }
        else {
            if (dx < 0){
                dx += stopSpeed;
                if (dx > 0) dx = 0;
            } else if (dx > 0){
                dx -= stopSpeed;
                if (dx < 0) dx = 0;
            }
        }

        if (up){
            dy -= moveSpeed;
            if (dy < -maxSpeed){
                dy = -maxSpeed;
            }
        }
        else if (down){
            dy += moveSpeed;
            if (dy > maxSpeed){
                dy = maxSpeed;
            }
        }
        else {
            if (dy < 0){
                dy += stopSpeed;
                if (dy > 0) dy = 0;
            } else if (dy > 0){
                dy -= stopSpeed;
                if (dy < 0) dy = 0;
            }
        }

    }


    public void draw() {
        setMapPosition();
        for(int i = 0; i < Bullets.size(); i++) {
            Bullets.get(i).draw();
        }

        super.draw();
    }
    public void keyPressed(int key) {
        if (key == 'W'){
            setUp(true);
        }
        if (key == 'D'){
            setRight(true);
        }
        if (key == 'A'){
            setLeft(true);
        }
        if (key == 'S'){
            setDown(true);
        }
        if (key == KeyEvent.VK_F1){
            if (Game.displayCollisions) {
                Game.displayCollisions = false;
            } else {
                Game.displayCollisions = true;
            }
        }
    }
    public void keyReleased(int key) {
        if (key == 'W'){
            setUp(false);
        }
        if (key == 'D'){
            setRight(false);
        }
        if (key == 'A'){
            setLeft(false);
        }
        if (key == 'S'){
            setDown(false);
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    public void hit(int damage){
        if (flinching) return;
        health -= damage;
        if (health < 0) health = 0;
        if (health == 0) dead = true;
        flinching = true;
        flinchingTimer = System.nanoTime();
    }
    public void setShooting(boolean b){
        shooting = b;
    }

}

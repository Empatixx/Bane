package cz.Empatix.Entity.Enemies;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.Sprite;
import cz.Empatix.Render.ByteBufferImage;
import cz.Empatix.Render.TileMap;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Slime extends Enemy {
    private Sprite[] sprites;

    public Slime(TileMap tm, Player player) {

        super(tm,player);



        moveSpeed = 0.3;
        maxSpeed = 0.8;

        width = 64;
        height = 48;
        cwidth = 64;
        cheight = 64;

        health = maxHealth = 15;
        damage = 1;

        type = melee;
        facingRight = true;

        collisionY = 12;

        spriteSheetCols = 4;
        spriteSheetRows = 1;

        // load sprites
        ByteBufferImage decoder = new ByteBufferImage();

        ByteBuffer spritesheet = decoder.decodeImage("Textures\\Sprites\\Enemies\\slime64.tga");
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width*spriteSheetCols, height*spriteSheetRows, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheet);
        sprites = new Sprite[4];
        for(int i = 0; i < sprites.length; i++) {
            float[] texCoords =
                    {
                            (float)i/spriteSheetCols,0,

                            (float)i/spriteSheetCols,1,

                            (1f+i)/spriteSheetCols,1,

                            (1f+i)/spriteSheetCols,0
                    };
            Sprite sprite = new Sprite(texCoords,id);
            sprites[i] = sprite;

        }
        // stb memory alloc free
        STBImage.stbi_image_free(spritesheet);

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

        // update animation
        animation.update();

        if (right) facingRight = true;
        if (left) facingRight = false;
    }

    public void draw() {

        //if(notOnScreen()) return;

        setMapPosition();

        super.draw();

    }
    
}


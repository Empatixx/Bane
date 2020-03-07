package cz.Empatix.Entity;

import cz.Empatix.Render.ByteBufferImage;
import cz.Empatix.Render.TileMap;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Bullet extends MapObject {

    // BASIC VARS
    private boolean hit;
    private boolean remove;

    // TEXTURE VARS
    private Sprite[] sprites;
    private Sprite[] hitSprites;


    public Bullet(TileMap tm, double x, double y) {

        super(tm);
        facingRight = true;


        width = 16;
        height = 16;

        cwidth = 16;
        cheight = 16;

        // load sprites
        spriteSheetCols = 4;
        spriteSheetRows = 2;

        double atan = Math.atan2(y,x);
        // 10 - speed of bullet
        dx = Math.cos(atan) * 10;
        dy = Math.sin(atan) * 10;

        ByteBufferImage decoder = new ByteBufferImage();

        ByteBuffer spritesheet = decoder.decodeImage("Textures\\Sprites\\Player\\bullet64.tga");

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

                            (float)i/spriteSheetCols,1f/spriteSheetRows,

                            (i+1f)/spriteSheetCols,1f/spriteSheetRows,

                            (i+1f)/spriteSheetCols,0
                    };
            Sprite sprite = new Sprite(texCoords,id);

            sprites[i] = sprite;

        }

        hitSprites = new Sprite[3];
        for(int i = 0; i < hitSprites.length; i++) {
            float[] texCoords =
                    {
                            (float)i/spriteSheetCols,1f/spriteSheetRows,

                            (float)i/spriteSheetCols,1,

                            (i+1f)/spriteSheetCols,1,

                            (i+1f)/spriteSheetCols,1f/spriteSheetRows
                    };
            Sprite sprite = new Sprite(texCoords,id);

            hitSprites[i] = sprite;

        }
        // free alloc of stb
        // stb memory alloc free
        STBImage.stbi_image_free(spritesheet);

        animation = new Animation();
        animation.setFrames(sprites);
        animation.setDelay(70);

    }

    public void setHit() {
        if(hit) return;
        hit = true;
        animation.setFrames(hitSprites);
        animation.setDelay(70);
        dx = 0;
        dy = 0;
    }

    public boolean shouldRemove() { return remove; }

    public void update() {

        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if((dx == 0 || dy == 0) && !hit) {
            setHit();
        }

        animation.update();
        if(hit && animation.hasPlayedOnce()) {
            remove = true;
        }

    }

    public void draw() {
        setMapPosition();

        super.draw();

    }
    public boolean isHit() {return hit;}

    @Override
    // finalize method is called on object once
    // before garbage collecting it
    protected void finalize()
    {
        System.out.println("Object garbage collected : " + this);
    }
}
package cz.Empatix.Entity;

import cz.Empatix.Render.ByteBufferImage;
import cz.Empatix.Render.TileMap;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Bullet extends MapObject {

    // BASIC VARS
    private boolean hit;
    private boolean remove;

    // TEXTURE VARS - int binding
    private int[] sprites;
    private int[] hitSprites;


    public Bullet(TileMap tm, double x, double y) {

        super(tm);


        width = 16;
        height = 16;

        cwidth = 16;
        cheight = 16;

        // load sprites

        double atan = Math.atan2(y,x);
        // 10 - speed of bullet
        dx = Math.cos(atan) * 10;
        dy = Math.sin(atan) * 10;

        try {
            BufferedImage spritesheet;

            spritesheet = ImageIO.read(new File("Textures\\Sprites\\Player\\bullet64.png"));

            sprites = new int[4];
            for(int i = 0; i < sprites.length; i++) {
                BufferedImage image = spritesheet.getSubimage(
                        i * width,
                        0,
                        width,
                        height
                );

                // GETTING RAW PIXELS OF IMAGE
                ByteBuffer buffer = new ByteBufferImage(image).decodeImage();

                int id = glGenTextures();

                glBindTexture(GL_TEXTURE_2D, id);

                glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
                glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);

                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

                sprites[i] = id;
                //free(buffer);

            }

            hitSprites = new int[3];
            for(int i = 0; i < hitSprites.length; i++) {
                BufferedImage image = spritesheet.getSubimage(
                        i * width,
                        height,
                        width,
                        height
                );
                int[] pixels;

                pixels = image.getRGB(0, 0, width, height, null, 0, width);

                ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); //4 for RGBA, 3 for RGB

                for (int y1 = 0; y1 < width; y1++) {
                    for (int x1 = 0; x1 < height; x1++) {
                        int pixel = pixels[y1 * width + x1];
                        buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                        buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                        buffer.put((byte) (pixel & 0xFF));               // Blue component
                        buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
                    }
                }

                buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

                // You now have a ByteBuffer filled with the color data of each pixel.
                // Now just create a texture ID and bind it. Then you can load it using
                // whatever OpenGL method you want, for example:

                int id = glGenTextures();

                glBindTexture(GL_TEXTURE_2D, id);

                glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
                glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);

                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

                hitSprites[i] = id;
            }

            animation = new Animation();
            animation.setFrames(sprites);
            animation.setDelay(70);

        }
        catch(Exception e) {
            e.printStackTrace();
        }

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
package Entity;

import Main.Gamepanel;
import Render.TileMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Bullet extends MapObject {

    private boolean hit;
    private boolean remove;
    private BufferedImage[] sprites;
    private BufferedImage[] hitSprites;


    public Bullet(TileMap tm, double x, double y) {

        super(tm);


        width = 16;
        height = 16;

        cwidth = 16;
        cheight = 16;

        // load sprites

        double atan = Math.atan2(y,x);
        dx = Math.cos(atan) *2;
        dy = Math.sin(atan) *2;

        try {
            BufferedImage spritesheet;
            if (!Gamepanel.devMode){
                spritesheet = ImageIO.read(new File("Textures\\Sprites\\Player\\bullet64.png"));
            } else {
                spritesheet = ImageIO.read(
                        getClass().getResourceAsStream(
                                "/kulka2.png"
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

            hitSprites = new BufferedImage[3];
            for(int i = 0; i < hitSprites.length; i++) {
                hitSprites[i] = spritesheet.getSubimage(
                        i * width,
                        height,
                        width,
                        height
                );
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

    public void draw(Graphics2D g) {
        setMapPosition();

        super.draw(g);

    }
    public boolean isHit() {return hit;}
}
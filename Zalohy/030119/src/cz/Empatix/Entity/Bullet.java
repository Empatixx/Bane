package cz.Empatix.Entity;

import cz.Empatix.Graphics.Model.ModelManager;
import cz.Empatix.Graphics.Shaders.ShaderManager;
import cz.Empatix.Graphics.Sprites.Sprite;
import cz.Empatix.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.TileMap;


public class Bullet extends MapObject {
    // SPRITE VARS
    private final static int sprites = 0;
    private final static int hitSprites = 1;

    // BASIC VARS
    private boolean hit;
    private boolean remove;

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
        // 20 - speed of bullet
        speed.x = (float)(Math.cos(atan) * 20);
        speed.y = (float)(Math.sin(atan) * 20);

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Player\\bullet64.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Player\\bullet64.tga");

            Sprite[] images = new Sprite[4];

            for(int i = 0; i < images.length; i++) {
                double[] texCoords =
                        {
                                (double)i/spriteSheetCols,0,

                                (double)i/spriteSheetCols,1.0/spriteSheetRows,

                                (i+1.0)/spriteSheetCols,1.0/spriteSheetRows,

                                (i+1.0f)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);

                images[i] = sprite;

            }
            spritesheet.addSprites(images);

            images = new Sprite[3];
            for(int i = 0; i < images.length; i++) {
                double[] texCoords =
                        {
                                (double)i/spriteSheetCols,1.0/spriteSheetRows,

                                (double) i/spriteSheetCols,1,

                                (i+1.0)/spriteSheetCols,1,

                                (i+1.0)/spriteSheetCols,1.0/spriteSheetRows
                        };
                Sprite sprite = new Sprite(texCoords);

                images[i] = sprite;

            }
            spritesheet.addSprites(images);
        }

        vboVertexes = ModelManager.getModel(width,height);
        if (vboVertexes == -1){
            vboVertexes = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(sprites));
        animation.setDelay(70);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader != null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        // because of scaling image by 2x
        width *= 2;
        height *= 2;
        cwidth *= 2;
        cheight *= 2;
    }

    public void setHit() {
        if(hit) return;
        hit = true;
        animation.setFrames(spritesheet.getSprites(hitSprites));
        animation.setDelay(70);
        speed.x = 0;
        speed.y = 0;
    }

    public boolean shouldRemove() { return remove; }

    public void update() {

        checkTileMapCollision();
        setPosition(temp.x, temp.y);

        if((speed.x == 0 || speed.y == 0) && !hit) {
            setHit();
        }

        animation.update();
        if(hit && animation.hasPlayedOnce()) {
            remove = true;
        }

    }

    public void draw(Camera camera) {
        setMapPosition();

        super.draw(camera);

    }
    public boolean isHit() {return hit;}

}
package cz.Empatix.Entity;

import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;

public class Shopkeeper extends RoomObject {
    private static final int IDLE = 0;

    private boolean reverse;
    public Shopkeeper(TileMap tm) {
        super(tm);
        if(tm.isServerSide()){
            width = 80;
            height = 80;
            cwidth = 80;
            cheight = 80;
            scale = 4;

            facingRight = true;

            animation = new Animation(10);
            animation.setDelay(170);

            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            reverse = false;
        } else {
            width = 80;
            height = 80;
            cwidth = 80;
            cheight = 80;
            scale = 4;

            facingRight = true;

            spriteSheetCols = 10;
            spriteSheetRows = 1;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\shopkeeper.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\shopkeeper.tga");
                Sprite[] sprites = new Sprite[10];
                for(int i = 0; i < sprites.length; i++) {
                    //Sprite sprite = new Sprite(texCoords);
                    Sprite sprite = new Sprite(5,i,0,width,height,spriteSheetRows,spriteSheetCols);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);

            }
            vboVertices = ModelManager.getModel(width,height);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,height);
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(IDLE));
            animation.setDelay(170);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 2x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            reverse = false;
        }
    }

    public void update() {
        setMapPosition();
        // update animation
        if(animation.getIndexOfFrame() == 9 && !reverse){
            animation.reverse();
            reverse = true;
        } else if(animation.getIndexOfFrame() == 0 && reverse){
            animation.unreverse();
            reverse = false;
        }
        animation.update();
        // update position
        checkTileMapCollision();
    }

    @Override
    public void draw() {
        super.draw();
    }

    @Override
    public void keyPress() {
    }
    @Override
    public void touchEvent(MapObject o) {
    }
}


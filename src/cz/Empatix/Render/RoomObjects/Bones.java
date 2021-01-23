package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;

public class Bones extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\bones.tga");
    }
    public Bones(TileMap tm){
        super(tm);
        width = 16;
        height = 16;
        cwidth = 16;
        cheight = 16;
        scale = 8;

        facingRight = true;
        flinching=false;

        spriteSheetCols = 1;
        spriteSheetRows = 2;

        collision = false;
        moveable=false;
        preDraw = true;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\bones.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\bones.tga");
            for(int j = 0;j < spriteSheetRows;j++){
                Sprite[] sprites = new Sprite[1];

                float[] texCoords =
                        {
                                    0, (float)j/spriteSheetRows,

                                    0, (float)(j+1)/spriteSheetRows,

                                    1, (float)(j+1)/spriteSheetRows,

                                    1, (float)j/spriteSheetRows,
                            };
                Sprite sprite = new Sprite(texCoords);
                sprites[0] = sprite;

                spritesheet.addSprites(sprites);

            }

        }
        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(Random.nextInt(2)));
        animation.setDelay(-1);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 8x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        remove = false;
    }

    @Override
    public void loadSave() {
        width = 16;
        height = 16;
        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\bones.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\bones.tga");
            for(int j = 0;j < spriteSheetRows;j++){
                Sprite[] sprites = new Sprite[1];

                float[] texCoords =
                        {
                                0, (float)j/spriteSheetRows,

                                0, (float)(j+1)/spriteSheetRows,

                                1, (float)(j+1)/spriteSheetRows,

                                1, (float)j/spriteSheetRows,
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[0] = sprite;

                spritesheet.addSprites(sprites);

            }

        }
        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(Random.nextInt(2)));
        animation.setDelay(-1);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 8x
        width *= scale;
        height *= scale;
    }

    public void update(){
        setMapPosition();

        animation.update();
    }

    @Override
    public void touchEvent() {
    }

    @Override
    public void draw() {
        // pokud neni object na obrazovce - zrusit
        if (isNotOnScrean()){
            return;
        }
        super.draw();
    }
    public boolean shouldRemove(){
        return remove;
    }
    @Override
    public void keyPress() {

    }
}

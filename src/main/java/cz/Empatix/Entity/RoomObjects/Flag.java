package cz.Empatix.Entity.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;

public class Flag extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\flag.tga");
    }
    public Flag(TileMap tm){
        super(tm);
        if(tm.isServerSide()) {
            width = 16;
            height = 16;
            cwidth = 16;
            cheight = 16;
            scale = 8;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 1;

            collision = false;
            moveable=false;
            preDraw = true;

            animation = new Animation(4);
            animation.setDelay(200);

            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            remove = false;
        }else{
            width = 16;
            height = 16;
            cwidth = 16;
            cheight = 16;
            scale = 8;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 1;

            collision = false;
            moveable=false;
            preDraw = true;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\flag.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\flag.tga");
                Sprite[] sprites = new Sprite[4];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (float) i/spriteSheetCols,0,

                                    (float)i/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,0
                            };
                    Sprite sprite = new Sprite(texCoords);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);

            }
            vboVertices = ModelManager.getModel(width,height);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,height);
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(0));
            animation.setDelay(200);

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
    }

    public void update(){
        setMapPosition();

        animation.update();
    }

    @Override
    public void touchEvent(MapObject o) {
    }

    @Override
    public void draw() {
        super.draw();
    }
    public boolean shouldRemove(){
        return remove;
    }
    @Override
    public void keyPress() {

    }
}

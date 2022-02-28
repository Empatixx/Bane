package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;

public class Barrel extends DestroyableObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\barrel.tga");
    }
    private static final int NORMAL = 0;
    private static final int HIT = 1;
    private static final int DESTROY = 2;

    private int currentAnimation;

    public Barrel(TileMap tm){
        super(tm);
        if(tm.isServerSide()){
            width = 64;
            height = 64;
            cwidth = 18;
            cheight = 20;
            scale = 6;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 2;

            collision = true;
            moveable=true;
            preDraw = true;
            behindCollision = true;

            itemDrop = true;

            maxHealth = health = 4;

            animation = new Animation(1);
            currentAnimation = NORMAL;
            animation.setDelay(-1);

            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            stopSpeed = 0.55f;
            maxMovement = 0.5f;
        } else {
            width = 64;
            height = 64;
            cwidth = 18;
            cheight = 20;
            scale = 6;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 2;

            collision = true;
            moveable=true;
            preDraw = true;
            behindCollision = true;


            itemDrop = true;

            maxHealth = health = 4;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\barrel.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\barrel.tga");
                Sprite[] sprites = new Sprite[1];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (float) i/spriteSheetCols,0,

                                    (float)i/spriteSheetCols,0.5f,

                                    (1.0f+i)/spriteSheetCols,0.5f,

                                    (1.0f+i)/spriteSheetCols,0
                            };
                    Sprite sprite = new Sprite(texCoords);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);

                sprites = new Sprite[3];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (float)i/spriteSheetCols,0,

                                    (float)i/spriteSheetCols,0.5f,

                                    (1.0f+i)/spriteSheetCols,0.5f,

                                    (1.0f+i)/spriteSheetCols,0
                            };
                    Sprite sprite = new Sprite(texCoords);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);

                sprites = new Sprite[4];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (float) i/spriteSheetCols,0.5f,

                                    (float) i/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,0.5f
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
            animation.setFrames(spritesheet.getSprites(NORMAL));
            currentAnimation = NORMAL;
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

            stopSpeed = 0.55f;
            maxMovement = 0.5f;
        }
    }
    @Override
    public void loadSave() {
        super.loadSave();
        width = 64;
        height = 64;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\barrel.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\barrel.tga");
            Sprite[] sprites = new Sprite[1];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float) i/spriteSheetCols,0,

                                (float)i/spriteSheetCols,0.5f,

                                (1.0f+i)/spriteSheetCols,0.5f,

                                (1.0f+i)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            sprites = new Sprite[3];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float)i/spriteSheetCols,0,

                                (float)i/spriteSheetCols,0.5f,

                                (1.0f+i)/spriteSheetCols,0.5f,

                                (1.0f+i)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float) i/spriteSheetCols,0.5f,

                                (float) i/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,0.5f
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
        animation.setFrames(spritesheet.getSprites(NORMAL));
        currentAnimation = NORMAL;
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
        if(tileMap.isServerSide() || !MultiplayerManager.multiplayer){
            checkTileMapCollision();
            checkRoomObjectsCollision();
            setPosition(temp.x, temp.y);
            stopping();
            sendMovePacket();
        }

        animation.update();
        if(currentAnimation == HIT && animation.hasPlayedOnce()){
            animation.setFrames(spritesheet.getSprites(NORMAL));
            animation.setDelay(-1);
            currentAnimation = NORMAL;
        }
        if(destroyed && animation.getIndexOfFrame() == 3){
            animation.setDelay(-1);
        }

    }
    public void stopping(){
        if (speed.x < 0){
            speed.x += stopSpeed;
            if (speed.x > 0) speed.x = 0;
        } else if (speed.x > 0){
            speed.x -= stopSpeed;
            if (speed.x < 0) speed.x = 0;
        }

        if (speed.y < 0){
            speed.y += stopSpeed;
            if (speed.y > 0) speed.y = 0;
        } else if (speed.y > 0){
            speed.y -= stopSpeed;
            if (speed.y < 0) speed.y = 0;
        }
    }

    @Override
    public void touchEvent(MapObject o) {

    }


    @Override
    public void draw() {
        super.draw();
    }
    @Override
    public void keyPress() {

    }

    @Override
    public void setHit(int damage) {
        boolean preDestroyed = isDestroyed();
        super.setHit(damage);
        if(destroyed && !preDestroyed){
            if(!tileMap.isServerSide()){
                animation.setFrames(spritesheet.getSprites(DESTROY));
                animation.setDelay(100);
            } else {
                animation = new Animation(4);
                animation.setDelay(100);
            }
            collision = false;
            moveable = false;
            preDraw=true;
            behindCollision=true;
            currentAnimation = DESTROY;
        } else if(currentAnimation == NORMAL) {
            if(!tileMap.isServerSide()){
                animation.setFrames(spritesheet.getSprites(HIT));
                animation.setDelay(100);
            }
            currentAnimation = HIT;
        }

    }

    @Override
    public boolean canDrop() {
        return animation.getIndexOfFrame() == 3 && destroyed && !itemAlreadyDropped;
    }

}

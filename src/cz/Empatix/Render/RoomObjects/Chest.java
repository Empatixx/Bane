package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Multiplayer.ItemManagerMP;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;

public class Chest extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\chest.tga");
    }
    private static final int IDLE = 0;
    private static final int OPEN = 1;

    private boolean opened;
    private boolean dropGun;
    private boolean dropArtefact;


    public Chest(TileMap tm){
        super(tm);
        if(tm.isServerSide()){
            width = 16;
            height = 16;
            cwidth = 16;
            cheight = 16;
            scale = 8;

            facingRight = true;
            flinching=false;

            opened = false;
            collision = true;
            moveable=true;
            preDraw = false;

            animation = new Animation(4);
            animation.setDelay(175);

            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            stopSpeed = 0.55f;
            maxMovement=1.5f;
            dropGun= false;
            dropArtefact = false;
        } else {
            width = 16;
            height = 16;
            cwidth = 16;
            cheight = 16;
            scale = 8;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 1;

            opened = false;
            collision = true;
            moveable=true;
            preDraw = false;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\chest.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\chest.tga");
                Sprite[] sprites = new Sprite[4];
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

                sprites = new Sprite[4];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (float) i/spriteSheetCols,0.5f,

                                    (float)i/spriteSheetCols,1,

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
            animation.setFrames(spritesheet.getSprites(IDLE));
            animation.setDelay(175);

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
            maxMovement=1.5f;
            dropGun= false;
            dropArtefact = false;
        }
    }

    @Override
    public void loadSave() {
        width = 16;
        height = 16;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\chest.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\chest.tga");
            Sprite[] sprites = new Sprite[4];
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

            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float) i/spriteSheetCols,0.5f,

                                (float)i/spriteSheetCols,1,

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
        animation.setFrames(spritesheet.getSprites(IDLE));
        animation.setDelay(175);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 8x
        width *= scale;
        height *= scale;
    }

    public void enableDropWeapon(){
        dropGun = true;
    }
    public void enableDropArtefact(){
        dropArtefact = true;
    }
    public void update(){
        setMapPosition();

        if(tileMap.isServerSide()){
            checkTileMapCollision();
            setPosition(temp.x, temp.y);
            if(opened && animation.hasPlayedOnce()){
                remove=true;

                Vector2f speed = new Vector2f();

                float x = (float) Random.nextDouble()*(-1+Random.nextInt(2)*2);
                float y = (float)Random.nextDouble()*(-1+Random.nextInt(2)*2);

                ItemManagerMP itemManager = ItemManagerMP.getInstance();

                if(dropGun) itemManager.dropWeapon((int)position.x,(int)position.y,speed);
                if(dropArtefact) itemManager.dropArtefact((int)position.x,(int)position.y);

                for(int i = 0;i<5;i++){
                    double atan = Math.atan2(x,
                            y) + 1.3 * i;
                    speed.x = (float)(Math.cos(atan) * 10);
                    speed.y = (float)(Math.sin(atan) * 10);

                    itemManager.createDrop((int)position.x,(int)position.y,speed);
                }
            }

        } else if (!MultiplayerManager.multiplayer){
            checkTileMapCollision();
            setPosition(temp.x, temp.y);
            if(opened && animation.hasPlayedOnce()){
                remove=true;

                Vector2f speed = new Vector2f();

                float x = (float) Random.nextDouble()*(-1+Random.nextInt(2)*2);
                float y = (float)Random.nextDouble()*(-1+Random.nextInt(2)*2);

                ItemManager itemManager = ItemManager.getInstance();

                if(dropGun) itemManager.dropWeapon((int)position.x,(int)position.y,speed);
                if(dropArtefact) itemManager.dropArtefact((int)position.x,(int)position.y);

                for(int i = 0;i<5;i++){
                    double atan = Math.atan2(x,
                            y) + 1.3 * i;
                    speed.x = (float)(Math.cos(atan) * 10);
                    speed.y = (float)(Math.sin(atan) * 10);

                    itemManager.createDrop((int)position.x,(int)position.y,speed);
                }
            }

        }
        animation.update();

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
        open();
    }

    public void open(){
        if(opened) return;
        opened = true;
        collision = false;
        moveable = false;
        if(tileMap.isServerSide()){
            animation = new Animation(4);
            animation.setDelay(175);
        } else {
            animation.setFrames(spritesheet.getSprites(OPEN));
        }
        speed.mul(1.7f);
    }

    @Override
    public void draw() {
        // pokud neni object na obrazovce - zrusit
        super.draw();
    }
    public boolean shouldRemove(){return remove;}
    @Override
    public void keyPress() {

    }
}

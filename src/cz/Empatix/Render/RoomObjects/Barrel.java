package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;

public class Barrel extends DestroyableObject {
    private static final int NORMAL = 0;
    private static final int HIT = 1;
    private static final int DESTROY = 2;

    private int currentAnimation;

    public Barrel(TileMap tm){
        super(tm);
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
        preDraw = false;
        speedMoveBoost = 0.9f;

        maxHealth = health = 4;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\barrel.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\barrel.tga");
            Sprite[] sprites = new Sprite[1];
            for(int i = 0; i < sprites.length; i++) {
                double[] texCoords =
                        {
                                (double) i/spriteSheetCols,0,

                                (double)i/spriteSheetCols,0.5,

                                (1.0+i)/spriteSheetCols,0.5,

                                (1.0+i)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            sprites = new Sprite[3];
            for(int i = 0; i < sprites.length; i++) {
                double[] texCoords =
                        {
                                (double) i/spriteSheetCols,0,

                                (double)i/spriteSheetCols,0.5,

                                (1.0+i)/spriteSheetCols,0.5,

                                (1.0+i)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                double[] texCoords =
                        {
                                (double) i/spriteSheetCols,0.5,

                                (double)i/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,0.5
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);
        }
        vboVerticles = ModelManager.getModel(width,height);
        if (vboVerticles == -1){
            vboVerticles = ModelManager.createModel(width,height);
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
    }

    public void update(){
        // set location by player collisions
        setPosition(temp.x, temp.y);

        setMapPosition();
        checkTileMapCollision();

        boolean[] collisionCheck = new boolean[tileMap.getRoomMapObjects().size()];
        checkRoomObjectsCollision(this,collisionCheck);

        setPosition(temp.x, temp.y);

        animation.update();
        if(currentAnimation == HIT && animation.hasPlayedOnce()){
            animation.setFrames(spritesheet.getSprites(NORMAL));
            animation.setDelay(-1);
            currentAnimation = NORMAL;
        }

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
    public void touchEvent() {

    }


    @Override
    public void draw() {
        if(animation.hasPlayedOnce() && destroyed) return;

        super.draw();
    }
    @Override
    public void keyPress() {

    }

    @Override
    public void setHit(int damage) {
        super.setHit(damage);
        if(destroyed){
            animation.setFrames(spritesheet.getSprites(DESTROY));
            collision = false;
            moveable = false;
            currentAnimation = DESTROY;
        } else if(currentAnimation == NORMAL) {
            animation.setFrames(spritesheet.getSprites(HIT));
            currentAnimation = HIT;
        }
        animation.setDelay(100);

        lastTimeDamaged = System.currentTimeMillis()- InGame.deltaPauseTime();

    }
    public boolean shouldRemove(){
        return animation.hasPlayedOnce() && destroyed;
    }



}

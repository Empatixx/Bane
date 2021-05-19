package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.ArrayList;


public class Bullet extends MapObject implements Serializable {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Player\\bullet64.tga");
    }

    // SPRITE VARS
    private final static int sprites = 0;
    private final static int hitSprites = 1;

    // BASIC VARS
    private boolean hit;
    private boolean remove;

    // audio
    private int soundWallhit;
    private int soundEnemyhit;

    private int damage;
    private boolean crit;

    private boolean friendlyFire;

    public Bullet(TileMap tm, double x, double y,double inaccuracy, int speed) {

        super(tm);
        facingRight = true;
        crit=false;

        width = 16;
        height = 16;

        cwidth = 16;
        cheight = 16;

        scale = 2;

        // load sprites
        spriteSheetCols = 4;
        spriteSheetRows = 2;

        double atan = Math.atan2(y,x) + inaccuracy;
        // 30 - speed of bullet
        this.speed.x = (float)(Math.cos(atan) * speed);
        this.speed.y = (float)(Math.sin(atan) * speed);

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Player\\bullet64.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Player\\bullet64.tga");

            Sprite[] images = new Sprite[4];

            for(int i = 0; i < images.length; i++) {
                float[] texCoords =
                        {
                                (float)i/spriteSheetCols,0,

                                (float)i/spriteSheetCols,1.0f/spriteSheetRows,

                                (i+1.0f)/spriteSheetCols,1.0f/spriteSheetRows,

                                (i+1.0f)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);

                images[i] = sprite;

            }
            spritesheet.addSprites(images);

            images = new Sprite[3];
            for(int i = 0; i < images.length; i++) {
                float[] texCoords =
                        {
                                (float)i/spriteSheetCols,1.0f/spriteSheetRows,

                                (float) i/spriteSheetCols,1,

                                (i+1.0f)/spriteSheetCols,1,

                                (i+1.0f)/spriteSheetCols,1.0f/spriteSheetRows
                        };
                Sprite sprite = new Sprite(texCoords);

                images[i] = sprite;

            }
            spritesheet.addSprites(images);
        }

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(sprites));
        animation.setDelay(70);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        // because of scaling image by 2x
        width *= 2;
        height *= 2;
        cwidth *= 2;
        cheight *= 2;

        // audio
        soundWallhit = AudioManager.loadSound("guns\\wallhit.ogg");
        soundEnemyhit = AudioManager.loadSound("guns\\enemyhit.ogg");
        source = AudioManager.createSource(Source.EFFECTS,0.35f);

        light = LightManager.createLight(new Vector3f(1.0f,0.0f,0.0f), new Vector2f((float)x+xmap,(float)y+ymap), 1.75f,this);
    }
    public void loadSave(){
        width = 16;
        height = 16;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Player\\bullet64.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Player\\bullet64.tga");

            Sprite[] images = new Sprite[4];

            for(int i = 0; i < images.length; i++) {
                float[] texCoords =
                        {
                                (float)i/spriteSheetCols,0,

                                (float)i/spriteSheetCols,1.0f/spriteSheetRows,

                                (i+1.0f)/spriteSheetCols,1.0f/spriteSheetRows,

                                (i+1.0f)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);

                images[i] = sprite;

            }
            spritesheet.addSprites(images);

            images = new Sprite[3];
            for(int i = 0; i < images.length; i++) {
                float[] texCoords =
                        {
                                (float)i/spriteSheetCols,1.0f/spriteSheetRows,

                                (float) i/spriteSheetCols,1,

                                (i+1.0f)/spriteSheetCols,1,

                                (i+1.0f)/spriteSheetCols,1.0f/spriteSheetRows
                        };
                Sprite sprite = new Sprite(texCoords);

                images[i] = sprite;

            }
            spritesheet.addSprites(images);
        }

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(sprites));
        animation.setDelay(70);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        // because of scaling image by 2x
        width *= 2;
        height *= 2;

        // audio
        soundWallhit = AudioManager.loadSound("guns\\wallhit.ogg");
        soundEnemyhit = AudioManager.loadSound("guns\\enemyhit.ogg");
        source = AudioManager.createSource(Source.EFFECTS,0.35f);

        light = LightManager.createLight(new Vector3f(1.0f,0.0f,0.0f), new Vector2f(position.x+xmap,position.y+ymap), 1.75f,this);
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setHit() {
        if(hit) return;
        hit = true;
        animation.setFrames(spritesheet.getSprites(hitSprites));
        animation.setDelay(70);
        speed.x = 0;
        speed.y = 0;
    }

    public int getDamage() {
        return damage;
    }

    public boolean shouldRemove() { return remove && !source.isPlaying(); }

    public void update() {
        setMapPosition();
        ArrayList<RoomObject> roomObjects = tileMap.getRoomMapObjects();
        for(RoomObject obj : roomObjects){
            if(this.intersects(obj) && obj.collision){
                setHit();
                break;
            }
        }
        checkTileMapCollision();
        setPosition(temp.x, temp.y);

        if((speed.x == 0 || speed.y == 0) && !hit) {
            source.play(soundWallhit);
            setHit();
        }
        if(remove && !source.isPlaying()){
            source.delete();
        }

        animation.update();
        if(hit) {
            if (animation.hasPlayedOnce()){
                remove = true;
                light.remove();
            } else {
                // decrease intensity every time we use next sprite of hitBullet
                light.setIntensity(1.5f-0.5f*animation.getIndexOfFrame());
            }
        }

    }

    public void draw() {
        if(source.isPlaying() && remove) return;
        super.draw();

    }
    public boolean isHit() {return hit;}

    public void playEnemyHit(){
        source.play(soundEnemyhit);
    }

    public boolean isCritical() {
        return crit;
    }

    public void setCritical(boolean crit) {
        this.crit = crit;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
        if(crit){
            damage/=2;
        }
        damage /= 2;
        if(damage < 1) damage = 1;
    }
    @Override
    public void calculateCorners(double x, double y) {
        // getting number of tile (row,collumn)

        int leftTile = (int) ((x - cwidth / 2) / tileSize);
        int rightTile = (int) ((x + cwidth / 2 - 1) / tileSize);
        int topTile = (int) ((y + tileSize/2 - cheight / 2) / tileSize);
        int bottomTile = (int) ((y + cheight / 2 - 1) / tileSize);


        // getting type of tile
        int tl = tileMap.getType(topTile, leftTile);
        int tr = tileMap.getType(topTile, rightTile);
        int bl = tileMap.getType(bottomTile, leftTile);
        int br = tileMap.getType(bottomTile, rightTile);

        // pokud tile má hodnotu 1 = collision
        topLeft = tl == Tile.BLOCKED;
        topRight = tr == Tile.BLOCKED;
        bottomLeft = bl == Tile.BLOCKED;
        bottomRight = br == Tile.BLOCKED;

    }

    /**
     * Calculating destinations and checking if there is any collision
     */
    @Override
    protected void checkTileMapCollision() {

        dest.x = position.x + speed.x;
        dest.y = position.y + speed.y;

        temp.x = position.x;
        temp.y = position.y;

        calculateCorners(position.x, dest.y);
        if(speed.y < 0) {
            if(topLeft || topRight) {
                speed.y = 0;
                if(tileSize < cheight/2) currRow = ((int)position.y - cheight / 2) / tileSize;
                else currRow = (int)(position.y+tileSize/2) / tileSize;
                temp.y = currRow * tileSize - tileSize/2 + cheight / 2;
            }
            else {
                temp.y += speed.y;
            }
        }
        if(speed.y > 0) {
            if(bottomLeft || bottomRight) {
                speed.y = 0;
                if(tileSize < cheight/2) currRow = ((int)position.y + cheight / 2 - 1) / tileSize;
                else currRow = (int)(position.y) / tileSize;
                temp.y = (currRow + 1) * tileSize - cheight / 2;
            }
            else {
                temp.y += speed.y;
            }
        }

        calculateCorners(dest.x, position.y);
        if(speed.x < 0) {
            if(topLeft || bottomLeft) {
                speed.x = 0;
                if(tileSize < cwidth/2) currCol = ((int)position.x - cwidth / 2) / tileSize;
                else currCol = (int)position.x / tileSize;

                temp.x = currCol * tileSize + cwidth / 2;
            }
            else {
                temp.x += speed.x;
            }
        }
        if(speed.x > 0) {
            if(topRight || bottomRight) {
                speed.x = 0;
                if(tileSize < cwidth/2) currCol = ((int)position.x + cwidth / 2 - 1) / tileSize;
                else currCol = (int)position.x / tileSize;

                temp.x = (currCol + 1) * tileSize - cwidth / 2;
            }
            else {
                temp.x += speed.x;
            }
        }
    }
}
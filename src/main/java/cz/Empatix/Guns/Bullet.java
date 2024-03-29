package cz.Empatix.Guns;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Multiplayer.MultiplayerManager;
import cz.Empatix.Multiplayer.GameServer;
import cz.Empatix.Multiplayer.Interpolator;
import cz.Empatix.Multiplayer.MPStatistics;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;


public class Bullet extends MapObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Player\\bullet64.tga");
    }

    // SPRITE VARS
    private final static int sprites = 0;
    private final static int hitSprites = 1;

    // BASIC VARS
    private boolean hit;
    private boolean remove;
    private boolean didHitEnemy;

    // audio
    private int soundWallhit;
    private int soundEnemyhit;

    private int damage;
    private boolean crit;

    private boolean friendlyFire;
    private int owner; // id connection of player who shooted this bullet

    public enum TypeHit {
        WALL,
        ENEMY,
        ROOMOBJECT,
        PLAYER
    }



    public Bullet(TileMap tm, float x, float y,double inaccuracy, int movementVelocity) {
        super(tm);
        this.movementVelocity = movementVelocity;
        this.moveAcceleration = 0;
        this.stopAcceleration = 0;

        if(tm.isServerSide()){
            facingRight = true;
            crit=false;

            width = 16;
            height = 16;

            cwidth = 16;
            cheight = 16;

            scale = 2;

            double atan = Math.atan2(y,x) + inaccuracy;

            // setting direction of bullet
            this.acceleration.x = (float)(Math.cos(atan));
            this.acceleration.y = (float)(Math.sin(atan));

            // because of scaling image by 2x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

        } else {
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
            // setting direction of bullet
            this.acceleration.x = (float)(Math.cos(atan));
            this.acceleration.y = (float)(Math.sin(atan));

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
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            // audio
            soundWallhit = AudioManager.loadSound("guns\\wallhit.ogg");
            soundEnemyhit = AudioManager.loadSound("guns\\enemyhit.ogg");
            source = AudioManager.createSource(Source.EFFECTS,0.35f);

            light = LightManager.createLight(new Vector3f(1.0f,0.0f,0.0f), new Vector2f(x+xmap,y+ymap), 1.75f,this);

        }

    }
    public Bullet(TileMap tm, int id) {

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

        speed.x = 1;
        speed.y = 1;
        acceleration.x = 1;
        acceleration.y = 1;

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
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        // audio
        soundWallhit = AudioManager.loadSound("guns\\wallhit.ogg");
        soundEnemyhit = AudioManager.loadSound("guns\\enemyhit.ogg");
        source = AudioManager.createSource(Source.EFFECTS,0.35f);

        light = LightManager.createLight(new Vector3f(1.0f,0.0f,0.0f), new Vector2f(xmap,ymap), 1.75f,this);
        this.id = id;

        interpolator = new Interpolator(this,1/30f);
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    /*
    mostly singleplayer method for setting bullet as hit, sending packet without any damage to object/enemy
    or bullet hitted player(not sending any damage)
     */
    public void setHit(TypeHit type) {
        if(hit) return;
        if(type == TypeHit.ENEMY) didHitEnemy = true;
        if(tileMap.isServerSide()){
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Server server = mpManager.server.getServer();
            Network.HitBullet hitBullet = new Network.HitBullet();
            mpManager.server.requestACK(hitBullet,hitBullet.idPacket);
            hitBullet.type = type;
            hitBullet.id = id;
            hitBullet.tick = GameServer.tick;
            server.sendToAllUDP(hitBullet);
        } else {
            if(type == TypeHit.WALL){
                source.play(soundWallhit);
            } else {
                source.play(soundEnemyhit);
            }
        }
        hit = true;
        if(!tileMap.isServerSide()){
            animation.setFrames(spritesheet.getSprites(hitSprites));
            animation.setDelay(70);
        }
        speed.x = 0;
        speed.y = 0;
        acceleration.x = 0;
        acceleration.y = 0;
    }
    /*
     setting bullet as hit, sending packet with damage to object/enemy
     functionality as setHit(par1) but with damage packet to MapObject
     */
    public void setHit(TypeHit type, int idHit) {
        if(hit) return;
        if(type == TypeHit.ENEMY) didHitEnemy = true;
        if(tileMap.isServerSide()){
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Server server = mpManager.server.getServer();
            Network.HitBullet hitBullet = new Network.HitBullet();
            mpManager.server.requestACK(hitBullet,hitBullet.idPacket);
            hitBullet.type = type;
            hitBullet.id = id;
            hitBullet.idHit = idHit;
            hitBullet.tick = GameServer.tick;
            server.sendToAllUDP(hitBullet);
            // increasing statistic of hit bullet so we can calculate accuracy
            if(owner != 0 && type != TypeHit.WALL){
                MPStatistics mpStatistics = mpManager.server.getMpStatistics();
                mpStatistics.addBulletHit(owner);
            }
        } else {
            if(type == TypeHit.WALL){
                source.play(soundWallhit);
            } else {
                source.play(soundEnemyhit);
            }
        }
        hit = true;
        if(!tileMap.isServerSide()){
            animation.setFrames(spritesheet.getSprites(hitSprites));
            animation.setDelay(70);
        } else {
            animation = new Animation(3);
            animation.setDelay(70);
        }
        speed.x = 0;
        speed.y = 0;
        acceleration.x = 0;
        acceleration.y = 0;
    }
    public int getDamage() {
        return damage;
    }

    public boolean shouldRemove() {
        if(tileMap.isServerSide()) return remove;
        else return remove && !source.isPlaying();
    }

    public void update() {
        if(tileMap.isServerSide()){
            ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
            for(ArrayList<RoomObject> objects : objectsArray) {
                if (objects == null) continue;
                for(RoomObject obj : objects){
                    if(this.intersects(obj) && obj.collision){
                        setHit(TypeHit.WALL);
                        break;
                    }
                }
            }
            getMovementSpeed();
            checkTileMapCollision();
            setPosition(temp.x, temp.y);

            if((speed.x == 0 || speed.y == 0) && !hit) {
                setHit(TypeHit.WALL);
            }

            if(hit) {
                remove = true;
            }

            Server server = MultiplayerManager.getInstance().server.getServer();
            Network.MoveBullet moveBullet = new Network.MoveBullet();
            moveBullet.x = position.x;
            moveBullet.y = position.y;
            moveBullet.id = id;
            moveBullet.tick = GameServer.tick;
            server.sendToAllUDP(moveBullet);

        } else {
            setMapPosition();
            if(!MultiplayerManager.multiplayer){
                ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
                for(ArrayList<RoomObject> objects : objectsArray) {
                    if (objects == null) continue;
                    for(RoomObject obj : objects){
                        if(this.intersects(obj) && obj.collision){
                            setHit(TypeHit.WALL);
                            break;
                        }
                    }
                }
                getMovementSpeed();
                checkTileMapCollision();
                setPosition(temp.x, temp.y);
            } else {
                if(!hit)interpolator.update(position.x,position.y);
            }
            if((speed.x == 0 || speed.y == 0) && !hit) {
                source.play(soundWallhit);
                setHit(TypeHit.WALL);
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

    }

    public void draw() {
        if((source.isPlaying() && remove ) ||
        (animation.hasPlayedOnce() && hit)) return;
        super.draw();

    }
    public void delete(){
        light.remove();
    }
    public boolean isHit() {return hit;}


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

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public boolean didHitEnemy() {
        return didHitEnemy;
    }

    public int getPlayerOwner() {
        return owner;
    }
    public void addInterpolationPosition(Network.MoveBullet p){
        interpolator.newUpdate(p.tick,new Vector3f(p.x,p.y,0));
    }
}
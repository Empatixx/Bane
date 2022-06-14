package cz.Empatix.Guns;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.RoomObjects.DestroyableObject;
import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.EnemyManagerMP;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Damageindicator.CombatIndicator;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.Room;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import cz.Empatix.Utility.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;


public class Grenadebullet extends MapObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Player\\explosion.tga");
    }
    // SPRITE VARS
    private final static int sprites = 0;
    private final static int explosion = 1;

    // BASIC VARS
    private boolean hit;
    private boolean remove;

    // audio
    private int soundhit;

    private int damage;
    private boolean crit;

    public long bulletShooted;

    public Grenadebullet(TileMap tm, double x, double y, double inaccuracy, int movementVelocity) {
        super(tm);
        this.movementVelocity = movementVelocity;
        this.moveAcceleration = 0;
        this.stopAcceleration = 1.25f;
        if(tm.isServerSide()){
            facingRight = true;

            width = 64;
            height = 64;

            cwidth = 32;
            cheight = 32;

            scale = 1;

            // load sprites
            spriteSheetCols = 12;
            spriteSheetRows = 2;

            double atan = Math.atan2(y,x) + inaccuracy;
            // direction of bullet
            this.acceleration.x = (float)(Math.cos(atan));
            this.acceleration.y = (float)(Math.sin(atan));

            // because of scaling image by 2x
            width *= 2;
            height *= 2;
            cwidth *= 2;
            cheight *= 2;
        }else {
            facingRight = true;

            width = 64;
            height = 64;

            cwidth = 32;
            cheight = 32;

            scale = 1;

            // load sprites
            spriteSheetCols = 12;
            spriteSheetRows = 2;

            double atan = Math.atan2(y,x) + inaccuracy;
            // direction of bullet
            this.acceleration.x = (float)(Math.cos(atan));
            this.acceleration.y = (float)(Math.sin(atan));

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Player\\explosion.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Player\\explosion.tga");

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

                images = new Sprite[12];

                for(int i = 0; i < images.length; i++) {
                    float[] texCoords =
                            {
                                    (float)i/spriteSheetCols,0.5f,

                                    (float)i/spriteSheetCols,1.0f,

                                    (i+1.0f)/spriteSheetCols,1.0f,

                                    (i+1.0f)/spriteSheetCols,0.5f
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
            soundhit = AudioManager.loadSound("guns\\explosion.ogg");
            source = AudioManager.createSource(Source.EFFECTS,0.35f);

            light = LightManager.createLight(new Vector3f(1.0f,0.0f,0.0f), new Vector2f((float)x+xmap,(float)y+ymap), 1.25f,this);
        }
        bulletShooted = System.currentTimeMillis() - InGame.deltaPauseTime();
    }
    public Grenadebullet(TileMap tm, int id) {
        super(tm);
        facingRight = true;

        width = 64;
        height = 64;

        cwidth = 32;
        cheight = 32;

        scale = 1;

        // load sprites
        spriteSheetCols = 12;
        spriteSheetRows = 2;
        
        speed.x = 1;
        speed.y = 1;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Player\\explosion.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Player\\explosion.tga");

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

            images = new Sprite[12];

            for(int i = 0; i < images.length; i++) {
                float[] texCoords =
                        {
                                (float)i/spriteSheetCols,0.5f,

                                (float)i/spriteSheetCols,1.0f,

                                (i+1.0f)/spriteSheetCols,1.0f,

                                (i+1.0f)/spriteSheetCols,0.5f
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
        soundhit = AudioManager.loadSound("guns\\explosion.ogg");
        source = AudioManager.createSource(Source.EFFECTS,0.35f);

        light = LightManager.createLight(new Vector3f(1.0f,0.0f,0.0f), new Vector2f(xmap,ymap), 1.25f,this);

        this.id = id;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setHit() {
        if(hit) return;
        hit = true;
        if(tileMap.isServerSide()){
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Server server = mpManager.server.getServer();
            Network.HitBullet hitBullet = new Network.HitBullet();
            mpManager.server.requestACK(hitBullet,hitBullet.idPacket);
            hitBullet.type = null;
            hitBullet.id = id;
            server.sendToAllUDP(hitBullet);
            setPosition(position.x,position.y-100);
            explosion();
            scale = 5;
        } else {
            source.play(soundhit);
            if(!MultiplayerManager.multiplayer){
                explosion();
            }
            setPosition(position.x,position.y-100);
            animation.setFrames(spritesheet.getSprites(explosion));
            animation.setDelay(50);
            light.setIntensity(0f);
            scale = 5;
        }
        speed.x = 0;
        speed.y = 0;
        acceleration.x = 0;
        acceleration.y = 0;
    }
    public void explosion(){
        if(tileMap.isServerSide()){
            EnemyManagerMP enemyManager = EnemyManagerMP.getInstance();
            ArrayList<Enemy> enemies = enemyManager.getEnemies();
            ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
            int size = 0;
            for (ArrayList<RoomObject> roomObjects : objectsArray) {
                if (roomObjects != null) size += roomObjects.size();
            }
            size += enemies.size();
            boolean[] typeHit = new boolean[size];
            int[] idHit = new int[size];
            int currIndex = 0;
            for(Enemy e : enemies){
                if(Math.abs(position.x-e.getX()) < 250 && Math.abs(position.y-e.getY()) < 250
                        && !e.isDead() && !e.isSpawning()){
                    e.hit(getDamage());
                    typeHit[currIndex] = true; // means it hitted enemy
                    idHit[currIndex] = e.getId();
                    currIndex++;
                }
            }
            for(ArrayList<RoomObject> objects : objectsArray) {
                if (objects == null) continue;
                for(RoomObject roomObject : objects){
                    if(roomObject instanceof DestroyableObject){
                        if(!((DestroyableObject) roomObject).isDestroyed()){
                            if(Math.abs(position.x-roomObject.getX()) < 250 && Math.abs(position.y-roomObject.getY()) < 250){
                                ((DestroyableObject) roomObject).setHit(getDamage());
                                typeHit[currIndex] = false; // means it hitted room object
                                idHit[currIndex] = roomObject.getId();
                                currIndex++;
                            }
                        }
                    }
                }
            }
            // reducing size of arrays - saving bandwidth in network
            boolean[] ftypeHit = Arrays.copyOf(typeHit,currIndex);
            int[] fidHit = Arrays.copyOf(idHit,currIndex);
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Server server = mpManager.server.getServer();
            Network.ExplosionDamage explosionDamage = new Network.ExplosionDamage();
            mpManager.server.requestACK(explosionDamage,explosionDamage.idPacket);
            explosionDamage.typeHit = ftypeHit;
            explosionDamage.idHit = fidHit;
            explosionDamage.damage = (byte)getDamage();
            explosionDamage.critical = crit;
            server.sendToAllUDP(explosionDamage);
        } else {
            EnemyManager enemyManager = EnemyManager.getInstance();
            for(Enemy e : enemyManager.getEnemies()){
                if(Math.abs(position.x-e.getX()) < 250 && Math.abs(position.y-e.getY()) < 250
                        && !e.isDead() && !e.isSpawning()){
                    e.hit(getDamage());
                    int cwidth = e.getCwidth();
                    int cheight = e.getCheight();
                    int x = -cwidth/4+ Random.nextInt(cwidth/2);
                    if(!isCritical()){
                        CombatIndicator.addDamageShow(damage,(int)e.getX()-x,(int)e.getY()-cheight/3
                                ,new Vector2f(-x/10f,-30f));
                    } else {
                        CombatIndicator.addCriticalDamageShow(damage,(int)e.getX()-x,(int)e.getY()-cheight/3
                                ,new Vector2f(-x/10f,-30f));
                    }
                }
            }
            ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
            for(ArrayList<RoomObject> objects : objectsArray) {
                if (objects == null) continue;
                for(RoomObject roomObject : objects){
                    if(roomObject instanceof DestroyableObject){
                        if(!((DestroyableObject) roomObject).isDestroyed()){
                            if(Math.abs(position.x-roomObject.getX()) < 250 && Math.abs(position.y-roomObject.getY()) < 250){
                                ((DestroyableObject) roomObject).setHit(getDamage());
                            }
                        }
                    }
                }
            }
        }
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
            if(!hit){
                long elapsed = System.currentTimeMillis() - InGame.deltaPauseTime() - bulletShooted;
                float delay = 1 - elapsed/750f;
                if(delay < 0) delay = 0;
                speed.x = movementVelocity * Game.deltaTime * acceleration.x * delay;
                speed.y = movementVelocity * Game.deltaTime * acceleration.y * delay;
            }
            checkTileMapCollision();
            setPosition(temp.x, temp.y);
            checkUnmovableCollisions(); // only for not movable room objects
            if((speed.x == 0 || speed.y == 0) && !hit) {
                setHit();
            }
            if(hit) {
                remove = true;
            } else {
                Server server = MultiplayerManager.getInstance().server.getServer();
                Network.MoveBullet moveBullet = new Network.MoveBullet();
                moveBullet.x = position.x;
                moveBullet.y = position.y;
                moveBullet.id = id;
                server.sendToAllUDP(moveBullet);
            }
        } else {
            setMapPosition();
            if(!MultiplayerManager.multiplayer){
                if(!hit){
                    long elapsed = System.currentTimeMillis() - InGame.deltaPauseTime() - bulletShooted;
                    float delay = 1 - elapsed/750f;
                    if(delay < 0) delay = 0;
                    speed.x = movementVelocity * Game.deltaTime * acceleration.x * delay;
                    speed.y = movementVelocity * Game.deltaTime * acceleration.y * delay;
                }
                checkTileMapCollision();
                checkUnmovableCollisions(); // only for not movable room objects
                setPosition(temp.x, temp.y);
            }

            if((speed.x == 0 || speed.y == 0) && !hit) {
                source.play(soundhit);
                setHit();
            }
            if(remove && !source.isPlaying()){
                source.delete();
            }

            animation.update();
            if(hit) {
                if (animation.hasPlayedOnce()) {
                    remove = true;
                    light.remove();
                }
            }
        }
    }

    public void draw() {
        if(animation.hasPlayedOnce() && remove) return;
        super.draw();

    }
    public boolean isHit() {return hit;}

    public boolean isCritical() {
        return crit;
    }
    public void setCritical(boolean crit){this.crit = crit;}

    private void checkUnmovableCollisions(){
        Room room = tileMap.getRoomByCoords(position.x,position.y);
        if(room == null) return;// grenade is not in any room
        ArrayList<RoomObject> roomObjects = room.getMapObjects();
        for(RoomObject object : roomObjects){
            if(object.intersects(this) && object.collision){
                setHit();
            }
        }
    }

}

package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Damageindicator.DamageIndicator;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;


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
    private final int soundhit;

    private long shotTime;
    private Vector2f originalspeed;

    private int damage;
    private boolean crit;

    public Grenadebullet(TileMap tm, double x, double y, double inaccuracy, int speed) {

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

        double atan = Math.atan2(y,x) + inaccuracy;
        // 30 - speed of bullet
        this.speed.x = (float)(Math.cos(atan) * speed);
        this.speed.y = (float)(Math.sin(atan) * speed);

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

        shotTime = System.currentTimeMillis() - InGame.deltaPauseTime();
        originalspeed = new Vector2f(this.speed.x,this.speed.y);

    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setHit() {
        if(hit) return;
        hit = true;
        for(Enemy e : EnemyManager.getEnemies()){
            if(Math.abs(position.x-e.getX()) < 250 && Math.abs(position.y-e.getY()) < 250
            && !e.isDead() && !e.isSpawning()){
                e.hit(getDamage());
                int cwidth = e.getCwidth();
                int cheight = e.getCheight();
                int x = -cwidth/4+ Random.nextInt(cwidth/2);
                if(!isCritical()){
                    DamageIndicator.addDamageShow(damage,(int)e.getX()-x,(int)e.getY()-cheight/3
                            ,new Vector2f(-x/25f,-1f));
                } else {
                    DamageIndicator.addCriticalDamageShow(damage,(int)e.getX()-x,(int)e.getY()-cheight/3
                            ,new Vector2f(-x/25f,-1f));
                }
            }
        }
        for(RoomObject roomObject : tileMap.getRoomMapObjects()){
            if(roomObject instanceof DestroyableObject){
                if(!((DestroyableObject) roomObject).isDestroyed()){
                    if(Math.abs(position.x-roomObject.getX()) < 250 && Math.abs(position.y-roomObject.getY()) < 250){
                        ((DestroyableObject) roomObject).setHit(getDamage());
                    }
                }
            }
        }
        animation.setFrames(spritesheet.getSprites(explosion));
        animation.setDelay(50);
        setPosition(position.x,position.y-100);
        light.setIntensity(0.f);
        scale = 5;
        speed.x = 0;
        speed.y = 0;
    }

    public int getDamage() {
        return damage;
    }

    public boolean shouldRemove() { return remove && !source.isPlaying(); }

    public void update() {
        setMapPosition();
        if(!hit)checkTileMapCollision();
        setPosition(temp.x, temp.y);

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
        float changeSpeed = (float) (1 - Math.pow((System.currentTimeMillis() - shotTime - InGame.deltaPauseTime()) / 750f,1));
        if(changeSpeed < 0){
            speed.x = 0;
            speed.y = 0;
        } else if (!hit) {
            speed.x = originalspeed.x * changeSpeed;
            speed.y = originalspeed.y * changeSpeed;

        }
    }

    public void draw() {
        if(source.isPlaying() && remove) return;
        super.draw();

    }
    public boolean isHit() {return hit;}

    public void playEnemyHit(){
        source.play(soundhit);
    }

    public boolean isCritical() {
        return crit;
    }
    public void setCritical(boolean crit){this.crit = crit;}

}
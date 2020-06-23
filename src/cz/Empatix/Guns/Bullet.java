package cz.Empatix.Guns;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;


public class Bullet extends MapObject {
    // SPRITE VARS
    private final static int sprites = 0;
    private final static int hitSprites = 1;

    // BASIC VARS
    private boolean hit;
    private boolean remove;

    // audio
    private final int soundWallhit;
    private final int soundEnemyhit;

    private int damage;

    public Bullet(TileMap tm, double x, double y,double inaccuracy, int speed) {

        super(tm);
        facingRight = true;


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
                double[] texCoords =
                        {
                                (double)i/spriteSheetCols,0,

                                (double)i/spriteSheetCols,1.0/spriteSheetRows,

                                (i+1.0)/spriteSheetCols,1.0/spriteSheetRows,

                                (i+1.0f)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);

                images[i] = sprite;

            }
            spritesheet.addSprites(images);

            images = new Sprite[3];
            for(int i = 0; i < images.length; i++) {
                double[] texCoords =
                        {
                                (double)i/spriteSheetCols,1.0/spriteSheetRows,

                                (double) i/spriteSheetCols,1,

                                (i+1.0)/spriteSheetCols,1,

                                (i+1.0)/spriteSheetCols,1.0/spriteSheetRows
                        };
                Sprite sprite = new Sprite(texCoords);

                images[i] = sprite;

            }
            spritesheet.addSprites(images);
        }

        vboVerticles = ModelManager.getModel(width,height);
        if (vboVerticles == -1){
            vboVerticles = ModelManager.createModel(width,height);
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

    public boolean shouldRemove() { return remove; }

    public void update() {
        setMapPosition();
        checkTileMapCollision();
        setPosition(temp.x, temp.y);

        if((speed.x == 0 || speed.y == 0) && !hit) {
            source.play(soundWallhit);
            setHit();
        }

        animation.update();
        if(hit) {
            if (animation.hasPlayedOnce()){
                remove = true;
                source.delete();
                light.remove();
            } else {
                // decrease intensity every time we use next sprite of hitBullet
                light.setIntensity(1.5f-0.5f*animation.getIndexOfFrame());
            }
        }

    }

    public void draw() {
        super.draw();

    }
    public boolean isHit() {return hit;}

    public void playEnemyHit(){
        source.play(soundEnemyhit);
    }
}
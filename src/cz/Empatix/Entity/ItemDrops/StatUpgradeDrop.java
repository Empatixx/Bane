package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class StatUpgradeDrop extends ItemDrop{
    public static final byte ARMOR = 0;
    public static final byte HEALTH = 1;
    private byte upgradeType;
    public static void load(){
        Loader.loadImage("Textures\\healthupgrade.tga");
        Loader.loadImage("Textures\\armorupgrade.tga");
    }
    public StatUpgradeDrop(TileMap tm, byte upgradeType){
        super(tm);
        this.upgradeType = upgradeType;
        if(tm.isServerSide()){
            type = STATUPGRADE;
            canDespawn = true;
            liveTime = System.currentTimeMillis()- InGame.deltaPauseTime();
            pickedUp = false;

            width = 32;
            height = 32;
            cwidth = 32;
            cheight = 32;
            scale = 3;
            facingRight = true;

            animation = new Animation(1);
            animation.setDelay(-1);

            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;
        } else {
            type = STATUPGRADE;
            canDespawn = true;
            liveTime = System.currentTimeMillis()-InGame.deltaPauseTime();
            pickedUp = false;

            width = 32;
            height = 32;
            cwidth = 32;
            cheight = 32;
            scale = 3;
            facingRight = true;


            String txt;
            if(upgradeType == ARMOR){
                txt = "Textures\\armorupgrade.tga";
            } else {
                txt = "Textures\\healthupgrade.tga";
            }

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet(txt);

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet(txt);
                Sprite[] sprites = new Sprite[1];
                float[] texCoords =
                        {
                                0.f,0.f,

                                0.f,1.f,

                                1.f,1.f,

                                1.f,0.f
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[0] = sprite;
                spritesheet.addSprites(sprites);
            }
            vboVertices = ModelManager.getModel(width,height);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,height);
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(0));
            animation.setDelay(-1);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }

            // because of scaling image by 3x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            light = LightManager.createLight(new Vector3f(1.0f,0.8274f,0.0f),new Vector2f(0,0),1.25f,this);
        }

    }


    public void update(){
        super.update();

        long timeNow = System.currentTimeMillis();
        float time = (float)(timeNow - liveTime - InGame.deltaPauseTime())/1000;
        if(time >= 30 && canDespawn){
            pickedUp = true;
            remove();
        } else if(time >= 25 && canDespawn){
            flinching = true;
        }
    }
    public void draw(){

        setMapPosition();

        long timeNow = System.currentTimeMillis() - InGame.deltaPauseTime();
        if(flinching){
            if((timeNow - liveTime) / 10 % 2 == 0) return;
        }
        super.draw();
    }

    public byte getUpgradeType() {
        return upgradeType;
    }
}


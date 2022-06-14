package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import cz.Empatix.Utility.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Coin extends ItemDrop{
    public static void load(){
        Loader.loadImage("Textures\\bane_coin.tga");
    }
    public Coin(TileMap tm){
        super(tm);
        if(tm.isServerSide()){
            type = COIN;
            canDespawn = true;
            facingRight = true;
            liveTime = System.currentTimeMillis()-InGame.deltaPauseTime();
            pickedUp = false;

            width = 33;
            height = 33;
            cwidth = 30;
            cheight = 30;
            scale = 1.5f;

            int minIncrease = tm.getFloor() - 1;
            if(minIncrease < 0){
                minIncrease = 0;
            }
            amount = 1 + minIncrease + Random.nextInt(tm.getFloor()+1);

            animation = new Animation(10);
            animation.setDelay(100);

            // because of scaling image by 3x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;
        } else {
            type = COIN;
            canDespawn = true;
            facingRight = true;
            liveTime = System.currentTimeMillis()-InGame.deltaPauseTime();
            pickedUp = false;

            width = 33;
            height = 33;
            cwidth = 30;
            cheight = 30;
            scale = 1.5f;

            int minIncrease = tm.getFloor() - 1;
            if(minIncrease < 0){
                minIncrease = 0;
            }
            amount = 1 + minIncrease + Random.nextInt(tm.getFloor()+1);

            spriteSheetCols = 10;

            //amount = Random.nextInt(3) + 1;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\bane_coin.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\bane_coin.tga");
                Sprite[] sprites = new Sprite[10];
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
            animation.setDelay(100);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }

            // because of scaling image by 3x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            light = LightManager.createLight(new Vector3f(1.0f,0.8274f,.0f),new Vector2f(0,0),1.25f,this);
        }


    }

    public void update(){
        super.update();

        animation.update();
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

        long timeNow = System.currentTimeMillis()- InGame.deltaPauseTime();
        if(flinching){
            if((timeNow - liveTime) / 10 % 2 == 0) return;
        }
        super.draw();
    }
}


package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Graphics.Model.ModelManager;
import cz.Empatix.Graphics.Shaders.ShaderManager;
import cz.Empatix.Graphics.Sprites.Sprite;
import cz.Empatix.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Coin extends ItemDrop{
    public Coin(TileMap tm){
        super(tm);
        type = COIN;
        canDespawn = true;
        liveTime = System.currentTimeMillis();
        pickedUp = false;

        width = 30;
        height = 30;
        cwidth = 15;
        cheight = 15;
        scale = 2;

        //amount = Random.nextInt(3) + 1;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\coin.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\coin.tga");
            Sprite[] sprites = new Sprite[1];
            double[] texCoords =
                    {
                            0.,0.,

                            0.,1.,

                            1.,1.,

                            1.,0.
                    };
            Sprite sprite = new Sprite(texCoords);
            sprites[0] = sprite;
            spritesheet.addSprites(sprites);
        }
        vboVerticles = ModelManager.getModel(width,height);
        if (vboVerticles == -1){
            vboVerticles = ModelManager.createModel(width,height);
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
        cwidth *= scale*2;
        cheight *= scale*2;

        light = LightManager.createLight(new Vector3f(1.0f,0.8274f,.0f),new Vector2f(0,0),1.25f,this);

    }

    public void update(){
        animation.update();
        long timeNow = System.currentTimeMillis();
        if((float)(timeNow - liveTime - InGame.deltaPauseTime())/1000 > 30  && canDespawn){
            pickedUp = true;
            remove();
        }
    }
    public void draw(Camera c){

        setMapPosition();

        long timeNow = System.currentTimeMillis();
        if((float)(timeNow - liveTime - InGame.deltaPauseTime())/1000 > 25  && canDespawn){
            if((timeNow - liveTime-InGame.deltaPauseTime()) / 10 % 2 == 0) return;
        }
        super.draw(c);
    }
}


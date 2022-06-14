package cz.Empatix.Entity.RoomObjects;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;

import static org.lwjgl.opengl.GL11.*;

public class Spike extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\spike.tga");
    }
    public boolean remove;
    private boolean damageAnimation;

    private boolean damageDone;
    public Spike(TileMap tm){
        super(tm);
        if(tm.isServerSide()){
            width = 16;
            height = 16;
            cwidth = 8;
            cheight = 8;
            scale = 8;

            facingRight = true;
            flinching=false;

            collision = false;
            moveable=false;
            preDraw = true;
            behindCollision = true;

            animation = new Animation(4);
            animation.setDelay(250);

            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            damageAnimation = true;
            remove = false;
        } else {
            width = 16;
            height = 16;
            cwidth = 8;
            cheight = 8;
            scale = 8;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 1;

            collision = false;
            moveable=false;
            preDraw = true;
            behindCollision = true;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\spike.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\spike.tga");
                Sprite[] sprites = new Sprite[4];
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
            animation.setDelay(250);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            damageAnimation = true;
            remove = false;
        }
    }

    public void update(){
        setMapPosition();
        animation.update();

        if(animation.getIndexOfFrame() >= 2){
            damageAnimation = false;
        } else {
            damageAnimation = true;
        }
        if(tileMap.isServerSide()){
             Server server = MultiplayerManager.getInstance().server.getServer();
             Network.RoomObjectAnimationSync roomObjectAnimationSync = new Network.RoomObjectAnimationSync();
             roomObjectAnimationSync.id = id;
             roomObjectAnimationSync.sprite = (byte)animation.getIndexOfFrame();
             roomObjectAnimationSync.time = animation.getTime();
             server.sendToAllUDP(roomObjectAnimationSync);
        }
    }

    @Override
    public void touchEvent(MapObject o) {
        if(!MultiplayerManager.multiplayer || tileMap.isServerSide()){
            if(damageAnimation){
                if(o instanceof Player) ((Player) o).hit(1);
                if(o instanceof DestroyableObject && !damageDone){
                    damageDone = true;
                    ((DestroyableObject) o).setHit(1);
                }
            } else {
                damageDone = false;
            }
        }
    }

    @Override
    public void draw() {
        super.draw();
        if (Game.displayCollisions){
            glColor3i(255,255,255);
            glBegin(GL_LINE_LOOP);
            // BOTTOM LEFT
            glVertex2f(position.x+xmap-cwidth/2,position.y+ymap-cheight/2);
            // TOP LEFT
            glVertex2f(position.x+xmap-cwidth/2, position.y+ymap+cheight/2);
            // TOP RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap+cheight/2);
            // BOTTOM RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap-cheight/2);
            glEnd();

            glPointSize(10);
            glColor3i(255,0,0);
            glBegin(GL_POINTS);
            glVertex2f(position.x+xmap,position.y+ymap);
            glEnd();


        }
    }
    public boolean shouldRemove(){
        return remove;
    }
    @Override
    public void keyPress() {

    }
}

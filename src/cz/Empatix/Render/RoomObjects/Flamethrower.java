package cz.Empatix.Render.RoomObjects;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;

public class Flamethrower extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\flamethrower.tga");
    }
    public boolean remove;
    private Player[] player;
    private boolean damageAnimation;

    private long cooldownTime;
    private boolean ready;

    public static final int VERTICAL = 0;
    public static final int HORIZONTAl = 1;

    private int type;
    public Flamethrower(TileMap tm, Player[] player){
        super(tm);
        if(tm.isServerSide()){
            this.player = player;
            width = 16;
            height = 32;
            cwidth = 12;
            cheight = 32;
            scale = 8;

            facingRight = true;
            flinching=false;

            collision = false;
            moveable=false;
            preDraw = true;

            animation = new Animation(4);
            animation.setDelay(150);

            cwidth *= scale;
            cheight *= scale;
            damageAnimation = true;
            remove = false;
            ready = true;
        } else {
            this.player = player;
            width = 16;
            height = 32;
            cwidth = 12;
            cheight = 32;
            scale = 8;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 1;

            collision = false;
            moveable=false;
            preDraw = true;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\flamethrower.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\flamethrower.tga");
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

                sprites = new Sprite[4];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (1.0f+i)/spriteSheetCols,0,

                                    (float) i/spriteSheetCols,0,

                                    (float)i/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,1

                            };
                    Sprite sprite = new Sprite(texCoords);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);

            }

            animation = new Animation();
            animation.setDelay(150);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 8x
            cwidth *= scale;
            cheight *= scale;
            damageAnimation = true;
            remove = false;
            ready = true;
        }

    }

    public void update(){
        setMapPosition();
        int currentFrame = animation.getIndexOfFrame();
        if(ready || currentFrame >= 1){
            animation.update();
        }
        if(tileMap.isServerSide()){
            Server server = MultiplayerManager.getInstance().server.getServer();
            Network.RoomObjectAnimationSync roomObjectAnimationSync = new Network.RoomObjectAnimationSync();
            roomObjectAnimationSync.id = id;
            roomObjectAnimationSync.sprite = (byte)animation.getIndexOfFrame();
            roomObjectAnimationSync.time = animation.getTime();
            server.sendToAllUDP(roomObjectAnimationSync);
        }
        if(System.currentTimeMillis() - cooldownTime - InGame.deltaPauseTime() > 1500 && (!MultiplayerManager.multiplayer || tileMap.isServerSide())){
            ready = true;
            cooldownTime = System.currentTimeMillis() - InGame.deltaPauseTime();
        }
        if(currentFrame != 2 && currentFrame != 1){
            damageAnimation = false;
        } else {
            if(currentFrame == 1){
                if(type == VERTICAL){
                    cheight=height/2;
                } else {
                    cwidth = width/2;
                }
            } else{
                if(type == VERTICAL){
                    cheight = height;
                } else {
                    cwidth = width;
                }
            }
            damageAnimation = true;
            ready = false;
        }
        for(int j = 0;j<player.length;j++){
            if(player[j] != null){
                if(damageAnimation && this.intersects(player[j])){
                    player[j].hit(1);
                }
            }
        }
    }

    @Override
    public void touchEvent(MapObject o) {
    }

    @Override
    public void draw() {
        super.draw();
    }
    public boolean shouldRemove(){
        return remove;
    }
    @Override
    public void keyPress() {

    }

    public void setType(int type){
        this.type = type;
        if(!tileMap.isServerSide()){
            animation.setFrames(spritesheet.getSprites(type));
            if(type == VERTICAL){
                vboVertices = ModelManager.getModel(width,height);
                if (vboVertices == -1){
                    vboVertices = ModelManager.createModel(width,height);
                }
            } else {
                vboVertices = ModelManager.getModel(height,width);
                if (vboVertices == -1){
                    vboVertices = ModelManager.createModel(height,width);
                }
            }
        }
        if (type != VERTICAL){
            int prevWidth;
            prevWidth = width;
            width = height;
            height = prevWidth;

            prevWidth = cwidth;
            cwidth = cheight;
            cheight = prevWidth;
        }
        width *= scale;
        height *= scale;
    }

    public int getType() {
        return type;
    }
}

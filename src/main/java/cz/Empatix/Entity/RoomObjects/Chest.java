package cz.Empatix.Entity.RoomObjects;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Multiplayer.MultiplayerManager;
import cz.Empatix.Multiplayer.ItemManagerMP;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import cz.Empatix.Utility.Random;
import org.joml.Vector2f;

public class Chest extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\chest.tga");
    }
    private static final int IDLE = 0;
    private static final int OPEN = 1;

    private boolean opened;
    private boolean dropGun;
    private boolean dropArtefact;


    public Chest(TileMap tm){
        super(tm);
        if(tm.isServerSide()){
            width = 16;
            height = 16;
            cwidth = 16;
            cheight = 16;
            scale = 8;

            facingRight = true;
            flinching=false;

            opened = false;
            collision = true;
            moveable=true;
            preDraw = false;

            animation = new Animation(4);
            animation.setDelay(175);

            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            maxMovement=1.5f;
            dropGun= false;
            dropArtefact = false;
        } else {
            width = 16;
            height = 16;
            cwidth = 16;
            cheight = 16;
            scale = 8;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 1;

            opened = false;
            collision = true;
            moveable=true;
            preDraw = false;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\chest.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\chest.tga");
                Sprite[] sprites = new Sprite[4];
                for(int i = 0; i < sprites.length; i++) {
                    float[] texCoords =
                            {
                                    (float) i/spriteSheetCols,0,

                                    (float)i/spriteSheetCols,0.5f,

                                    (1.0f+i)/spriteSheetCols,0.5f,

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
                                    (float) i/spriteSheetCols,0.5f,

                                    (float)i/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,1,

                                    (1.0f+i)/spriteSheetCols,0.5f
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
            animation.setFrames(spritesheet.getSprites(IDLE));
            animation.setDelay(175);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            maxMovement=1.5f;
            dropGun= false;
            dropArtefact = false;
        }
    }

    public void enableDropWeapon(){
        dropGun = true;
    }
    public void enableDropArtefact(){
        dropArtefact = true;
    }
    public void update(){
        setMapPosition();

        if(tileMap.isServerSide()){
            checkTileMapCollision();
            setPosition(temp.x, temp.y);
            if(opened && animation.hasPlayedOnce()){
                remove=true;

                Vector2f speed = new Vector2f();

                float x = (float) Random.nextDouble()*(-1+Random.nextInt(2)*2);
                float y = (float)Random.nextDouble()*(-1+Random.nextInt(2)*2);

                ItemManagerMP itemManager = ItemManagerMP.getInstance();

                if(dropGun) itemManager.dropWeapon((int)position.x,(int)position.y);
                if(dropArtefact) itemManager.dropArtefact((int)position.x,(int)position.y);

                for(int i = 0;i<5;i++){
                    double atan = Math.atan2(x,
                            y) + 1.3 * i;
                    speed.x = (float)Math.cos(atan);
                    speed.y = (float)Math.sin(atan);

                    itemManager.createDrop((int)position.x,(int)position.y,speed);
                }
            }
            getMovementSpeed();
            sendMovePacket();
        } else if (!MultiplayerManager.multiplayer){
            checkTileMapCollision();
            setPosition(temp.x, temp.y);
            if(opened && animation.hasPlayedOnce()){
                remove=true;

                Vector2f acceleration = new Vector2f();

                float x = (float) Random.nextDouble()*(-1+Random.nextInt(2)*2);
                float y = (float)Random.nextDouble()*(-1+Random.nextInt(2)*2);

                ItemManager itemManager = ItemManager.getInstance();

                if(dropGun) itemManager.dropWeapon((int)position.x,(int)position.y,acceleration);
                if(dropArtefact) itemManager.dropArtefact((int)position.x,(int)position.y);

                for(int i = 0;i<5;i++){
                    double atan = Math.atan2(x,
                            y) + 1.3 * i;
                    acceleration.x = (float)(Math.cos(atan));
                    acceleration.y = (float)(Math.sin(atan));

                    itemManager.createDrop((int)position.x,(int)position.y,acceleration);
                }
            }
            getMovementSpeed();
        } else {
            if(opened && animation.hasPlayedOnce()) {
                remove = true;
            }
        }
        animation.update();

    }

    @Override
    public void touchEvent(MapObject o) {
        open();
    }

    public void open(){
        if(opened) return;
        opened = true;
        collision = false;
        moveable = false;
        if(tileMap.isServerSide()){
            animation = new Animation(4);
            animation.setDelay(175);
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Network.OpenChest openChest = new Network.OpenChest();
            mpManager.server.requestACK(openChest,openChest.idPacket);
            openChest.id = getId();
            openChest.idRoom = (byte)tileMap.getRoomByCoords(position.x,position.y).getId();
            Server server = mpManager.server.getServer();
            server.sendToAllUDP(openChest);
        } else {
            animation.setFrames(spritesheet.getSprites(OPEN));
        }
        speed.mul(1.7f);
    }

    @Override
    public void draw() {
        // pokud neni object na obrazovce - zrusit
        super.draw();
    }
    public boolean shouldRemove(){return remove;}
    @Override
    public void keyPress() {

    }
}

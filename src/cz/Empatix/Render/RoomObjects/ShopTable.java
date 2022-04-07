package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Loader;
import cz.Empatix.Multiplayer.ItemManagerMP;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PacketHolder;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;

public class ShopTable extends RoomObject{
    private boolean itemCreated;
    public static void load(){
        Loader.loadImage("Textures\\table.tga");
    }
    public ShopTable(TileMap tm){
        super(tm);
        if(tm.isServerSide()){
            width = 96;
            height = 64;
            cwidth = 96;
            cheight = 64;
            scale = 2.5f;

            facingRight = true;
            flinching=false;

            collision = false;
            moveable = false;
            preDraw = true;

            animation = new Animation(1);
            animation.setDelay(-1);

            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;
        } else {
            width = 96;
            height = 64;
            cwidth = 96;
            cheight = 64;
            scale = 2.5f;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 1;
            spriteSheetRows = 1;

            collision = false;
            moveable = false;
            preDraw = true;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\table.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\table.tga");
                Sprite[] sprites = new Sprite[1];
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
            animation.setDelay(-1);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;
        }

    }

    public void createItem(){
        if(tileMap.isServerSide()){
            ItemManagerMP itemManagerMP = ItemManagerMP.getInstance();
            itemManagerMP.createShopDrop(position.x,position.y-20,getId());
        } else if (!MultiplayerManager.multiplayer) {
            ItemManager itemManager = ItemManager.getInstance();
            itemManager.createShopDrop(position.x,position.y-20);
        }
    }

    public void update(){
        setMapPosition();
        checkTileMapCollision();

        animation.update();

        if (speed.x < 0){
            speed.x += stopSpeed;
            if (speed.x > 0) speed.x = 0;
        } else if (speed.x > 0){
            speed.x -= stopSpeed;
            if (speed.x < 0) speed.x = 0;
        }

        if (speed.y < 0){
            speed.y += stopSpeed;
            if (speed.y > 0) speed.y = 0;
        } else if (speed.y > 0){
            speed.y -= stopSpeed;
            if (speed.y < 0) speed.y = 0;
        }
        // handling packets of shop item drop
        if(!itemCreated && MultiplayerManager.multiplayer && !tileMap.isServerSide()) {
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Object[] packets = mpManager.packetHolder.getWithoutClear(PacketHolder.SHOPITEM);
            for(Object o : packets){
                Network.ShopDropitem packet = (Network.ShopDropitem) o;
                if(packet.idObject == getId()){
                    itemCreated = true;
                    ItemManager itemManager = ItemManager.getInstance();
                    itemManager.createShopDrop(packet,position.x,position.y-20);
                    mpManager.packetHolder.remove(PacketHolder.SHOPITEM,o);
                    break;
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

    @Override
    public void keyPress() {

    }
}

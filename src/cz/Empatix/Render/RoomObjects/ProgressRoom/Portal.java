package cz.Empatix.Render.RoomObjects.ProgressRoom;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Multiplayer.ProgressRoomMP;
import cz.Empatix.Java.Loader;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Portal extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\portal.tga");
    }
    private static final int IDLE = 0;
    private boolean message;

    private TextRender textRender;

    private boolean packetChangeSent;

    public Portal(TileMap tm){
        super(tm);
        if(tm.isServerSide()){
            width = 86;
            height = 80;
            cwidth = 43;
            cheight = 40;
            scale = 4;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 8;
            spriteSheetRows = 1;

            collision = false;
            moveable=false;
            preDraw = true;

            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;
        } else {
            width = 86;
            height = 80;
            cwidth = 43;
            cheight = 40;
            scale = 4;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 8;
            spriteSheetRows = 1;

            collision = false;
            moveable=false;
            preDraw = true;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\portal.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\portal.tga");
                Sprite[] sprites = new Sprite[8];
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
            animation.setFrames(spritesheet.getSprites(IDLE));
            animation.setDelay(125);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            light = LightManager.createLight(new Vector3f(0.466f, 0.043f, 0.596f),new Vector2f(0,0),8f,this);

            textRender = new TextRender();
        }
    }

    @Override
    public void loadSave() {
        width = 86;
        height = 80;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\portal.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\portal.tga");
            Sprite[] sprites = new Sprite[8];
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
        animation.setFrames(spritesheet.getSprites(IDLE));
        animation.setDelay(125);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 8x
        width *= scale;
        height *= scale;

        light = LightManager.createLight(new Vector3f(0.466f, 0.043f, 0.596f),new Vector2f(0,0),8f,this);

        textRender = new TextRender();
        packetChangeSent = false;
    }

    public void update(){
        setMapPosition();
        animation.update();
        if(MultiplayerManager.multiplayer && ProgressRoomMP.ready) {
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            String username = mpManager.getUsername();
            // if players leave portal area
            if (!message) {
                ProgressRoomMP.ready = false;
                Network.Ready ready = new Network.Ready();
                Client client = mpManager.client.getClient();
                ready.username = username;
                ready.state = false;
                client.sendUDP(ready);
                packetChangeSent = false;
            } else {
                if(!packetChangeSent){
                    Network.Ready ready = new Network.Ready();
                    Client client = mpManager.client.getClient();
                    ready.username = username;
                    ready.state = true;
                    client.sendUDP(ready);
                    packetChangeSent = true;
                }

            }
        }
        message = false;
    }

    @Override
    public void touchEvent(MapObject o) {
        if(o instanceof PlayerMP){
            if(((PlayerMP) o).isOrigin()) message = true;
        } else {
            message = true;
        }

    }


    @Override
    public void draw() {
        // pokud neni object na obrazovce - zrusit
        if (isNotOnScrean()){
            return;
        }
        super.draw();

        float time = (float)Math.sin(System.currentTimeMillis() % 2000 / 600f)+(1-(float)Math.cos((System.currentTimeMillis() % 2000 / 600f) +0.5f));
        if(MultiplayerManager.multiplayer && ProgressRoomMP.ready && message){
            int totalPlayers = MultiplayerManager.getInstance().client.getTotalPlayers();
            textRender.drawMap("Waiting for players "+ProgressRoomMP.readyNumPlayers+"/"+totalPlayers,new Vector3f(position.x-155,position.y+155,0),2,
                    new Vector3f((float)Math.sin(time),(float)Math.cos(0.5f+time),1f));
        }else if(message){
            textRender.drawMap("Press E to enter game",new Vector3f(position.x-155,position.y+155,0),2,
                    new Vector3f((float)Math.sin(time),(float)Math.cos(0.5f+time),1f));
        }
    }
    public boolean shouldRemove(){return remove;}
    @Override
    public void keyPress() {
        GameStateManager.EnterGame();
    }
}

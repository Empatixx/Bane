package cz.Empatix.Entity.ProgressRoom;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Hud.MultiplayerNPC.MultiplayerMenu;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class MultiplayerNPC extends MapObject {
    private static final int IDLE = 0;

    private boolean touching;
    private boolean interract;

    private MultiplayerMenu multiplayerMenu;

    private TextRender textRender;


    public static void load(){
        Loader.loadImage("Textures\\ProgressRoom\\multiplayernpc.tga");
        Loader.loadImage("Textures\\ProgressRoom\\serverfound.tga");
    }

    public MultiplayerNPC(TileMap tm, GameStateManager gsm) {
        super(tm);

        width = 100;
        height = 100;
        cwidth = 100;
        cheight = 100;
        scale = 4;

        facingRight = true;

        spriteSheetCols = 14;
        spriteSheetRows = 1;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\ProgressRoom\\multiplayernpc.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\ProgressRoom\\multiplayernpc.tga");
            Sprite[] sprites = new Sprite[14];
            for(int i = 0; i < sprites.length; i++) {
                //Sprite sprite = new Sprite(texCoords);
                float[] texCoords =
                        {
                                (float)i/spriteSheetCols,0.f,

                                (float)i/spriteSheetCols,1.f,

                                (i+1f)/spriteSheetCols,1.f,

                                (i+1f)/spriteSheetCols,0.f
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
        animation.setDelay(130);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 2x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        textRender = new TextRender();

        multiplayerMenu = new MultiplayerMenu(gsm);
    }

    public void update(float x,float y) {
        if(!touching){
            interract = false;
            multiplayerMenu.playerAbandonedInteraction();
        }
        touching = false;
        setMapPosition();
        animation.update();
        multiplayerMenu.update(x,y);
        // update position
        checkTileMapCollision();
    }

    @Override
    public void draw() {
        super.draw();
        if(touching && !interract) {
            float time = (float) Math.sin(System.currentTimeMillis() % 2000 / 600f) + (1 - (float) Math.cos((System.currentTimeMillis() % 2000 / 600f) + 0.5f));

            float centerX = TextRender.getHorizontalCenter((int)position.x-50,(int)position.x+50,"Press E to talk",2);
            textRender.drawMap("Press E to talk",new Vector3f(centerX,position.y+cheight/2 + 20,0),2,
                    new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
        }
    }
    public void drawHud(){
        if(canShowHud()){
            multiplayerMenu.draw();
        }
    }

    public void keyPress(int k) {
        if(isUsingInputBar()){
            multiplayerMenu.keyPress(k);
        } else {
            if(k == ControlSettings.getValue(ControlSettings.OBJECT_INTERACT)){
                interract = !interract;
                if(!interract){
                    multiplayerMenu.playerAbandonedInteraction();
                }
            }
            else if(k == GLFW.GLFW_KEY_ESCAPE){
                interract = false;
                multiplayerMenu.playerAbandonedInteraction();
            }
        }
    }
    public void keyReleased(int k) {
        multiplayerMenu.keyReleased(k);
    }
    public void touching(MapObject obj){
        if(this.intersects(obj)){
            touching = true;
        }
    }

    public boolean canShowHud() {
        return touching && interract;
    }

    public void mousePressed(float x, float y, Player p){
        if(canShowHud())multiplayerMenu.mousePressed(x,y,p);
    }
    public void mouseReleased(float x, float y){
        multiplayerMenu.mouseReleased(x,y);
    }

    public boolean isInteracting(){
        return interract;
    }

    public void mouseScroll(double x, double y) {
        multiplayerMenu.mouseScroll(x,y);
    }
    public boolean isUsingInputBar(){
        return multiplayerMenu.isUsingInputBar();
    }
}
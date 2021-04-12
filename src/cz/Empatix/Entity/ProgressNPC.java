package cz.Empatix.Entity;

import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Hud.ProgressNPC.UpgradeMenu;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class ProgressNPC extends MapObject {
    private static final int IDLE = 0;

    private boolean touching;
    private boolean interract;

    private UpgradeMenu upgradeMenu;

    private TextRender textRender;

    public static void load(){
        Loader.loadImage("Textures\\ProgressRoom\\upgradenpc.tga");
    }

    public ProgressNPC(TileMap tm) {
        super(tm);

        moveSpeed = 0f;
        maxSpeed = 1.5f;
        stopSpeed = 1.5f;

        width = 64;
        height = 64;
        cwidth = 64;
        cheight = 64;
        scale = 6;

        facingRight = true;

        spriteSheetCols = 5;
        spriteSheetRows = 1;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\ProgressRoom\\upgradenpc.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\ProgressRoom\\upgradenpc.tga");
            Sprite[] sprites = new Sprite[10];
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

        upgradeMenu = new UpgradeMenu();

        textRender = new TextRender();
    }

    public void update(float x,float y) {
        if(!touching) interract = false;
        touching = false;
        setMapPosition();
        animation.update();
        upgradeMenu.update(x,y);
        // update position
        checkTileMapCollision();
    }

    @Override
    public void draw() {
        super.draw();
        if(touching && !interract) {
            float time = (float) Math.sin(System.currentTimeMillis() % 2000 / 600f) + (1 - (float) Math.cos((System.currentTimeMillis() % 2000 / 600f) + 0.5f));

            textRender.drawMap("Press E to talk",new Vector3f(position.x-110,position.y+cheight/2,0),2,
                    new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
        }
    }
    public void drawHud(){
        if(canShowHud()){
            upgradeMenu.draw();
        }
    }

    public void keyPress(int k) {
        if(k == GLFW.GLFW_KEY_E){
            interract = !interract;
            upgradeMenu.unlockSlider();
        }
        else if(k == GLFW.GLFW_KEY_ESCAPE)interract = false;
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
        if(canShowHud()) upgradeMenu.mousePressed(x,y, p);
    }
    public void mouseReleased(float x, float y){
        if(canShowHud()) upgradeMenu.mouseReleased(x,y);
    }

    public boolean isInteracting(){
        return interract;
    }

    public void mouseScroll(double x, double y) {
        upgradeMenu.mouseScroll(x,y);
    }
}


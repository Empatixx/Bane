package cz.Empatix.Entity;

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

    private boolean reverse;

    private boolean touching;
    private boolean interract;

    private UpgradeMenu upgradeMenu;

    public ProgressNPC(TileMap tm) {
        super(tm);

        moveSpeed = 0f;
        maxSpeed = 1.5f;
        stopSpeed = 1.5f;

        width = 80;
        height = 80;
        cwidth = 80;
        cheight = 80;
        scale = 4;

        facingRight = true;

        spriteSheetCols = 10;
        spriteSheetRows = 1;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\shopkeeper.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\shopkeeper.tga");
            Sprite[] sprites = new Sprite[10];
            for(int i = 0; i < sprites.length; i++) {
                //Sprite sprite = new Sprite(texCoords);
                Sprite sprite = new Sprite(5,i,0,width,height,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

        }
        vboVerticles = ModelManager.getModel(width,height);
        if (vboVerticles == -1){
            vboVerticles = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(IDLE));
        animation.setDelay(170);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 2x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        reverse = false;
        upgradeMenu = new UpgradeMenu();
    }

    public void update(float x,float y) {
        if(!touching) interract = false;
        touching = false;
        setMapPosition();
        // update animation
        if(animation.getIndexOfFrame() == 9 && !reverse){
            animation.reverse();
            reverse = true;
        } else if(animation.getIndexOfFrame() == 0 && reverse){
            animation.unreverse();
            reverse = false;
        }
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
            TextRender.renderMapText("Press E to talk",new Vector3f(position.x-80,position.y+cheight/2,position.z),2,
                    new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
        }
    }
    public void drawHud(){
        if(canShowHud()){
            upgradeMenu.draw();
        }
    }

    public void keyPress(int k) {
        if(k == GLFW.GLFW_KEY_E)interract = !interract;
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

}


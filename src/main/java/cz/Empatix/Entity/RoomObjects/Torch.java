package cz.Empatix.Entity.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class Torch extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\torch.tga");
    }
    public static final int SIDELEFT = 0;
    public static final int SIDERIGHT = 1;
    public static final int TOP = 2;

    private int type;

    public Torch(TileMap tm){
        super(tm);
        if(tm.isServerSide()){
            width = 16;
            height = 16;
            cwidth = 16;
            cheight = 16;
            scale = 8;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 2;

            collision = false;
            moveable=false;
            preDraw = true;

            animation = new Animation(4);
            animation.setDelay(150);

            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            remove = false;
            // because of light bug - not updated when not seen
            xmap = -100000000;
            ymap = -100000000;
        } else {
            width = 16;
            height = 16;
            cwidth = 16;
            cheight = 16;
            scale = 8;

            facingRight = true;
            flinching=false;

            spriteSheetCols = 4;
            spriteSheetRows = 2;

            collision = false;
            moveable=false;
            preDraw = true;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\torch.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\torch.tga");
                for(int j = 0;j < spriteSheetRows;j++){

                    Sprite[] sprites = new Sprite[4];

                    for(int i = 0; i < sprites.length; i++) {
                        float[] texCoords =
                                {
                                        (float) i / spriteSheetCols, (float)j/spriteSheetRows,

                                        (float) i / spriteSheetCols, (float)(j+1)/spriteSheetRows,

                                        (1.0f + i) / spriteSheetCols, (float)(j+1)/spriteSheetRows,

                                        (1.0f + i) / spriteSheetCols, (float)j/spriteSheetRows
                                };
                        Sprite sprite = new Sprite(texCoords);
                        sprites[i] = sprite;
                    }

                    spritesheet.addSprites(sprites);

                }

            }
            vboVertices = ModelManager.getModel(width,height);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,height);
            }

            animation = new Animation();
            animation.setDelay(150);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 8x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            remove = false;

            light = LightManager.createLight(new Vector3f(0.905f, 0.788f, 0.450f),new Vector2f(0,0),2f,this);

            // because of light bug - not updated when not seen
            xmap = -100000000;
            ymap = -100000000;
        }
    }

    public void update(){
        setMapPosition();

        animation.update();

        if(!tileMap.isServerSide()){
            light.setIntensity(1.9f+0.3f*(float)Math.sin(2*Math.PI*((System.currentTimeMillis()%2000)/2000d)));
        }
    }

    @Override
    public void touchEvent(MapObject o) {
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
    public void setType(int type){
        this.type = type;
        if(tileMap.isServerSide()) return;
        animation.setFrames(spritesheet.getSprites(type == TOP ? 1 : 0));
        if(type == SIDELEFT) facingRight = false;
        else facingRight = true;
    }

    @Override
    public void delete() {
        super.delete();
        if(!tileMap.isServerSide())light.remove();
    }

    public int getType() {
        return type;
    }
}

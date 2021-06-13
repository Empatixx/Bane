package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class Flamethrower extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\flamethrower.tga");
    }
    public boolean remove;
    private Player player;
    private boolean damageAnimation;

    private long cooldownTime;
    private boolean ready;

    public static final int VERTICAL = 0;
    public static final int HORIZONTAl = 1;

    private int type;

    public Flamethrower(TileMap tm, Player player){
        super(tm);
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

    @Override
    public void loadSave() {
        width = 16;
        height = 32;

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
        setType(type);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
    }

    public void update(){
        setMapPosition();
        int currentFrame = animation.getIndexOfFrame();
        if(ready || currentFrame >= 1){
            animation.update();
        }
        if(System.currentTimeMillis() - cooldownTime - InGame.deltaPauseTime() > 1500){
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
        if(damageAnimation && this.intersects(player)){
            player.hit(1);
        }
    }

    @Override
    public void touchEvent(MapObject o) {
    }

    @Override
    public void draw() {
        // pokud neni object na obrazovce - zrusit
        if (isNotOnScrean()){
            return;
        }

        Matrix4f target;
        if (facingRight) {
            target = new Matrix4f().translate(position)
                    .scale(scale);
        } else {
            target = new Matrix4f().translate(position)
                    .scale(scale)
                    .rotateY(3.14f);

        }
        Camera.getInstance().projection().mul(target,target);

        shader.bind();
        shader.setUniformi("sampler",0);
        shader.setUniformm4f("projection",target);
        glActiveTexture(GL_TEXTURE0);
        spritesheet.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);


        glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);
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
        animation.setFrames(spritesheet.getSprites(type));
        this.type = type;

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
}

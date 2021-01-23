package cz.Empatix.Render.RoomObjects;


import cz.Empatix.Entity.Animation;
import cz.Empatix.Java.Loader;
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

public class PathWall extends RoomObject {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\wall.tga");
    }
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;

    private boolean reverse;
    private int direction;

    public void setDirection(int d){
        direction = d;
        animation.setFrames(spritesheet.getSprites(d));
    }

    public PathWall(TileMap tm){
        super(tm);
        collision=false;
        moveable=false;
        preDraw = false;
        reverse=false;
        height = 64;
        width = 64;
        cwidth = 65;
        cheight = 65;
        scale = 2;
        spriteSheetCols=4;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\wall.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\wall.tga");
            // TOP
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
            // LEFT
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
            // BOTTOM
            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (1.0f+i)/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,0,

                                (float) i/spriteSheetCols,0,

                                (float)i/spriteSheetCols,1


                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);
            // RIGHT
            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float)i/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,0,

                                (float) i/spriteSheetCols,0


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
        animation.setDelay(75);

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

    @Override
    public void loadSave() {
        height = 64;
        width = 64;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\wall.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\wall.tga");
            // TOP
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
            // LEFT
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
            // BOTTOM
            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (1.0f+i)/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,0,

                                (float) i/spriteSheetCols,0,

                                (float)i/spriteSheetCols,1


                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);
            // RIGHT
            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                float[] texCoords =
                        {
                                (float)i/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,1,

                                (1.0f+i)/spriteSheetCols,0,

                                (float) i/spriteSheetCols,0


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
        animation.setFrames(spritesheet.getSprites(direction));
        animation.setDelay(75);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 8x
        width *= scale;
        height *= scale;
    }

    public void update() {
        setMapPosition();
        if(!collision && !reverse && animation.getIndexOfFrame() == 3){
            animation.reverse();
            reverse=true;
        }
        if (collision && animation.getIndexOfFrame() != 3) {
            animation.update();

        }else if (!collision && animation.getIndexOfFrame() != 0){
            animation.update();
        }
    }
    @Override
    public void draw() {
        if(collision || (animation.getIndexOfFrame() != 0 && reverse)){
            // pokud neni object na obrazovce - zrusit
            if (isNotOnScrean()){
                return;
            }

            Matrix4f target;
            target = new Matrix4f().translate(position)
                        .scale(scale);
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

        }

    }

    @Override
    public void touchEvent() {

    }
    @Override
    public void keyPress() {

    }
}

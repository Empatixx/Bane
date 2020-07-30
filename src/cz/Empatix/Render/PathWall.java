package cz.Empatix.Render;


import cz.Empatix.Entity.Animation;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class PathWall extends RoomObject {
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;

    private boolean reverse;

    public void setDirection(int d){
        animation.setFrames(spritesheet.getSprites(d));
    }

    PathWall(TileMap tm){
        super(tm);
        collision=false;
        moveable=false;
        preDraw = false;
        reverse=false;
        height = 64;
        width = 64;
        cwidth = 64;
        cheight = 64;
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
                double[] texCoords =
                        {
                                (double) i/spriteSheetCols,0,

                                (double)i/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);
            // LEFT
            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                double[] texCoords =
                        {
                                (1.0+i)/spriteSheetCols,0,

                                (double) i/spriteSheetCols,0,

                                (double)i/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,1

                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);
            // BOTTOM
            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                double[] texCoords =
                        {
                                (1.0+i)/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,0,

                                (double) i/spriteSheetCols,0,

                                (double)i/spriteSheetCols,1


                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);
            // RIGHT
            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                double[] texCoords =
                        {
                                (double)i/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,0,

                                (double) i/spriteSheetCols,0


                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);
        }
        vboVerticles = ModelManager.getModel(width,height);
        if (vboVerticles == -1){
            vboVerticles = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setDelay(55);

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
    public void update() {
        setMapPosition();
        if(collision && !reverse){
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
        if(collision || animation.getIndexOfFrame() != 0){
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


            glBindBuffer(GL_ARRAY_BUFFER, vboVerticles);
            glVertexAttribPointer(0,2,GL_INT,false,0,0);


            glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());
            glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER,0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

        }


    }

    @Override
    public void touchEvent() {

    }

}

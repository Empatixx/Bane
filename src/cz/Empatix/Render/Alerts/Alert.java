package cz.Empatix.Render.Alerts;

import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;

public class Alert {
    public static void load(){
        Loader.loadImage("Textures\\alerts.tga");
    }

    private final int type;
    private final String mess;

    private final long timeCreation;
    private final Vector3f position;

    private int vboVertices;
    private Shader shader;

    private Spritesheet spritesheet;

    private boolean remove;

    private float alpha;
    private boolean disableText;

    private TextRender textRender;
    Alert(int type, String mess){
        this.mess = mess;
        this.type = type;

        timeCreation = System.currentTimeMillis() - InGame.deltaPauseTime();

        position = new Vector3f(1450,320,0);

        shader = ShaderManager.getShader("shaders\\image");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\image");
        }

        int width = 79;
        int height = 20;

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(width, height);
        }

        spritesheet = SpritesheetManager.getSpritesheet("Textures\\alerts.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\alerts.tga");
            for(int j = 0;j<2;j++) {

                Sprite[] images = new Sprite[1];
                float[] texCoords =
                            {
                                    0, (float) j / 2,

                                    0, (1.0f + j) / 2,

                                    1, (1.0f + j) / 2,


                                    1, (float) j / 2
                            };

                Sprite sprite = new Sprite(texCoords);

                images[0] = sprite;

                spritesheet.addSprites(images);
            }
        }

        alpha = 1f;
    }
    public void update(int row){
        long delay = System.currentTimeMillis() - InGame.deltaPauseTime() - timeCreation;
        if(delay > 5000){
            remove = true;
        } else if (delay > 4500){
            alpha = 1f - (delay / 5000f);
            disableText = true;
            position.y += (370+row*100 - position.y) * 1.5f * Game.deltaTime;;
            return;
        }
        position.x += (1750 - position.x) * 2.75 * Game.deltaTime;
        position.y += (320+row*100 - position.y) * 2.75 * Game.deltaTime;

    }
    public void draw(){
        if(textRender == null){
            textRender = new TextRender();
        }
        shader.bind();
        shader.setUniformi("sampler",0);
        Matrix4f matrixPos = new Matrix4f()
                .translate(position)
                .scale(4f);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);

        shader.setUniformm4f("projection",matrixPos);
        shader.setUniformf("alpha",alpha);

        glActiveTexture(GL_TEXTURE0);
        spritesheet.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,spritesheet.getSprites(type)[0].getVbo());
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);
        float center = TextRender.getHorizontalCenter((int)position.x-87,(int)position.x+133,mess,1);
        if(!disableText)textRender.draw(mess, new Vector3f(center,position.y,0),1,new Vector3f(0.184f,0.149f,0.117f));
    }
    public boolean shouldRemove(){return remove;}
}

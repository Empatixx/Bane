package cz.Empatix.Render;


import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;

public class Background {
    private Shader shader;
    private int vboVertices;
    private Spritesheet spritesheet;
    private Matrix4f matrixPos;

    private boolean fadeEffect;
    private long time;
    private float alpha;

    public Background(String filepath) {
        shader = ShaderManager.getShader("shaders\\background");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\background");
        }
        int vbo;
        vbo = ModelManager.getModel(Camera.getWIDTH(), Camera.getHEIGHT());
        if (vbo == -1) {
            vbo = ModelManager.createModel(Camera.getWIDTH(), Camera.getHEIGHT());
        }
        vboVertices = vbo;

        spritesheet = SpritesheetManager.getSpritesheet(filepath);

        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet(filepath);
            for(int j = 0;j<2;j++) {

                Sprite[] images = new Sprite[1];
                float[] texCoords =
                        {
                                0, 0,

                                0, 1,

                                1, 1,


                                1, 0
                        };

                Sprite sprite = new Sprite(texCoords);

                images[0] = sprite;

                spritesheet.addSprites(images);
            }
        }
        matrixPos = new Matrix4f().translate(new Vector3f((float) Camera.getWIDTH() / 2, (float) Camera.getHEIGHT() / 2, 0));
        Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
    }

    /**
     *
     * @param width - new width of bg
     * @param height - new height of bg
     */
    public void setDimensions(int width, int height){
        int vbo;
        vbo = ModelManager.getModel(width,height);
        if (vbo == -1){
            vbo = ModelManager.createModel(width,height);
        }
        vboVertices = vbo;
    }
    public void draw() {
        shader.bind();
        if(fadeEffect){
            shader.setUniformf("alpha",alpha);
        } else {
            shader.setUniformf("alpha",1f);
        }
        shader.setUniformi("sampler",0);
        shader.setUniformm4f("projection",matrixPos);
        glActiveTexture(GL_TEXTURE0);
        spritesheet.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,spritesheet.getSprites(0)[0].getVbo());
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);
    }

    public void setFadeEffect(boolean fadeEffect) {
        this.fadeEffect = fadeEffect;
    }
    public void update() {
        if (fadeEffect){
            long currentTime = System.currentTimeMillis()- InGame.deltaPauseTime();
            alpha = 1 - (float) (currentTime-time ) / 1000 / 1.6f;
            if (alpha < 0) alpha = 0;
        }
    }
    public void updateFadeTime(){
        time = System.currentTimeMillis()- InGame.deltaPauseTime();
    }
}
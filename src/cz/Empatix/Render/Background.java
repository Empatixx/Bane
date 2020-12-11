package cz.Empatix.Render;


import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class Background {
    private Shader shader;
    private int idTexture;
    private int vboVertices;
    private int vboTextures;
    private Matrix4f matrixPos;

    private boolean fadeEffect;
    private long time;
    private float alpha;

    public Background(String filepath) {
        shader = ShaderManager.getShader("shaders\\background");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\background");
        }
        ByteBufferImage decoder = Loader.getImage(filepath);
        ByteBuffer spritesheetImage = decoder.getBuffer();
        int channels = decoder.getChannels();
        if(channels == 3) {
            int width = decoder.getWidth();
            int height = decoder.getHeight();

            idTexture = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, idTexture);

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, spritesheetImage);

            int vbo;
            vbo = ModelManager.getModel(Camera.getWIDTH(), Camera.getHEIGHT());
            if (vbo == -1) {
                vbo = ModelManager.createModel(Camera.getWIDTH(), Camera.getHEIGHT());
            }
            vboVertices = vbo;

            float[] texCoords =
                    {
                            0, 0, 0, 1, 1, 1, 1, 0
                    };

            FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
            buffer.put(texCoords);
            buffer.flip();
            vboTextures = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER, vboTextures);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            matrixPos = new Matrix4f().translate(new Vector3f((float) Camera.getWIDTH() / 2, (float) Camera.getHEIGHT() / 2, 0));
            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
        } else if(channels == 4){
            int width = decoder.getWidth();
            int height = decoder.getHeight();

            idTexture = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, idTexture);

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

            int vbo;
            vbo = ModelManager.getModel(Camera.getWIDTH(), Camera.getHEIGHT());
            if (vbo == -1) {
                vbo = ModelManager.createModel(Camera.getWIDTH(), Camera.getHEIGHT());
            }
            vboVertices = vbo;

            float[] texCoords =
                    {
                            0, 0, 0, 1, 1, 1, 1, 0
                    };

            FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
            buffer.put(texCoords);
            buffer.flip();
            vboTextures = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER, vboTextures);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            matrixPos = new Matrix4f().translate(new Vector3f((float) Camera.getWIDTH() / 2, (float) Camera.getHEIGHT() / 2, 0));
            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
        }
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
        glBindTexture(GL_TEXTURE_2D,idTexture);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
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
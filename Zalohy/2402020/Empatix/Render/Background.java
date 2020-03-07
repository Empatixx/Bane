package cz.Empatix.Render;


import cz.Empatix.Graphics.ByteBufferImage;
import cz.Empatix.Graphics.Model.ModelManager;
import cz.Empatix.Graphics.Shaders.Shader;
import cz.Empatix.Graphics.Shaders.ShaderManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import static org.lwjgl.opengl.GL20.*;

public class Background {

    private Shader shader;
    private final int idTexture;
    private int vboVertices;
    private int vboTextures;
    private Matrix4f matrixPos;

    private boolean fadeEffect;
    private long time;
    private float alpha;

    public Background(String filepath, Camera camera) {
        shader = ShaderManager.getShader("shaders\\background");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\background");
        }

        ByteBufferImage decoder = new ByteBufferImage();
        ByteBuffer spritesheetImage = decoder.decodeImage(filepath);

        int width = decoder.getWidth();
        int height = decoder.getHeight();

        idTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        STBImage.stbi_image_free(spritesheetImage);

        int vbo;
        vbo = ModelManager.getModel(Game.WIDTH,Game.HEIGHT);
        if (vbo == -1){
            vbo = ModelManager.createModel(Game.WIDTH,Game.HEIGHT);
        }
        vboVertices = vbo;

        double[] texCoords =
                {
                        0,0, 0,1, 1,1, 1,0
                };

        DoubleBuffer buffer = BufferUtils.createDoubleBuffer(texCoords.length);
        buffer.put(texCoords);
        buffer.flip();
        vboTextures = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

        matrixPos = new Matrix4f().translate(new Vector3f((float)Game.WIDTH/2,(float)Game.HEIGHT/2,0));
        camera.hardProjection().mul(matrixPos,matrixPos);
    }

    /**
     *
     * @param width - new width of bg
     * @param height - new height of bg
     */
    public void setDimensions(int width, int height){
        // scaling on different resolution then fullhd
        width*= Settings.scaleWidth();
        height*= Settings.scaleHeight();


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
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);
    }

    public void setFadeEffect(boolean fadeEffect) {
        this.fadeEffect = fadeEffect;
    }
    public void update() {
        if (fadeEffect){
            long currentTime = System.currentTimeMillis();
            alpha = 1 - (float) (currentTime-time) / 1000 / 1.6f;
            if (alpha < 0) alpha = 0;
        }
    }
    public void updateFadeTime(){
        time = System.currentTimeMillis();
    }
}
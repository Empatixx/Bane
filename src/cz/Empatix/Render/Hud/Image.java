package cz.Empatix.Render.Hud;

import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Image {
    private Shader shader;
    private int idTexture;

    private int vboTextures;
    private int vboVertices;

    private Matrix4f matrixPos;


    /**
     *
     * @param file - path of texture
     * @param pos - position of menu bar
     * @param scale - scaling of texture(width*scale,height*scale)
     * @param camera - camera
     */
    public Image(String file, Vector3f pos, float scale, Camera camera){
        ByteBufferImage decoder = new ByteBufferImage();
        ByteBuffer spritesheetImage = decoder.decodeImage(file);


        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        idTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        int width = decoder.getWidth();
        int height = decoder.getHeight();

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        STBImage.stbi_image_free(spritesheetImage);

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(width, height);
        }
        // clicking icon
        double[] texCoords =
                {
                        0,0,
                        0,1,
                        1,1,
                        1,0
                };

        DoubleBuffer buffer = BufferUtils.createDoubleBuffer(texCoords.length);
        buffer.put(texCoords);
        buffer.flip();
        vboTextures = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

        matrixPos = new Matrix4f()
                .translate(pos)
                .scale(scale);
        camera.hardProjection().mul(matrixPos,matrixPos);

    }

    public void draw(){
        shader.bind();
        shader.setUniformi("sampler",0);
        shader.setUniformm4f("projection",matrixPos);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,idTexture);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
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
}

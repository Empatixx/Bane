package cz.Empatix.Render.Postprocessing;


import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Graphics.Framebuffer;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public abstract class Postprocess {
    private int vboVertices;

    Shader shader;


    Postprocess(String shaderURL){
        shader = ShaderManager.getShader(shaderURL);
        if (shader == null){
            shader = ShaderManager.createShader(shaderURL);
        }

        float[] vertices =
                {
                        -1f,1f, // BOTTOM LEFT
                        -1f,-1f,// ,height/2, // BOTTOM TOP
                        1f,-1f, // RIGHT TOP
                        1f,1f // BOTTOM RIGHT



                };
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices);
        buffer.flip();

        vboVertices = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);


    }

    public void draw(Framebuffer framebuffer){
        shader.setUniformi("sampler",0);
        shader.setUniform2f("resolution",new Vector2f(Settings.WIDTH, Settings.HEIGHT));

        glActiveTexture(GL_TEXTURE0);
        framebuffer.bindTexture();

        glEnableVertexAttribArray(0);


        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glVertexAttribPointer(0,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);

    }
    public void unbind(){
        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);
    }
}

package cz.Empatix.Render;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class    LoadingScreen {
    private Shader shader;
    private final int idTexture;
    private int vboVertices;

    private Animation animation;

    public LoadingScreen() {
        shader = ShaderManager.getShader("shaders\\loading");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\loading");
        }

        float[] vertices =
                {
                        -1f,1f, // BOTTOM LEFT
                        -1f,-1f,// ,height/2, // BOTTOM TOP
                        1f,-1f, // RIGHT TOP
                        1f,1f, // BOTTOM RIGHT



                };
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices);
        buffer.flip();

        vboVertices = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);


        Sprite[] sprites = new Sprite[8];
        for(int i = 0; i < sprites.length; i++) {
            float[] texCoords =
                    {
                            (float) i/8,0,

                            (float)i/8,1F,

                            (1.0f+i)/8,1f,

                            (1.0f+i)/8,0
                    };
            Sprite sprite = new Sprite(texCoords);
            sprites[i] = sprite;

        }

        ByteBufferImage decoder = new ByteBufferImage();
        ByteBuffer image = decoder.decodeImage("Textures\\Menu\\loading.tga");

        idTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, idTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,image);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        STBImage.stbi_image_free(image);

        animation = new Animation();
        animation.setFrames(sprites);
        animation.setDelay(100);
    }

    public void draw() {
        shader.bind();
        shader.setUniformi("sampler",0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,idTexture);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glVertexAttribPointer(0,2,GL_FLOAT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
    public void update(){
        animation.update();
    }
}

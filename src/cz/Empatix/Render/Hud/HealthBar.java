package cz.Empatix.Render.Hud;

import cz.Empatix.Graphics.ByteBufferImage;
import cz.Empatix.Graphics.Model.ModelManager;
import cz.Empatix.Graphics.Shaders.Shader;
import cz.Empatix.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;

public class HealthBar extends HUD{
    private int previousHealth;

    private final int vboVerticesBar;
    private final int idTextureBar;
    private final int width;
    private final int height;

    private Shader barShader;
    private final Matrix4f matrixPos;

    public HealthBar(String file, Vector3f pos, int scale, Camera camera){
        super(file+".tga", pos, scale, camera,HUD.Changing);
        barShader = ShaderManager.getShader("shaders\\shader");
        if (barShader == null){
            barShader = ShaderManager.createShader("shaders\\shader");
        }

        ByteBufferImage decoder = new ByteBufferImage();
        ByteBuffer spritesheetImage = decoder.decodeImage(file+"-bar.tga");

        int width = decoder.getWidth();
        int height = decoder.getHeight();

        idTextureBar = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTextureBar);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        STBImage.stbi_image_free(spritesheetImage);

        int vbo;
        vbo = ModelManager.getModel(width,height);
        if (vbo == -1){
            vbo = ModelManager.createModel(width,height);
        }
        vboVerticesBar = vbo;

        pos.x+=44;
        matrixPos = new Matrix4f().translate(pos).scale(scale);
        camera.hardProjection().mul(matrixPos,matrixPos);

        this.width = width;
        this.height = height;
    }

    @Override
    public void draw() {

        // rendering bar
        barShader.bind();
        barShader.setUniformi("sampler",0);
        barShader.setUniformm4f("projection",matrixPos);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,idTextureBar);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER,vboVerticesBar);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        barShader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);

        // rendering hud of healthbar
        super.draw();
    }
    public void update(int currentHealth, int maxHealth){

        float deltaHealth = 1 - (float)currentHealth / maxHealth;
        if (currentHealth != previousHealth){

            int[] vertices =
                    {
                            -width/2,-height/2, // BOTTOM LEFT
                            -width/2,height/2, // BOTTOM TOP
                            width/2-(int)(width*deltaHealth),height/2, // RIGHT TOP
                            width/2-(int)(width*deltaHealth),-height/2 // BOTTOM RIGHT



                    };
            IntBuffer buffer = BufferUtils.createIntBuffer(vertices.length);
            buffer.put(vertices);
            buffer.flip();

            glBindBuffer(GL_ARRAY_BUFFER,vboVerticesBar);
            glBufferSubData(GL_ARRAY_BUFFER,0,buffer);
        }
        previousHealth = currentHealth;
    }
}

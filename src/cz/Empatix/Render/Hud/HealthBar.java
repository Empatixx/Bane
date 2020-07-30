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

    public HealthBar(String file, Vector3f pos, int scale, int xFix, int yFix){
        super(file+".tga", pos, scale,HUD.Static);
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
        vbo = ModelManager.createChangingModel(width,height);
        vboVerticesBar = vbo;

        pos.x+=xFix;
        pos.y+=yFix;
        matrixPos = new Matrix4f().translate(pos).scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);

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

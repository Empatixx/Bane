package cz.Empatix.Render.Hud;

import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class CheckBox {
    public static void load(){
        Loader.loadImage("Textures\\Menu\\checkbox.tga");
    }

    private final float minX;
    private final float maxX;

    private final float minY;
    private final float maxY;

    private int type;

    private Shader shader;
    private int idTexture;

    int[] vboTextures;
    int vboVertices;

    private Matrix4f matrixPos;

    private boolean selected;

    /**
     *
     * @param pos - position of menu bar
     * @param scale - scaling of texture(width*scale,height*scale)
     * @param width - width on screen
     * @param height- height on screen
     */
    public CheckBox(Vector3f pos, float scale, int width, int height){
        String file = "Textures\\Menu\\checkbox.tga";
        ByteBufferImage decoder = Loader.getImage(file);
        ByteBuffer spritesheetImage = decoder.getBuffer();


        minX = (int)pos.x-width*scale/2;
        minY = (int)pos.y-height*scale/2;

        maxY = (int)pos.y + height*scale/2;
        maxX = (int)pos.x + width*scale/2;

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        idTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(width, height);
        }
        vboTextures = new int[2];
        for(int i = 0;i<2;i++){
            // clicking icon
            float[] texCoords2 =
                        {
                                0,i*0.5f,
                                0,0.5f+i*0.5f,
                                1,0.5f+i*0.5f,
                                1,i*0.5f
                        };

            FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords2.length);
            buffer.put(texCoords2);
            buffer.flip();
            vboTextures[i] = glGenBuffers();

            glBindBuffer(GL_ARRAY_BUFFER,vboTextures[i]);
            glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER,0);
        }

        matrixPos = new Matrix4f()
                .translate(pos)
                .scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);

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

        glBindBuffer(GL_ARRAY_BUFFER,selected ? vboTextures[1] : vboTextures[0]);
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
    public boolean intersects(float x, float y){
        return (x >= minX && x <= maxX && y >= minY && y <= maxY);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}

package cz.Empatix.Render.Graphics.Sprites;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

public class Sprite {
    private final int vboTextures;
    private float[] texCoords;
    public Sprite(float[] texCoords){
        this.texCoords = texCoords;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
        buffer.put(texCoords);
        buffer.flip();
        vboTextures = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

    }
    public Sprite(int padding,int curCol,int curRow,int width,int height, int rows,int cols){
        int maxWidth = (cols+1)*padding+cols*width;
        int maxHeight = (rows+1)*padding+rows*height;
        float[] texCoords =
                {
                        (float)(curCol*width+padding*(curCol+1))/maxWidth,(float)(padding*(curRow+1)+height*curRow)/maxHeight,

                        (float)(curCol*width+padding*(curCol+1))/maxWidth,(float)((padding+height)*(curRow+1))/maxHeight,

                        (float)((curCol+1)*width+padding*(curCol+1))/maxWidth,(float)((padding+height)*(curRow+1))/maxHeight,

                        (float)((curCol+1)*width+padding*(curCol+1))/maxWidth,(float)(padding*(curRow+1)+height*curRow)/maxHeight
                };
        this.texCoords = texCoords;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
        buffer.put(texCoords);
        buffer.flip();
        vboTextures = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

    }
    public int getVbo() {
        return vboTextures;
    }

    public float[] getTexCoords() {
        return texCoords;
    }
}

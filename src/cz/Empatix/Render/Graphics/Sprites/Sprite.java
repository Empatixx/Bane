package cz.Empatix.Render.Graphics.Sprites;

import org.lwjgl.BufferUtils;

import java.nio.DoubleBuffer;

import static org.lwjgl.opengl.GL15.*;

public class Sprite {
    private final int vboTextures;
    public Sprite(double[] texCoords){
        DoubleBuffer buffer = BufferUtils.createDoubleBuffer(texCoords.length);
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
        double[] texCoords =
                {
                        (double)(curCol*width+padding*(curCol+1))/maxWidth,(double)(padding*(curRow+1)+height*curRow)/maxHeight,

                        (double)(curCol*width+padding*(curCol+1))/maxWidth,(double)((padding+height)*(curRow+1))/maxHeight,

                        (double)((curCol+1)*width+padding*(curCol+1))/maxWidth,(double)((padding+height)*(curRow+1))/maxHeight,

                        (double)((curCol+1)*width+padding*(curCol+1))/maxWidth,(double)(padding*(curRow+1)+height*curRow)/maxHeight
                };
        DoubleBuffer buffer = BufferUtils.createDoubleBuffer(texCoords.length);
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
}

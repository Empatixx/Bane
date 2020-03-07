package cz.Empatix.Graphics.Sprites;

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
    public int getVbo() {
        return vboTextures;
    }
}

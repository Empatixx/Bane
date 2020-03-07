package cz.Empatix.Graphics.Model;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL15.*;

public class ModelManager {
    private final static HashMap<String,Integer> models = new HashMap<>();

    public static int getModel(int width, int height){
        if  (models.get(width+":"+height) != null){
            return models.get(width+":"+height);
        }
        return -1;
    }
    public static int createModel(int width, int height){
        int[] vertices =
                {
                        -width/2,-height/2, // BOTTOM LEFT
                        -width/2,height/2, // BOTTOM TOP
                        width/2,height/2, // RIGHT TOP
                        width/2,-height/2 // BOTTOM RIGHT



                };

        String buildInt = width+":"+height;
        IntBuffer buffer = BufferUtils.createIntBuffer(vertices.length);
        buffer.put(vertices);
        buffer.flip();

        int vboVertexes = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboVertexes);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

        models.put(buildInt,vboVertexes);

        return vboVertexes;
    }
    public static int createChangingModel(int width, int height){
        int[] vertices =
                {
                        -width/2,-height/2, // BOTTOM LEFT
                        -width/2,height/2, // BOTTOM TOP
                        width/2,height/2, // RIGHT TOP
                        width/2,-height/2 // BOTTOM RIGHT



                };

        IntBuffer buffer = BufferUtils.createIntBuffer(vertices.length);
        buffer.put(vertices);
        buffer.flip();

        int vboVertexes = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboVertexes);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

        return vboVertexes;
    }
}

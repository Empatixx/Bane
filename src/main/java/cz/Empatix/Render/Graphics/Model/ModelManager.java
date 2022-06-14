package cz.Empatix.Render.Graphics.Model;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL15.*;

public class ModelManager {
    private final static HashMap<String,Integer> models = new HashMap<>();
    private final static HashMap<String,Integer> modelsNotCentered = new HashMap<>();

    public static int getModel(int width, int height){
        if  (models.get(width+"|"+height) != null){
            return models.get(width+"|"+height);
        }
        return -1;
    }
    public static int getModel(float width, float height){
        if  (models.get(width+"|"+height) != null){
            return models.get(width+"|"+height);
        }
        return -1;
    }
    public static int getModelNotCentered(int width, int height){
        if  (modelsNotCentered.get(width+"|"+height) != null){
            return modelsNotCentered.get(width+"|"+height);
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

        String buildInt = width+"|"+height;
        IntBuffer buffer = BufferUtils.createIntBuffer(vertices.length);
        buffer.put(vertices);
        buffer.flip();

        int vboVertexes = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboVertexes);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

        models.put(buildInt,vboVertexes);

        return vboVertexes;
    }
    public static int createModel(float width, float height){
        float[] vertices =
                {
                        -width/2,-height/2, // BOTTOM LEFT
                        -width/2,height/2, // BOTTOM TOP
                        width/2,height/2, // RIGHT TOP
                        width/2,-height/2 // BOTTOM RIGHT
                };

        String buildInt = width+"|"+height;
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices);
        buffer.flip();

        int vboVertexes = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboVertexes);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

        models.put(buildInt,vboVertexes);

        return vboVertexes;
    }
    public static int createModelNotCentered(int width, int height){
        int[] vertices =
                {
                        0,0, // BOTTOM LEFT
                        0,height, // BOTTOM TOP
                        width,height, // RIGHT TOP
                        width,0 // BOTTOM RIGHT

                };
        String buildInt = width+"|"+height;
        IntBuffer buffer = BufferUtils.createIntBuffer(vertices.length);
        buffer.put(vertices);
        buffer.flip();

        int vboVertexes = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboVertexes);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

        modelsNotCentered.put(buildInt,vboVertexes);

        return vboVertexes;
    }
}

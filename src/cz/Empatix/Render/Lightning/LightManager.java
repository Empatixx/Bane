package cz.Empatix.Render.Lightning;

import cz.Empatix.Entity.MapObject;
import cz.Empatix.Graphics.ByteBufferImage;
import cz.Empatix.Graphics.Framebuffer;
import cz.Empatix.Graphics.Shaders.Shader;
import cz.Empatix.Graphics.Shaders.ShaderManager;
import cz.Empatix.Main.Settings;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;

public class LightManager {
    private static ArrayList<LightPoint> lights;

    private int vboVertices;
    private int vboTexCoords;

    private Shader shader;

    private int noiseTexture;

    public LightManager(){
        lights = new ArrayList<>();
        shader = ShaderManager.getShader("shaders\\light");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\light");
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


        double[] texCoords =
                {
                        0,0,
                        0,1,
                        1,1,
                        1,0
                };

        DoubleBuffer buffer2 = BufferUtils.createDoubleBuffer(texCoords.length);
        buffer2.put(texCoords);
        buffer2.flip();
        vboTexCoords = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords);
        glBufferData(GL_ARRAY_BUFFER,buffer2,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);


        ByteBufferImage decoder = new ByteBufferImage();
        ByteBuffer bufferNoise = decoder.decodeImage("Textures\\noise.png");
        noiseTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, noiseTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,bufferNoise);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        STBImage.stbi_image_free(bufferNoise);

    }

    public void draw(Framebuffer framebuffer){

        ArrayList<LightPoint> lights = new ArrayList<>(LightManager.lights);
        for(int i = 0;i<lights.size();i++){
            LightPoint light = lights.get(i);
            if(light.isNotOnScreen()){
                lights.remove(light);
                i--;
            }
        }

        shader.bind();

        shader.setUniformi("noise",1);
        shader.setUniformi("texture",0);
        shader.setUniformi("lightCount",lights.size());
        //shader.setUniformf("iTime", (float) GLFW.glfwGetTime());
        shader.setUniform2f("size",new Vector2f(Settings.WIDTH, Settings.HEIGHT));
        shader.setUniformLights(lights.toArray());

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D,noiseTexture);


        glActiveTexture(GL_TEXTURE0);
        framebuffer.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glVertexAttribPointer(0,2,GL_FLOAT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords);
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);
    }
    public static LightPoint createLight(Vector3f color, Vector2f pos, float intensity, MapObject object){
        LightPoint light = new LightPoint(pos,color,intensity,object);
        lights.add(light);

        return light;
    }
    public void update(){
        for (int i = 0;i < lights.size();i++){
            LightPoint light = lights.get(i);
            light.update();
            if(light.shouldRemove()){
                lights.remove(light);
                i--;
            }
        }

    }
}

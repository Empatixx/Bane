package cz.Empatix.Render.Postprocessing.Lightning;

import cz.Empatix.Entity.MapObject;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Framebuffer;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;

public class LightManager {
    private static ArrayList<LightPoint> lights;

    private int vboVertices;

    private Shader shader;

    private int noiseTexture;

    private static TileMap tm;

    public LightManager(TileMap tm){
        LightManager.tm = tm;
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
        shader.setUniformi("enabled",Settings.LIGHTNING ? 1 : 0);
        shader.setUniformf("brightness",Settings.BRIGHTNESS);
        shader.setUniform2f("size",new Vector2f(Settings.WIDTH, Settings.HEIGHT));
        shader.setUniformLights(lights.toArray());

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D,noiseTexture);


        glActiveTexture(GL_TEXTURE0);
        framebuffer.bindTexture();

        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER,vboVertices);
        glVertexAttribPointer(0,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);
    }
    public static LightPoint createLight(Vector3f color, Vector2f pos, float intensity, MapObject object){
        LightPoint light = new LightPoint(tm,pos,color,intensity,object);
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

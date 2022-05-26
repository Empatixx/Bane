package cz.Empatix.Render.Hud;

import cz.Empatix.Java.Loader;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class SliderBar {
    public static void load(){
        Loader.loadImage("Textures\\Menu\\volume_slider.tga");
        Loader.loadImage("Textures\\Menu\\volume_slider_rail.tga");
    }
    // value that is affected by slider
    private float value;

    // background slider
    private float minX;
    private float maxX;

    private float minY;
    private float maxY;

    private Shader shader;
    private int idTexture;

    private int vboTextures;
    private int vboVertices;

    private Matrix4f matrixPos;

    // button slider
    private int idTextureSlider;
    private int vboVerticesSlider;

    private float scale;
    private Vector3f pos;

    private float sliderMin;
    private float sliderMax;

    private int type;

    private float height;
    private float width;
    private int sliderWidth;
    private int sliderHeight;

    private boolean locked;

    private boolean vertical;

    public SliderBar(Vector3f pos, float scale){
        String file = "Textures\\Menu\\volume_slider";
        this.pos = pos;
        this.scale = scale;
        ByteBufferImage decoder = Loader.getImage(file+"_rail.tga");
        ByteBuffer spritesheetImage = decoder.getBuffer();

        width = decoder.getWidth();
        height = decoder.getHeight();

        minX = (int) (pos.x-width*scale/2);
        minY = (int) (pos.y-height*scale/2);

        maxY = (int) (pos.y + height*scale/2);
        maxX = (int) (pos.x + width*scale/2);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        idTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, (int) width,(int) height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        vboVertices = ModelManager.getModel((int) width,(int) height);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel((int) width, (int)height);
        }
        // clicking icon
        float[] texCoords =
                    {
                            0,0,
                            0,1,
                            1,1,
                            1,0
                    };

        FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
        buffer.put(texCoords);
        buffer.flip();
        vboTextures = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);


        matrixPos = new Matrix4f()
                .translate(pos)
                .scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);

        ///
        ///
        decoder = Loader.getImage(file+".tga");
        spritesheetImage = decoder.getBuffer();

        this.sliderWidth = decoder.getWidth();
        this.sliderHeight = decoder.getHeight();

        idTextureSlider = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTextureSlider);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.sliderWidth, sliderHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        vboVerticesSlider = ModelManager.getModel(sliderWidth,sliderHeight);
        if (vboVerticesSlider == -1) {
            vboVerticesSlider = ModelManager.createModel(sliderWidth, sliderHeight);
        }

        sliderMin = (int) (minX+this.sliderWidth/2*scale);
        sliderMax = (int) (maxX-this.sliderWidth/2*scale);


    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public void update(float x, float y){
        if(!vertical){
            pos.x += (x-pos.x) * 6.5f * Game.deltaTime;
            if(pos.x> sliderMax) pos.x = sliderMax;
            if(pos.x< sliderMin) pos.x = sliderMin;
            value = (pos.x- sliderMin)/(sliderMax - sliderMin);
        } else {
            pos.y += (y-pos.y) * 6.5f * Game.deltaTime;
            if(pos.y> sliderMax) pos.y = sliderMax;
            if(pos.y< sliderMin) pos.y = sliderMin;
            value = (pos.y- sliderMin)/(sliderMax - sliderMin);
        }

    }
    public void draw(){

        shader.bind();
        shader.setUniformi("sampler",0);
        shader.setUniformm4f("projection",matrixPos);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,idTexture);

        // background draw
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);


        Matrix4f target = new Matrix4f()
                .translate(pos)
                .scale(scale)
                .rotateX(3.14f);

        Camera.getInstance().hardProjection().mul(target,target);
        shader.bind();
        shader.setUniformi("sampler",0);
        shader.setUniformm4f("projection",target);
        glBindTexture(GL_TEXTURE_2D,idTextureSlider);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVerticesSlider);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);


    }
    public boolean intersects(float x, float y){
        return (x >= minX && x <= maxX && y >= minY && y <= maxY);
    }

    public boolean isLocked() {
        return locked;
    }
    public void unlock(){
        locked=false;
    }

    public void setValue(float value) {
        this.value = value;
        if(vertical){
            pos.y = sliderMin + (sliderMax - sliderMin) * value;
        } else {
            pos.x = sliderMin + (sliderMax - sliderMin) * value;
        }
    }

    public float getValue() {
        return value;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public Vector3f getPos() {
        return pos;
    }

    public void setLength(int size){
        if(!vertical){
            width = size/scale;

            minX = pos.x - size/2;
            maxX = pos.x + size/2;

            sliderMin = (int)(minX+this.sliderWidth/2*scale);
            sliderMax = (int)(maxX-this.sliderWidth/2*scale);

            vboVertices = ModelManager.getModel((int)width,(int) height);
            if (vboVertices == -1) {
                vboVertices = ModelManager.createModel((int) width, (int) height);
            }
        }

    }

    public void setVertical() {
        this.vertical = true;

        minX = pos.x - (int)(height * scale / 2);
        minY = pos.y - (int)(width * scale / 2);

        maxX = pos.x + (int)(height * scale / 2);
        maxY = pos.y + (int)(width * scale / 2);

        sliderMin = (int)(minY+this.sliderWidth/2*scale);
        sliderMax = (int)(maxY-this.sliderWidth/2*scale);

        vboVertices = ModelManager.getModel((int) height, (int)width);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel((int) height, (int) width);
        }
        vboVerticesSlider = ModelManager.getModel(sliderHeight, sliderWidth);
        if (vboVerticesSlider == -1){
            vboVerticesSlider = ModelManager.createModel(sliderHeight, sliderWidth);
        }
        glDeleteBuffers(vboTextures);
        float[] texCoords =
                {
                        0, 1,
                        1, 1,
                        1, 0,
                        0, 0

                };

        FloatBuffer buffer = BufferUtils.createFloatBuffer(texCoords.length);
        buffer.put(texCoords);
        buffer.flip();
        vboTextures = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vboTextures);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}

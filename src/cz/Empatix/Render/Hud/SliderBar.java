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
import java.nio.DoubleBuffer;

import static org.lwjgl.opengl.GL20.*;

public class SliderBar {
    // value that is affected by slider
    private float value;

    // background slider
    private final float minX;
    private final float maxX;

    private final float minY;
    private final float maxY;

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

    private float sliderMinX;
    private float sliderMaxX;

    private int type;



    private boolean locked;
    public SliderBar(String file, Vector3f pos, float scale){
        this.pos = pos;
        this.scale = scale;
        ByteBufferImage decoder = new ByteBufferImage();
        ByteBuffer spritesheetImage = decoder.decodeImage(file+"_rail.tga");

        int width = decoder.getWidth();
        int height = decoder.getHeight();

        minX = pos.x-width*scale/2;
        minY = pos.y-height*scale/2;

        maxY = pos.y + height*scale/2;
        maxX = pos.x + width*scale/2;

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        idTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        STBImage.stbi_image_free(spritesheetImage);

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(width, height);
        }
        // clicking icon
        double[] texCoords =
                    {
                            0,0,
                            0,1,
                            1,1,
                            1,0
                    };

        DoubleBuffer buffer = BufferUtils.createDoubleBuffer(texCoords.length);
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
        decoder = new ByteBufferImage();
        spritesheetImage = decoder.decodeImage(file+".tga");

        width = decoder.getWidth();
        height = decoder.getHeight();

        idTextureSlider = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTextureSlider);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        STBImage.stbi_image_free(spritesheetImage);

        vboVerticesSlider = ModelManager.getModel(width,height);
        if (vboVerticesSlider == -1) {
            vboVerticesSlider = ModelManager.createModel(width, height);
        }

        sliderMinX = minX+width/2*scale;
        sliderMaxX = maxX-width/2*scale;


    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public void update(float x){
        // smooth movement - multiply 0.3
        pos.x += (x-pos.x) * .3f;
        if(pos.x>sliderMaxX) pos.x = sliderMaxX;
        if(pos.x<sliderMinX) pos.x = sliderMinX;
        value = (pos.x-sliderMinX)/(sliderMaxX-sliderMinX);
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
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);


        Matrix4f target = new Matrix4f()
                .translate(pos)
                .scale(scale);

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
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);


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
        pos.x = sliderMinX + (sliderMaxX-sliderMinX) * value;
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
}

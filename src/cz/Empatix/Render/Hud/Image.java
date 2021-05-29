package cz.Empatix.Render.Hud;

import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class Image {
    private Shader shader;
    private int vboVertices;

    private int width;
    private int height;

    private float alpha;
    private float scale;
    private Vector3f pos;

    private Spritesheet spritesheet;

    /**
     *
     * @param file - path of texture
     * @param pos - position of menu bar
     * @param scale - scaling of texture(width*scale,height*scale)
     */
    public Image(String file, Vector3f pos, float scale){
        ByteBufferImage decoder = Loader.getImage(file);

        shader = ShaderManager.getShader("shaders\\image");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\image");
        }

        width = decoder.getWidth();
        height = decoder.getHeight();

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(width, height);
        }

        spritesheet = SpritesheetManager.getSpritesheet(file);

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet(file);
            for(int j = 0;j<2;j++) {

                Sprite[] images = new Sprite[1];
                float[] texCoords =
                        {
                                0, 0,

                                0, 1,

                                1, 1,


                                1, 0
                        };

                Sprite sprite = new Sprite(texCoords);

                images[0] = sprite;

                spritesheet.addSprites(images);
            }
        }
        alpha = 1f;
        this.scale = scale;
        this.pos = pos;
    }

    public void draw(){
        shader.bind();
        shader.setUniformi("sampler",0);
        Matrix4f matrixPos = new Matrix4f()
                .translate(pos)
                .scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);

        shader.setUniformm4f("projection",matrixPos);
        shader.setUniformf("alpha",alpha);

        glActiveTexture(GL_TEXTURE0);
        spritesheet.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glBindBuffer(GL_ARRAY_BUFFER,spritesheet.getSprites(0)[0].getVbo());
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);
    }

    public int getIdTexture() {
        return spritesheet.getIdTexture();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setPosition(Vector3f position){
        this.pos = position;
    }
    public void setScale(float scale){
        this.scale = scale;
    }
    public Vector3f getPos() {
        return pos;
    }

    public int getVboTextures() {
        return spritesheet.getSprites(0)[0].getVbo();
    }

    public int getVboVertices() {
        return vboVertices;
    }
    
}

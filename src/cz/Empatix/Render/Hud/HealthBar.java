package cz.Empatix.Render.Hud;

import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;

public class HealthBar extends HUD{
    public static void load(){
        Loader.loadImage("Textures\\bosshealthbar.tga");
        Loader.loadImage("Textures\\bosshealthbar-bar.tga");
        Loader.loadImage("Textures\\healthBar.tga");
        Loader.loadImage("Textures\\healthBar-bar.tga");
    }
    private int health;
    private int maxHealth;

    private int vboVerticesBar;
    private final int idTextureBar;
    private final int width;
    private final int height;
    private final int scale;
    private Vector3f pos;

    private float delayedHealth;
    private long healthChangeTime;

    private Shader barShader;
    private final Matrix4f matrixPos;

    public HealthBar(String file, Vector3f pos, int scale, int xFix, int yFix){
        super(file+".tga", pos, scale);
        barShader = ShaderManager.getShader("shaders\\healthbar");
        if (barShader == null){
            barShader = ShaderManager.createShader("shaders\\healthbar");
        }

        ByteBufferImage decoder = Loader.getImage(file+"-bar.tga");
        ByteBuffer spritesheetImage = decoder.getBuffer();

        int width = decoder.getWidth();
        int height = decoder.getHeight();

        idTextureBar = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTextureBar);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        vboVerticesBar = ModelManager.getModel(width,height);
        if (vboVerticesBar == -1){
            vboVerticesBar = ModelManager.createModel(width,height);
        }

        pos.x+=xFix;
        pos.y+=yFix;
        matrixPos = new Matrix4f().translate(pos).scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);

        this.width = width;
        this.height = height;
        this.scale = scale;
        this.pos = pos;


    }
    public void initHealth(int health,int maxHealth){
        this.health = health;
        this.maxHealth = maxHealth;

    }

    public void draw() {

        // rendering bar
        barShader.bind();
        barShader.setUniformm4f("projection",matrixPos);

        float minX = pos.x * (float)Settings.WIDTH/1920 - (float)(width * scale)/2*(float)Settings.WIDTH/1920;
        float maxX = minX + (width*scale) * ((float)health/maxHealth)*(float)Settings.WIDTH/1920;
        float premaxX = minX + (width*scale) * (delayedHealth/maxHealth)*(float)Settings.WIDTH/1920;
        // revert height coords because opengl is from down to up 0-1
        float maxY = Settings.HEIGHT - pos.y * (float)Settings.HEIGHT/1080 + (float)((height * scale)/2)*(float)Settings.HEIGHT/1080;

        barShader.setUniformi("resolutionY", Settings.HEIGHT);
        barShader.setUniformf("maxX",maxX);
        // new health removed or added visual
        barShader.setUniformf("premaxX",premaxX);
        barShader.setUniformf("maxY",maxY);
        barShader.setUniformf("stepSize",((float)height*scale/4)*(float)Settings.HEIGHT/1080);
        barShader.setUniform3f("color", new Vector3f(0.529f, 0.298f, 0.262f));

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,idTextureBar);

        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER,vboVerticesBar);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);

        barShader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);

        // rendering hud of healthbar
        super.draw();
    }
    public void update(int currentHealth, int maxHealth){
        if(currentHealth != health){
            health = currentHealth;
            healthChangeTime = System.currentTimeMillis() - InGame.deltaPauseTime();
        }
        if(System.currentTimeMillis() - InGame.deltaPauseTime() - healthChangeTime > 100){
            delayedHealth+=(health - delayedHealth) * 0.07;
        }

        this.maxHealth = maxHealth;
    }
}

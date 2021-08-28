package cz.Empatix.Render.Hud;

import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class ArmorBar extends HUD{
    public static void load(){
        Loader.loadImage("Textures\\armorbar.tga");
        Loader.loadImage("Textures\\armorbar-bar.tga");
    }
    private int armor;
    private int maxArmor;

    private int vboVerticesBar;
    private final int width;
    private final int height;
    private int scale;
    private Vector3f pos;

    private float delayedArmor;
    private long armorChangeTime;

    private Shader barShader;
    private final Matrix4f matrixPos;

    public ArmorBar(String file, Vector3f pos, int scale){
        super(file+".tga", pos, scale);
        barShader = ShaderManager.getShader("shaders\\healthbar");
        if (barShader == null){
            barShader = ShaderManager.createShader("shaders\\healthbar");
        }

        ByteBufferImage decoder = Loader.getImage("Textures\\armorbar-bar.tga");

        int width = decoder.getWidth();
        int height = decoder.getHeight();


        vboVerticesBar = ModelManager.getModel(width,height);
        if (vboVerticesBar == -1){
            vboVerticesBar = ModelManager.createModel(width,height);
        }

        pos.x+=18;
        pos.y+=4;
        matrixPos = new Matrix4f().translate(pos).scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);

        this.width = width;
        this.height = height;
        this.pos = pos;
        this.scale = scale;

    }

    @Override
    public void draw() {

        // rendering bar
        barShader.bind();
        barShader.setUniformm4f("projection",matrixPos);
        float minX = pos.x * (float)Settings.WIDTH/1920 - (float)(width * scale)/2*(float)Settings.WIDTH/1920;
        float maxX = minX + (width*scale) * ((float)armor/maxArmor)*(float)Settings.WIDTH/1920;
        float premaxX = minX + (float)Math.floor((width*scale) * (delayedArmor/maxArmor)*(float)Settings.WIDTH/1920);
        // revert height coords because opengl is from down to up 0-1
        float maxY = Settings.HEIGHT - pos.y * (float)Settings.HEIGHT/1080 + (float)((height * scale)/2)*(float)Settings.HEIGHT/1080;

        barShader.setUniformf("maxX",maxX);
        // new health removed or added visual
        barShader.setUniformf("premaxX",premaxX);
        barShader.setUniformf("maxY",maxY);
        barShader.setUniformf("stepSize",(float)height*scale/4*(float)Settings.HEIGHT/1080);
        barShader.setUniform3f("color", new Vector3f(0.603f, 0.670f, 0.709f));
        

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
    public void update(int currentArmor, int maxArmor){
        if(currentArmor != armor){
            armor = currentArmor;
            armorChangeTime = System.currentTimeMillis() - InGame.deltaPauseTime();
        }
        if(System.currentTimeMillis() - InGame.deltaPauseTime() - armorChangeTime > 100){
            delayedArmor+=(armor - delayedArmor) * 0.07;
        }

        this.maxArmor = maxArmor;
    }
    public void initArmor(int armor,int maxArmor){
        this.armor = armor;
        this.maxArmor = maxArmor;

    }
}
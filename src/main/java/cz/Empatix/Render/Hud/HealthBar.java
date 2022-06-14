package cz.Empatix.Render.Hud;

import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;

public class HealthBar extends HUD{
    public static void load(){
        Loader.loadImage("Textures\\bosshealthbar.tga");
        Loader.loadImage("Textures\\healthBar.tga");
        Loader.loadImage("Textures\\mphealthBar.tga");
        Loader.loadImage("Textures\\mparmorBar.tga");
    }
    private int health;
    private int maxHealth;

    private int vboVerticesBar;
    private final float width;
    private final float height;
    private final int scale;
    private Vector3f pos;

    private float delayedHealth;
    private long healthChangeTime;

    private Shader barShader;
    private Matrix4f matrixPos;

    private boolean showDisplayValues;
    private final TextRender valueTextRender;

    private float minX, maxX, minY, maxY;
    private boolean canHover;

    public HealthBar(String file, Vector3f pos, int scale, float widthBar, float heightBar){
        super(file+".tga", pos, scale);
        barShader = ShaderManager.getShader("shaders\\healthbar");
        if (barShader == null){
            barShader = ShaderManager.createShader("shaders\\healthbar");
        }

        vboVerticesBar = ModelManager.getModel(widthBar,heightBar);
        if (vboVerticesBar == -1){
            vboVerticesBar = ModelManager.createModel(widthBar,heightBar);
        }
        //pos.y+=yFix;

        this.width = widthBar;
        this.height = heightBar;
        this.scale = scale;
        this.pos = pos;

        matrixPos = new Matrix4f().translate(pos).scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);
        valueTextRender = new TextRender();
        minX = (int)pos.x - width*scale/2;
        minY = (int)pos.y - height*scale/2;

        maxY = (int)pos.y + height*scale/2;
        maxX = (int)pos.x + width*scale/2;

        showDisplayValues = false;
    }
    public void initHealth(int health,int maxHealth){
        this.health = health;
        this.maxHealth = maxHealth;
    }
    public void showDisplayValues(boolean value) {
        this.showDisplayValues = value;
    }
    public boolean intersects(float x, float y){
        return (x >= minX && x <= maxX && y >= minY && y <= maxY);
    }

    /**
     * bar inside health bar is not centered in middle in texture, so we must little bit offset the bar so it can totally fit inside healthbar
     * @param x - offset X of bar
     * @param y - offset Y of bar
     */
    public void setOffsetsBar(int x, int y){
        pos.x+=(x/2f)*scale;
        pos.y+=(y/2f)*scale;
        minX = (int)pos.x - width*scale/2;
        minY = (int)pos.y - height*scale/2;

        maxY = (int)pos.y + height*scale/2;
        maxX = (int)pos.x + width*scale/2;
        matrixPos = new Matrix4f().translate(pos).scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);
    }

    public void draw() {
        // rendering bar
        barShader.bind();
        barShader.setUniformm4f("projection",matrixPos);

        float minX = pos.x * (float)Settings.WIDTH/1920 - (width * scale)/2*(float)Settings.WIDTH/1920;
        float maxX = minX + (width*scale) * ((float)health/maxHealth)*(float)Settings.WIDTH/1920;
        float premaxX = minX + (float)Math.floor((width*scale) * (delayedHealth/maxHealth)*(float)Settings.WIDTH/1920);
        // revert height coords because opengl is from down to up 0-1
        float maxY = Settings.HEIGHT - pos.y * (float)Settings.HEIGHT/1080 + (float)((height * scale)/2)*(float)Settings.HEIGHT/1080;

        barShader.setUniformi("resolutionY", Settings.HEIGHT);
        barShader.setUniformf("maxX",maxX);
        // new health removed or added visual
        barShader.setUniformf("premaxX",premaxX);
        barShader.setUniformf("maxY",maxY);
        barShader.setUniformf("stepSize",((float)height*scale/4)*(float)Settings.HEIGHT/1080);
        barShader.setUniform3f("color", new Vector3f(0.529f, 0.298f, 0.262f));

        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER,vboVerticesBar);
        glVertexAttribPointer(0,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);

        barShader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(GL_TEXTURE0);

        // rendering hud of healthbar
        super.draw();
        if(showDisplayValues && canHover){
            Vector3f tpos = new Vector3f(pos);
            tpos.y -= 5 * scale;
            String s = health+"/"+maxHealth;
            tpos.x = TextRender.getHorizontalCenter((int)this.minX,(int)this.maxX,s,2);
            valueTextRender.draw(s,tpos,2,new Vector3f(0.9686f,0.4f,0.09803f));
        }
    }
    public void enableHoverValuesShow(){canHover = true;}
    public void update(int currentHealth, int maxHealth){
        if(currentHealth != health){
            health = currentHealth;
            healthChangeTime = System.currentTimeMillis() - InGame.deltaPauseTime();
        }
        if(System.currentTimeMillis() - InGame.deltaPauseTime() - healthChangeTime > 100){
            delayedHealth+=(health - delayedHealth) * Game.deltaTime * 2;
        }

        this.maxHealth = maxHealth;
    }

}

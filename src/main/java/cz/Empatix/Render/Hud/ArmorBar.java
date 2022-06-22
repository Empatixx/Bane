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

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class ArmorBar extends HUD{
    private int armor;
    private int maxArmor;

    private int vboVerticesBar;
    private final float width;
    private final float height;
    private int scale;
    private Vector3f pos;

    private float delayedArmor;
    private long armorChangeTime;

    private Shader barShader;
    private Matrix4f matrixPos;

    private boolean showDisplayValues;
    private TextRender valueTextRender;

    private float minX, maxX, minY, maxY;
    private boolean canHover;

    public ArmorBar(String file, Vector3f pos, int scale, float widthBar, float heightBar){
        super(file+".tga", pos, scale);
        barShader = ShaderManager.getShader("shaders\\healthbar");
        if (barShader == null){
            barShader = ShaderManager.createShader("shaders\\healthbar");
        }

        vboVerticesBar = ModelManager.getModel(widthBar,heightBar);
        if (vboVerticesBar == -1){
            vboVerticesBar = ModelManager.createModel(widthBar,heightBar);
        }

        matrixPos = new Matrix4f().translate(pos).scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);

        this.width = widthBar;
        this.height = heightBar;
        this.pos = pos;
        this.scale = scale;

        valueTextRender = new TextRender();
        minX = (int)pos.x - width*scale/2;
        minY = (int)pos.y - height*scale/2;

        maxY = (int)pos.y + height*scale/2;
        maxX = (int)pos.x + width*scale/2;

        showDisplayValues = false;
    }
    /**
     * bar inside health bar is not centered in middle in texture, so we must little bit offset the bar so it can totally fit inside healthbar
     * @param x - offset X of bar
     * @param y - offset Y of bar
     */
    public void setOffsetsBar(float x, float y){
        pos.x+=(x/2f)*scale;
        pos.y+=(y/2f)*scale;
        minX = (int)pos.x - width*scale/2;
        minY = (int)pos.y - height*scale/2;

        maxY = (int)pos.y + height*scale/2;
        maxX = (int)pos.x + width*scale/2;
        matrixPos = new Matrix4f().translate(pos).scale(scale);
        Camera.getInstance().hardProjection().mul(matrixPos,matrixPos);
    }
    @Override
    public void draw() {

        // rendering bar
        barShader.bind();
        barShader.setUniformm4f("projection",matrixPos);
        double minX = pos.x * (double)Settings.WIDTH/1920 - (double)(width * scale)/2*(double)Settings.WIDTH/1920;
        double maxX = minX + (width*scale) * ((double)armor/maxArmor)*(double)Settings.WIDTH/1920;
        double premaxX = minX + Math.floor((width*scale) * (delayedArmor/maxArmor)*(double)Settings.WIDTH/1920);
        // revert height coords because opengl is from down to up 0-1
        double maxY = Settings.HEIGHT - pos.y * (double)Settings.HEIGHT/1080 + (double)((height * scale)/2)*(double)Settings.HEIGHT/1080;

        barShader.setUniformf("maxX",(float)maxX);
        // new health removed or added visual
        barShader.setUniformf("premaxX",(float)premaxX);
        barShader.setUniformf("maxY",(float)maxY);
        barShader.setUniformf("stepSize",height*scale/4*(float)Settings.HEIGHT/1080);
        barShader.setUniform3f("color", new Vector3f(0.603f, 0.670f, 0.709f));


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
            String s = armor+"/"+maxArmor;
            tpos.x = TextRender.getHorizontalCenter((int)this.minX,(int)this.maxX,s,1);
            valueTextRender.draw(s,tpos,1,new Vector3f(0.207f, 0.603f, 0.815f));
        }
    }
    public void update(int currentArmor, int maxArmor){
        if(currentArmor != armor){
            armor = currentArmor;
            armorChangeTime = System.currentTimeMillis() - InGame.deltaPauseTime();
        }
        if(System.currentTimeMillis() - InGame.deltaPauseTime() - armorChangeTime > 100){
            delayedArmor+=(armor - delayedArmor) * Game.deltaTime * 2;
        }

        this.maxArmor = maxArmor;
    }
    public void initArmor(int armor,int maxArmor){
        this.armor = armor;
        this.maxArmor = maxArmor;

    }
    public void showDisplayValues(boolean value) {
        this.showDisplayValues = value;
    }
    public void enableHoverValuesShow(){canHover = true;}

    public boolean intersects(float x, float y){
        return (x >= minX && x <= maxX && y >= minY && y <= maxY);
    }
}
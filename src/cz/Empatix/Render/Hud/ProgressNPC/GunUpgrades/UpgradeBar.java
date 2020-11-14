package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;

import cz.Empatix.Entity.Player;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Hud.Image;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;


public abstract class UpgradeBar {
    private Spritesheet iconset;
    private Shader shader;
    private int vboVerticles;

    private Image weapon;
    private Image bar;
    private Image clickedBar;

    private boolean clicked;

    private final float minX;
    private final float maxX;

    private final float minY;
    private final float maxY;

    private Vector3f position;

    ArrayList<UpgradeSideBar> sideBars;

    public UpgradeBar(String filepath, float weaponscale, int row){
        Vector3f pos = new Vector3f(680,240+row*120,0);
        position = pos;

        weapon = new Image(filepath,pos,weaponscale);
        bar = new Image("Textures\\ProgressRoom\\upgradetab-guns.tga", pos,1.5f);
        clickedBar = new Image("Textures\\ProgressRoom\\upgradetab-guns-clicked.tga", pos,1.5f);

        int width = bar.getWidth();
        int height = bar.getHeight();
        float scale = 1.5f;

        minX = (int)pos.x-width*scale/2;
        minY = (int)pos.y-height*scale/2;

        maxY = (int)pos.y + height*scale/2;
        maxX = (int)pos.x + width*scale/2;

        sideBars = new ArrayList<>();

        vboVerticles = ModelManager.getModel(32,32);
        if (vboVerticles == -1){
            vboVerticles = ModelManager.createModel(32,32);
        }

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        // try to find spritesheet if it was created once
        iconset = SpritesheetManager.getSpritesheet("Textures\\ProgressRoom\\upgradeicons.tga");

        // creating a new spritesheet
        if (iconset == null){
            iconset = SpritesheetManager.createSpritesheet("Textures\\ProgressRoom\\upgradeicons.tga");
            Sprite[] images = new Sprite[4];

            for (int j = 0; j < 4; j++) {

                double[] texCoords =
                        {
                                (double) j / 4, 0,
                                (double) j / 4, 1,
                                (1.0 + j) / 4, 1,
                                (1.0 + j) / 4, 0
                        };
                Sprite sprite = new Sprite(texCoords);

                images[j] = sprite;
            }
            iconset.addSprites(images);
        }
    }

    public void draw(){
        if(clicked){
            clickedBar.draw();
        } else {
            bar.draw();
        }
        weapon.draw();
    }
    public void drawUpgrades(){
        boolean locked = false;
        for (UpgradeSideBar sideBar:sideBars
        ) {
            sideBar.draw(locked);
            locked = !sideBar.isBought();
            int type = sideBar.getType();
            Vector3f pos = sideBar.getPosition();
            Vector3f newPos = new Vector3f(pos.x-205,pos.y,pos.z);
            Matrix4f target;
            target = new Matrix4f().translate(newPos).scale(2);

            Camera.getInstance().hardProjection().mul(target,target);
            shader.bind();
            shader.setUniformi("sampler",0);
            shader.setUniformm4f("projection",target);
            glActiveTexture(GL_TEXTURE0);
            iconset.bindTexture();

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);


            glBindBuffer(GL_ARRAY_BUFFER, vboVerticles);
            glVertexAttribPointer(0,2,GL_INT,false,0,0);


            glBindBuffer(GL_ARRAY_BUFFER,iconset.getSprites(0)[type].getVbo());
            glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER,0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            shader.unbind();
            glBindTexture(GL_TEXTURE_2D,0);
            glActiveTexture(0);
        }
    }
    public boolean intersects(float x, float y){
        return (x >= minX && x <= maxX && y >= minY && y <= maxY);
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }
    public void mouseClick(float x,float y, Player p){
        for(UpgradeSideBar sideBar:sideBars){
            sideBar.mouseClick(x,y, p);
            if(!sideBar.isBought()) break;
        }
    }
    public void mouseHover(float x,float y){
        for(UpgradeSideBar sideBar:sideBars){
            sideBar.mouseHover(x,y);
        }
    }

    public boolean isClicked() {
        return clicked;
    }

    public void update(float y) {
        Vector3f pos = new Vector3f(position.x,position.y+y,0);
        weapon.setPosition(pos);
        bar.setPosition(pos);
        clickedBar.setPosition(pos);
    }
}
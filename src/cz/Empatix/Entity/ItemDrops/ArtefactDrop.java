package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

public class ArtefactDrop extends ItemDrop {
    private int vboVerticesWeapon;
    private int vboTexturesWeapon;
    private int textureId;

    private boolean canPick;

    private final Artefact artefact;

    private Shader outlineShader;

    private final int textureWidth;
    private final int textureHeight;


    public ArtefactDrop(TileMap tm, Artefact artefact){
        super(tm);
        this.artefact = artefact;
        type = ARTEFACT;
        canDespawn = false;
        liveTime = System.currentTimeMillis()-InGame.deltaPauseTime();
        pickedUp = false;

        Image imageOfWeapon = artefact.getImageArtefact();

        width=cwidth=imageOfWeapon.getWidth();
        height=cheight=imageOfWeapon.getHeight();
        scale = artefact.getScale();
        facingRight = true;

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        vboTexturesWeapon = imageOfWeapon.getVboTextures();
        vboVerticesWeapon = imageOfWeapon.getVboVertices();


        textureId = imageOfWeapon.getIdTexture();
        textureWidth = width;
        textureHeight = height;

        light = LightManager.createLight(new Vector3f(1.0f,0.8274f,0.0f),new Vector2f(0,0),1.25f,this);

        stopSpeed = 0.35f;

        cwidth*=scale;
        cheight*=scale;

        outlineShader = ShaderManager.getShader("shaders\\outline");
        if (outlineShader == null){
            outlineShader = ShaderManager.createShader("shaders\\outline");
        }
    }
    public void draw(){

        long timeNow = System.currentTimeMillis();
        if((float)(timeNow - liveTime - InGame.deltaPauseTime())/1000 > 25  && canDespawn){
            if((timeNow - liveTime-InGame.deltaPauseTime()) / 10 % 2 == 0) return;
        }

        setMapPosition();

        // pokud neni object na obrazovce - zrusit
        if (isNotOnScrean()){
            return;
        }
        Matrix4f target;
        if (facingRight) {
            target = new Matrix4f().translate(position)
                    .scale(scale);
        } else {
            target = new Matrix4f().translate(position)
                    .scale(scale)
                    .rotateY(3.14f);

        }
        Camera.getInstance().projection().mul(target,target);

        shader.bind();
        shader.setUniformi("sampler",0);
        shader.setUniformm4f("projection",target);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,textureId);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVerticesWeapon);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);


        glBindBuffer(GL_ARRAY_BUFFER, vboTexturesWeapon);
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);



        if(canPick){
            outlineShader.bind();
            outlineShader.setUniformi("sampler",0);
            outlineShader.setUniformm4f("projection",target);
            outlineShader.setUniformf("outlineAlpha",1f);
            outlineShader.setUniform2f("stepSize",new Vector2f(1f/textureWidth,1f/textureHeight));
            outlineShader.setUniform3f("color",new Vector3f(0.949f, 0.933f, 0.027f));

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D,textureId);

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);


            glBindBuffer(GL_ARRAY_BUFFER, vboVerticesWeapon);
            glVertexAttribPointer(0,2,GL_INT,false,0,0);


            glBindBuffer(GL_ARRAY_BUFFER, vboTexturesWeapon);
            glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER,0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
        }

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);

        if (Game.displayCollisions){
            glColor3i(255,255,255);
            glBegin(GL_LINE_LOOP);
            // BOTTOM LEFT
            glVertex2f(position.x+xmap-cwidth/2,position.y+ymap-cheight/2);
            // TOP LEFT
            glVertex2f(position.x+xmap-cwidth/2, position.y+ymap+cheight/2);
            // TOP RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap+cheight/2);
            // BOTTOM RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap-cheight/2);
            glEnd();

            glPointSize(10);
            glColor3i(255,0,0);
            glBegin(GL_POINTS);
            glVertex2f(position.x+xmap,position.y+ymap);
            glEnd();
        }
    }

    @Override
    public boolean intersects(MapObject o) {
        Rectangle o1 = getRectangleArtefact();
        Rectangle o2 = o.getRectangle();
        return o2.intersects(o1);
    }
    private Rectangle getRectangleArtefact() {
        return new Rectangle(
                (int)position.x-cwidth/2-25,
                (int)position.y-cheight/2-25,
                cwidth+50,
                cheight+50
        );
    }

    @Override
    public void update() {
        super.update();
        checkTileMapCollision();
        setPosition(temp.x, temp.y);
    }

    public void setCanPick(boolean canPick) {
        this.canPick = canPick;
    }

    public boolean isCanPick() {
        return canPick;
    }

    public double distance(float px, float py){
        return Math.sqrt(Math.pow(position.x-px,2)+Math.pow(position.y-py,2));
    }

    public Artefact getArtefact() {
        return artefact;
    }

}

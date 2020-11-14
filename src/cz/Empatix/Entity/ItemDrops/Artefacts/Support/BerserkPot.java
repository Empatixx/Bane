package cz.Empatix.Entity.ItemDrops.Artefacts.Support;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class BerserkPot extends Artefact {
    private long time;
    private float bonusSpeed;
    private boolean removedSpeed;

    private ArrayList<SprintParticle> sprintParticles;
    private long lastTimeSprintParticle;

    public BerserkPot(TileMap tm, Player p){
        super(tm,p);
        maxCharge = 4;
        charge = 0;

        scale = 3f;

        imageArtefact = new Image("Textures\\artefacts\\berserkpot.tga",new Vector3f(1401,975,0),
                scale);
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        rarity = 1;

        removedSpeed = true;
        sprintParticles = new ArrayList<>();

    }
    @Override
    protected void update() {

        if(!removedSpeed){
            if(System.currentTimeMillis() - time - InGame.deltaPauseTime() > 20000){
                removedSpeed = true;
                p.setMaxSpeed(p.getMaxSpeed()-bonusSpeed);

            }
            Vector3f speed = p.getSpeed();float maxSpeed = p.getMaxSpeed();

            if(Math.abs(speed.x) >= maxSpeed || Math.abs(speed.y) >= maxSpeed){

                Vector3f position = p.getPosition();
                boolean up = p.isMovingUp(), down = p.isMovingDown(), left = p.isMovingLeft(), right = p.isMovingRight();
                int height = p.getCheight();

                float value = Math.abs(speed.x);
                if(value < Math.abs(speed.y)) value = Math.abs(speed.y);
                if(System.currentTimeMillis() - InGame.deltaPauseTime() - lastTimeSprintParticle > 500-value*20){
                    lastTimeSprintParticle = System.currentTimeMillis()- InGame.deltaPauseTime();
                    SprintParticle sprintParticle = new SprintParticle(tm);
                    if((up || down) && !left && !right){
                        sprintParticle.setPosition(
                                position.x+16*(float)Math.sin(2*Math.PI*((System.currentTimeMillis()%1000)/1000d)),
                                position.y+height/2);
                    } else if((right || left) && !up && !down){
                        sprintParticle.setPosition(
                                position.x+(right ? -25 : 0)+(left ? 25 : 0),
                                position.y+height/2+16*(float)Math.sin(Math.PI*(1+((System.currentTimeMillis()%1000)/1000d))));
                    } else {
                        sprintParticle.setPosition(position.x,position.y+height/2);

                    }
                    sprintParticles.add(sprintParticle);
                }
            }
        }
        for(int i = 0;i<sprintParticles.size();i++){
            SprintParticle sprintParticle = sprintParticles.get(i);

            sprintParticle.update();
            if(sprintParticle.shouldRemove()){
                sprintParticles.remove(i);
                i--;
            }
        }
    }

    @Override
    protected void draw() {

        for(SprintParticle sprintParticle : sprintParticles){
            sprintParticle.draw();
        }
    }

    @Override
    protected void drawHud() {
        imageArtefact.draw();
        Matrix4f matrixPos;

        geometryShader.bind();

        for(int i = 0;i<charge;i++){
            if(chargeAnimation == i && charge == maxCharge){
                geometryShader.setUniform3f("color", new Vector3f(0.141f, 0.980f, 0));
            } else {
                geometryShader.setUniform3f("color", new Vector3f(0.035f, 0.784f, 0.117f));
            }

            matrixPos = new Matrix4f()
                    .translate(new Vector3f( 1376+16*i,1055,0));
            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
            geometryShader.setUniformm4f("projection", matrixPos);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glDisableVertexAttribArray(0);
        }
        geometryShader.unbind();


        chargeBar.draw();
    }

    @Override
    protected void activate() {
        charge = 0;
        removedSpeed = false;
        // refills player armor to full
        bonusSpeed = p.getMaxSpeed()*0.25f;
        p.setMaxSpeed(p.getMaxSpeed()*1.25f);
        time = System.currentTimeMillis() - InGame.deltaPauseTime();
    }

    @Override
    protected void charge() {
        charge++;
        if(charge > maxCharge) charge = maxCharge;
    }

    private static class SprintParticle extends MapObject {
        // sprint particles
        SprintParticle(TileMap tm){
            super(tm);
            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\artefacts\\berserkpot-particle.tga");


            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\artefacts\\berserkpot-particle.tga");
                for(int i = 0; i < 3; i++) {

                    Sprite[] images = new Sprite[3];

                    for (int j = 0; j < 3; j++) {

                        double[] texCoords =
                                {
                                        (double) j / 3, 0,

                                        (double) j / 3, 1,

                                        (1.0 + j) / 3, 1,

                                        (1.0 + j) / 3, 0
                                };


                        Sprite sprite = new Sprite(texCoords);

                        images[j] = sprite;

                    }

                    spritesheet.addSprites(images);
                }
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(0));
            animation.setDelay(85);

            vboVertices = ModelManager.getModel(16,16);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(16,16);
            }
            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }

            facingRight = true;
            scale = 4;
        }
        public boolean shouldRemove(){
            return animation.hasPlayedOnce();
        }
        public void update(){
            setMapPosition();
            animation.update();
        }
        public void draw(){
            super.draw();
        }
    }
}

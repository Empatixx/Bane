package cz.Empatix.Entity.ItemDrops.Artefacts.Support;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class BerserkPot extends Artefact {
    public static void load(){
        Loader.loadImage("Textures\\artefacts\\berserkpot.tga");
        Loader.loadImage("Textures\\artefacts\\artifactcharge.tga");
        Loader.loadImage("Textures\\artefacts\\berserkpot-particle.tga");
    }
    private long time;
    private float bonusSpeed;
    private boolean removedSpeed;

    transient private ArrayList<SprintParticle> sprintParticles;
    private long lastTimeSprintParticle;

    private float timeLeft;

    private TextRender textRender;
    private long flichingDelay;

    public BerserkPot(TileMap tm, Player p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;

        scale = 2f;

        imageArtefact = new Image("Textures\\artefacts\\berserkpot.tga",new Vector3f(1401,975,0),
                scale);
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        rarity = 1;

        removedSpeed = true;
        sprintParticles = new ArrayList<>(5);

        textRender = new TextRender();

    }
    public BerserkPot(TileMap tm, Player[] p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;
        rarity = 1;
        removedSpeed = true;
    }
    @Override
    public void update(boolean pause) {

        if(!removedSpeed){

            if(!pause){
                timeLeft = (System.currentTimeMillis() - this.time - InGame.deltaPauseTime() )/ 1000f;
                timeLeft = 20 - timeLeft;
            }

            if(System.currentTimeMillis() - time - InGame.deltaPauseTime() > 20000){
                removedSpeed = true;
                p[0].setMaxSpeed(p[0].getMaxSpeed()-bonusSpeed);

            }
            Vector3f speed = p[0].getSpeed();float maxSpeed = p[0].getMaxSpeed();

            if(Math.abs(speed.x) >= maxSpeed || Math.abs(speed.y) >= maxSpeed){

                Vector3f position = p[0].getPosition();
                boolean up = p[0].isMovingUp(), down = p[0].isMovingDown(), left = p[0].isMovingLeft(), right = p[0].isMovingRight();
                int height = p[0].getCheight();

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
    public void update(String username) {
        if(!removedSpeed){
            for(Player player : p){
                if(player != null) {
                    if(((PlayerMP)player).getUsername().equalsIgnoreCase(username)) continue;
                    timeLeft = (System.currentTimeMillis() - this.time )/ 1000f;
                    timeLeft = 20 - timeLeft;

                    if(System.currentTimeMillis() - time > 20000){
                        removedSpeed = true;
                        player.setMaxSpeed(player.getMaxSpeed()-bonusSpeed);

                    }
                    Vector3f speed = player.getSpeed();float maxSpeed = player.getMaxSpeed();

                    if(Math.abs(speed.x) >= maxSpeed || Math.abs(speed.y) >= maxSpeed){

                        Vector3f position = player.getPosition();
                        boolean up = player.isMovingUp(), down = player.isMovingDown(), left = player.isMovingLeft(), right = player.isMovingRight();
                        int height = player.getCheight();

                        if(!tm.isServerSide()){
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
                    break;
                }
            }
        }
        if(!tm.isServerSide()){
            for(int i = 0;i<sprintParticles.size();i++){
                SprintParticle sprintParticle = sprintParticles.get(i);

                sprintParticle.update();
                if(sprintParticle.shouldRemove()){
                    sprintParticles.remove(i);
                    i--;
                }
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
                geometryShader.setUniform3f("color", new Vector3f(0.109f, 0.552f, 0.203f));
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
        if(!removedSpeed){
            if(System.currentTimeMillis() - flichingDelay < 150 && time < 3){
                return;
            }
            flichingDelay = System.currentTimeMillis();
            float value = timeLeft/20;
            float b = 0f,r,g;
            if (value <= 0.5f){
                r = 1.f;
                g = 0.f + 2 * value;
            } else{
                r = 2 * (1-value);
                g = 1f;
            }

            float center = TextRender.getHorizontalCenter(1340,1465,String.format("%.1f",timeLeft) + "s",2);
            textRender.draw(String.format("%.1f",timeLeft) + "s",new Vector3f(center,900,0),2,new Vector3f(r,g,b));
        }
    }

    @Override
    public void activate() {
        charge = 0;
        removedSpeed = false;
        // refills player armor to full
        bonusSpeed = p[0].getMaxSpeed()*0.25f;
        p[0].setMaxSpeed(p[0].getMaxSpeed()*1.25f);
        time = System.currentTimeMillis() - InGame.deltaPauseTime();
    }
    @Override
    public void activate(String username) {
        charge = 0;
        removedSpeed = false;
        // refills player armor to full
        for(Player player : p){
            if(player == null) continue;
            if(((PlayerMP)player).getUsername().equalsIgnoreCase(username)){
                bonusSpeed = player.getMaxSpeed()*0.25f;
                player.setMaxSpeed(player.getMaxSpeed()*1.25f);
                time = System.currentTimeMillis() - InGame.deltaPauseTime();
                break;
            }
        }
    }
    @Override
    public void charge() {
        charge++;
        if(charge > maxCharge) charge = maxCharge;
    }

    @Override
    public void loadSave() {
        imageArtefact = new Image("Textures\\artefacts\\berserkpot.tga",new Vector3f(1401,975,0),
                scale);
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        sprintParticles = new ArrayList<>(5);
    }

    private static class SprintParticle extends MapObject implements Serializable {
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

                        float[] texCoords =
                                {
                                        (float) j / 3, 0,

                                        (float) j / 3, 1,

                                        (1.0f + j) / 3, 1,

                                        (1.0f + j) / 3, 0
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

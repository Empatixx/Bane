package cz.Empatix.Entity.ItemDrops.Artefacts.Support;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Loader;
import cz.Empatix.Multiplayer.ArtefactManagerMP;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightPoint;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class ShieldHorn extends Artefact {
    public static void load(){
        Loader.loadImage("Textures\\artefacts\\shieldhorn.tga");
        Loader.loadImage("Textures\\artefacts\\magicshield.tga");
    }
    private boolean shield;
    private Spritesheet magicShield;
    private Animation animation;
    private int vboShield;
    private Shader shader;
    private Vector3f position;
    private LightPoint light;
    public ShieldHorn(TileMap tm, Player p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;

        scale = 4f;

        imageArtefact = new Image("Textures\\artefacts\\shieldhorn.tga",new Vector3f(1400,975,0),
                scale  );

        // try to find spritesheet if it was created once
        magicShield = SpritesheetManager.getSpritesheet("Textures\\artefacts\\magicshield.tga");

        // creating a new spritesheet
        if (magicShield == null){
            magicShield = SpritesheetManager.createSpritesheet("Textures\\artefacts\\magicshield.tga");
                Sprite[] images = new Sprite[4];

                for (int j = 0; j < 4; j++) {

                    float[] texCoords =
                            {
                                    (float) j / 4, 0,

                                    (float) j / 4, 1,

                                    (1.0f + j) / 4, 1,

                                    (1.0f + j) / 4, 0
                            };


                    Sprite sprite = new Sprite(texCoords);

                    images[j] = sprite;

                }

                magicShield.addSprites(images);
        }

        animation = new Animation();
        animation.setFrames(magicShield.getSprites(0));
        animation.setDelay(100);

        vboShield = ModelManager.getModel(64,64);
        if (vboShield == -1){
            vboShield = ModelManager.getModel(64,64);
        }
        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        rarity = 1;

        position = p.getPosition();
    }
    public ShieldHorn(TileMap tm, Player[] p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;
        rarity = 1;

        scale = 4f;
        if(!tm.isServerSide()){
            imageArtefact = new Image("Textures\\artefacts\\shieldhorn.tga",new Vector3f(1400,975,0),
                    scale  );

            // try to find spritesheet if it was created once
            magicShield = SpritesheetManager.getSpritesheet("Textures\\artefacts\\magicshield.tga");

            // creating a new spritesheet
            if (magicShield == null){
                magicShield = SpritesheetManager.createSpritesheet("Textures\\artefacts\\magicshield.tga");
                Sprite[] images = new Sprite[4];

                for (int j = 0; j < 4; j++) {

                    float[] texCoords =
                            {
                                    (float) j / 4, 0,

                                    (float) j / 4, 1,

                                    (1.0f + j) / 4, 1,

                                    (1.0f + j) / 4, 0
                            };


                    Sprite sprite = new Sprite(texCoords);

                    images[j] = sprite;

                }

                magicShield.addSprites(images);
            }

            animation = new Animation();
            animation.setFrames(magicShield.getSprites(0));
            animation.setDelay(100);

            vboShield = ModelManager.getModel(64,64);
            if (vboShield == -1){
                vboShield = ModelManager.getModel(64,64);
            }
            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }

            chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                    2.6f);
            rarity = 1;
        }
    }
    @Override
    public void updateSP(boolean pause) {
        if(shield) light.update();
        if(!pause){
            animation.update();
        }
    }

    @Override
    public void updateMPClient() {
        if(shield) light.update();
        animation.update();
    }


    @Override
    public void updateMPServer(String username) {
    }

    @Override
    public void handleAddBulletPacket(Network.ArtefactAddBullet addBullet) {

    }

    @Override
    protected void preDraw() {
    }

    @Override
    protected void draw() {
        if(shield){
            Matrix4f target = new Matrix4f().translate(position)
                    .scale(scale);
            Camera.getInstance().projection().mul(target,target);

            shader.bind();
            shader.setUniformi("sampler",0);
            glActiveTexture(GL_TEXTURE0);
            shader.setUniformm4f("projection",target);
            magicShield.bindTexture();

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);

            glBindBuffer(GL_ARRAY_BUFFER, vboShield);
            glVertexAttribPointer(0,2,GL_INT,false,0,0);

            glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());
            glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER,0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            shader.unbind();
            glBindTexture(GL_TEXTURE_2D,0);
            glActiveTexture(GL_TEXTURE0);
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
    }

    @Override
    public void activate() {
        charge = 0;
        shield = true;
        light = LightManager.createLight(new Vector3f(0,0,1),new Vector2f(p[0].getX(),p[0].getY()),3,p[0]);
    }

    @Override
    public void activate(int idUser) {
        charge = 0;
        shield = true;
    }
    @Override
    public void activateClientSide(int idUser) {
        super.activateClientSide(idUser);
        for(Player p : p){
            if(p == null) continue;
            if(((PlayerMP)p).getIdConnection() == idUser){
                light = LightManager.createLight(new Vector3f(0,0,1),new Vector2f(0,0),3,p);
                shield = true;
                light.setPos(p.getX(),p.getY());
                position = p.getPosition();
                break;
            }
        }
        charge = 0;
    }
    @Override
    public void charge() {
        charge++;
        if(charge > maxCharge) charge = maxCharge;
    }

    @Override
    public void despawn() {
        super.despawn();
    }

    @Override
    public void handleHitBulletPacket(Network.HitBullet p) {

    }

    @Override
    public void handleMoveBulletPacket(Network.MoveBullet moveBullet) {

    }

    @Override
    public boolean playerHitEvent() {
        if(shield){
            shield = false;
            if(tm.isServerSide()){
                Network.ArtefactEventState aes = new Network.ArtefactEventState();
                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                mpManager.server.requestACK(aes,aes.idPacket);
                aes.slot = ArtefactManagerMP.getInstance().getArtefactSlot(this);
                aes.state = 0;
                Server server = mpManager.server.getServer();
                server.sendToAllUDP(aes);
            } else {
                light.remove();
                light = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public void playerDropEvent() {
        if(shield) {
            shield = false;
            if(tm.isServerSide()){
                Network.ArtefactEventState aes = new Network.ArtefactEventState();
                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                mpManager.server.requestACK(aes,aes.idPacket);
                aes.slot = ArtefactManagerMP.getInstance().getArtefactSlot(this);
                aes.state = 1;
                Server server = MultiplayerManager.getInstance().server.getServer();
                server.sendToAllUDP(aes);
            } else {
                light.remove();
                light = null;
            }
        }
    }
}
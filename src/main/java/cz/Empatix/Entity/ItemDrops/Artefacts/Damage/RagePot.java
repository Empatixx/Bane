package cz.Empatix.Entity.ItemDrops.Artefacts.Damage;

import cz.Empatix.Buffs.Buff;
import cz.Empatix.Buffs.BuffManager;
import cz.Empatix.Buffs.BuffManagerMP;
import cz.Empatix.Buffs.RageBuff;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.Player;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class RagePot extends Artefact {
    public static void load(){
        Loader.loadImage("Textures\\artefacts\\ragepot.tga");
    }
    private TextRender textRender;
    private long flinchingDelay;

    public RagePot(TileMap tm, Player p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;

        scale = 3f;

        imageArtefact = new Image("Textures\\artefacts\\ragepot.tga",new Vector3f(1401,975,0),
                scale);
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        rarity = 1;

        textRender = new TextRender();

    }
    public RagePot(TileMap tm, Player[] p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;
        rarity = 1;

        scale = 3f;
        if(!tm.isServerSide()){
            imageArtefact = new Image("Textures\\artefacts\\berserkpot.tga",new Vector3f(1401,975,0),
                    scale);
            chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                    2.6f);
            textRender = new TextRender();
        }
    }
    @Override
    public void updateSP(boolean pause) {
    }

    @Override
    public void updateMPClient() {
    }

    @Override
    public void updateMPServer(String username) {
        BuffManagerMP buffManager = BuffManagerMP.getInstance();
        if(!buffManager.isActiveBuff(Buff.RAGE,idUser)){
            idUser = 0;
        }
    }

    @Override
    public void handleAddBulletPacket(Network.ArtefactAddBullet addBullet) {

    }

    @Override
    protected void preDraw() {
    }

    @Override
    protected void draw() {

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
        BuffManager buffManager = BuffManager.getInstance();
        if(buffManager.isActiveBuff(Buff.RAGE)){
            float timeLeft = buffManager.getTimeLeft(Buff.RAGE);
            if(System.currentTimeMillis() - flinchingDelay < 125 && timeLeft < 3){
                return;
            }
            flinchingDelay = System.currentTimeMillis();
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
    public void handleHitBulletPacket(Network.HitBullet p) {

    }

    @Override
    public void handleMoveBulletPacket(Network.MoveBullet moveBullet) {

    }

    @Override
    public boolean playerHitEvent() {
        return false;
    }

    @Override
    public void playerDropEvent() {

    }

    @Override
    public void activate() {
        charge = 0;

        RageBuff buff = new RageBuff();
        BuffManager buffManager = BuffManager.getInstance();
        buffManager.addBuff(buff);

    }
    @Override
    public void activate(int idUser) {
        charge = 0;

        RageBuff buff = new RageBuff(idUser);
        BuffManagerMP buffManager = BuffManagerMP.getInstance();
        buffManager.addBuff(buff,idUser);
    }
    @Override
    public void activateClientSide(int idUser) {
        super.activateClientSide(idUser);
        charge = 0;
    }
    @Override
    public void charge() {
        charge++;
        if(charge > maxCharge) charge = maxCharge;
    }
}

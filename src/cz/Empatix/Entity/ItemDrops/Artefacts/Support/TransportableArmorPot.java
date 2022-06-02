package cz.Empatix.Entity.ItemDrops.Artefacts.Support;

import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.Player;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class TransportableArmorPot extends Artefact {
    public static void load(){
        Loader.loadImage("Textures\\artefacts\\tap.tga");
        Loader.loadImage("Textures\\artefacts\\artifactcharge.tga");
    }
    public TransportableArmorPot(TileMap tm, Player p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;

        scale = 3f;

        imageArtefact = new Image("Textures\\artefacts\\tap.tga",new Vector3f(1403,975,0),
                scale  );
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        rarity = 1;

    }
    public TransportableArmorPot(TileMap tm, Player[] p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;
        rarity = 1;

        scale = 3f;
        if(!tm.isServerSide()){
            imageArtefact = new Image("Textures\\artefacts\\tap.tga",new Vector3f(1403,975,0),
                    scale  );
            chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                    2.6f);
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
        // refills player armor to full
        p[0].addArmor(100);
    }
    @Override
    public void activate(int idUser) {
        charge = 0;
        // refills player armor to full
        for(Player player : p){
            if(player != null){
                if(((PlayerMP)player).getIdConnection() == idUser){
                    player.addArmor(100);
                    break;
                }
            }
        }
    }
    @Override
    public void activateClientSide(int idUser) {
        super.activateClientSide(idUser);
        for(Player player : p){
            if(player != null){
                if(((PlayerMP)player).getIdConnection() == idUser){
                    player.addArmor(100);
                    break;
                }
            }
        }
        charge = 0;
    }
    @Override
    public void charge() {
        charge++;
        if(charge > maxCharge) charge = maxCharge;
    }
}


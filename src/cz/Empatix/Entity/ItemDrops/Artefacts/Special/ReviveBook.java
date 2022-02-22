package cz.Empatix.Entity.ItemDrops.Artefacts.Special;

import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.Player;
import cz.Empatix.Java.Loader;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;

public class ReviveBook extends Artefact {
    public static void load(){
        Loader.loadImage("Textures\\artefacts\\revivebook.tga");
    }
    public ReviveBook(TileMap tm, Player p){
        super(tm,p);
        maxCharge = 1;
        charge = maxCharge;

        scale = 3f;

        imageArtefact = new Image("Textures\\artefacts\\revivebook.tga",new Vector3f(1403,975,0),
                scale  );
        chargeBar = new Image("Textures\\artefacts\\artifactcharge1.tga",new Vector3f(1400,1055,0),
                2.6f);
        rarity = 1;
        obtainable = false;
        oneUse = true;

    }
    public ReviveBook(TileMap tm, Player[] p){
        super(tm,p);
        maxCharge = 1;
        charge = maxCharge;

        rarity = 1;

        scale = 3f;
        oneUse = true;
        canShopItem = true;
    }
    @Override
    public void update(boolean pause) {
    }

    @Override
    public void update(String username) {
    }

    @Override
    public void handleAddBulletPacket(Network.ArtefactAddBullet addBullet) {

    }

    @Override
    protected void draw() {
    }

    @Override
    protected void drawHud() {
        imageArtefact.draw();
        Matrix4f matrixPos;

        geometryShader.bind();

        geometryShader.bind();

        for(int i = 0;i<charge;i++){
            long elapsed = System.nanoTime() / 1000000;
            if (elapsed / 750 % 2 == 0){
                geometryShader.setUniform3f("color", new Vector3f(0.141f, 0.980f, 0));
            } else {
                geometryShader.setUniform3f("color", new Vector3f(0.109f, 0.552f, 0.203f));
            }

            matrixPos = new Matrix4f()
                    .translate(new Vector3f( 1376+25,1055,0));
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
        // it is only multiplayer item
    }
    @Override
    public void activate(String username) {
        for(Player player : p){
            if(player != null){
                if(player.isDead()){
                    ((PlayerMP)player).reset();
                    for(Player user : p){
                        if(user != null){
                            if(((PlayerMP)user).getUsername().equalsIgnoreCase(username)){
                                player.setPosition(user.getX(),user.getY());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void activateClientSide() {
        super.activateClientSide();
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
}


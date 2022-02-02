package cz.Empatix.Entity.ItemDrops.Artefacts.Special;

import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.ItemDrops.Coin;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Java.Loader;
import cz.Empatix.Multiplayer.ItemManagerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class LuckyCoin extends Artefact {
    private int multiplier;
    private TextRender textRender;
    public static void load(){
        Loader.loadImage("Textures\\artefacts\\luckycoin.tga");
    }
    public LuckyCoin(TileMap tm, Player p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;

        scale = 2f;

        imageArtefact = new Image("Textures\\artefacts\\luckycoin.tga",new Vector3f(1403,975,0),
                scale  );
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        rarity = 1;
        multiplier = 0;

        textRender = new TextRender();
    }
    public LuckyCoin(TileMap tm, Player[] p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;

        rarity = 1;
        multiplier = 0;

        scale = 2f;

    }
    @Override
    public void loadSave() {
        imageArtefact = new Image("Textures\\artefacts\\luckycoin.tga",new Vector3f(1403,975,0),
                scale  );
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
    }

    @Override
    public void update(boolean pause) {
    }

    @Override
    public void update(String username) {
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

        float center = TextRender.getHorizontalCenter(1340,1465,"Bonus: "+multiplier,2);
        textRender.draw("Bonus: "+multiplier,new Vector3f(center,900,0),2,new Vector3f(0.886f,0.6f,0.458f));
    }

    @Override
    public void activate() {

        charge = 0;
        ItemManager im = ItemManager.getInstance();

        for(int i = 0;i<5 + multiplier;i++){
            int tileSize = tm.getTileSize();

            int playerX = (int)p[0].getX();
            int playerY = (int)p[0].getY();

            int xMinTile = playerX - 150;
            int yMinTile = playerY - 150;

            int xMaxTile = playerX + 150;
            int yMaxTile = playerY + 150;

            int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
            int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;

            while(tm.getType(y/tileSize,x/tileSize) == Tile.BLOCKED){
                x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
            }
            double atan = Math.atan2(y-playerY,x-playerX);
            float dx = (float)(Math.cos(atan) * 10);
            float dy = (float)(Math.sin(atan) * 10);

            Coin coin = new Coin(tm);
            coin.setPosition(x,y);
            coin.setSpeed(dx,dy);
            im.addItemDrop(coin);
        }
        multiplier++;
    }
    @Override
    public void activate(String username) {

        charge = 0;
        ItemManagerMP im = ItemManagerMP.getInstance();

        for(int i = 0;i<5 + multiplier;i++){
            int tileSize = tm.getTileSize();

            int playerX = (int)p[0].getX();
            int playerY = (int)p[0].getY();

            int xMinTile = playerX - 150;
            int yMinTile = playerY - 150;

            int xMaxTile = playerX + 150;
            int yMaxTile = playerY + 150;

            int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
            int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;

            while(tm.getType(y/tileSize,x/tileSize) == Tile.BLOCKED){
                x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
            }
            double atan = Math.atan2(y-playerY,x-playerX);
            float dx = (float)(Math.cos(atan) * 10);
            float dy = (float)(Math.sin(atan) * 10);

            Coin coin = new Coin(tm);
            coin.setPosition(x,y);
            coin.setSpeed(dx,dy);
            im.addItemDrop(coin);
        }
        multiplier++;
    }
    @Override
    public void charge() {
        charge++;
        if(charge > maxCharge) charge = maxCharge;
    }

    @Override
    public void despawn() {
        super.despawn();
        multiplier = 0;
    }
}


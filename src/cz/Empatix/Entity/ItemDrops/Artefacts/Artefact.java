package cz.Empatix.Entity.ItemDrops.Artefacts;

import cz.Empatix.Entity.Player;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;

import java.io.Serializable;

public abstract class Artefact implements Serializable {
    protected int maxCharge;
    protected int charge;

    // charge draw animation
    protected int chargeAnimation;
    protected long chargeTime;

    protected boolean dropped;
    protected float scale;

    protected int rarity;
    transient protected Image imageArtefact;
    transient protected Image chargeBar;
    transient protected Shader geometryShader;
    transient protected int vboVertices;

    protected TileMap tm;
    protected Player p;
    protected Artefact(TileMap tm, Player p){
        this.tm = tm;
        this.p = p;

        geometryShader = ShaderManager.getShader("shaders\\geometry");
        if (geometryShader == null){
            geometryShader = ShaderManager.createShader("shaders\\geometry");
        }

        vboVertices = ModelManager.getModel(12,6);
        if(vboVertices == -1){
            vboVertices = ModelManager.createModel(12,6);
        }
        dropped = false;
    }
    protected abstract void draw();
    protected abstract void charge();
    protected abstract void activate();
    protected abstract void update(boolean pause);
    protected abstract void drawHud();
    protected boolean canBeActivated(){return maxCharge == charge;}

    public void updateChargeAnimation(){
        if(System.currentTimeMillis()- chargeTime > 250){
            chargeTime = System.currentTimeMillis() ;
            chargeAnimation++;
            if(chargeAnimation>=charge) chargeAnimation = 0;
        }
    }

    public Image getImageArtefact() {
        return imageArtefact;
    }
    public float getScale() {
        return scale;
    }
    public void despawn(){
        dropped=false;
        charge = maxCharge;
    }
    public abstract void loadSave();
}


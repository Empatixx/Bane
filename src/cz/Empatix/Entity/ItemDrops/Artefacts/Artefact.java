package cz.Empatix.Entity.ItemDrops.Artefacts;

import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Java.Random;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Damageindicator.CombatIndicator;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;

public abstract class Artefact {
    protected int maxCharge;
    protected int charge;

    // charge draw animation
    protected int chargeAnimation;
    protected long chargeTime;

    public boolean dropped;
    protected float scale;

    protected int rarity;
    protected Image imageArtefact;
    protected Image chargeBar;
    protected Shader geometryShader;
    protected int vboVertices;

    protected boolean obtainable; // if it can be dropped by chest
    protected boolean oneUse;
    protected boolean canShopItem;

    public TileMap tm;
    public Player[] p;
    protected int idUser;
    // singleplayer
    public Artefact(TileMap tm, Player p){
        this.tm = tm;
        this.p = new Player[1];
        this.p[0] = p;

        geometryShader = ShaderManager.getShader("shaders\\geometry");
        if (geometryShader == null){
            geometryShader = ShaderManager.createShader("shaders\\geometry");
        }

        vboVertices = ModelManager.getModel(12,6);
        if(vboVertices == -1){
            vboVertices = ModelManager.createModel(12,6);
        }
        dropped = false;
        obtainable = true;
    }
    // multiplayer
    public Artefact(TileMap tm, Player[] p){
        this.tm = tm;
        this.p = p;
        dropped = false;
        obtainable = true;
        oneUse = false;
        canShopItem = false;
        if(!tm.isServerSide()){
            geometryShader = ShaderManager.getShader("shaders\\geometry");
            if (geometryShader == null){
                geometryShader = ShaderManager.createShader("shaders\\geometry");
            }

            vboVertices = ModelManager.getModel(12,6);
            if(vboVertices == -1){
                vboVertices = ModelManager.createModel(12,6);
            }
        }
    }
    protected abstract void preDraw();
    protected abstract void draw();

    public abstract void charge();
    /**
     * activates artefact in singleplayer
     */
    public abstract void activate();

    /**
     * activates artefact in multiplayer - server side
     * @param idUser id connection of player who activated artefact
     */
    public abstract void activate(int idUser);

    /**
     * activation of artefact in multiplayer - client side
     * @param idUser id connection of player who activated artefact
     */
    public void activateClientSide(int idUser){
        if(oneUse){
            ArtefactManager artefactManager = ArtefactManager.getInstance();
            artefactManager.setCurrentArtefact(null); // reseting artefact
            dropped = false;
        }
        this.idUser = idUser;
    }

    /**
     * updates artefact in singeplayer
     * @param pause - if game is paused, needed for pausing timings
     */
    public abstract void updateSP(boolean pause);

    /**
     * updates artefact in multiplayer, works with more players
     * variable userMP is playername who used artefact
     * client side
     */
    public abstract void updateMPClient();

    /**
     * updates artefact in multiplayer, skips all visual,audio effects
     * variable userMP is playername who used artefact
     * server side
     * @param username
     */
    public abstract void updateMPServer(String username);
    public abstract void handleAddBulletPacket(Network.ArtefactAddBullet addBullet);


    protected abstract void drawHud();
    public boolean canBeActivated(){return maxCharge == charge;}

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
    public abstract void handleHitBulletPacket(Network.HitBullet p);
    public abstract void handleMoveBulletPacket(Network.MoveBullet moveBullet);
    public void showDamageIndicator(int damage, boolean critical, Enemy enemy){
        int cwidth = enemy.getCwidth();
        int cheight = enemy.getCheight();
        int x = -cwidth/4+ Random.nextInt(cwidth/2);
        if(critical){
            CombatIndicator.addCriticalDamageShow(damage,(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                    ,new Vector2f(-x/25f,-1f));
        } else {
            CombatIndicator.addDamageShow(damage,(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                    ,new Vector2f(-x/25f,-1f));
        }
    }

    public boolean isOneUse() {
        return oneUse;
    }

    public boolean isObtainable() {
        return obtainable;
    }

    public boolean canBeShopItem() {
        return canShopItem;
    }

    public abstract boolean playerHitEvent();
    public abstract void playerDropEvent();

    public void handleArtefactEvent(Network.ArtefactEventState p) {
        if(p.state == 0) this.playerHitEvent();
        if(p.state == 1) this.playerDropEvent();
    }
}


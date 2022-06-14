package cz.Empatix.Entity.ItemDrops.Artefacts;

import cz.Empatix.Entity.ItemDrops.Artefacts.Damage.RagePot;
import cz.Empatix.Entity.ItemDrops.Artefacts.Damage.RingOfFire;
import cz.Empatix.Entity.ItemDrops.Artefacts.Special.LuckyCoin;
import cz.Empatix.Entity.ItemDrops.Artefacts.Special.ReviveBook;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.Ammobelt;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.BerserkPot;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.ShieldHorn;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.TransportableArmorPot;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PacketHolder;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import cz.Empatix.Utility.Random;
import org.joml.Vector3f;

import java.util.ArrayList;

public class ArtefactManager {
    public static void load(){
        Loader.loadImage("Textures\\Artefacts\\artefacthud.tga");
        Loader.loadImage("Textures\\artefacts\\artifactcharge1.tga");
        Loader.loadImage("Textures\\artefacts\\artifactcharge.tga");
        RingOfFire.load();
        BerserkPot.load();
        TransportableArmorPot.load();
        LuckyCoin.load();
        Ammobelt.load();
        ReviveBook.load();
        ShieldHorn.load();
        RagePot.load();
    }
    private ArrayList<Artefact> artefacts;

    private Image artefactHud;

    private Artefact currentArtefact;

    private static ArtefactManager artefactManager;
    public static void init(ArtefactManager artefactManager
    ){
        ArtefactManager.artefactManager = artefactManager;
    }
    public static ArtefactManager getInstance(){ return artefactManager;}

    private boolean firstAlert;


    public ArtefactManager(TileMap tm, Player player){
        artefacts = new ArrayList<>();
        // preventing to keeping artefact from previous game
        currentArtefact = null;

        artefacts.add(new RingOfFire(tm,player));
        artefacts.add(new TransportableArmorPot(tm,player));
        artefacts.add(new BerserkPot(tm,player));
        artefacts.add(new LuckyCoin(tm,player));
        artefacts.add(new Ammobelt(tm,player));
        artefacts.add(new ReviveBook(tm,player));
        artefacts.add(new ShieldHorn(tm,player));
        artefacts.add(new RagePot(tm,player));

        artefactHud = new Image("Textures\\Artefacts\\artefacthud.tga",new Vector3f(1400,975,0),2.6f);

        firstAlert = false;
    }
    public ArtefactManager(TileMap tm, Player[] players){
        artefacts = new ArrayList<>();
        // preventing to keeping artefact from previous game
        currentArtefact = null;

        artefacts.add(new RingOfFire(tm,players));
        artefacts.add(new TransportableArmorPot(tm,players));
        artefacts.add(new BerserkPot(tm,players));
        artefacts.add(new LuckyCoin(tm,players));
        artefacts.add(new Ammobelt(tm,players));
        artefacts.add(new ReviveBook(tm,players));
        artefacts.add(new ShieldHorn(tm,players));

        artefactHud = new Image("Textures\\Artefacts\\artefacthud.tga",new Vector3f(1400,975,0),2.6f);

        firstAlert = false;
    }

    public void preDraw(){
        for(Artefact artefact:artefacts){
            artefact.preDraw();
        }
    }
    public void draw(){
        for(Artefact artefact:artefacts){
            artefact.draw();
        }
    }
    public void drawHud(){
        if(currentArtefact != null){
            artefactHud.draw();
            currentArtefact.drawHud();
        }
    }
    public void charge(){
        if(currentArtefact != null){
            currentArtefact.charge();
            if(currentArtefact.canBeActivated() && !firstAlert){
                AlertManager.add(AlertManager.INFORMATION,"You've charged artefact");
                firstAlert = true;
            }
        }
    }
    public void activate(){
        if(currentArtefact != null){
            if(currentArtefact.canBeActivated()){
                currentArtefact.activate();
                firstAlert = false;
            }
        }
    }
    // singleplayer
    public void  update(boolean pause){
        for(Artefact artefact:artefacts){
            artefact.updateSP(pause);
        }
        if(currentArtefact != null){
            currentArtefact.updateChargeAnimation();
        }
    }
    // multiplayer
    public void  update(Object[] hitBullets){
        // receive packet, that player used artefact sucessfully
        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        Object[] packets = mpManager.packetHolder.get(PacketHolder.ARTEFACTINFO);
        int idPlayer = MultiplayerManager.getInstance().getIdConnection();
        for(Object o : packets) {
            Network.ArtefactInfo p = (Network.ArtefactInfo)o;
            if(idPlayer == p.idPlayer){
                if(p.slot != -1){
                    currentArtefact = artefacts.get(p.slot);
                }
            }
        }
        packets = mpManager.packetHolder.get(PacketHolder.ARTEFACTACTIVATED);
        for(Object o : packets) {
            Network.ArtefactActivate p = (Network.ArtefactActivate)o;
            artefacts.get(p.slot).activateClientSide(p.idPlayer);
        }
        packets = mpManager.packetHolder.get(PacketHolder.ARTEFACTSTATE);
        for(Object o : packets) {
            Network.ArtefactEventState p = (Network.ArtefactEventState)o;
            artefacts.get(p.slot).handleArtefactEvent(p);
        }
        packets = mpManager.packetHolder.get(PacketHolder.ARTEFACTADDBULLET);
        for(Object o : packets) {
            Network.ArtefactAddBullet p = (Network.ArtefactAddBullet)o;
            artefacts.get(p.slot).handleAddBulletPacket(p);
        }
        for (Object o : hitBullets) {
            Network.HitBullet p = (Network.HitBullet)o;
            for (Artefact a : artefacts) {
                a.handleHitBulletPacket(p);
            }
        }

        for(Artefact artefact:artefacts){
            artefact.updateMPClient();
        }
        if(currentArtefact != null){
            currentArtefact.updateChargeAnimation();
        }
    }
    public Artefact randomArtefact(){
        Artefact artefact = artefacts.get(Random.nextInt(artefacts.size()));
        while(artefact.dropped || !artefact.obtainable){
            artefact = artefacts.get(Random.nextInt(artefacts.size()));
        }
        artefact.dropped = true;
        return artefact;
    }

    public void setCurrentArtefact(Artefact currentArtefact, int x, int y) {
        if(this.currentArtefact != null){
            ItemManager itemManager = ItemManager.getInstance();
            itemManager.dropPlayerArtefact(this.currentArtefact,x,y);
            this.currentArtefact.playerDropEvent();
        }
        this.currentArtefact = currentArtefact;

    }
    public void setCurrentArtefact(Artefact currentArtefact) {
        this.currentArtefact = currentArtefact;

    }
    public Artefact getArtefact(int slot) {
        return artefacts.get(slot);
    }

    public void handleBulletMovePacket(Object[] moveBullets) {
        for(Object o : moveBullets) {
            Network.MoveBullet p = (Network.MoveBullet) o;
            for (Artefact a : artefacts) {
                a.handleMoveBulletPacket(p);
            }
        }
    }

    public void clear(){
        // clearing bullets
        ((RingOfFire)artefacts.get(0)).clear();
    }

    public boolean playeHitEvent() {
        if(currentArtefact != null){
            return currentArtefact.playerHitEvent();
        }
        return false;
    }
}

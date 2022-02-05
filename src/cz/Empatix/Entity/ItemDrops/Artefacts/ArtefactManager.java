package cz.Empatix.Entity.ItemDrops.Artefacts;

import cz.Empatix.Entity.ItemDrops.Artefacts.Damage.RingOfFire;
import cz.Empatix.Entity.ItemDrops.Artefacts.Special.LuckyCoin;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.Ammobelt;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.BerserkPot;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.TransportableArmorPot;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PacketHolder;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class ArtefactManager {
    public static void load(){
        Loader.loadImage("Textures\\Artefacts\\artefacthud.tga");
        RingOfFire.load();
        BerserkPot.load();
        TransportableArmorPot.load();
        LuckyCoin.load();
        Ammobelt.load();
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

        artefactHud = new Image("Textures\\Artefacts\\artefacthud.tga",new Vector3f(1400,975,0),2.6f);

        firstAlert = false;
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
    public void update(boolean pause){
        // receive packet, that player used artefact sucessfully
        if(MultiplayerManager.multiplayer){
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Object[] packets = mpManager.packetHolder.get(PacketHolder.ARTEFACTACTIVATED);
            for(Object o : packets) {
                Network.ArtefactActivate p = (Network.ArtefactActivate)o;
                if(mpManager.getUsername().equalsIgnoreCase(p.username)){
                    artefacts.get(p.slot).activateClientSide();
                }
            }
            packets = mpManager.packetHolder.get(PacketHolder.ARTEFACTADDBULLET);
            for(Object o : packets) {
                Network.ArtefactAddBullet p = (Network.ArtefactAddBullet)o;
                artefacts.get(p.slot).handleAddBulletPacket(p);
            }
            packets = mpManager.packetHolder.getWithoutClear(PacketHolder.HITBULLET);
            for (Object o : packets) {
                Network.HitBullet p = (Network.HitBullet)o;
                for (Artefact a : artefacts) {
                    a.handleHitBulletPacket(p);
                }
            }

        }
        for(Artefact artefact:artefacts){
            artefact.update(pause);
        }
        if(currentArtefact != null){
            currentArtefact.updateChargeAnimation();
        }
    }
    public Artefact randomArtefact(){
        Artefact artefact = artefacts.get(Random.nextInt(artefacts.size()));
        while(artefact.dropped){
            artefact = artefacts.get(Random.nextInt(artefacts.size()));
        }
        artefact.dropped = true;
        return artefact;
    }

    public void setCurrentArtefact(Artefact currentArtefact, int x, int y) {
        if(this.currentArtefact != null){
            ItemManager itemManager = ItemManager.getInstance();
            itemManager.dropPlayerArtefact(this.currentArtefact,x,y);
        }
        this.currentArtefact = currentArtefact;

    }
    public void setCurrentArtefact(Artefact currentArtefact) {
        this.currentArtefact = currentArtefact;

    }
    public Artefact getArtefact(int slot) {
        return artefacts.get(slot);
    }

    public void handleBulletMovePacket(Network.MoveBullet moveBullet) {
        for(Artefact a : artefacts){
            a.handleMoveBulletPacket(moveBullet);
        }
    }

    public void clear(){
        // clearing bullets
        ((RingOfFire)artefacts.get(0)).clear();
    }
}

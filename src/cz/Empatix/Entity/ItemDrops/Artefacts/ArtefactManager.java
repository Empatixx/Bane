package cz.Empatix.Entity.ItemDrops.Artefacts;

import cz.Empatix.Entity.ItemDrops.Artefacts.Damage.RingOfFire;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.BerserkPot;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.TransportableArmorPot;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class ArtefactManager {
    private static ArrayList<Artefact> artefacts;

    public Image artefactHud;

    private static Artefact currentArtefact;

    private static Player p;
    public ArtefactManager(TileMap tm, Player player){
        p = player;
        artefacts = new ArrayList<>();

        artefacts.add(new RingOfFire(tm,player));
        artefacts.add(new TransportableArmorPot(tm,player));
        artefacts.add(new BerserkPot(tm,player));

        artefactHud = new Image("Textures\\Artefacts\\artefacthud.tga",new Vector3f(1400,975,0),2.6f);
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
    public static void charge(){
        if(currentArtefact != null){
            currentArtefact.charge();
        }
    }
    public void activate(){
        if(currentArtefact != null){
            if(currentArtefact.canBeActivated()){
                currentArtefact.activate();
                currentArtefact.dropped = false;
                currentArtefact = null;
            }
        }
    }
    public void update(){
        for(Artefact artefact:artefacts){
            artefact.update();
        }
    }
    public static Artefact randomArtefact(){
        Artefact artefact = artefacts.get(Random.nextInt(artefacts.size()));
        while(currentArtefact == artefact || artefact.dropped){
            artefact = artefacts.get(Random.nextInt(artefacts.size()));
        }
        artefact.dropped = true;
        return artefact;
    }

    public void setCurrentArtefact(Artefact currentArtefact) {
        if(ArtefactManager.currentArtefact != null){
            ItemManager.dropArtefact(ArtefactManager.currentArtefact,(int)p.getX(),(int)p.getY());
        }
        ArtefactManager.currentArtefact = currentArtefact;

    }
}

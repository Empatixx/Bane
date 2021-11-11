package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.ItemDrops.Artefacts.Damage.RingOfFire;
import cz.Empatix.Entity.ItemDrops.Artefacts.Special.LuckyCoin;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.Ammobelt;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.BerserkPot;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.TransportableArmorPot;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;

public class ArtefactManagerMP {
    private PlayerArtefacts[] playerArtefacts;
    private ArrayList<Artefact> artefacts;

    private static ArtefactManagerMP artefactManager;

    public static void init(ArtefactManagerMP artefactManager
    ){
        ArtefactManagerMP.artefactManager = artefactManager;
    }
    public static ArtefactManagerMP getInstance(){ return artefactManager;}

    public ArtefactManagerMP(TileMap tm, PlayerMP[] player){
        artefacts = new ArrayList<>();
        playerArtefacts = new PlayerArtefacts[player.length];
        for(int i = 0;i<player.length;i++){
            if(player[i] != null){
                playerArtefacts[i] = new PlayerArtefacts(player[i].getUsername());
            } else {
                break;
            }
        }

        artefacts.add(new RingOfFire(tm,player));
        artefacts.add(new TransportableArmorPot(tm,player));
        artefacts.add(new BerserkPot(tm,player));
        artefacts.add(new LuckyCoin(tm,player));
        artefacts.add(new Ammobelt(tm,player));
        init(this);
    }

    public void charge(){
        for(PlayerArtefacts playerArtefact : playerArtefacts){
            if(playerArtefact == null) continue;
            playerArtefact.charge();
        }
    }
    public void activate(String username){
        for(PlayerArtefacts playerArtefact : playerArtefacts){
            if(playerArtefact == null) continue;
            playerArtefact.activate(username);
        }
    }
    public void update(){
        for(Artefact artefact : artefacts){
            boolean updateSuccessly = false;
            for(PlayerArtefacts playerArtefact : playerArtefacts){
                if(playerArtefact != null){
                    updateSuccessly = playerArtefact.update(artefact);
                }
            }
            if(!updateSuccessly) artefact.update(null);
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
    public int getArtefactSlot(Artefact artefact){
        return artefacts.indexOf(artefact);
    }
    public void setCurrentArtefact(Artefact currentArtefact, int x, int y, String username) {
        for(PlayerArtefacts playerArtefact : playerArtefacts){
            if(playerArtefact == null) continue;
            playerArtefact.setCurrentArtefact(currentArtefact,x,y,username);
        }
    }
    private static class PlayerArtefacts{
        private String username;
        private Artefact currentArtefact;
        private boolean firstAlert;

        public PlayerArtefacts(String username){
            this.username = username;
            // preventing to keeping artefact from previous game
            currentArtefact = null;

            firstAlert = false;
        }
        public void charge(){
            if(currentArtefact != null){
                currentArtefact.charge();
                if(currentArtefact.canBeActivated() && !firstAlert){
                    Server server = MultiplayerManager.getInstance().server.getServer();
                    Network.Alert alert = new Network.Alert();
                    alert.text = "You've charged artefact";
                    alert.type = AlertManager.INFORMATION;
                    alert.username = username;
                    server.sendToAllUDP(alert);
                    firstAlert = true;
                }
            }
        }
        public void activate(String username){
            if(!this.username.equalsIgnoreCase(username)) return;
            if(currentArtefact != null){
                if(currentArtefact.canBeActivated()){
                    currentArtefact.activate();
                    firstAlert = false;
                }
            }
        }
        public boolean update(Artefact artefact){
            if(artefact == currentArtefact){
                currentArtefact.update(username);
                return true;
            }
            return false;
        }
        public void setCurrentArtefact(Artefact currentArtefact, int x, int y, String username) {
            if(!this.username.equalsIgnoreCase(username)) return;
            if(this.currentArtefact != null){
                ItemManager itemManager = ItemManager.getInstance();
                itemManager.dropPlayerArtefact(this.currentArtefact,x,y);
            }
            this.currentArtefact = currentArtefact;

        }
    }
}
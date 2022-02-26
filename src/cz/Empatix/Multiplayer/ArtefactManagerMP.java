package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.ItemDrops.Artefacts.Damage.RingOfFire;
import cz.Empatix.Entity.ItemDrops.Artefacts.Special.LuckyCoin;
import cz.Empatix.Entity.ItemDrops.Artefacts.Special.ReviveBook;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.Ammobelt;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.BerserkPot;
import cz.Empatix.Entity.ItemDrops.Artefacts.Support.TransportableArmorPot;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Java.Random;
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
        artefacts.add(new ReviveBook(tm,player));

        init(this);
    }

    public void charge(){
        for(PlayerArtefacts playerArtefact : playerArtefacts){
            if(playerArtefact == null) continue;
            playerArtefact.charge();
        }
    }
    public void activate(String username,Network.ArtefactActivate artefactActivate){
        for(PlayerArtefacts playerArtefact : playerArtefacts){
            if(playerArtefact == null) continue;
            playerArtefact.activate(username, artefactActivate);
        }
    }
    public void update(){
        for(Artefact artefact : artefacts){
            boolean updateSuccessly = false;
            for(PlayerArtefacts playerArtefact : playerArtefacts){
                if(playerArtefact != null){
                    updateSuccessly = playerArtefact.update(artefact); // updating players movement speed etc.
                }
            }
            if(!updateSuccessly) artefact.update(null); // updating like bullets etc. meanwhile players don't have artefact
        }
    }
    public Artefact randomArtefact(){
        boolean possible = false;
        for(Artefact artefact : artefacts){
            if (!artefact.dropped && artefact.isObtainable()) {
                possible = true;
                break;
            }
        }
        if(!possible) return null;
        Artefact artefact = artefacts.get(Random.nextInt(artefacts.size()));
        while(artefact.dropped || !artefact.isObtainable()){
            artefact = artefacts.get(Random.nextInt(artefacts.size()));
        }
        artefact.dropped = true;
        return artefact;
    }
    public Artefact randomShopArtefact(){
        boolean possible = false;
        for(Artefact artefact : artefacts){
            if(!artefact.dropped && artefact.isObtainable() && artefact.canBeShopItem()){
                possible = true;
                break;
            }
        }
        if(!possible) return null;
        Artefact artefact = artefacts.get(Random.nextInt(artefacts.size()));
        while(artefact.dropped || !artefact.isObtainable() || !artefact.canBeShopItem()){
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

    public boolean playeHitEvent(String username) {
        for(PlayerArtefacts playerArtefact : playerArtefacts){
            if(playerArtefact == null) continue;
            boolean immune = playerArtefact.playerHitEvent(username);
            if(immune) return true;
        }
        return false;
    }

    private class PlayerArtefacts{
        private String username;
        private Artefact currentArtefact;

        public PlayerArtefacts(String username){
            this.username = username;
            // preventing to keeping artefact from previous game
            currentArtefact = null;
        }
        public void charge(){
            if(currentArtefact != null){
                currentArtefact.charge();
            }
        }
        public void activate(String username, Network.ArtefactActivate artefactActivate){
            if(this.username.equalsIgnoreCase(username)){
                if(currentArtefact != null){
                    if(currentArtefact.canBeActivated()){
                        currentArtefact.activate(username);
                        // sending acknowledge of activating artefact
                        Server server = MultiplayerManager.getInstance().server.getServer();
                        artefactActivate.slot = (byte)artefacts.indexOf(currentArtefact);
                        server.sendToAllTCP(artefactActivate);
                        if(currentArtefact.isOneUse()){
                            currentArtefact = null;
                        }
                    }
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
            if(this.username.equalsIgnoreCase(username)){
                if(this.currentArtefact != null){
                    ItemManagerMP itemManager = ItemManagerMP.getInstance();
                    itemManager.dropPlayerArtefact(this.currentArtefact,x,y,username);
                }
                this.currentArtefact = currentArtefact;
            }
        }

        public boolean playerHitEvent(String username) {
            if(this.username.equalsIgnoreCase(username)) {
                if (this.currentArtefact != null) {
                    return this.currentArtefact.playerHitEvent();
                }
            }
            return false;
        }
    }
    public void clear(){
        // clearing bullets
        ((RingOfFire)artefacts.get(0)).clearMP();
    }
}
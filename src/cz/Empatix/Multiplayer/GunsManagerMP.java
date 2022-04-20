package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Guns.*;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GunsManagerMP {

    private static GunsManagerMP instance;

    private ArrayList<Weapon> weapons;

    private final static int FIRSTSLOT = 0;
    private final static int SECONDARYSLOT = 1;

    private PlayerWeapons[] playerWeapons;

    private PlayerMP[] players;
    private GunUpgradesCache gunUpgrades;

    public GunsManagerMP(TileMap tileMap, PlayerMP[] p, GunUpgradesCache upgrades){
        instance = this;
        this.gunUpgrades = upgrades;

        weapons = new ArrayList<>();
        for(int i = 0;i < p.length;i++){
            weapons.add(new Pistol(tileMap,p));
        }
        weapons.add(new Shotgun(tileMap,p));
        weapons.add(new Uzi(tileMap,p));
        weapons.add(new Revolver(tileMap,p));
        weapons.add(new Grenadelauncher(tileMap,p));
        weapons.add(new Luger(tileMap,p));
        weapons.add(new M4(tileMap,p));
        weapons.add(new Thompson(tileMap,p));
        weapons.add(new ModernShotgun(tileMap,p));

        playerWeapons = new PlayerWeapons[p.length];
        for(int i = 0;i<p.length;i++){
            if(p[i] != null){
                playerWeapons[i] = new PlayerWeapons(p[i],p[i].getIdConnection(), i);
            } else {
                break;
            }
        }

        players = p;
    }
    public static GunsManagerMP getInstance(){return instance;}

    public int getWeaponSlot(Weapon weapon){
        if(weapon == null) return -1;
        return weapons.indexOf(weapon);
    }

    public void stopShooting(int idPlayer){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.stopShooting(idPlayer);
        }
    }
    public void startShooting(int idPlayer){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.startShooting(idPlayer);
        }
    }
    public void shoot(){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.shoot();
        }
    }
    public void reload(int idPlayer){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.reload(idPlayer);
        }
    }
    public void update(){
        for(Weapon weapon : weapons){
            weapon.updateAmmo();
        }
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.update();
        }
    }

    public void checkCollisions(ArrayList<Enemy> enemies){
        for(Weapon weapon:weapons){
            weapon.checkCollisions(enemies);
        }
    }
    public void changeGun(int x, int y, Weapon weapon, int idPlayer){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.changeGun(x,y,weapon,idPlayer);
        }
    }
    public Weapon randomGun(){
        Weapon weapon = weapons.get(players.length+ Random.nextInt(weapons.size()-players.length));
        while(weapon.hasAlreadyDropped()){
            weapon = weapons.get(players.length+Random.nextInt(weapons.size()-players.length));
        }
        return weapon;
    }
    public void changeGunScroll(int idPlayer){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.changeGunScroll(idPlayer);
        }
    }
    public int[] getWeaponTypes(int idUser){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            if(playerWeapons.isThisPlayer(idUser)) return playerWeapons.getWeaponTypes();
        }
        return null;
    }
    public boolean addAmmo(int amountpercent, int type, int idPlayer) {
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            if(playerWeapons.isThisPlayer(idPlayer)) return playerWeapons.addAmmo(amountpercent,type);
        }
        return false;
    }
    public void switchWeaponSlot(Network.SwitchWeaponSlot weaponSlot){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.switchWeapon(weaponSlot);
        }
    }

    public void handleDropWeaponPacket(Network.PlayerDropWeapon dropWeapon) {
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.dropPlayerWeapon(dropWeapon);
        }
    }
    public void dropPlayerWeapon(int idPlayer,int x, int y, int slot) {
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.dropPlayerWeapon(idPlayer,x,y,slot);
        }
    }
    public int getCurrentWeaponSlot(int idUser){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            if(playerWeapons.isThisPlayer(idUser)){
                return playerWeapons.getCurrentslot();
            }
        }
        return -1;
    }

    private class PlayerWeapons{
        private float px,py;
        private float mouseX,mouseY;
        private final int idPlayer; // id connection of player

        private int currentslot;
        private Weapon current;

        private Weapon[] equipedweapons;

        private long switchDelay;

        public PlayerWeapons(PlayerMP p, int idPlayer, int index){
            this.idPlayer = idPlayer;
            equipedweapons = new Weapon[2];

            equipedweapons[0] = weapons.get(index);
            equipedweapons[0].restat(p.getIdConnection(), true);

            current = equipedweapons[FIRSTSLOT];
            currentslot = FIRSTSLOT;

        }
        public void shoot(){
            if(current != null){
                current.shoot(mouseX,mouseY,px,py,idPlayer);
            }
        }
        public void reload(int idPlayer){
            if(current == null)return;
            if(this.idPlayer == idPlayer) {
                current.reload();
            }
        }
        public void switchWeapon(Network.SwitchWeaponSlot weaponSlot){
            if(weaponSlot.idPlayer == idPlayer){
                weaponSlot.sucessful = setCurrentWeapon(equipedweapons[weaponSlot.slot], weaponSlot.slot);
                Server server = MultiplayerManager.getInstance().server.getServer();
                server.sendToAllUDP(weaponSlot);
            }
        }
        public void update(){
            if(current == null) return;
            current.update();

            Server server = MultiplayerManager.getInstance().server.getServer();
            Network.WeaponInfo weaponInfo = new Network.WeaponInfo();
            weaponInfo.currentAmmo = (short)current.getCurrentAmmo();
            weaponInfo.currentMagazineAmmo = (short)current.getCurrentMagazineAmmo();
            weaponInfo.slots[0] = (byte)getWeaponSlot(equipedweapons[0]);
            weaponInfo.slots[1] = (byte)getWeaponSlot(equipedweapons[1]);
            weaponInfo.currSlot = (byte)currentslot;
            weaponInfo.idPlayer = idPlayer;

            server.sendToUDP(idPlayer,weaponInfo); // sends ammo info only to specified player
        }
        // return true - if swapping was successful, false if not
        public boolean setCurrentWeapon(Weapon current, int slot) {
            if (System.currentTimeMillis() - InGame.deltaPauseTime() - switchDelay < 500) return false;
            // when slot is same as current
            if (currentslot == slot) {
                return false;
            }
            // when current gun is not realoding
            if (this.current == null) {
                currentslot = slot;
                switchDelay = System.currentTimeMillis() - InGame.deltaPauseTime();
                this.current = current;
                stopShooting();
                return true;
            } else if (this.current.canSwap()) {
                currentslot = slot;
                switchDelay = System.currentTimeMillis() - InGame.deltaPauseTime();
                this.current = current;
                stopShooting();
                return true;
            }
            return false;
        }
        public void stopShooting(int idPlayer){
            if(this.idPlayer == idPlayer){
                if(current == null) return;
                current.setShooting(false);
            }
        }
        public void startShooting(int idPlayer){
            if(this.idPlayer == idPlayer){
                if(current == null) return;
                current.setShooting(true);
            }
        }
        private void stopShooting(){
            if(current == null) return;
            current.setShooting(false);
        }
        public boolean addAmmo(int amountpercent, int type) {
            // first check main gun in hand
            if(current != null){
                if(current.getType() == type){
                    if(!current.isFullAmmo()){
                        current.addAmmo(amountpercent);
                        return true;
                    }
                }
            }
            // check all guns in inventory
            for (int i = 0; i < 2; i++) {
                Weapon weapon = equipedweapons[i];
                if (weapon == null) continue;
                if (weapon.getType() == type) {
                    if(!weapon.isFullAmmo()){
                        weapon.addAmmo(amountpercent);
                        return true;
                    }
                }
            }

            return false;
        }
        public void changeGun(int x, int y, Weapon weapon, int idPlayer){
            if(this.idPlayer == idPlayer){
                stopShooting();
                // check player's currentslot
                if(equipedweapons[currentslot] == null){
                    equipedweapons[currentslot] = weapon;
                    current = weapon;
                    current.restat(idPlayer, false);
                    return;
                }
                // check player's all slots
                for(int i = 0;i<2;i++){
                    if(equipedweapons[i] == null){
                        if(i==currentslot) current = weapon;
                        equipedweapons[i] = weapon;
                        weapon.restat(idPlayer, false);
                        return;
                    }
                }
                // if player's slots are already filled

                ItemManagerMP itemManager = ItemManagerMP.getInstance();
                itemManager.dropPlayerWeapon(current,x,y,weapons.indexOf(current),idPlayer);
                equipedweapons[currentslot] = weapon;
                current=weapon;
            }
        }
        public void changeGunScroll(int idPlayer){
            if(idPlayer == this.idPlayer){
                int slot = currentslot;
                slot++;
                if(slot > 1) slot = 0;
                setCurrentWeapon(equipedweapons[slot],slot);
            }
        }
        public int[] getWeaponTypes(){
            int[] types = new int[2];
            if(equipedweapons[0] != null) types[0] = equipedweapons[0].getType();
            else types[0] = -1;
            if(equipedweapons[1] != null) types[1] = equipedweapons[1].getType();
            else types[1] = -1;
            return types;
        }
        public boolean isThisPlayer(int id){return this.idPlayer == id;} // id connection of player

        public void setMouseLocations(float x, float y){
            mouseX = x;
            mouseY = y;
        }
        public void setPlayerLocation(float x, float y){
            px = x;
            py = y;
        }

        public void dropPlayerWeapon(Network.PlayerDropWeapon dropWeapon) {
            if(dropWeapon.idPlayer == idPlayer){
                if(current != null){
                    stopShooting();
                    ItemManagerMP itemManagerMP = ItemManagerMP.getInstance();
                    itemManagerMP.dropPlayerWeapon(current, dropWeapon.x, dropWeapon.y,weapons.indexOf(current),idPlayer);
                    current = null;
                    equipedweapons[currentslot] = null;
                    // sending back packet with the success
                    dropWeapon.sucessful = true;
                    dropWeapon.playerSlot = (byte)currentslot;
                    Server server = MultiplayerManager.getInstance().server.getServer();
                    server.sendToAllUDP(dropWeapon);
                }
            }
        }
        public int getCurrentslot() {
            return currentslot;
        }

        public void dropPlayerWeapon(int idPlayer, int x, int y, int slot) {
            if(this.idPlayer == idPlayer){
                stopShooting();
                current = null;
                // sending back packet so it will remove weapon in hud from client side
                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                Server server = MultiplayerManager.getInstance().server.getServer();
                Network.PlayerDropWeapon dropWeapon = new Network.PlayerDropWeapon();
                mpManager.server.requestACK(dropWeapon,dropWeapon.idPacket);
                if(equipedweapons[slot] != null){
                    ItemManagerMP itemManagerMP = ItemManagerMP.getInstance();
                    itemManagerMP.dropPlayerWeapon(equipedweapons[slot], x, y,weapons.indexOf(equipedweapons[slot]),idPlayer);
                    equipedweapons[slot] = null;
                    dropWeapon.sucessful = true;
                    dropWeapon.idPlayer = idPlayer;
                    dropWeapon.playerSlot = (byte)slot;
                    server.sendToAllUDP(dropWeapon);
                }
            }
        }
    }
    public void handleMouseCoords(Network.MouseCoords coords){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            if(playerWeapons.isThisPlayer(coords.idPlayer)){
                playerWeapons.setMouseLocations(coords.x,coords.y);
            }
        }
    }
    public void updatePlayerLocations(){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            for(PlayerMP player : players){
                if(player == null) continue;
                int id = player.getIdConnection();
                if(playerWeapons.isThisPlayer(id)){
                    playerWeapons.setPlayerLocation(player.getX(),player.getY());
                }
            }
        }
    }
    public int getNumWeapons(){return weapons.size();}

    public void setGunUpgrades(GunUpgradesCache gunUpgrades) {
        this.gunUpgrades = gunUpgrades;
    }

    public static class GunUpgradesCache {
        private final List<PlayerData> playersData;
        public GunUpgradesCache(){
            playersData = Collections.synchronizedList(new ArrayList<>());
        }
        public void addPlayer(int idPlayer){
            PlayerData data = new PlayerData(idPlayer);
            playersData.add(data);
        }
        public void removePlayer(int idPlayer){
            synchronized (playersData){
                for(int i = 0;i<playersData.size();i++){
                    if(playersData.get(i).idPlayer == idPlayer){
                        playersData.remove(i);
                        i--;
                    }
                }
            }
        }
        private static class PlayerData{
            private int idPlayer;
            private int[] numUpgrades;
            private static final int MAX_WEAPONS = 9;
            public PlayerData(int idPlayer){
                this.idPlayer = idPlayer;
                numUpgrades = new int[MAX_WEAPONS];
            }
            // all upgrades
            public void handleNumUpgradesPacket(Network.NumUpgrades packet){
                if(idPlayer == packet.idPlayer){
                    numUpgrades = packet.numUpgrades;
                }

            }
            // only one upgrade
            public void handleNumUpgradesPacket(Network.NumUpgradesUpdate packet){
                if(idPlayer == packet.idPlayer){
                    numUpgrades[nameToIndex(packet.gunName)] = packet.numUpgrades;
                }
            }
            public int getNumUpgrades(int idPlayer, String gunName){
                if(this.idPlayer == idPlayer){
                    return numUpgrades[nameToIndex(gunName)];
                }
                return -1;
            }
            public int nameToIndex(String username){
                int index;
                switch(username){
                    case "Pistol":{
                        index = 0;
                        break;
                    }
                    case "Luger":{
                        index = 1;
                        break;
                    }
                    case "Shotgun":{
                        index = 2;
                        break;
                    }
                    case "Uzi":{
                        index = 3;
                        break;
                    }
                    case "M4":{
                        index = 4;
                        break;
                    }
                    case "Grenade Launcher":{
                        index = 5;
                        break;
                    }
                    case "Revolver":{
                        index = 6;
                        break;
                    }
                    case "Thompson":{
                        index = 7;
                        break;
                    }
                    case "ModernShotgun":{
                        index = 8;
                        break;
                    }
                    default:{
                        index = -1;
                        break;
                    }
                }
                return index;
            }
        }
        public void handleNumUpgradesPacket(Network.NumUpgrades packet){
            synchronized (playersData){
                for(PlayerData data : playersData){
                    if(data == null) continue;
                    data.handleNumUpgradesPacket(packet);
                }
            }
        }
        public void handleNumUpgradesPacketUpdate(Network.NumUpgradesUpdate packet){
            synchronized (playersData){
                for(PlayerData data : playersData){
                    if(data == null) continue;
                    data.handleNumUpgradesPacket(packet);
                }
            }

        }
        public int getNumUpgrades(int idPlayer, String gunName){
            synchronized (playersData){
                for(PlayerData data : playersData){
                    if(data == null) continue;
                    if (data.getNumUpgrades(idPlayer,gunName) != -1) return data.getNumUpgrades(idPlayer,gunName);
                }
            }
            return 0;
        }
    }
    public int getNumUpgrades(int idPlayer, String gunName){
        return gunUpgrades.getNumUpgrades(idPlayer,gunName);
    }

    /**
     * Returns id connection of player who has equiped that weapon
     * @param weapon - weapon that is equiped by someone
     * @return id player who has gun
     */
    public int getWeaponOwner(Weapon weapon){
        for(PlayerWeapons pw : playerWeapons){
            if(pw != null){
                for(Weapon w: pw.equipedweapons){
                    if(w == weapon){
                        return pw.idPlayer;
                    }
                }
            }
        }
        return -1;
    }
}

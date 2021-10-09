package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Guns.*;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;

public class GunsManagerMP {

    public static int bulletShooted;
    public static int hitBullets;

    private ArrayList<Weapon> weapons;

    private final static int FIRSTSLOT = 0;
    private final static int SECONDARYSLOT = 1;

    private PlayerWeapons[] playerWeapons;

    private PlayerMP[] players;

    public GunsManagerMP(TileMap tileMap, PlayerMP[] p){
        weapons = new ArrayList<>();
        weapons.add(new Pistol(tileMap,p[0]));
        weapons.add(new Pistol(tileMap,p[0]));
        weapons.add(new Shotgun(tileMap,p[0]));
        weapons.add(new Submachine(tileMap,p[0]));
        weapons.add(new Revolver(tileMap,p[0]));
        weapons.add(new Grenadelauncher(tileMap,p[0]));
        weapons.add(new Luger(tileMap,p[0]));
        weapons.add(new M4(tileMap,p[0]));
        weapons.add(new Thompson(tileMap,p[0]));

        playerWeapons = new PlayerWeapons[p.length];
        for(int i = 0;i<p.length;i++){
            if(p[i] != null){
                playerWeapons[i] = new PlayerWeapons(p[i], i);
            } else {
                break;
            }
        }

        bulletShooted = 0;
        hitBullets = 0;

        players = p;
    }
    public void stopShooting(String username){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.stopShooting(username);
        }
    }
    public void startShooting(String username){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.startShooting(username);
        }
    }
    public void shot(){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.shoot();
        }
    }
    public void reload(String username){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.reload(username);
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
    public void changeGun(int x, int y, Weapon weapon, String username){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.changeGun(x,y,weapon,username);
        }
    }
    public Weapon randomGun(){
        Weapon weapon = weapons.get(2+ Random.nextInt(weapons.size()-2));
        while(weapon.hasAlreadyDropped()){
            weapon = weapons.get(2+Random.nextInt(weapons.size()-2));
        }
        return weapon;
    }
    public Weapon getWeapon(int index){
        return weapons.get(index);
    }
    public void changeGunScroll(String username){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            playerWeapons.changeGunScroll(username);
        }
    }
    public int[] getWeaponTypes(String username){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            if(playerWeapons.isThisPlayer(username)) return playerWeapons.getWeaponTypes();
        }
        return null;
    }
    public boolean addAmmo(int amountprocent, int type, String username) {
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            if(playerWeapons.isThisPlayer(username)) return playerWeapons.addAmmo(amountprocent,type,username);
        }
        return false;
    }
    private class PlayerWeapons{
        private float px,py;
        private float mouseX,mouseY;
        private String username;

        private int currentslot;
        private Weapon current;

        private Weapon[] equipedweapons;

        private long switchDelay;
        public PlayerWeapons(PlayerMP p, int index){
            equipedweapons = new Weapon[2];

            equipedweapons[0] = weapons.get(index);

            current = equipedweapons[FIRSTSLOT];
            currentslot = FIRSTSLOT;

            bulletShooted = 0;
            hitBullets = 0;

            username = p.getUsername();
        }
        public void shoot(){
            if(current != null){
                current.shot(mouseX,mouseY,px,py);
            }
        }
        public void reload(String username){
            if(current == null)return;
            if(username.equalsIgnoreCase(this.username)) {
                current.reload();
            }
        }
        public void update(){
            if(current == null) return;
            current.update();

            Server server = MultiplayerManager.getInstance().server.getServer();
            Network.WeaponInfo weaponInfo = new Network.WeaponInfo();
            weaponInfo.currentAmmo = current.getCurrentAmmo();
            weaponInfo.currentMagazineAmmo = current.getCurrentMagazineAmmo();
            weaponInfo.username = username;

            server.sendToAllUDP(weaponInfo);
        }
        public void setCurrentWeapon(Weapon current, int slot) {
            if (System.currentTimeMillis() - InGame.deltaPauseTime() - switchDelay < 500) return;
            // when slot is same as current
            if (currentslot == slot) {
                return;
            }
            // when current gun is not realoding
            if (this.current == null) {
                currentslot = slot;
                switchDelay = System.currentTimeMillis() - InGame.deltaPauseTime();
                this.current = current;
                stopShooting();
            } else if (this.current.canSwap()) {
                currentslot = slot;
                switchDelay = System.currentTimeMillis() - InGame.deltaPauseTime();
                this.current = current;
                stopShooting();
            }
        }
        public void stopShooting(String username){
            if(this.username.equalsIgnoreCase(username)){
                if(current == null) return;
                current.setShooting(false);
            }
        }
        public void startShooting(String username){
            if(this.username.equalsIgnoreCase(username)){
                if(current == null) return;
                current.setShooting(true);
            }
        }
        private void stopShooting(){
            if(current == null) return;
            current.setShooting(false);
        }
        private void startShooting(){
            if(current == null) return;
            current.setShooting(true);
        }
        public void keyPressed(int k, int x, int y, String username){
            if(this.username.equalsIgnoreCase(username)){
                if(k == ControlSettings.getValue(ControlSettings.WEAPON_DROP)){
                    if(current != null){
                        stopShooting();
                        ItemManager itemManager = ItemManager.getInstance();
                        itemManager.dropPlayerWeapon(current, x,y);
                    }
                    current = null;
                    equipedweapons[currentslot] = null;
                } else if (k == ControlSettings.getValue(ControlSettings.WEAPON_SLOT1)){
                    setCurrentWeapon(equipedweapons[FIRSTSLOT],FIRSTSLOT);
                } else if (k == ControlSettings.getValue(ControlSettings.WEAPON_SLOT2)){
                    setCurrentWeapon(equipedweapons[SECONDARYSLOT],SECONDARYSLOT);
                }
            }
        }
        public boolean addAmmo(int amountprocent, int type, String username) {
            if(this.username.equalsIgnoreCase(username)){
                // first check main gun in hand
                if(current != null){
                    if(current.getType() == type){
                        if(current.isFullAmmo()) return false;
                        current.addAmmo(amountprocent);
                        return true;
                    }
                }
                // check all guns in inventory
                for (int i = 0; i < 2; i++) {
                    Weapon weapon = equipedweapons[i];
                    if (weapon == null) continue;
                    if (weapon.getType() == type) {
                        if(weapon.isFullAmmo()) return false;
                        weapon.addAmmo(amountprocent);
                        return true;
                    }
                }
            }
            return false;
        }
        public void changeGun(int x, int y, Weapon weapon, String username){
            if(this.username.equalsIgnoreCase(username)){
                stopShooting();
                // check player's currentslot
                if(equipedweapons[currentslot] == null){
                    equipedweapons[currentslot] = weapon;
                    current = weapon;
                    return;
                }
                // check player's all slots
                for(int i = 0;i<2;i++){
                    if(equipedweapons[i] == null){
                        if(i==currentslot) current = weapon;
                        equipedweapons[i] = weapon;
                        return;
                    }
                }
                // if player's slots are already filled
                ItemManager itemManager = ItemManager.getInstance();
                itemManager.dropPlayerWeapon(current,x,y);
                equipedweapons[currentslot] = weapon;
                current=weapon;
            }
        }
        public void changeGunScroll(String username){
            if(this.username.equalsIgnoreCase(username)){
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
        public boolean isThisPlayer(String username){return this.username.equalsIgnoreCase(username);}

        public void setMouseLocations(float x, float y){
            mouseX = x;
            mouseY = y;
        }
        public void setPlayerLocation(float x, float y){
            px = x;
            py = y;
        }
    }
    public void handleShootPacket(Network.Shoot shoot){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            if(playerWeapons.isThisPlayer(shoot.username)){

                playerWeapons.setMouseLocations(shoot.x,shoot.y);
                playerWeapons.startShooting();
            }
        }
    }
    public void updatePlayerLocations(){
        for(PlayerWeapons playerWeapons : playerWeapons){
            if(playerWeapons == null) continue;
            for(PlayerMP player : players){
                if(player == null) continue;
                String pUsername = player.getUsername();
                if(playerWeapons.isThisPlayer(pUsername)){
                    playerWeapons.setPlayerLocation(player.getX(),player.getY());
                }
            }
        }
    }
}

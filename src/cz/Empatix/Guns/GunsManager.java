package cz.Empatix.Guns;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PacketHolder;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;

public class GunsManager {
    public static void load(){
        Loader.loadImage("Textures\\weapon_hud.tga");
        Bullet.load();
        Grenadebullet.load();

        Luger.load();
        Grenadelauncher.load();
        M4.load();
        Pistol.load();
        Revolver.load();
        Shotgun.load();
        Submachine.load();
        Thompson.load();
    }
    private static GunsManager gunsManager;
    public static void init(GunsManager gunsManager
    ){
        GunsManager.gunsManager = gunsManager;
    }
    public static GunsManager getInstance(){ return gunsManager;}

    public static int bulletShooted;
    public static int hitBullets;

    private ArrayList<Weapon> weapons;

    private final static int FIRSTSLOT = 0;
    private final static int SECONDARYSLOT = 1;

    private int currentslot;
    private Weapon current;


    private Weapon[] equipedweapons;
    private int soundSwitchingGun;
    private Source source;
    private long switchDelay;

    private Image weaponBorder_hud;

    private Player[] players;

    public GunsManager(TileMap tileMap, Player p){
        weapons = new ArrayList<>();
        weapons.add(new Pistol(tileMap,p,this));
        weapons.add(new Shotgun(tileMap,p,this));
        weapons.add(new Submachine(tileMap,p,this));
        weapons.add(new Revolver(tileMap,p,this));
        weapons.add(new Grenadelauncher(tileMap,p,this));
        weapons.add(new Luger(tileMap,p,this));
        weapons.add(new M4(tileMap,p,this));
        weapons.add(new Thompson(tileMap,p,this));


        weaponBorder_hud = new Image("Textures\\weapon_hud.tga",new Vector3f(1675,975,0),2.6f);


        soundSwitchingGun = AudioManager.loadSound("guns\\switchgun.ogg");
        source = AudioManager.createSource(Source.EFFECTS,0.35f);

        equipedweapons = new Weapon[2];

        equipedweapons[0] = weapons.get(0);

        current = equipedweapons[FIRSTSLOT];
        currentslot = FIRSTSLOT;

        bulletShooted = 0;
        hitBullets = 0;

    }

    public GunsManager(TileMap tileMap, PlayerMP[] p){
        weapons = new ArrayList<>();
        weapons.add(new Pistol(tileMap,p[0],this));
        weapons.add(new Shotgun(tileMap,p[0],this));
        weapons.add(new Submachine(tileMap,p[0],this));
        weapons.add(new Revolver(tileMap,p[0],this));
        weapons.add(new Grenadelauncher(tileMap,p[0],this));
        weapons.add(new Luger(tileMap,p[0],this));
        weapons.add(new M4(tileMap,p[0],this));
        weapons.add(new Thompson(tileMap,p[0],this));


        weaponBorder_hud = new Image("Textures\\weapon_hud.tga",new Vector3f(1675,975,0),2.6f);


        soundSwitchingGun = AudioManager.loadSound("guns\\switchgun.ogg");
        source = AudioManager.createSource(Source.EFFECTS,0.35f);

        equipedweapons = new Weapon[2];

        equipedweapons[0] = weapons.get(0);

        current = equipedweapons[FIRSTSLOT];
        currentslot = FIRSTSLOT;

        bulletShooted = 0;
        hitBullets = 0;

        this.players = p;
    }
    public void loadSave(){
        weaponBorder_hud = new Image("Textures\\weapon_hud.tga",new Vector3f(1675,975,0),2.6f);

        soundSwitchingGun = AudioManager.loadSound("guns\\switchgun.ogg");
        source = AudioManager.createSource(Source.EFFECTS,0.35f);
        for(Weapon weapon: weapons){
            weapon.loadSave();
        }
    }
    // singleplayer
    public void shoot(float x, float y, float px, float py){
        if(current == null) return;
        current.shoot(x,y,px,py);
    }
    // multiplayer
    public void shoot(float x, float y, float px, float py, String username){
        if(current == null) return;
        current.shoot(x,y,px,py,username);

        if(MultiplayerManager.multiplayer){
            Client client = MultiplayerManager.getInstance().client.getClient();
            Network.MouseCoords mouseCoords = new Network.MouseCoords();
            mouseCoords.x = x;
            mouseCoords.y = y;
            mouseCoords.username = MultiplayerManager.getInstance().getUsername();
            client.sendUDP(mouseCoords);
        }
    }
    public void reload(){
        if(current == null)return;
        current.reload();
    }
    public void update(){
        // multiplayer
        if(MultiplayerManager.multiplayer) {
            PacketHolder packetHolder = MultiplayerManager.getInstance().packetHolder;
            int totalPlayers = 0;
            for (Player player : players) {
                if (player != null) totalPlayers++;
            }
            for (Object o : packetHolder.get(PacketHolder.ADDBULLET)) {
                Network.AddBullet addBullet = (Network.AddBullet) o;
                int index = addBullet.slot - (totalPlayers - 1);
                if (index < 0) index = 0;
                weapons.get(index).handleBulletPacket(addBullet);
            }
            for (Object o : packetHolder.getWithoutClear(PacketHolder.HITBULLET)) {
                for (Weapon w : weapons) {
                    w.handleHitBulletPacket((Network.HitBullet) o);
                }
            }
        }
        for(Weapon weapon : weapons){
            weapon.updateAmmo();
        }
        if(current == null) return;
        current.update();
    }

    public void draw(){
        for(Weapon weapon : weapons){
            weapon.drawAmmo();
        }
    }
    public void drawHud(){
        if(current != null) {
            current.draw();
        }

        weaponBorder_hud.draw();
    }

    private void setCurrentWeapon(Weapon current, int slot) {
        if(System.currentTimeMillis()-InGame.deltaPauseTime()-switchDelay < 500) return;
        // when slot is same as current
        if(currentslot == slot){
            return;
        }
        // when current gun is not realoding
        if(this.current == null){
            currentslot = slot;
            switchDelay = System.currentTimeMillis()-InGame.deltaPauseTime();
            this.current = current;
            stopShooting();
            source.play(soundSwitchingGun);
        }
        else if(this.current.canSwap()){
            currentslot = slot;
            switchDelay = System.currentTimeMillis()-InGame.deltaPauseTime();
            this.current = current;
            stopShooting();
            source.play(soundSwitchingGun);
        }
    }
    public void checkCollisions(ArrayList<Enemy> enemies){
        for(Weapon weapon:weapons){
            weapon.checkCollisions(enemies);
        }
    }

    public void keyPressed(int k, int x,int y) {
        if(k == ControlSettings.getValue(ControlSettings.WEAPON_DROP)){
            if(MultiplayerManager.multiplayer){
                MultiplayerManager mp = MultiplayerManager.getInstance();
                Client client = mp.client.getClient();
                Network.PlayerDropWeapon dropWeapon = new Network.PlayerDropWeapon();
                dropWeapon.username = mp.getUsername();
                dropWeapon.x = x;
                dropWeapon.y = y;
                client.sendTCP(dropWeapon);
            } else {
                if(current != null){
                    stopShooting();
                    ItemManager itemManager = ItemManager.getInstance();
                    itemManager.dropWeapon(current, x,y);
                }
                current = null;
                equipedweapons[currentslot] = null;
            }
        } else if (k == ControlSettings.getValue(ControlSettings.WEAPON_SLOT1)){
            if(MultiplayerManager.multiplayer){
                Client client = MultiplayerManager.getInstance().client.getClient();
                Network.SwitchWeaponSlot switchWeaponSlot = new Network.SwitchWeaponSlot();
                switchWeaponSlot.username = MultiplayerManager.getInstance().getUsername();
                switchWeaponSlot.slot = FIRSTSLOT;
                client.sendTCP(switchWeaponSlot);
            } else {
                setCurrentWeapon(equipedweapons[FIRSTSLOT],FIRSTSLOT);
            }
        } else if (k == ControlSettings.getValue(ControlSettings.WEAPON_SLOT2)){
            if(MultiplayerManager.multiplayer){
                Client client = MultiplayerManager.getInstance().client.getClient();
                Network.SwitchWeaponSlot switchWeaponSlot = new Network.SwitchWeaponSlot();
                switchWeaponSlot.username = MultiplayerManager.getInstance().getUsername();
                switchWeaponSlot.slot = SECONDARYSLOT;
                client.sendTCP(switchWeaponSlot);
            } else {
                setCurrentWeapon(equipedweapons[SECONDARYSLOT],SECONDARYSLOT);
            }
        }
    }
    public void stopShooting(){
        if(current == null) return;
        current.setShooting(false);
        if(MultiplayerManager.multiplayer){
            Network.StopShooting stopShooting = new Network.StopShooting();
            stopShooting.username = ((PlayerMP)players[0]).getUsername();
            Client client = MultiplayerManager.getInstance().client.getClient();
            client.sendTCP(stopShooting);
        }
    }
    public void startShooting(){
        if(current == null) return;
        current.setShooting(true);
        if(MultiplayerManager.multiplayer){
            Network.StartShooting startShooting = new Network.StartShooting();
            startShooting.username = ((PlayerMP)players[0]).getUsername();
            Client client = MultiplayerManager.getInstance().client.getClient();
            client.sendTCP(startShooting);
        }
    }
    public boolean addAmmo(int amountpercent, int type) {
        // first check main gun in hand
        if(current != null){
            if(current.getType() == type){
                if(current.isFullAmmo()) return false;
                current.addAmmo(amountpercent);
                return true;
            }
        }
        // check all guns in inventory
        for (int i = 0; i < 2; i++) {
            Weapon weapon = equipedweapons[i];
            if (weapon == null) continue;
            if (weapon.getType() == type) {
                if(weapon.isFullAmmo()) return false;
                weapon.addAmmo(amountpercent);
                return true;
            }
        }
        return false;
    }
    public void changeGun(int x, int y, Weapon weapon){
        source.play(soundSwitchingGun);
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
        itemManager.dropWeapon(current,x,y);
        equipedweapons[currentslot] = weapon;
        current=weapon;
    }
    public void changeGun(Weapon weapon){
        source.play(soundSwitchingGun);
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
        equipedweapons[currentslot] = weapon;
        current=weapon;
    }
    public Weapon randomGun(){
        Weapon weapon = weapons.get(1+Random.nextInt(weapons.size()-1));
        while(weapon.hasAlreadyDropped()){
            weapon = weapons.get(1+Random.nextInt(weapons.size()-1));
        }
        return weapon;
    }
    public Weapon getWeapon(int index){
        return weapons.get(index);
    }
    public void changeGunScroll(){
        int slot = currentslot;
        slot++;
        if(slot > 1) slot = 0;
        setCurrentWeapon(equipedweapons[slot],slot);
    }
    public int[] getWeaponTypes(){
        int[] types = new int[2];
        if(equipedweapons[0] != null) types[0] = equipedweapons[0].getType();
        else types[0] = -1;
        if(equipedweapons[1] != null) types[1] = equipedweapons[1].getType();
        else types[1] = -1;
        return types;
    }

    public int getCurrentslot() {
        return currentslot;
    }

    public void handleAddBulletPacket(Network.AddBullet response){
        PacketHolder packetHolder = MultiplayerManager.getInstance().packetHolder;
        packetHolder.add(response,PacketHolder.ADDBULLET);
        if(current != null){
            if(MultiplayerManager.getInstance().getUsername().equalsIgnoreCase(response.username)){
                current.shootSound(response);
            }
        }
    }

    public void handleBulletMovePacket(Network.MoveBullet moveBullet) {
        for(Weapon w : weapons){
            w.handleBulletMovePacket(moveBullet);
        }
    }

    public void handleWeaponInfoPacket(Network.WeaponInfo weaponInfo){
        String username = MultiplayerManager.getInstance().getUsername();
        if(username.equalsIgnoreCase(weaponInfo.username)){
            if(current != null){
                current.handleWeaponInfoPacket(weaponInfo);
            }
        }
    }

    public void handleSwitchWeaponPacket(Network.SwitchWeaponSlot switchWeapon) {
        if(switchWeapon.sucessful){
            if(MultiplayerManager.getInstance().getUsername().equalsIgnoreCase(switchWeapon.username)){
                setCurrentWeapon(equipedweapons[switchWeapon.slot],switchWeapon.slot);
            }
        }
    }

    public void handleDropPlayerWeaponPacket(Network.PlayerDropWeapon playerDropWeapon) {
        if(playerDropWeapon.sucessful){
            if(MultiplayerManager.getInstance().getUsername().equalsIgnoreCase(playerDropWeapon.username)){
                if(current != null){
                    stopShooting();
                    source.play(soundSwitchingGun);
                }
                current = null;
                equipedweapons[currentslot] = null;
            }
        }
    }

    public void handleDropWeaponPacket(Network.DropWeapon dropWeapon) {
        // converting indexing from server to client ones
        int totalPlayers = 0;
        for(Player player : players){
            if(player != null) totalPlayers++;
        }
        int index = dropWeapon.slot-(totalPlayers-1);
        if(index < 0) index = 0;
        dropWeapon.slot = index;
        PacketHolder packetHolder = MultiplayerManager.getInstance().packetHolder;
        packetHolder.add(dropWeapon,PacketHolder.DROPWEAPON);
    }
}

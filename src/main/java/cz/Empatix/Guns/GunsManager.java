package cz.Empatix.Guns;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Multiplayer.*;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import cz.Empatix.Utility.Random;
import org.joml.Vector3f;

import java.util.ArrayList;

public class GunsManager {
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
        weapons.add(new Pistol(tileMap,p));
        weapons.add(new Shotgun(tileMap,p));
        weapons.add(new Uzi(tileMap,p));
        weapons.add(new Revolver(tileMap,p));
        weapons.add(new Grenadelauncher(tileMap,p));
        weapons.add(new Luger(tileMap,p));
        weapons.add(new M4(tileMap,p));
        weapons.add(new Thompson(tileMap,p));
        weapons.add(new ModernShotgun(tileMap,p));


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
    private ArrayList<Network.HitBullet> hitPackets;
    public GunsManager(TileMap tileMap, PlayerMP[] p){
        weapons = new ArrayList<>();
        weapons.add(new Pistol(tileMap,p[0]));
        weapons.add(new Shotgun(tileMap,p[0]));
        weapons.add(new Uzi(tileMap,p[0]));
        weapons.add(new Revolver(tileMap,p[0]));
        weapons.add(new Grenadelauncher(tileMap,p[0]));
        weapons.add(new Luger(tileMap,p[0]));
        weapons.add(new M4(tileMap,p[0]));
        weapons.add(new Thompson(tileMap,p[0]));
        weapons.add(new ModernShotgun(tileMap,p[0]));

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
        hitPackets = new ArrayList<>();
    }
    // singleplayer
    public void shoot(float x, float y, float px, float py){
        if(current == null) return;
        current.shoot(x,y,px,py);
    }
    // multiplayer
    public void shoot(float x, float y, float px, float py, int idPlayer){
        if(current == null) return;
        current.shoot(x,y,px,py,idPlayer);

        Client client = MultiplayerManager.getInstance().client.getClient();
        Network.MouseCoords mouseCoords = new Network.MouseCoords();
        mouseCoords.x = x;
        mouseCoords.y = y;
        mouseCoords.idPlayer = idPlayer;
        client.sendUDP(mouseCoords);

    }
    public void reload(){
        if(current == null)return;
        current.reload();
    }
    // singleplayer
    public void update(){
        for(Weapon weapon : weapons){
            weapon.updateAmmo();
        }
        if(current == null) return;
        current.update();
    }
    // multiplayer)
    public void update(Object[] hitPackets){
        if(MultiplayerManager.multiplayer) {
            PacketHolder packetHolder = MultiplayerManager.getInstance().packetHolder;
            Object[] infoPackets = packetHolder.get(PacketHolder.WEAPONINFO);
            for (Object o : infoPackets) {
                handleWeaponInfoPacket((Network.WeaponInfo) o);
            }
            Object[] addPackets = packetHolder.get(PacketHolder.ADDBULLET);
            for (Object o : addPackets) {
                Network.AddBullet addBullet = (Network.AddBullet) o;
                int slot = translateSlot(addBullet.slot);
                weapons.get(slot).handleAddBulletPacket(addBullet);
            }
            for (Object o : hitPackets) {
                this.hitPackets.add((Network.HitBullet) o);
            }

            for (Weapon w : weapons) {
                for (Network.HitBullet hitBullet : this.hitPackets) {
                    if (hitBullet.tick <= GameClient.interpolationTick) {
                        w.handleHitBulletPacket(hitBullet);
                    }
                }
            }
            this.hitPackets.removeIf(hitBullet -> hitBullet.tick <= GameClient.interpolationTick);

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
                mp.client.requestACK(dropWeapon,dropWeapon.idPacket);
                dropWeapon.idPlayer = mp.getIdConnection();
                dropWeapon.x = x;
                dropWeapon.y = y;
                client.sendUDP(dropWeapon);
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
                MultiplayerManager.getInstance().client.requestACK(switchWeaponSlot,switchWeaponSlot.idPacket);
                switchWeaponSlot.idPlayer = MultiplayerManager.getInstance().getIdConnection();
                switchWeaponSlot.slot = FIRSTSLOT;
                client.sendUDP(switchWeaponSlot);
            } else {
                setCurrentWeapon(equipedweapons[FIRSTSLOT],FIRSTSLOT);
            }
        } else if (k == ControlSettings.getValue(ControlSettings.WEAPON_SLOT2)){
            if(MultiplayerManager.multiplayer){
                Client client = MultiplayerManager.getInstance().client.getClient();
                Network.SwitchWeaponSlot switchWeaponSlot = new Network.SwitchWeaponSlot();
                MultiplayerManager.getInstance().client.requestACK(switchWeaponSlot,switchWeaponSlot.idPacket);
                switchWeaponSlot.idPlayer = MultiplayerManager.getInstance().getIdConnection();
                switchWeaponSlot.slot = SECONDARYSLOT;
                client.sendUDP(switchWeaponSlot);
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
            MultiplayerManager.getInstance().client.requestACK(stopShooting,stopShooting.idPacket);
            stopShooting.idPlayer = MultiplayerManager.getInstance().getIdConnection();
            Client client = MultiplayerManager.getInstance().client.getClient();
            client.sendUDP(stopShooting);
        }
    }
    public void startShooting(){
        if(current == null) return;
        current.setShooting(true);
        if(MultiplayerManager.multiplayer){
            Network.StartShooting startShooting = new Network.StartShooting();
            MultiplayerManager.getInstance().client.requestACK(startShooting,startShooting.idPacket);
            startShooting.idPlayer = MultiplayerManager.getInstance().getIdConnection();
            Client client = MultiplayerManager.getInstance().client.getClient();
            client.sendUDP(startShooting);
        }
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
        if(MultiplayerManager.multiplayer){
            Client client = MultiplayerManager.getInstance().client.getClient();
            Network.SwitchWeaponSlot switchWeaponSlot = new Network.SwitchWeaponSlot();
            MultiplayerManager.getInstance().client.requestACK(switchWeaponSlot,switchWeaponSlot.idPacket);
            switchWeaponSlot.idPlayer = MultiplayerManager.getInstance().getIdConnection();
            switchWeaponSlot.slot = (byte)slot;
            client.sendUDP(switchWeaponSlot);
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

    public int getCurrentslot() {
        return currentslot;
    }

    public void handleAddBulletPacket(Network.AddBullet response){
        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        PacketHolder packetHolder = mpManager.packetHolder;
        packetHolder.add(response,PacketHolder.ADDBULLET);
    }

    public void handleBulletMovePacket(Object[] moveBullets) {
        for(Object o : moveBullets){
            Network.MoveBullet p = (Network.MoveBullet) o;
            for(Weapon w : weapons){
                boolean correct = w.handleMoveBulletPacket(p);
                if(correct) break;
            }
        }
    }

    private int lastWeaponInfoTick = 0;
    public void handleWeaponInfoPacket(Network.WeaponInfo weaponInfo){
        int currentslot = 0;
        int idPlayer = MultiplayerManager.getInstance().getIdConnection();
        if(idPlayer == weaponInfo.idPlayer) {
            if(lastWeaponInfoTick < weaponInfo.tick){
                lastWeaponInfoTick = weaponInfo.tick;
                for (byte slot : weaponInfo.slots) {
                    if(slot == -1){
                        // slot is null
                        equipedweapons[currentslot++] = null;
                    } else {
                        int clientSlot = translateSlot(slot);
                        equipedweapons[currentslot++] = weapons.get(clientSlot);
                    }
                }
                if(equipedweapons[weaponInfo.currSlot] != null) equipedweapons[weaponInfo.currSlot].handleWeaponInfoPacket(weaponInfo);
                this.currentslot = weaponInfo.currSlot;
                current = equipedweapons[weaponInfo.currSlot];
            }
        }
    }

    public void handleSwitchWeaponPacket(Network.SwitchWeaponSlot switchWeapon) {
        if(switchWeapon.sucessful){
            if(MultiplayerManager.getInstance().getIdConnection() == switchWeapon.idPlayer){
                setCurrentWeapon(equipedweapons[switchWeapon.slot],switchWeapon.slot);
            }
        }
    }

    public void handleDropPlayerWeaponPacket(Network.PlayerDropWeapon playerDropWeapon) {
        if(playerDropWeapon.sucessful){
            if(MultiplayerManager.getInstance().getIdConnection() == playerDropWeapon.idPlayer){
                if(equipedweapons[playerDropWeapon.playerSlot] != null){
                    stopShooting();
                    source.play(soundSwitchingGun);
                }
                if(current == equipedweapons[playerDropWeapon.playerSlot]){
                    current = null;
                }
                equipedweapons[playerDropWeapon.playerSlot] = null;
            }
        }
    }

    public void handleDropWeaponPacket(Network.DropWeapon dropWeapon) {
        // converting indexing from server to client ones
        dropWeapon.slot = (byte)translateSlot(dropWeapon.slot);
        PacketHolder packetHolder = MultiplayerManager.getInstance().packetHolder;
        packetHolder.add(dropWeapon,PacketHolder.DROPWEAPON);
    }

    /**
     * translates slot from server to client
     * @param serverSlot slots on server is different than on client
     * @return client slot
     */
    public int translateSlot(int serverSlot){
        // converting indexing from server to client ones
        /*int totalPlayers = 0;
        for(Player player : players){ //TODO: handle disconnect, wont work with 3+ players, we must offset by TotalPlayerOnStart - 1
            if(player != null) totalPlayers++;
        }*/
        int index = serverSlot-1;
        if(index < 0) index = 0;
        return index;
    }
}

package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.Bullet;

// This class is a convenient place to keep things common to both the client and server.
public class Network {
    private static int idPacketServer = 0;
    private static int idPacketClient = 0;

    static int port = 54555;
    // This registers objects that are going to be sent over the network.
    public static void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Join.class);
        kryo.register(AddPlayer.class);
        kryo.register(MovePlayer.class);
        kryo.register(MovePlayerInput.class);
        kryo.register(Disconnect.class);
        kryo.register(Ready.class);
        kryo.register(int[][].class);
        kryo.register(int[].class);
        kryo.register(boolean[].class);
        kryo.register(byte[].class);
        kryo.register(TransferRoomMap.class);
        kryo.register(TransferRoom.class);
        kryo.register(MapLoaded.class);
        kryo.register(ChangeGamestate.class);
        kryo.register(RequestForPlayers.class);
        kryo.register(StartShooting.class);
        kryo.register(StopShooting.class);
        kryo.register(WeaponInfo.class);
        kryo.register(Reload.class);
        kryo.register(AddBullet.class);
        kryo.register(AddEnemy.class);
        kryo.register(RemoveEnemy.class);
        kryo.register(MoveEnemy.class);
        kryo.register(MoveBullet.class);
        kryo.register(HitBullet.class);
        kryo.register(Bullet.TypeHit.class);
        kryo.register(DropItem.class);
        kryo.register(RemoveItem.class);
        kryo.register(SwitchWeaponSlot.class);
        kryo.register(PlayerDropWeapon.class);
        kryo.register(DropWeapon.class);
        kryo.register(DropInteract.class);
        kryo.register(MoveDropItem.class);
        kryo.register(TypeRoomObject.class);
        kryo.register(AddRoomObject.class);
        kryo.register(MoveRoomObject.class);
        kryo.register(MouseCoords.class);
        kryo.register(AddEnemyProjectile.class);
        kryo.register(MoveEnemyProjectile.class);
        kryo.register(HitEnemyProjectile.class);
        kryo.register(ShopDropitem.class);
        kryo.register(PlayerInfo.class);
        kryo.register(Alert.class);
        kryo.register(OpenChest.class);
        kryo.register(Player.DamageAbsorbedBy.class);
        kryo.register(PlayerHit.class);
        kryo.register(DropArtefact.class);
        kryo.register(ArtefactActivate.class);
        kryo.register(ArtefactAddBullet.class);
        kryo.register(LockRoom.class);
        kryo.register(NextFloor.class);
        kryo.register(ObjectInteract.class);
        kryo.register(AllPlayersDeath.class);
        kryo.register(PstatsUpdate.class);
        kryo.register(RoomObjectAnimationSync.class);
        kryo.register(TrapArrowAdd.class);
        kryo.register(TrapArrowHit.class);
        kryo.register(TrapArrowMove.class);
        kryo.register(CanJoin.class);
        kryo.register(EnemySync.class);
        kryo.register(LaserBeamSync.class);
        kryo.register(LaserBeamHit.class);
        kryo.register(Ping.class);
        kryo.register(NumUpgradesUpdate.class);
        kryo.register(NumUpgrades.class);
        kryo.register(EnemyHealthHeal.class);
        kryo.register(ArtefactEventState.class);
        kryo.register(PacketACK.class);
        kryo.register(ExplosionDamage.class);
        kryo.register(TrapRoomObjectDamage.class);
        kryo.register(PMovementSync.class);

    }
    // MAIN
    public static class Join {
        public String username;
        public boolean host;
    }
    public static class CanJoin {
        public boolean can;
        public int idPlayer;
    }
    public static class PMovementSync {
        public int idPacket;
        public int idPlayer;
    }
    public static class AllPlayersDeath {
    }
    public static class AddPlayer {
        public String username;
        public int idPlayer;
    }
    public static class Disconnect {
        public int idPlayer;
    }
    public static class MovePlayer {
        public int idPacket;
        //public String username;
        public int idPlayer;
        public float x;
        public float y;
        public boolean up,down,left,right;
    }
    public static class MovePlayerInput {
        public int idPlayer;
        public boolean up,down,left,right;
    }
    public static class ArtefactEventState {
        public int idPacket;
        /*
        States:
            0 - Player artefact hit
            1 - Player artefact drop
         */
        public int slot;
        public int state;
        public ArtefactEventState(){
            idPacket = getIdPacketS();
        }
    }
    // PROGRESS ROOM + MAP GENERATION, POST DEATH
    public static class Ready {
        public int idPacket;
        public int idPlayer;
        public boolean state;
        public Ready(){
            idPacket = getIdPacketC();
        }
    }
    public static class TransferRoomMap {
        public int[][] roomMap;
        public byte roomX,roomY;
    }
    public static class TransferRoom {
        public byte id, x, y;
        public String mapFilepath;
        public byte type;
        public byte index; // index in roomArrayList
        public byte previousIndex; // index of old mm room, that we will add new one
        public boolean top, bottom, left, right;
    }
    public static class MapLoaded {
        public byte totalRooms;
    }
    public static class RequestForPlayers {
        public int exceptIdPlayer; // except the origin plaqyer
    }
    public static class ChangeGamestate{
        public int gamestate;
    }
    // GUNS
    public static class MouseCoords{
        public int idPlayer;
        public float x,y;
    }
    public static class StartShooting {
        public int idPlayer;
        public int idPacket;
        public StartShooting(){
            idPacket = getIdPacketC();
        }
    }
    public static class StopShooting {
        public int idPlayer;
        public int idPacket;
        public StopShooting(){
            idPacket = getIdPacketC();
        }
    }
    public static class Ping{
    }
    public static class Reload {
        public int idPlayer;
        public int idPacket;
        public Reload(){
            idPacket = getIdPacketC();
        }
    }
    public static class AddBullet {
        public int idPlayer;
        public float x,y;
        public float px,py;
        public byte damage;
        public boolean critical;
        public byte speed;
        public float inaccuracy;
        public byte slot;
        public int id;
        public int idPacket;
        public AddBullet(){
            idPacket = getIdPacketS();
        }
    }
    public static class MoveBullet{
        public float x,y;
        public int id;
        public int idPacket;
        public MoveBullet(){
            idPacket = getIdPacketS();
        }
    }
    public static class HitBullet{
        public Bullet.TypeHit type;
        public int id;
        public int idHit;
        public int idPacket;
        public HitBullet(){
            idPacket = getIdPacketS();
        }
    }

    /**
     * typeHit
     * true - enemy
     * false - room object
     */
    public static class ExplosionDamage{
        public int[] idHit;
        public boolean[] typeHit;
        public int idPacket;
        public byte damage;
        public boolean critical;

        public ExplosionDamage(){
            idPacket = getIdPacketS();
        }
    }
    public static class DropItem{
        public byte type;
        public int id;
        public int x, y;
        public byte amount;
        public int idPacket;
        public boolean despawn;
        public DropItem(){
            idPacket = getIdPacketS();
        }
    }
    public static class MoveDropItem{
        public float x,y;
        public int id;
        public int idPacket;
        public MoveDropItem(){
            idPacket = getIdPacketS();
        }
    }
    public static class RemoveItem{
        public int id;
        public int idPacket;
        public RemoveItem(){
            idPacket = getIdPacketS();
        }
    }
    public static class MoveEnemy {
        public float x, y;
        public int id;
        public boolean up, down, right, left, facingRight;
        public int idPacket;
        public MoveEnemy(){
            idPacket = getIdPacketS();
        }
    }
    public static class AddEnemy {
        public float x,y;
        public String type;
        public int id;
        public int idPacket;
        public AddEnemy(){
            idPacket = getIdPacketS();
        }
    }
    public static class RemoveEnemy {
        public int id;
        public int idPacket;
        public RemoveEnemy(){
            idPacket = getIdPacketS();
        }
    }
    public static class WeaponInfo {
        public int idPlayer;
        public short currentAmmo;
        public short currentMagazineAmmo;
        public int idPacket;
        public WeaponInfo(){
            idPacket = getIdPacketS();
        }
    }
    public static class SwitchWeaponSlot{
        public int idPlayer;
        public byte slot;
        public boolean sucessful;
        public int idPacket;
        public SwitchWeaponSlot(){
            idPacket = getIdPacketC();
        }
    }
    public static class PlayerDropWeapon{
        public int idPlayer;
        public boolean sucessful;
        public int x, y;
        public byte playerSlot;
        public int idPacket;
        public PlayerDropWeapon(){
            idPacket = getIdPacketC();
        }
    }
    public static class DropWeapon{
        public int x, y;
        public int id;
        public byte slot;
        public int idPacket;
        public DropWeapon(){
            idPacket = getIdPacketS();
        }
    }
    public static class DropArtefact{
        public short dx, dy;
        public int x, y;
        public int id;
        public byte slot;
        public int idPlayer;
        public int idPacket;
        public DropArtefact(){
            idPacket = getIdPacketS();
        }
    }
    public static class DropInteract {
        public int idPlayer;
        public boolean sucessful;
        public int id;
        public int x, y;
        public int idPacket;
        public DropInteract(){
            idPacket = getIdPacketC();
        }
    }
    public static class ObjectInteract {
        public int idPlayer;
        public int idPacket;
        public ObjectInteract(){
            idPacket = getIdPacketC();
        }
    }
    public enum TypeRoomObject{
        CHEST,SPIKE,BONES,TORCH,BARREL,POT,SHOPKEEPER,SHOPTABLE,FLAMETHROWER,LADDER,
        FLAG,ARROWTRAP, CRYSTAL
    }
    public static class AddRoomObject{
        public int id;
        public int idRoom;
        public int x, y;
        public TypeRoomObject type;
        public byte objectType; // type as TOP/LEFT/RIGHT torch, or TOP/SIDE arrowtrap
        public int idPacket;
        public AddRoomObject(){
            idPacket = getIdPacketS();
        }
    }
    public static class MoveRoomObject{
        public int id;
        public float x, y;
        public int idPacket;
        public MoveRoomObject(){
            idPacket = getIdPacketS();
        }
    }
    public static class AddEnemyProjectile{
        public int id;
        public int idEnemy;
        public float x, y; // direction
        public float inaccuracy;
        public int idPacket;
        public AddEnemyProjectile(){
            idPacket = getIdPacketS();
        }
    }
    public static class MoveEnemyProjectile{
        public int id;
        public int idEnemy;
        public float x, y;
        public int idPacket;
        public MoveEnemyProjectile(){
            idPacket = getIdPacketS();
        }
    }
    public static class HitEnemyProjectile {
        public int id; // id of projectile
        public int idEnemy; // id of owner's projectile
        public int idHit; // id of room that was hitted
        public int idPacket;
        public HitEnemyProjectile(){
            idHit= -1;
            idPacket = getIdPacketS();
        }
        // idHit == -1  means that player was hitted or room object
        // damage hit is done on serverside, only if it is room object damage is done on clientside
    }

    public static class ShopDropitem {
        public int id;
        public int idObject;
        public byte type;
        public short price;
        public byte amount;
        public byte objectSlot;
        public byte subType;
    }
    public static class PlayerInfo {
        public int idPlayer;
        public byte health, maxHealth;
        public short coins;
        public byte armor, maxArmor;
        public int idPacket;
        public PlayerInfo(){
            idPacket = getIdPacketS();
        }
    }
    public static class Alert {
        public int idPlayer;
        public String text;
        public boolean warning;
        public int idPacket;
        public Alert(){
            idPacket = getIdPacketS();
        }
    }
    public static class OpenChest {
        public int id;
        public byte idRoom;
        public int idPacket;
        public OpenChest(){
            idPacket = getIdPacketS();
        }
    }
    public static class PlayerHit {
        public int idPlayer;
        public Player.DamageAbsorbedBy type;
        public int idPacket;
        public PlayerHit(){
            idPacket = getIdPacketS();
        }
    }
    public static class ArtefactActivate{
        public int idPlayer;
        public byte slot;
        public int idPacket;
        public ArtefactActivate(){
            idPacket = getIdPacketC();
        }
    }
    public static class ArtefactAddBullet{
        public int idPlayer;
        public float x, y; // direction
        public float px,py;
        public int id;
        public float inaccuracy;
        public byte slot;
        public int idPacket;
        public ArtefactAddBullet(){
            idPacket = getIdPacketS();
        }
    }
    public static class LockRoom{
        public boolean lock;
        public byte idRoom;
        public int idPacket;
        public LockRoom(){
            idPacket = getIdPacketS();
        }
    }
    public static class NextFloor{
        public byte floor;
        public byte[] affixes;
    }
    public static class PstatsUpdate {
        public int idPlayer;
        public short shootShooted;
        public short enemiesKilled;
        public short bulletsHit;
        public long deathTime;
        public int idPacket;
        public PstatsUpdate(){
            idPacket = getIdPacketS();
        }
    }
    public static class RoomObjectAnimationSync{ // made so animations of trap are same as server logic
        public int id;
        public byte sprite;
        public long time;
        public int idPacket;
        public long cooldown;
        public RoomObjectAnimationSync(){
            idPacket = getIdPacketS();
        }
    }
    public static class TrapArrowMove{
        public int id;
        public float x,y;
        public int idPacket;
        public TrapArrowMove(){
            idPacket = getIdPacketS();
        }
    }
    public static class TrapArrowAdd {
        public int id;
        public int idTrap;
        public float x,y;
        public boolean horizontal;
        public boolean facingRight;
        public int idPacket;
        public TrapArrowAdd(){
            idPacket = getIdPacketS();
        }
    }
    public static class TrapArrowHit{
        public int id;
        public int idPacket;
        public TrapArrowHit(){
            idPacket = getIdPacketS();
        }
    }
    public static class TrapRoomObjectDamage{
        public int idHit;
        public int idPacket;
        public TrapRoomObjectDamage(){
            idPacket = getIdPacketS();
        }
    }
    public static class EnemySync {
        public int id;
        public byte currAction;
        public byte sprite;
        public long time;
        public int idPacket;
        public EnemySync(){
            idPacket = getIdPacketS();
        }
    }
    public static class LaserBeamSync {
        public int id;
        public byte sprite;
        public float x,y;
        public long time;
        public double angle;
        public int idPacket;
        public int lastTarget;
        public LaserBeamSync(){
            idPacket = getIdPacketS();
        }
    }
    public static class LaserBeamHit {
        public int idHit;
        public int idPacket;
        public LaserBeamHit(){
            idPacket = getIdPacketS();
        }
    }
    public static class NumUpgrades {
        public int idPlayer;
        public int[] numUpgrades;
    }
    public static class NumUpgradesUpdate {
        public int idPlayer;
        public String gunName;
        public byte numUpgrades;
    }
    public static class EnemyHealthHeal {
        public int id;
        public short amount;
        public int idPacket;
        public EnemyHealthHeal(){
            idPacket = getIdPacketS();
        }
    }
    // WHEN UDP PACKET WAS RECEIVED BY SERVER or CLIENT
    // IT WILL BE SEND AS ACKNOWLEDGE PACKET IN TCP, BUT WITHOUT WAITING FOR CORRECTION
    public static class PacketACK{
        int id;
    }

    private static int getIdPacketS(){return idPacketServer++;}
    private static int getIdPacketC(){return idPacketClient++;}

}

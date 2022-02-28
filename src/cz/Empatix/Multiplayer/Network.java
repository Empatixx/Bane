package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.Bullet;

// This class is a convenient place to keep things common to both the client and server.
public class Network {

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

    }
    // MAIN
    public static class Join {
        public String username;
        public boolean host;
    }
    public static class CanJoin {
        public boolean can;
    }
    public static class AllPlayersDeath {
    }
    public static class AddPlayer {
        public String username;
    }
    public static class Disconnect {
        public String username;
    }
    public static class MovePlayer {
        public String username;
        public float x;
        public float y;
        public boolean up,down,left,right;
        public long time;
    }
    public static class MovePlayerInput {
        public String username;
        public boolean up,down,left,right;
    }
    public static class ArtefactEventState {
        /*
        States:
            0 - Player artefact hit
            1 - Player artefact drop
         */
        public int slot;
        public int state;
    }
    // PROGRESS ROOM + MAP GENERATION, POST DEATH
    public static class Ready {
        public String username;
        public boolean state;
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
        public String exceptUsername; // except origin player
    }
    public static class ChangeGamestate{
        public int gamestate;
    }
    // GUNS
    public static class MouseCoords{
        public String username;
        public float x,y;
    }
    public static class StartShooting {
        public String username;
    }
    public static class StopShooting {
        public String username;
    }
    public static class Ping{
    }
    public static class Reload {
        public String username;
    }
    public static class AddBullet {
        public String username;
        public float x,y;
        public float px,py;
        public byte damage;
        public boolean critical;
        public byte speed;
        public float inaccuracy;
        public byte slot;
        public int id;
    }
    public static class MoveBullet{
        public float x,y;
        public int id;
    }
    public static class HitBullet{
        public Bullet.TypeHit type;
        public int id;
        public int idHit;
    }
    public static class DropItem{
        public byte type;
        public int id;
        public int x, y;
        public byte amount;
    }
    public static class MoveDropItem{
        public float x,y;
        public int id;
    }
    public static class RemoveItem{
        public int id;
    }
    public static class MoveEnemy {
        public float x, y;
        public int id;
        public boolean up, down, right, left, facingRight;
    }
    public static class AddEnemy {
        public float x,y;
        public String type;
        public int id;
    }
    public static class RemoveEnemy {
        public int id;
    }
    public static class WeaponInfo {
        public String username;
        public short currentAmmo;
        public short currentMagazineAmmo;
    }
    public static class SwitchWeaponSlot{
        public String username;
        public byte slot;
        public boolean sucessful;
    }
    public static class PlayerDropWeapon{
        public String username;
        public boolean sucessful;
        public int x, y;
        public byte playerSlot;
    }
    public static class DropWeapon{
        public int x, y;
        public int id;
        public byte slot;
    }
    public static class DropArtefact{
        public short dx, dy;
        public int x, y;
        public int id;
        public byte slot;
        public String username;
    }
    public static class DropInteract {
        public String username;
        public boolean sucessful;
        public int id;
        public int x, y;
    }
    public static class ObjectInteract {
        public String username;
        public int id;
    }
    public enum TypeRoomObject{
        CHEST,SPIKE,BONES,TORCH,BARREL,POT,SHOPKEEPER,SHOPTABLE,FLAMETHROWER,LADDER,
        FLAG,ARROWTRAP
    }
    public static class AddRoomObject{
        public int id;
        public int idRoom;
        public int x, y;
        public TypeRoomObject type;
        public byte objectType; // type as TOP/LEFT/RIGHT torch, or TOP/SIDE arrowtrap
    }
    public static class MoveRoomObject{
        public int id;
        public float x, y;
    }
    public static class AddEnemyProjectile{
        public int id;
        public int idEnemy;
        public float x, y; // direction
        public float inaccuracy;
    }
    public static class MoveEnemyProjectile{
        public int id;
        public int idEnemy;
        public float x, y;
    }
    public static class HitEnemyProjectile {
        public int id; // id of projectile
        public int idEnemy; // id of owner's projectile
        public int idHit; // id of room that was hitted
        public HitEnemyProjectile(){
            idHit= -1;
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
    }
    public static class PlayerInfo {
        public String username;
        public byte health, maxHealth;
        public short coins;
        public byte armor, maxArmor;
    }
    public static class Alert {
        public String username;
        public String text;
        public boolean warning;
    }
    public static class OpenChest {
        public int id;
        public byte idRoom;
    }
    public static class PlayerHit {
        public String username;
        public Player.DamageAbsorbedBy type;
    }
    public static class ArtefactActivate{
        public String username;
        public byte slot;
    }
    public static class ArtefactAddBullet{
        public String username;
        public float x, y; // direction
        public float px,py;
        public int id;
        public float inaccuracy;
        public byte slot;
    }
    public static class LockRoom{
        public boolean lock;
        public byte idRoom;
    }
    public static class NextFloor{
        public byte floor;
    }
    public static class PstatsUpdate {
        public String username;
        public short shootShooted;
        public short enemiesKilled;
        public short bulletsHit;
        public long deathTime;
    }
    public static class RoomObjectAnimationSync{ // made so animations of trap are same as server logic
        public int id;
        public byte sprite;
        public long time;
        public long cooldown; // like in flamethrower, arrowtrap
    }
    public static class TrapArrowMove{
        public int id;
        public float x,y;
    }
    public static class TrapArrowAdd {
        public int id;
        public int idTrap;
        public float x,y;
        public boolean horizontal;
        public boolean facingRight;
    }
    public static class TrapArrowHit{
        public int id;
    }
    public static class EnemySync {
        public int id;
        public byte currAction;
        public byte sprite;
        public long time;
    }
    public static class LaserBeamSync {
        public int id;
        public byte sprite;
        public float x,y;
        public long time;
        public double angle;
    }
    public static class LaserBeamHit {
        public int idHit;
    }
    public static class NumUpgrades {
        public String username;
        public int[] numUpgrades;
    }
    public static class NumUpgradesUpdate {
        public String username;
        public String gunName;
        public byte numUpgrades;
    }
    public static class EnemyHealthHeal {
        public int id;
        public short amount;
    }
}

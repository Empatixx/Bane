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
        kryo.register(MoveEnemy.class);
        kryo.register(MoveBullet.class);
        kryo.register(ConfirmChangeGS.class);
        kryo.register(HitBullet.class);
        kryo.register(Bullet.TypeHit.class);
        kryo.register(DropItem.class);
        kryo.register(RemoveItem.class);
        kryo.register(SwitchWeaponSlot.class);
        kryo.register(PlayerDropWeapon.class);
        kryo.register(DropWeapon.class);
        kryo.register(ObjectInteract.class);
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

    }
    // MAIN
    public static class Join {
        public String username;
        public boolean host;
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
    // PROGRESS ROOM + MAP GENERATION
    public static class Ready {
        public String username;
        public boolean state;
    }
    public static class TransferRoomMap {
        public int[][] roomMap;
        public int roomX,roomY;
    }
    public static class TransferRoom {
        public int id, x, y;
        public String mapFilepath;
        public int type;
        public int index; // index in roomArrayList
        public int previousIndex; // index of old mm room, that we will add new one
        public boolean top, bottom, left, right;
    }
    public static class MapLoaded {}
    public static class RequestForPlayers {
        public String exceptUsername; // except origin player
    }
    public static class ChangeGamestate{
        public int gamestate;
    }
    public static class ConfirmChangeGS {
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
    public static class Reload {
        public String username;
    }
    public static class AddBullet {
        public String username;
        public float x,y;
        public float px,py;
        public int damage;
        public boolean critical;
        public int speed;
        public float inaccuracy;
        public int slot;
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
        public int type;
        public int id;
        public int x, y;
        public int amount;
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
    public static class WeaponInfo {
        public String username;
        public int currentAmmo;
        public int currentMagazineAmmo;
    }
    public static class SwitchWeaponSlot{
        public String username;
        public int slot;
        public boolean sucessful;
    }
    public static class PlayerDropWeapon{
        public String username;
        public boolean sucessful;
        public int x, y;
    }
    public static class DropWeapon{
        public int x, y;
        public int id;
        public int slot;
    }
    public static class DropArtefact{
        public int dx, dy;
        public int x, y;
        public int id;
        public int slot;
        public String username;
    }
    public static class ObjectInteract {
        public String username;
        public boolean sucessful;
        public int id;
        public int x, y;
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
        public int objectType; // type as TOP/LEFT/RIGHT torch, or TOP/SIDE arrowtrap
    }
    public static class MoveRoomObject{
        public int id;
        public float x, y;
    }
    public static class AddEnemyProjectile{
        public int id;
        public int idEnemy;
        public float x, y;
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
        // else idHit != -1=> hitted object is room object
        // else it is wall hit
    }

    public static class ShopDropitem {
        public int id;
        public int idObject;
        public int type;
        public int price;
        public int amount;
        public int weaponSlot;
    }
    public static class PlayerInfo {
        public String username;
        public int health, maxHealth;
        public int coins;
        public int armor, maxArmor;
    }
    public static class Alert {
        public String username;
        public String text;
        public int type;
    }
    public static class OpenChest {
        public int id;
    }
    public static class PlayerHit {
        public String username;
        public Player.DamageAbsorbedBy type;
    }
    public static class ArtefactActivate{
        public String username;
        public int slot;
    }
}

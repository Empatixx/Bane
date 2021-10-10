package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
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
        kryo.register(Disconnect.class);
        kryo.register(Ready.class);
        kryo.register(int[][].class);
        kryo.register(int[].class);
        kryo.register(TransferRoomMap.class);
        kryo.register(TransferRoom.class);
        kryo.register(MapLoaded.class);
        kryo.register(ChangeGamestate.class);
        kryo.register(RequestForPlayers.class);
        kryo.register(Shoot.class);
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
    public static class Shoot {
        public float x,y;
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
    public static class ObjectInteract {
        public String username;
        public boolean sucessful;
        public int id;
        public int x, y;
    }
}

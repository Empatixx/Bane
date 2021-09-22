package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

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
        kryo.register(AddBullet.class);
        kryo.register(AddEnemy.class);
        kryo.register(MoveEnemy.class);

    }

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
        public int x;
        public int y;
        public boolean up,down,left,right;
    }
    public static class MoveEnemy {
        public float x,y;
        public int id;
    }
    public static class Ready {
        public String username;
        public boolean state;
    }
    public static class TransferRoomMap {
        public int[][] roomMap;
        public int roomX,roomY;
    }
    public static class TransferRoom {
        public int id,x,y;
        public String mapFilepath;
        public int type;
        public int index; // index in roomArrayList
        public int previousIndex; // index of old mm room, that we will add new one
        public boolean top,bottom,left,right;
    }
    public static class MapLoaded {}
    public static class RequestForPlayers {
        public String exceptUsername; // except origin player
    }
    public static class ChangeGamestate{
        public int gamestate;
    }
    public static class AddBullet {
        public float x,y;
        public float px,py;
        public boolean critical;
        public float inaccuracy;
        public int speed, damage;
        public int indexWeapon;
    }
    public static class AddEnemy {
        public float x,y;
        public String type;
        public int id;
    }
}

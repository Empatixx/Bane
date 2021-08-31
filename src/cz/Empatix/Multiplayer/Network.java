package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

// This class is a convenient place to keep things common to both the client and server.
public class Network {

    static int port = 54555;
    // This registers objects that are going to be sent over the network.
    public static void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(pong.class);
        kryo.register(ping.class);
        kryo.register(Join.class);
        kryo.register(AddPlayer.class);
        kryo.register(MovePlayer.class);
        kryo.register(Disconnect.class);
        kryo.register(SucessfulJoin.class);
        kryo.register(Ready.class);
    }

    public static class ping {
    }
    public static class pong {
    }
    public static class SucessfulJoin{ }
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
    public static class Ready {
        public String username;
        public boolean state;
    }
}

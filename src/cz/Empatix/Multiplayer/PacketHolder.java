package cz.Empatix.Multiplayer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PacketHolder {
    private final ArrayList<PacketArray> packetArrays;

    public final static int DROPITEM = 0;
    public final static int DROPWEAPON = 1;
    public final static int REMOVEITEM = 2;
    public final static int OBJECTINTERACT = 3;
    public final static int MOVEENEMY = 4;
    public final static int ADDENEMY = 5;
    public final static int ADD_ENEMYPROJECTION = 6;
    public final static int HIT_ENEMYPROJECTILE = 7;
    public final static int ADDBULLET = 8;
    public final static int HITBULLET = 9;
    public final static int MOVE_ENEMYPROJECTILE = 10;
    public final static int MOVEPLAYER = 11;
    public final static int ORIGINMOVEPLAYER = 46;
    public final static int ADDROOMOBJECT = 12;
    public final static int MOVEITEM = 13;
    public final static int MOVEROOMOBJECT = 14;
    public final static int SHOPITEM = 15;
    public final static int PLAYERINFO = 16;
    public final static int ALERT = 17;
    public final static int OPENCHEST = 18;
    public final static int PLAYERHIT = 19;
    public final static int DROPARTEFACT = 20;
    public final static int ARTEFACTACTIVATED = 21;
    public final static int ARTEFACTADDBULLET = 22;
    public final static int LOCKROOM = 23;
    public final static int NEXTFLOOR = 24;
    public final static int TRANSFERROOM = 25;
    public final static int TRANSFERROOMMAP = 26;
    public final static int MAPLOADED = 27;
    public final static int REMOVEENEMY = 28;
    public final static int ALLPLAYERDEAD = 29;
    public final static int PLAYERSSTATS = 30;
    public final static int ROANIMSYNC = 31; // room object animation sync with server
    public final static int TRAPARROWHIT = 32;
    public final static int TRAPARROWMOVE = 33;
    public final static int TRAPARROWADD = 34;
    public static final int CANJOIN = 35;
    public static final int DISCONNECTPLAYER = 36;
    public static final int JOINPLAYER = 37;
    public static final int ENEMYSYNC = 38;
    public static final int LASERBEAMSYNC = 39;
    public static final int LASERBEAMHIT = 40;
    public static final int ENEMYHEAL = 41; // INCREASE IN HEALTH
    public static final int ARTEFACTSTATE = 42; // ARTEFACT EVENTS
    public static final int WEAPONINFO = 43;
    public static final int EXPLOSIONDAMAGE = 44;
    public static final int TRAPRODAMAGE = 45;
    public static final int ARTEFACTINFO = 47;


    public PacketHolder(){
        final int size = 48;

        packetArrays = new ArrayList<>(size);
        for(int i = 0;i<size;i++){
            PacketArray packetArray = new PacketArray();
            packetArrays.add(packetArray);
        }
    }
    public void add(Object o, int id){
        PacketArray packetArray = packetArrays.get(id);
        packetArray.lock();
        try {
            packetArray.packets.add(o);
        } finally {
            packetArray.unlock();
        }
    }

    public Object[] get(int id){
        PacketArray packetArray = packetArrays.get(id);
        packetArray.lock();
        try {
            Object[] packets =  packetArray.packets.toArray();
            packetArray.packets.clear();
            return packets;
        } finally {
            packetArray.unlock();
        }
    }

    public Object[] getWithoutClear(int id) {
        PacketArray packetArray = packetArrays.get(id);
        packetArray.lock();
        try {
            return packetArray.packets.toArray();
        } finally {
            packetArray.unlock();
        }
    }
    public void remove(int id, Object o) {
        PacketArray packetArray = packetArrays.get(id);
        packetArray.lock();
        try {
            packetArrays.get(id).packets.remove(o);
        } finally {
            packetArray.unlock();
        }
    }
    public void clear(int id) {
        PacketArray packetArray = packetArrays.get(id);
        packetArray.lock();
        try {
            packetArrays.get(id).packets.clear();
        } finally {
            packetArray.unlock();
        }
    }
    private static class PacketArray{
        private final LinkedList<Object> packets;
        private final Lock lock;
        PacketArray(){
            packets = new LinkedList<>();
            lock = new ReentrantLock();
        }

        public void lock(){
            lock.lock();
        }
        public void unlock(){
            lock.unlock();
        }
    }
}

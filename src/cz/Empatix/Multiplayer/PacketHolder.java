package cz.Empatix.Multiplayer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PacketHolder {
    private ArrayList<PacketArray> packetArrays;

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
    public final static int ADDROOMOBJECT = 12;
    public final static int MOVEITEM = 13;
    public final static int MOVEROOMOBJECT = 14;
    public final static int SHOPITEM = 15;
    public final static int PLAYERINFO = 16;
    public final static int ALERT = 17;
    public final static int OPENCHEST = 18;
    public final static int PLAYERHIT = 19;
    public final static int DROPARTEFACT = 20;

    public PacketHolder(){
        final int size = 21;

        packetArrays = new ArrayList<>(size);
        for(int i = 0;i<size;i++){
            PacketArray packetArray = new PacketArray(i);
            packetArrays.add(packetArray);
        }
    }
    public void add(Object o, int id){
        for (PacketArray packetArray : packetArrays) {
            if (id == packetArray.id) {
                packetArray.lock();
                try {
                    packetArray.packets.add(o);
                } finally {
                    packetArray.unlock();
                }
                break;
            }
        }
    }

    public Object[] get(int id){
        for (PacketArray packetArray : packetArrays) {
            if (id == packetArray.id) {
                packetArray.lock();
                try {
                    Object[] packets =  packetArray.packets.toArray();
                    packetArray.packets.toArray();
                    packetArray.packets.clear();
                    return packets;
                } finally {
                    packetArray.unlock();
                }
            }
        }
        return null;
    }

    public Object[] getWithoutClear(int id) {
        for (PacketArray packetArray : packetArrays) {
            if (id == packetArray.id) {
                packetArray.lock();
                try {
                    Object[] packets =  packetArray.packets.toArray();
                    packetArray.packets.toArray();
                    return packets;
                } finally {
                    packetArray.unlock();
                }
            }
        }
        return null;
    }
    public void remove(int id, Object o) {
        for (PacketArray packetArray : packetArrays) {
            if (id == packetArray.id) {
                packetArray.lock();
                try {
                    packetArrays.get(id).packets.remove(o);
                } finally {
                    packetArray.unlock();
                }
            }
        }
    }
    private static class PacketArray{
        private int id;
        private LinkedList<Object> packets;
        private Lock lock;
        PacketArray(int id){
            packets = new LinkedList<>();
            lock = new ReentrantLock();
            this.id = id;
        }

        public void lock(){
            lock.lock();
        }
        public void unlock(){
            lock.unlock();
        }
    }
}

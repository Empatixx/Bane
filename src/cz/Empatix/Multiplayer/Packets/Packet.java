package cz.Empatix.Multiplayer.Packets;

import cz.Empatix.Multiplayer.GameClient;
import cz.Empatix.Multiplayer.GameServer;

public abstract class Packet {
    public enum PacketType{
        INVALID(-1), LOGIN(00), DISCONNECT(01), MOVE(02),
        ENTERREADY(03), ENTERGAME(04);

        private int packetId;
        PacketType(int packetId){
            this.packetId = packetId;
        }
        public int getId(){
            return packetId;
        }
    }
    public byte packetId;
    public Packet(int packetId){
        this.packetId = (byte)packetId;
    }

    // packet that client sends to server
    public abstract void writeData(GameClient client);

    // packet that server sends to clients
    public abstract void writeData(GameServer server);

    // data of packet
    public abstract byte[] getData();

    // first two chars are id of packet, rest of chars are data
    public String readData(byte[] data){
        String message = new String(data).trim();
        return message.substring(2);
    }
    // get type of packet - id
    public static PacketType lookupPacket(int id){
        for(PacketType p : PacketType.values()){
            if(p.getId() == id){
                return p;
            }
        }
        return PacketType.INVALID;
    }
    public static PacketType lookupPacket(String packetId){
        try{
            return lookupPacket(Integer.parseInt(packetId));
        } catch (Exception e){
            return PacketType.INVALID;
        }
    }
}

package cz.Empatix.Multiplayer;

public abstract class Packet {
    public static enum PacketType{
        INVALID(-1), LOGIN(00), DISCONNECT(01);

        private int packetId;
        private PacketType(int packetId){
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

    public abstract void writeData(GameClient client);
    public abstract byte[] getData();
    public abstract void writeData(GameServer server);

    public String readData(byte[] data){
        String message = new String(data).trim();
        return message.substring(2);
    }
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

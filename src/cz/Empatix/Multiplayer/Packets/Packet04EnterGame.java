package cz.Empatix.Multiplayer.Packets;

import cz.Empatix.Multiplayer.GameClient;
import cz.Empatix.Multiplayer.GameServer;

public class Packet04EnterGame extends Packet {

    private int[][] roomMap;
    private int roomX,roomY;

    public Packet04EnterGame(byte[] data){
        super(04);
    }

    public Packet04EnterGame(){
        super(04);

    }

    @Override
    public void writeData(GameClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("04" + "").getBytes();
    }

    @Override
    public void writeData(GameServer server) {
        server.sendDataToAllClients(getData());
    }
}

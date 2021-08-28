package cz.Empatix.Multiplayer.Packets;

import cz.Empatix.Multiplayer.GameClient;
import cz.Empatix.Multiplayer.GameServer;

public class Packet03ReadyStart extends Packet {

    private String username;
    private int state;

    public Packet03ReadyStart(byte[] data){
        super(03);
        String[] dataArray = readData(data).split(",");
        this.username = dataArray[0];
        this.state = Integer.parseInt(dataArray[1]);
    }

    public Packet03ReadyStart(String username, int state){
        super(03);
        this.username = username;
        this.state = state;
    }

    @Override
    public void writeData(GameClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("03" + this.username + "," + state).getBytes();
    }

    @Override
    public void writeData(GameServer server) {
        server.sendDataToAllClients(getData());
    }
    public String getUsername(){return username;}

    public int getState() {
        return state;
    }
}

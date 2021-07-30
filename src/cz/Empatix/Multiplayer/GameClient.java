package cz.Empatix.Multiplayer;

import cz.Empatix.Gamestates.GameState;

import java.io.IOException;
import java.net.*;

public class GameClient extends Thread {
    private InetAddress ipAdress;
    private DatagramSocket socket;
    private GameState game;

    public GameClient(GameState game, String ipAdress){
        this.game = game;
        try {
            this.socket = new DatagramSocket();
            this.ipAdress = InetAddress.getByName(ipAdress);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data,data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("SERVER > "+new String(packet.getData()));
        }
    }
    public void sendData(byte[] data){
        DatagramPacket packet = new DatagramPacket(data,data.length,ipAdress,7777);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

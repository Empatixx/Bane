package cz.Empatix.Multiplayer;

import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.ProgressRoom;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class GameServer extends Thread {
    private DatagramSocket socket;
    private GameState game;

    private int numPlayers;

    private List<PlayerMP> connectedPlayers;

    public GameServer(GameState game){
        this.game = game;
        try {
            this.socket = new DatagramSocket(7777);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        numPlayers = 0;
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
            parseSocket(packet.getData(),packet.getAddress(),packet.getPort());

            /*
            String message = new String(packet.getData());
            System.out.println("Client["+packet.getAddress().getHostAddress()+":"+packet.getPort()+"] > "+message);
            if(message.trim().equalsIgnoreCase("ping")){
                sendData("pong".getBytes(),packet.getAddress(),packet.getPort());
            }
            */
        }
    }

    private void parseSocket(byte[] data, InetAddress adress, int port) {
        String message = new String(data).trim();
        Packet.PacketType type = Packet.lookupPacket(message.substring(0,2));
        switch (type) {
            case INVALID: { break; }
            case DISCONNECT: { break; }
            case LOGIN : {
                Packet00Login packet = new Packet00Login(data);
                System.out.println("[" + adress.getHostAddress() + ":" + port + "]" + packet.getUsername());

                PlayerMP playerMP = new PlayerMP(((ProgressRoom) game).tileMap, adress, port);
                this.connectedPlayers.add(playerMP);

                ((ProgressRoom) game).player[numPlayers] = playerMP;
                numPlayers++;

                break;
            }
        }
    }

    public void sendData(byte[] data, InetAddress ipAdress,int port){
        DatagramPacket packet = new DatagramPacket(data,data.length,ipAdress,port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDataToAllClients(byte[] data) {
        for(PlayerMP p : connectedPlayers){
            sendData(data,p.getIpAdress(),p.getPort());
        }
    }
}

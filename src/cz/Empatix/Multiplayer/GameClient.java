package cz.Empatix.Multiplayer;

import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.ProgressRoomMP;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.Packets.*;

import java.io.IOException;
import java.net.*;

public class GameClient extends Thread {
    private InetAddress ipAdress;
    private DatagramSocket socket;

    private GameStateManager gsm;

    private int numPlayers;


    public GameClient(GameStateManager gsm, String ipAdress){
        this.gsm = gsm;
        try {
            this.socket = new DatagramSocket();
            this.ipAdress = InetAddress.getByName(ipAdress);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        numPlayers = 1;
    }

    @Override
    public void run() {
        while(Game.running){
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data,data.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            parseSocket(packet.getData(),packet.getAddress(),packet.getPort());

        }
        socket.close();
    }

    public int getTotalPlayers(){return numPlayers;}


    private void parseSocket(byte[] data, InetAddress adress, int port) {
        String message = new String(data).trim();
        Packet.PacketType type = Packet.lookupPacket(message.substring(0,2));
        Packet packet;
        switch (type) {
            case INVALID: { break; }
            case DISCONNECT: {
                packet = new Packet01Disconnect(data);
                System.out.println("[" + adress.getHostAddress() + ":" + port + "]" + ((Packet01Disconnect) (packet)).getUsername() + " has left..");

                GameState gameState = gsm.getCurrentGamestate();
                if(gameState instanceof ProgressRoomMP){
                    ((PlayerMP)((ProgressRoomMP) gameState).player[numPlayers]).remove();
                    ((ProgressRoomMP) gameState).player[numPlayers] = null;
                }
                numPlayers--;
                break;
            }
            case LOGIN : {
                packet = new Packet00Login(data);
                System.out.println("[" + adress.getHostAddress() + ":" + port + "]" + ((Packet00Login)(packet)).getUsername()+" has joined the game..");

                GameState gameState = gsm.getCurrentGamestate();
                String username = ((Packet00Login)(packet)).getUsername();
                if(gameState instanceof ProgressRoomMP) {
                    PlayerMP playerMP = new PlayerMP(((ProgressRoomMP) gameState).tileMap, adress, port,username);
                    ((ProgressRoomMP) gameState).player[numPlayers] = playerMP;
                    ((ProgressRoomMP) gameState).playerReadies[numPlayers] = new ProgressRoomMP.PlayerReady(username);
                    numPlayers++;

                }
                break;
            }
            case MOVE:{
                packet = new Packet02Move(data);
                handleMove((Packet02Move)packet);
                break;
            }
            case READYSTART:{
                packet = new Packet03ReadyStart(data);

                int state = ((Packet03ReadyStart) packet).getState();

                GameState gameState = gsm.getCurrentGamestate();
                String username = ((Packet03ReadyStart)(packet)).getUsername();
                if(gameState instanceof ProgressRoomMP) {
                    for(ProgressRoomMP.PlayerReady playerReady : ((ProgressRoomMP) gameState).playerReadies){
                        if(playerReady.getUsername().equalsIgnoreCase(username)){
                            playerReady.setReady(state == 1);
                        }
                    }
                }
                break;
            }
        }
    }

    public void sendData(byte[] data){
        DatagramPacket packet = new DatagramPacket(data,data.length,ipAdress,23333);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMove(Packet02Move packet) {
        GameState gameState = gsm.getCurrentGamestate();
        if(gameState instanceof ProgressRoomMP) {
            Player[] players = ((ProgressRoomMP) gameState).player;
            for (Player p : players) {
                if (p != null) {
                    if (p instanceof PlayerMP) {
                        if (((PlayerMP) p).getUsername().equalsIgnoreCase(packet.getUsername())) {
                            p.setPosition(packet.getX(), packet.getY());
                            p.setUp(packet.isMovingUp());
                            p.setDown(packet.isMovingDown());
                            p.setRight(packet.isMovingRight());
                            p.setLeft(packet.isMovingLeft());
                        }
                    }
                }
            }
        }
    }
}

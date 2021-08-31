package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Multiplayer.ProgressRoomMP;
import cz.Empatix.Render.Alerts.AlertManager;

import java.io.IOException;

public class GameClient{
    private GameStateManager gsm;
    private Client client;

    private String hostIpAdress;

    private int numPlayers;
    private boolean loggedIn;

    public GameClient(GameStateManager gsm, String ipAdress){
        this.gsm = gsm;
        loggedIn = false;
        numPlayers = 1;

        client = new Client();
        client.start();

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof Network.AddPlayer) {
                    System.out.println("ADDING");

                    Network.AddPlayer player = (Network.AddPlayer) object;

                    GameState gameState = gsm.getCurrentGamestate();
                    String packetUsername = player.username;

                    if (gameState instanceof ProgressRoomMP) {
                        PlayerMP playerMP = new PlayerMP(((ProgressRoomMP) gameState).tileMap, packetUsername);
                        ((ProgressRoomMP) gameState).player[numPlayers] = playerMP;
                        ((ProgressRoomMP) gameState).playerReadies[numPlayers] = new ProgressRoomMP.PlayerReady(packetUsername);
                        numPlayers++;
                    }
                }
                if (object instanceof Network.Disconnect) {
                    Network.Disconnect packet = (Network.Disconnect) object;

                    GameState gameState = gsm.getCurrentGamestate();
                    numPlayers--;
                    if(gameState instanceof ProgressRoomMP){
                        ((PlayerMP)((ProgressRoomMP) gameState).player[numPlayers]).remove();
                        ((ProgressRoomMP) gameState).player[numPlayers] = null;
                    }
                    String packetUsername = packet.username;
                    String playerUsername = MultiplayerManager.getInstance().getUsername();
                    if(!packetUsername.equalsIgnoreCase(playerUsername)) AlertManager.add(AlertManager.WARNING,packetUsername+" has left the lobby!");
                }
                if (object instanceof Network.MovePlayer){
                    handleMove((Network.MovePlayer) object);
                }
                if (object instanceof Network.SucessfulJoin){
                    loggedIn = true;
                }
                if (object instanceof Network.Ready){
                    boolean state = ((Network.Ready) object).state;

                    GameState gameState = gsm.getCurrentGamestate();
                    String packetUsername = ((Network.Ready) object).username;
                    if(gameState instanceof ProgressRoomMP) {
                        for(ProgressRoomMP.PlayerReady playerReady : ((ProgressRoomMP) gameState).playerReadies){
                            if(playerReady.getUsername().equalsIgnoreCase(packetUsername)){
                                playerReady.setReady(state);
                            }
                        }
                    }
                    if(state){
                        String playerUsername = MultiplayerManager.getInstance().getUsername();
                        if(!packetUsername.equalsIgnoreCase(playerUsername)) AlertManager.add(AlertManager.INFORMATION,packetUsername+" is ready!");
                    }
                }
                if (object instanceof Network.pong){
                    System.out.println("PONG");
                }
            }
        });

        Network.register(client);

        try {
            client.connect(5000, "127.0.0.1", 54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Client getClient() {
        return client;
    }

    public int getTotalPlayers() {
        return numPlayers;
    }
    public void handleMove(Network.MovePlayer movePlayerPacket){
        if(!loggedIn) return;
        GameState gameState = gsm.getCurrentGamestate();
        if(gameState instanceof ProgressRoomMP) {
            Player[] players = ((ProgressRoomMP) gameState).player;
            for (Player p : players) {
                if (p != null) {
                    if (p instanceof PlayerMP) {
                        if (((PlayerMP) p).getUsername().equalsIgnoreCase(movePlayerPacket.username)) {
                            p.setPosition(movePlayerPacket.x, movePlayerPacket.y);
                            p.setUp(movePlayerPacket.up);
                            p.setDown(movePlayerPacket.down);
                            p.setRight(movePlayerPacket.right);
                            p.setLeft(movePlayerPacket.left);
                        }
                    }
                }
            }
        }
    }
    public void close(){
        client.close();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}

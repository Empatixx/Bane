package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Gamestates.GameStateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private final GameStateManager gsm;
    private final Server server;

    private final List<PlayerMP> connectedPlayers;

    public GameServer(GameStateManager gsm) {
        this.gsm = gsm;
        connectedPlayers = new ArrayList<>();

        server = new Server();
        server.start();
        try {
            server.bind(54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof Network.Join) {
                    Network.Join joinPacket = (Network.Join) object;

                    String packetUsername = joinPacket.username;

                    // player is already connected
                    for (PlayerMP player : connectedPlayers) {
                        if (player.getUsername().equalsIgnoreCase(packetUsername)) {
                            close();
                            return;
                        }
                    }

                    handleJoin(joinPacket, connection);
                }
                else if (object instanceof Network.Disconnect) {
                    Network.Disconnect disconnectPacket = (Network.Disconnect) object;
                    handleDisconnect(disconnectPacket);
                }
                else if (object instanceof Network.RequestForPlayers) {
                    Network.RequestForPlayers request = (Network.RequestForPlayers) object;
                    for(PlayerMP player : connectedPlayers){
                        if(!player.getUsername().equalsIgnoreCase(request.exceptUsername)){
                            Network.AddPlayer addPlayer = new Network.AddPlayer();
                            addPlayer.username = player.getUsername();
                            connection.sendTCP(addPlayer);
                        }
                    }
                }
                else if (object instanceof Network.MovePlayer) {
                    Network.MovePlayer movePacket = (Network.MovePlayer) object;
                    handleMovement(movePacket, connection);
                }
                else if (object instanceof Network.MoveEnemy) {
                    Network.MoveEnemy movePacket = (Network.MoveEnemy) object;
                    handleMovement(movePacket, connection);
                    System.out.println("CHANGIN3");

                }
                else if (object instanceof Network.Ready){
                    server.sendToAllTCP(object);
                }
                else if (    object instanceof Network.AddPlayer ||
                        object instanceof Network.TransferRoomMap ||
                        object instanceof Network.TransferRoom ||
                        object instanceof Network.MapLoaded ||
                        object instanceof Network.ChangeGamestate ||
                        object instanceof Network.AddBullet ||
                        object instanceof Network.AddEnemy
                ){
                    server.sendToAllExceptTCP(connection.getID(),object);
                }
            }
        });

        Network.register(server);

    }

    private void handleDisconnect(Network.Disconnect disconnectPacket) {
        String packetUsername = disconnectPacket.username;

        // player is already connceted
        for(int i = 0;i<connectedPlayers.size();i++){
            PlayerMP player = connectedPlayers.get(i);
            if (player.getUsername().equalsIgnoreCase(packetUsername)) {
                connectedPlayers.remove(i);
                i--;
            }
        }
        server.sendToAllTCP(disconnectPacket);

    }


    private void handleMovement(Network.MovePlayer movePacket, Connection connection) {
        server.sendToAllExceptUDP(connection.getID(),movePacket);
    }
    private void handleMovement(Network.MoveEnemy movePacket, Connection connection) {
        server.sendToAllExceptUDP(connection.getID(),movePacket);
    }
    private void handleJoin(Network.Join join, Connection connection) {
        PlayerMP playerMP = new PlayerMP(null, join.username);
        playerMP.remove();

        if(join.host){
            connectedPlayers.add(playerMP);
        } else {
            for (PlayerMP otherPlayer : connectedPlayers) {
                Network.AddPlayer addPlayer = new Network.AddPlayer();
                addPlayer.username = otherPlayer.getUsername();
                // send other players to new player
                connection.sendTCP(addPlayer);
            }

            connectedPlayers.add(playerMP);

            Network.AddPlayer addPlayer = new Network.AddPlayer();
            addPlayer.username = join.username;
            // send new player to others players
            server.sendToAllExceptTCP(connection.getID(),addPlayer);
        }
    }
    public Server getServer() {
        return server;
    }
    public void close(){
        server.close();
    }

}

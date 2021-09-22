package cz.Empatix.Gamestates.Multiplayer;

import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Multiplayer.GameClient;
import cz.Empatix.Multiplayer.GameServer;
import cz.Empatix.Multiplayer.Network;

public class MultiplayerManager {

    private static MultiplayerManager multiplayerManager;

    public GameClient client;
    public GameServer server;

    private boolean host;
    private String username;

    public static boolean multiplayer = false;

    public static MultiplayerManager getInstance(){ return multiplayerManager;}
    public MultiplayerManager(boolean host, GameStateManager gsm) {
        multiplayerManager = this;
        multiplayer = true;
        this.host = host;
        if(host) {
            server = new GameServer(gsm);
        }

        client = new GameClient(gsm,"localhost");

    }

    public String getUsername() {
        return username;
    }

    public boolean isHost() {
        return host;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void close(){
        if(!multiplayer) return;

        Network.Disconnect disconnect = new Network.Disconnect();
        disconnect.username = getUsername();
        client.getClient().sendTCP(disconnect);

        multiplayer = false;

        if(isHost()) {
            server.close();
            server = null;
        }
        client.close();
        client = null;
    }
}

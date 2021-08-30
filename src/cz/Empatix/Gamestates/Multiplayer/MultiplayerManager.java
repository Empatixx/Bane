package cz.Empatix.Gamestates.Multiplayer;

import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Multiplayer.GameClient;
import cz.Empatix.Multiplayer.GameServer;

public class MultiplayerManager {

    private static MultiplayerManager multiplayerManager;

    public GameClient socketClient;
    public GameServer socketServer;

    private boolean host;
    private String username;

    public static boolean multiplayer = false;

    public static MultiplayerManager getInstance(){ return multiplayerManager;}
    public MultiplayerManager(boolean host, GameStateManager gsm){
        multiplayerManager = this;
        multiplayer = true;
        this.host = host;
        if(host) {
            socketServer = new GameServer(gsm);
            socketServer.start();
        }

        socketClient = new GameClient(gsm,"localhost");
        socketClient.start();

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
        multiplayer = false;
        if(isHost()) socketServer = null;
        socketClient = null;
    }
}

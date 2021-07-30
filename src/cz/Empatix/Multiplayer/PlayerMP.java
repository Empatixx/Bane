package cz.Empatix.Multiplayer;

import cz.Empatix.Entity.Player;
import cz.Empatix.Render.TileMap;

import java.net.InetAddress;

public class PlayerMP extends Player {
    private InetAddress ipAdress;
    private int port;
    public PlayerMP(TileMap tm, InetAddress ip, int port){
        super(tm);
        this.ipAdress = ip;
        this.port = port;
    }

    @Override
    public void update() {
        super.update();
    }

    public InetAddress getIpAdress() {
        return ipAdress;
    }

    public int getPort() {
        return port;
    }
}

package cz.Empatix.Multiplayer;

import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Multiplayer.Packets.Packet02Move;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.net.InetAddress;

public class PlayerMP extends Player {
    public InetAddress ipAdress;
    public int port;
    private String username;

    private boolean origin;
    private MultiplayerManager mpManager;

    private TextRender textRender;

    public PlayerMP(TileMap tm, InetAddress ip, int port, String username){
        super(tm);
        this.ipAdress = ip;
        this.port = port;
        this.username = username;

        mpManager = MultiplayerManager.getInstance();

    }

    public void setOrigin(boolean origin) {
        this.origin = origin;
    }

    public boolean isOrigin() {
        return origin;
    }

    @Override
    public void draw() {
        super.draw();
        if(textRender == null) textRender = new TextRender();
        float centerX = TextRender.getHorizontalCenter((int)(position.x+xmap),(int)(position.x+xmap),username,2);
        textRender.draw(username,new Vector3f(centerX,position.y+ymap-cheight/2-10,0),2,new Vector3f(0.874f,0.443f,0.149f));
    }

    @Override
    public void update() {
        super.update();
        if(isOrigin()){
            Packet02Move packet = new Packet02Move(username,(int)position.x,(int)position.y);
            packet.setMovementDirections(up,down,right,left);
            packet.writeData(mpManager.socketClient);
        }
    }
    public InetAddress getIpAdress() {
        return ipAdress;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public void remove(){
        light.remove();
    }
}

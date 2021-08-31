package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

public class PlayerMP extends Player {
    private String username;

    private boolean origin;
    private MultiplayerManager mpManager;

    private TextRender textRender;

    public PlayerMP(TileMap tm, String username){
        super(tm);
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
            Client client = mpManager.client.getClient();
            Network.MovePlayer movePlayer = new Network.MovePlayer();
            movePlayer.username = username;
            movePlayer.down = down;
            movePlayer.up = up;
            movePlayer.left = left;
            movePlayer.right = right;
            movePlayer.x = (int)position.x;
            movePlayer.y = (int)position.y;
            client.sendUDP(movePlayer);
        }
    }

    public String getUsername() {
        return username;
    }

    public void remove(){
        light.remove();
    }
}

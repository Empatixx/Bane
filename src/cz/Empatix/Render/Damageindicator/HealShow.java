package cz.Empatix.Render.Damageindicator;

import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class HealShow {
    private final long time;
    private final int amount;
    private final Vector3f pos;
    private final Vector2f dir;

    private final TextRender textRender;
    HealShow(int amount, int x, int y, Vector2f dir){
        this.amount = amount;
        pos = new Vector3f(x,y,0);
        time = System.currentTimeMillis() - InGame.deltaPauseTime();
        this.dir = dir;

        textRender = new TextRender();
    }

    boolean shouldRemove(){
        return System.currentTimeMillis() - time - InGame.deltaPauseTime() > 500;
    }
    public void update(){
        pos.x+=dir.x * Game.deltaTime;
        pos.y+=dir.y * Game.deltaTime;
    }
    void draw(){
        textRender.drawMap("+"+amount,pos,2,new Vector3f(0.050f, 0.729f, 0.290f));

    }

}

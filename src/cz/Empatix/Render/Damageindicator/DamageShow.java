package cz.Empatix.Render.Damageindicator;

import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class DamageShow {
    private long time;
    private int dmg;
    private boolean crit;
    private Vector3f pos;
    private Vector2f dir;
    DamageShow(int dmg, int x, int y, Vector2f dir){
        this.dmg = dmg;
        pos = new Vector3f(x,y,0);
        time = System.currentTimeMillis() - InGame.deltaPauseTime();
        this.dir = dir;
    }

    void setCrit(boolean crit) {
        this.crit = crit;
    }

    int getDmg() {
        return dmg;
    }
    boolean shouldRemove(){
        return System.currentTimeMillis() - time - InGame.deltaPauseTime() > 500;
    }
    public void update(){
        pos.x+=dir.x;
        pos.y+=dir.y;
    }
    void draw(){
        TextRender.renderMapText(Integer.toString(dmg),pos,2,new Vector3f(0.937f, 0.223f, 0.223f));
    }
}

package cz.Empatix.Render.Damageindicator;

import org.joml.Vector2f;

import java.util.ArrayList;

public class DamageIndicator {
    private static ArrayList<DamageShow> damageShows;
    public DamageIndicator(){
        damageShows = new ArrayList<>();
    }
    public static void addDamageShow(int dmg, int x, int y, Vector2f dir){
        DamageShow damageShow = new DamageShow(dmg, x, y,dir);
        damageShows.add(damageShow);

    }
    public void draw(){

        for(DamageShow show : damageShows){
            show.draw();

        }
    }
    public void update(){
        for(int i = 0;i<damageShows.size();i++){
            DamageShow show = damageShows.get(i);
            if(show.shouldRemove()){
                damageShows.remove(i);
                i--;
                continue;
            }
            show.update();
        }
    }
}

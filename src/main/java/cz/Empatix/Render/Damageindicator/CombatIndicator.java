package cz.Empatix.Render.Damageindicator;

import org.joml.Vector2f;

import java.util.ArrayList;

public class CombatIndicator {
    private static ArrayList<DamageShow> damageShows;
    private static ArrayList<HealShow> healShows;
    public CombatIndicator(){
        damageShows = new ArrayList<>();
        healShows = new ArrayList<>();
    }
    public static void addDamageShow(int dmg, int x, int y, Vector2f dir){
        DamageShow damageShow = new DamageShow(dmg, x, y,dir);
        damageShows.add(damageShow);
    }
    public static void addHealShow(int amount, int x, int y, Vector2f dir){
        HealShow healShow = new HealShow(amount,x,y,dir);
        healShows.add(healShow);

    }
    public static void addCriticalDamageShow(int dmg, int x, int y, Vector2f dir){
        DamageShow damageShow = new DamageShow(dmg, x, y,dir);
        damageShow.setCrit();
        damageShows.add(damageShow);

    }
    public void draw(){

        for(DamageShow show : damageShows){
            show.draw();
        }
        for(HealShow show : healShows){
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
        for(int i = 0;i<healShows.size();i++){
            HealShow show = healShows.get(i);
            if(show.shouldRemove()){
                healShows.remove(i);
                i--;
                continue;
            }
            show.update();
        }
    }
}

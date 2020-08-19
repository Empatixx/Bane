package cz.Empatix.Render.Hud.ProgressNPC;

import cz.Empatix.Entity.Player;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades.*;

import java.util.ArrayList;

public class UpgradeMenu {
    private Background background;
    private ArrayList<UpgradeBar> bars;

    public UpgradeMenu(){
        background = new Background("Textures\\ProgressRoom\\upgrademenu-guns.tga");
        background.setFadeEffect(false);
        background.setDimensions(900,750);

        bars = new ArrayList<>();
        UpgradeBar pistol = new PistolUpgrade(0);
        pistol.setClicked(true);
        bars.add(pistol);
        bars.add(new LugerUpgrade(1));
        bars.add(new ShotgunUpgrade(2));
        bars.add(new SubmachineUpgrade(3));
        bars.add(new M4Upgrade(4));
        bars.add(new GrenadelauncherUpgrade(5));

    }
    public void draw(){
        background.draw();
        for (UpgradeBar bar:bars
        ) {
            bar.draw();
        }
    }
    public void update(float x, float y){
        for (UpgradeBar bar:bars
        ) {
            bar.mouseHover(x,y);
        }
    }

    public void mousePressed(float x, float y, Player p){
        for (UpgradeBar bar:bars
             ) {
            if(bar.isClicked()) bar.mouseClick(x,y, p);

            if(bar.intersects(x,y)){
                bar.setClicked(true);
                for (UpgradeBar anotherbar:bars
                     ) {
                    if(bar != anotherbar) anotherbar.setClicked(false);
                }
            }
        }
    }
}

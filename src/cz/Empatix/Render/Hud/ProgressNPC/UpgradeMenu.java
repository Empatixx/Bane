package cz.Empatix.Render.Hud.ProgressNPC;

import cz.Empatix.Entity.Player;
import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades.*;
import cz.Empatix.Render.Hud.SliderBar;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class UpgradeMenu {
    public static void load(){
        Loader.loadImage("Textures\\ProgressRoom\\upgrademenu-guns.tga");
    }
    private Background background;
    private ArrayList<UpgradeBar> bars;

    private SliderBar weaponSlider;

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
        bars.add(new RevolverUpgrade(6));
        bars.add(new ThompsonUpgrade(7));

        weaponSlider = new SliderBar(new Vector3f(842f,540,0),3f);
        weaponSlider.setLength(730);
        weaponSlider.setVertical();
        weaponSlider.setValue(0f);
    }
    public void draw(){
        background.draw();

        for (UpgradeBar bar:bars
        ) {
            glEnable(GL_SCISSOR_TEST);

            glScissor(523,177,312,726);
            bar.draw();


            glDisable(GL_SCISSOR_TEST);
            if(bar.isClicked()){
                bar.drawUpgrades();
            }
        }
        weaponSlider.draw();
    }
    public void update(float x, float y){
        float sliderY = weaponSlider.getValue() * -((bars.size()-6) * 120);
        for (UpgradeBar bar:bars
        ) {
            bar.mouseHover(x,y);
            bar.update((int)sliderY);
        }
        if(weaponSlider.isLocked()){
            weaponSlider.update(x,y);
        }
    }

    public void mousePressed(float x, float y, Player p){
        float sliderY = weaponSlider.getValue() * -((bars.size()-6) * 120);
        // if mouse is in weapon menu section(not outside)
            for (UpgradeBar bar : bars
            ) {
                if (bar.isClicked()) bar.mouseClick(x, y, p);

                if(x >= 526 && x <= 833 && y <= 900 && y >= 178){
                    if (bar.intersects(x, y - sliderY)){
                        bar.setClicked(true);
                        for (UpgradeBar anotherbar : bars
                        ) {
                            if (bar != anotherbar) anotherbar.setClicked(false);
                        }
                    }
                }
            }
        if(weaponSlider.intersects(x,y)){
            weaponSlider.setLocked(true);
        }
    }
    public void mouseReleased(float x, float y){
        unlockSlider();
    }
    public void unlockSlider(){
        weaponSlider.setLocked(false);
    }
}

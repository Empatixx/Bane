package cz.Empatix.Render.Hud.ProgressNPC;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.Entity.Player;
import cz.Empatix.Multiplayer.MultiplayerManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades.*;
import cz.Empatix.Render.Hud.SliderBar;
import cz.Empatix.Utility.Loader;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class UpgradeMenu {
    private Background background;
    private ArrayList<UpgradeBar> bars;

    private SliderBar weaponSlider;

    private float scrollY;

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
        bars.add(new UziUpgrade(3));
        bars.add(new M4Upgrade(4));
        bars.add(new GrenadelauncherUpgrade(5));
        bars.add(new RevolverUpgrade(6));
        bars.add(new ThompsonUpgrade(7));
        bars.add(new ModernShotgunUpgrade(8));
        sendAllNumUpgradesPacket(); // sends packet if player is in mp

        weaponSlider = new SliderBar(new Vector3f(842f,540,0),3f);
        weaponSlider.setLength(730);
        weaponSlider.setVertical();
        weaponSlider.setValue(0f);

        scrollY = 0;
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
        for (UpgradeBar bar:bars
        ) {
            if(bar.isClicked()){
                bar.drawStats();
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
            scrollY = weaponSlider.getValue();
        }
        float value = weaponSlider.getValue();
        value += (scrollY - value) * Game.deltaTime * 10;
        if(value > 1) value = 1;
        else if (value < 0) value = 0;
        weaponSlider.setValue(value);
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
                        break;
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

    public void mouseScroll(double x, double y) {
        float value = weaponSlider.getValue();
        scrollY = value-(float)y/10;
    }

    public int getCountAvailableUpgrades(Player p){
        int c = 0;
        for(int i = 0;i<bars.size();i++){
            c+=bars.get(i).getAvailableUpgrades(p);
        }
        return c;
    }
    public int[] getAllNumUpgrades(){
        int[] numUpgrades = new int[bars.size()];
        for(int i = 0;i<bars.size();i++){
            numUpgrades[i] = bars.get(i).getNumUpgrades();
        }
        return numUpgrades;
    }
    public void sendAllNumUpgradesPacket(){
        if(MultiplayerManager.multiplayer){
            Client c = MultiplayerManager.getInstance().client.getClient();
            Network.NumUpgrades numUpgradesP = new Network.NumUpgrades();
            numUpgradesP.idPlayer = MultiplayerManager.getInstance().getIdConnection();
            numUpgradesP.numUpgrades = getAllNumUpgrades();
            c.sendTCP(numUpgradesP);
        }
    }
}

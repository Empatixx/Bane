package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;

import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

public class M4Upgrade extends UpgradeBar {
    public M4Upgrade(int row){
        super("Textures\\M4.tga",2,row);

        info = new WeaponInfo();
        info.maxAmmo = 250;
        info.maxMagazineAmmo = 32;
        info.maxDamage = 2;
        info.minDamage = 2;
        info.firerate = 1f/0.750f;
        info.name = "M4";

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("m4","upgrades");
        this.numUpgrades = numUpgrades;


        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"m4");
        String[] text = new String[]{"Increase magazine capacity by 4"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(20);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.maxMagazineAmmo+=4;
        }

        bar = new UpgradeSideBar(sideBars.size(),"m4");
        text = new String[]{"Speed of bullets will be increased by 20%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.ACCURACYUPGRADE);
        bar.setPrice(50);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"m4");
        text = new String[]{"Increase maximum damage by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(90);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.maxDamage++;
        }

        bar = new UpgradeSideBar(sideBars.size(),"m4");
        text = new String[]{"Increase damage by 2"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(170);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.maxDamage+=2;
            info.minDamage+=2;
        }

    }
    @Override
    public void drawStats() {
        super.drawStats();
        textRender[5].draw(info.name,new Vector3f(TextRender.getHorizontalCenter(202,496,info.name,2),325,0),2, new Vector3f(0.78f,0.737f,0.027f));

        textRender[0].draw("Min. damage: " +info.minDamage,new Vector3f(295,400,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[1].draw("Max. damage: " +info.maxDamage,new Vector3f(295,430,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[2].draw("Fire rate: 4x" +String.format("%.2f",info.firerate),new Vector3f(295,460,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[3].draw("Max ammo: " +info.maxMagazineAmmo+"/"+info.maxAmmo,new Vector3f(295,490,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[4].draw("Critical hits: " +info.areCritical_hits_enabled(),new Vector3f(295,520,0),1, new Vector3f(0.686f,0.4f,0.258f));

    }

    @Override
    public void updateStats() {
        int numUpgrades = GameStateManager.getDb().getValueUpgrade("m4","upgrades");
        if(numUpgrades == 1){
            info.maxMagazineAmmo+=4;
        }
        if(numUpgrades == 3){
            info.maxDamage++;
        }
        if(numUpgrades == 4){
            info.maxDamage+=2;
            info.minDamage+=2;
        }
        this.numUpgrades = numUpgrades;

    }
}

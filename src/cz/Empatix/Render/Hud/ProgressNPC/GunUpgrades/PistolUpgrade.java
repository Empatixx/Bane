package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;

import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

public class PistolUpgrade extends UpgradeBar {
    public PistolUpgrade(int row){
        super("Textures\\pistol.tga",2,row);

        info = new WeaponInfo();
        info.maxAmmo = 120;
        info.maxMagazineAmmo = 7;
        info.maxDamage = 3;
        info.minDamage = 1;
        info.firerate = 1f/0.250f;
        info.name = "Pistol";

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("pistol","upgrades");


        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"pistol");
        String[] text = new String[]{"Increase magazine capacity by 2"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(20);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"pistol");
        text = new String[]{"Your bullets can cause critical hits"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(60);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"pistol");
        text = new String[]{"Increase minimum damage by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(80);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"pistol");
        text = new String[]{"Shoot 2 bullets intead of 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.OTHERUPGRADE);
        bar.setPrice(160);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }
    }
    @Override
    public void drawStats() {
        super.drawStats();
        textRender[5].draw(info.name,new Vector3f(TextRender.getHorizontalCenter(202,496,info.name,2),325,0),2, new Vector3f(0.78f,0.737f,0.027f));

        textRender[0].draw("Min. damage: " +info.minDamage,new Vector3f(295,400,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[1].draw("Max. damage: " +info.maxDamage,new Vector3f(295,430,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[2].draw("Fire rate: " +String.format("%.2f",info.firerate),new Vector3f(295,460,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[3].draw("Max ammo: " +info.maxMagazineAmmo+"/"+info.maxAmmo,new Vector3f(295,490,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[4].draw("Critical hits: " +info.areCritical_hits_enabled(),new Vector3f(295,520,0),1, new Vector3f(0.686f,0.4f,0.258f));

    }

    @Override
    public void updateStats() {
        int numUpgrades = GameStateManager.getDb().getValueUpgrade("pistol","upgrades");
        if(numUpgrades >= 1){
            info.maxMagazineAmmo+=2;
        }
        if(numUpgrades >= 2){
            info.crit_hits = true;
        }
        if(numUpgrades >= 3){
            info.minDamage++;
        }
    }
}

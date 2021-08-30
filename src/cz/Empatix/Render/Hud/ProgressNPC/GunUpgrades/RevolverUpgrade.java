package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;

import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

public class RevolverUpgrade extends UpgradeBar {
    public RevolverUpgrade(int row){
        super("Textures\\revolver.tga",2,row);

        info = new WeaponInfo();
        info.maxAmmo = 60;
        info.maxMagazineAmmo = 6;
        info.maxDamage = 6;
        info.minDamage = 4;
        info.firerate = 1f/0.450f;
        info.name = "Revolver";


        int numUpgrades = GameStateManager.getDb().getValueUpgrade("revolver","upgrades");


        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"revolver");
        String[] text = new String[]{"Your bullets can cause critical hits"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(20);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.crit_hits = true;
        }

        bar = new UpgradeSideBar(sideBars.size(),"revolver");
        text = new String[]{"Increase maximum damage by 2"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(60);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.maxDamage+=2;
        }

        bar = new UpgradeSideBar(sideBars.size(),"revolver");
        text = new String[]{"Each time your bullet is not critical","your critical chance for next bullet","is increased by 10%, effect stacks","","Critical hit resets bonus"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.OTHERUPGRADE);
        bar.setPrice(120);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"revolver");
        text = new String[]{"Increase critical damage by 100%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(160);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
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
        int numUpgrades = GameStateManager.getDb().getValueUpgrade("revolver","upgrades");
        if(numUpgrades == 1){
            info.crit_hits = true;
        }
        if(numUpgrades == 2){
            info.maxDamage+=2;
        }
    }
}

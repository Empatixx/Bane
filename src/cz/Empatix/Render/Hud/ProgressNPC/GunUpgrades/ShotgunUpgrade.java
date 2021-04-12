package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;


import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

public class ShotgunUpgrade extends UpgradeBar {
    WeaponInfo info;
    public ShotgunUpgrade(int row){
        super("Textures\\shotgun.tga",2,row);

        info = new WeaponInfo();
        info.maxAmmo = 36;
        info.maxMagazineAmmo = 6;
        info.maxDamage = 3;
        info.minDamage = 2;
        info.firerate = 1f/0.400f;
        info.name = "Shotgun";

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("shotgun","upgrades");

        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"shotgun");
        String[] text = new String[]{"Increase maximum capacity of ammo by 6"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(20);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"shotgun");
        text = new String[]{"Increase magazine capacity by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(50);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"shotgun");
        text = new String[]{"Increase maximum damage by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(85);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"shotgun");
        text = new String[]{"Reduces cooldown between shots by 15%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.OTHERUPGRADE);
        bar.setPrice(125);
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
        int numUpgrades = GameStateManager.getDb().getValueUpgrade("shotgun","upgrades");
        if(numUpgrades >= 1){
            info.maxAmmo += 6;
        }
        if(numUpgrades >= 2){
            info.maxMagazineAmmo++;
        }
        if(numUpgrades >= 3){
            info.maxDamage++;
        }
        if(numUpgrades >= 4){
            info.firerate = 1f/0.425f;
        }
    }
}

package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;


import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

public class ModernShotgunUpgrade extends UpgradeBar {
    public ModernShotgunUpgrade(int row){
        super("Textures\\modernShotgun.tga",2,row);

        info = new WeaponInfo();
        info.maxAmmo = 55;
        info.maxMagazineAmmo = 5;
        info.maxDamage = 4;
        info.minDamage = 3;
        info.firerate = 1f/0.450f;
        info.name = "Modern Shotgun";

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("modernshotgun","upgrades");
        this.numUpgrades = numUpgrades;

        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"modernshotgun");
        String[] text = new String[]{"Increase maximum damage by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(20);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.maxDamage++;
        }

        bar = new UpgradeSideBar(sideBars.size(),"modernshotgun");
        text = new String[]{"Reduces cooldown between shots by 5%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.OTHERUPGRADE);
        bar.setPrice(50);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.firerate = (1f/0.450f)*0.95f;
        }

        bar = new UpgradeSideBar(sideBars.size(),"modernshotgun");
        text = new String[]{"Increase maximum damage by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(85);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.maxDamage++;
        }

        bar = new UpgradeSideBar(sideBars.size(),"modernshotgun");
        text = new String[]{"Each bullet that hits enemy","has chance to restore 1 armor to player"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.OTHERUPGRADE);
        bar.setPrice(125);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.firerate = 1f/0.425f;
        }
    }
    @Override
    public void drawStats() {
        super.drawStats();
        textRender[5].draw(info.name,new Vector3f(TextRender.getHorizontalCenter(202,496,info.name,2),325,0),2, new Vector3f(0.78f,0.737f,0.027f));

        textRender[0].draw("Min. damage: " +info.minDamage,new Vector3f(295,400,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[1].draw("Max. damage: " +info.maxDamage,new Vector3f(295,430,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[2].draw("Fire rate: 3x" +String.format("%.2f",info.firerate),new Vector3f(295,460,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[3].draw("Max ammo: " +info.maxMagazineAmmo+"/"+info.maxAmmo,new Vector3f(295,490,0),1, new Vector3f(0.686f,0.4f,0.258f));
        textRender[4].draw("Critical hits: " +info.areCritical_hits_enabled(),new Vector3f(295,520,0),1, new Vector3f(0.686f,0.4f,0.258f));

    }

    @Override
    public void updateStats() {
        int numUpgrades = GameStateManager.getDb().getValueUpgrade("modernshotgun","upgrades");
        this.numUpgrades = numUpgrades;

        if(numUpgrades == 1){
            info.maxDamage++;
        }
        if(numUpgrades == 2){
            info.firerate = (1f/0.450f)*0.95f;
        }
        if(numUpgrades == 3){
            info.maxDamage++;
        }
        if(numUpgrades == 4){
        }
    }
}

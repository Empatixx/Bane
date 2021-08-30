package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;


import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

public class ThompsonUpgrade extends UpgradeBar {
    public ThompsonUpgrade(int row){
        super("Textures\\thompson.tga",2,row);
        info = new WeaponInfo();
        info.maxAmmo = 300;
        info.maxMagazineAmmo = 50;
        info.maxDamage = 3;
        info.minDamage = 1;
        info.firerate = 1f/0.200f;
        info.name = "Thompson";
        int numUpgrades = GameStateManager.getDb().getValueUpgrade("thompson","upgrades");

        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"thompson");
        String[] text = new String[]{"Increase minimum damage by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(30);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.minDamage+=1;
        }

        bar = new UpgradeSideBar(sideBars.size(),"thompson");
        text = new String[]{"Increase magazine capacity by 10"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(60);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.maxMagazineAmmo+=10;
        }

        bar = new UpgradeSideBar(sideBars.size(),"thompson");
        text = new String[]{"Increase maximum capacity of ammo by 60"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(85);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
            info.maxAmmo+=100;
        }

        bar = new UpgradeSideBar(sideBars.size(),"thompson");
        text = new String[]{"Your fire rate slightly builts by firing","","Your firate can be increased up by 35%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.OTHERUPGRADE);
        bar.setPrice(175);
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
        int numUpgrades = GameStateManager.getDb().getValueUpgrade("thompson","upgrades");
        if(numUpgrades == 1){
            info.minDamage+=1;
        }
        if(numUpgrades == 2){
            info.maxMagazineAmmo+=10;
        }
        if(numUpgrades == 3){
            info.maxAmmo+=100;
        }
    }
}

package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;


import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

public class LugerUpgrade extends UpgradeBar {

    WeaponInfo info;

    public LugerUpgrade(int row){
        super("Textures\\lahti.tga",2,row);
        info = new WeaponInfo();
        info.maxAmmo = 120;
        info.maxMagazineAmmo = 9;
        info.maxDamage = 2;
        info.minDamage = 1;
        info.firerate = 1f/0.250f;
        info.name = "Luger";

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("luger","upgrades");


        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"luger");
        String[] text = new String[]{"Increase maximum and minimum damage by 1"};
        bar.setText(text);
        bar.setPrice(20);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"luger");
        text = new String[]{"Increase chance of firing addional bullets by 20%"};
        bar.setText(text);
        bar.setPrice(45);
        bar.setType(UpgradeSideBar.OTHERUPGRADE);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"luger");
        text = new String[]{"Addional bullets no longer consumes magazine ammo"};
        bar.setText(text);
        bar.setPrice(80);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"luger");
        text = new String[]{"All bullets can be affected by critical hits"};
        bar.setText(text);
        bar.setPrice(160);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
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
        int numUpgrades = GameStateManager.getDb().getValueUpgrade("luger","upgrades");
        if(numUpgrades >= 1){
            info.maxDamage++;
            info.minDamage++;
        }
        if(numUpgrades >= 4){
            info.crit_hits = true;
        }
    }
}

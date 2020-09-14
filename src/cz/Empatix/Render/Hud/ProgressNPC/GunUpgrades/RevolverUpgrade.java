package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;

import cz.Empatix.Gamestates.GameStateManager;

public class RevolverUpgrade extends UpgradeBar {
    public RevolverUpgrade(int row){
        super("Textures\\revolver.tga",2,row);

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("revolver","upgrades");


        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"revolver");
        String[] text = new String[]{"Increase maximum damage by 2"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(20);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"pistol");
        text = new String[]{"Your bullets can be affected by critical hits"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(60);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"pistol");
        text = new String[]{"Increase firate by 10%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.ACCURACYUPGRADE);
        bar.setPrice(120);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"pistol");
        text = new String[]{"Increase critical damage by 100%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(160);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }
    }

}

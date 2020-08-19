package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;

import cz.Empatix.Gamestates.GameStateManager;

public class M4Upgrade extends UpgradeBar {
    public M4Upgrade(int row){
        super("Textures\\M4.tga",2,row);

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("m4","upgrades");


        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"m4");
        String[] text = new String[]{"Increase magazine capacity by 4"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(20);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"m4");
        text = new String[]{"Speed of bullets will be faster by 20%"};
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
        }

        bar = new UpgradeSideBar(sideBars.size(),"m4");
        text = new String[]{"Increase maximum damage by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(170);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }
    }

}

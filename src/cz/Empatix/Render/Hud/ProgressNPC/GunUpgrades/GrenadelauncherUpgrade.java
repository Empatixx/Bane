package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;


import cz.Empatix.Gamestates.GameStateManager;

public class GrenadelauncherUpgrade extends UpgradeBar {
    public GrenadelauncherUpgrade(int row){
        super("Textures\\grenadelauncher.tga",2,row);

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("grenadelauncher","upgrades");


        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"grenadelauncher");
        String[] text = new String[]{"Increase maximum capacity of ammo by 4"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(20);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"grenadelauncher");
        text = new String[]{"Increase magazine capacity by 3"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(40);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"grenadelauncher");
        text = new String[]{"Increase damage by 2"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(80);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"grenadelauncher");
        text = new String[]{"Your explosions can be affected by critical hits"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(140);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }
    }

}

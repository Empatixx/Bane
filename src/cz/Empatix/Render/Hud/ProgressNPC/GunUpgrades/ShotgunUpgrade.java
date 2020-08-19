package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;


import cz.Empatix.Gamestates.GameStateManager;

public class ShotgunUpgrade extends UpgradeBar {
    public ShotgunUpgrade(int row){
        super("Textures\\shotgun.tga",2,row);

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
        text = new String[]{"Reduces cooldown between shots by 50%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.OTHERUPGRADE);
        bar.setPrice(125);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }
    }

}

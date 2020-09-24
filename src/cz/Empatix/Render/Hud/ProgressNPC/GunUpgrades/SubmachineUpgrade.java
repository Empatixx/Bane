package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;


import cz.Empatix.Gamestates.GameStateManager;

public class SubmachineUpgrade extends UpgradeBar {
    public SubmachineUpgrade(int row){
        super("Textures\\submachine.tga",2,row);

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("uzi","upgrades");

        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"uzi");
        String[] text = new String[]{"Increase firerate by 30%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.ACCURACYUPGRADE);
        bar.setPrice(30);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"uzi");
        text = new String[]{"Increase maximum damage by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(60);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"uzi");
        text = new String[]{"Increase chance to not consume ammo by 20%"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
        bar.setPrice(85);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"uzi");
        text = new String[]{"Your bullets can be affected by critical hits"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(160);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
        }
    }

}

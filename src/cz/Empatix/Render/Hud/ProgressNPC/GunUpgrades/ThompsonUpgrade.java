package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;


import cz.Empatix.Gamestates.GameStateManager;

public class ThompsonUpgrade extends UpgradeBar {
    public ThompsonUpgrade(int row){
        super("Textures\\thompson.tga",2,row);

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

}

package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;

import cz.Empatix.Gamestates.GameStateManager;

public class PistolUpgrade extends UpgradeBar {
    public PistolUpgrade(int row){
        super("Textures\\pistol.tga",2,row);

        int numUpgrades = GameStateManager.getDb().getValueUpgrade("pistol","upgrades");


        UpgradeSideBar bar = new UpgradeSideBar(sideBars.size(),"pistol");
        String[] text = new String[]{"Increase magazine capacity by 2"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.AMMOUPGRADE);
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
        text = new String[]{"Increase minimum damage by 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.DAMAGEUPGRADE);
        bar.setPrice(80);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }

        bar = new UpgradeSideBar(sideBars.size(),"pistol");
        text = new String[]{"Shoot 2 bullets intead of 1"};
        bar.setText(text);
        bar.setType(UpgradeSideBar.OTHERUPGRADE);
        bar.setPrice(160);
        sideBars.add(bar);
        if(numUpgrades > 0){
            bar.setBought(true);
            numUpgrades--;
        }
    }

}

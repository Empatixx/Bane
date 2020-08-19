package cz.Empatix.Render.Hud.ProgressNPC.GunUpgrades;


import cz.Empatix.Gamestates.GameStateManager;

public class LugerUpgrade extends UpgradeBar {

    public LugerUpgrade(int row){
        super("Textures\\lahti.tga",2,row);

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
        text = new String[]{"Increase chance of firing addional bullets by 10%"};
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

}

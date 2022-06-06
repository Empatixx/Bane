package cz.Empatix.Buffs;

import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Background;

public class Blindness extends Buff{
    private Background blindBg;

    private boolean lastTimeUpdate;

    public Blindness(){
        super(5000);
        idBuff = BLINDNESS;
        blindBg = new Background("Textures\\blind-vignette.tga");
        blindBg.setFadeEffect(true);
        blindBg.setTimeFadeTrans(1000);
        blindBg.setFadeAlpha(1f);
    }
    public Blindness(PlayerMP p){
        super(p.getIdConnection(),5000);
        idBuff = BLINDNESS;
    }
    @Override
    public void apply() {
        blindBg.updateFadeTime();
    }
    @Override
    public void draw(){
        blindBg.draw();
    }


    @Override
    public void update() {
        if(timeLeft() < 1000) {
            if (!lastTimeUpdate) {
                blindBg.updateFadeTime();
                lastTimeUpdate = true;
            }
            blindBg.update();
        }
    }

    @Override
    public void remove() {
    }
}

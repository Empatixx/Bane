package cz.Empatix.Buffs;

import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Multiplayer.PlayerMP;

public class Poison extends Buff{
    public long lastTimeTick;
    private Player player;
    public Poison(Player p){
        super(5100);
        this.player = p;
        idBuff = POISON;
        lastTimeTick = System.currentTimeMillis() - InGame.deltaPauseTime();
    }
    public Poison(PlayerMP p){
        super(p.getIdConnection(),5100);
        this.player = p;
        idBuff = POISON;
        lastTimeTick = System.currentTimeMillis();
    }
    @Override
    public void apply() {
    }


    @Override
    public void update() {
        if(System.currentTimeMillis() - InGame.deltaPauseTime() - lastTimeTick >= 2000){
            lastTimeTick = System.currentTimeMillis() - InGame.deltaPauseTime();
            player.hit(1);
        }
    }

    @Override
    public void remove() {
    }
    @Override
    public void draw() {

    }
}

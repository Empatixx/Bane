package cz.Empatix.Buffs;

import cz.Empatix.Gamestates.Singleplayer.InGame;

public abstract class Buff {
    private long timeStarted; // time in millis when de/buff started
    private long timeBuff; // in millis

    // if it negative for player, slow, blind, poison etc.
    private boolean negative;
    // so we know type of buff
    protected int idBuff;

    public static final int BERSERK = 0;
    public static final int BLINDNESS = 1;
    public static final int POISON = 2;
    public static final int RAGE = 3;

    protected int idPlayer = -1;

    // singleplayer
    public Buff(long timeBuff){
        this.timeBuff = timeBuff;
        this.timeStarted = System.currentTimeMillis() - InGame.deltaPauseTime();
    }
    // multiplayer
    public Buff(int idPlayer, long timeBuff){
        this.timeBuff = timeBuff;
        this.idPlayer = idPlayer;
        this.timeStarted = System.currentTimeMillis() - InGame.deltaPauseTime();
    }


    public void setNegative() {
        this.negative = true;
    }

    public boolean shouldRemove(){return System.currentTimeMillis() - InGame.deltaPauseTime() - timeStarted >= timeBuff;}
    public abstract void update();
    public abstract void remove();
    public abstract void apply();
    public abstract void draw();
    public boolean isNegative() {
        return negative;
    }
    protected long timeLeft(){
        return timeBuff - (System.currentTimeMillis() - timeStarted - InGame.deltaPauseTime());
    }
}

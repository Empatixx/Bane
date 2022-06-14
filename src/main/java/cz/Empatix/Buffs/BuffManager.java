package cz.Empatix.Buffs;


import cz.Empatix.Utility.Loader;

import java.util.ArrayList;

public class BuffManager {
    public static void load(){
        Loader.loadImage("Textures\\blind-vignette.tga");
    }
    protected float bonusDamagePercent;
    protected int bonusDamage;

    protected float bonusMovementVelocityPercent;
    protected int bonusMovementVelocity;

    protected float baseCriticalChance;
    protected float baseCriticalChanceMultiplier;

    protected ArrayList<Buff> buffs;

    private static BuffManager buffManager;
    public static BuffManager getInstance() {
        return buffManager;
    }
    public BuffManager(){
        buffManager = this;

        bonusDamage = 0;
        bonusDamagePercent = 0;

        bonusMovementVelocity = 0;
        bonusMovementVelocityPercent = 0;

        baseCriticalChance = 0.1f;
        baseCriticalChanceMultiplier = 1;

        buffs = new ArrayList<>();
    }
    public void addBuff(Buff buff){
        buff.apply();
        buffs.add(buff);
    }
    public boolean isActiveBuff(int type){
        for(Buff b : buffs){
            if(b.idBuff == type){
                return true;
            }
        }
        return false;
    }
    // in secs
    public float getTimeLeft(int type){
        for(Buff b : buffs){
            if(b.idBuff == type){
                return b.timeLeft()/1000f;
            }
        }
        return -1;
    }
    public void update(){
        for(int i = 0;i<buffs.size();i++){
            Buff b = buffs.get(i);
            b.update();
            if(b.shouldRemove()){
                b.remove();
                buffs.remove(i);
                i--;
            }
        }
    }
    public void draw(){
        for(Buff b : buffs){
            b.draw();
        }
    }
    public int applyMovementBonuses(int velocity){
        return velocity + bonusMovementVelocity + (int)((velocity + bonusMovementVelocity) * bonusMovementVelocityPercent);
    }
    public int applyDamageBonuses(int damage){
        return damage + bonusDamage + (int)((damage+bonusDamage)*bonusDamagePercent);
    }
    public float getCriticalChance(float bonusGunCritChance){
        return (baseCriticalChance + bonusGunCritChance) * baseCriticalChanceMultiplier;
    }
}

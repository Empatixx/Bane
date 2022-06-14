package cz.Empatix.Buffs;


import cz.Empatix.Multiplayer.PlayerMP;

import java.util.ArrayList;

public class BuffManagerMP {
    private ArrayList<PlayerBuffs> playerBuffs;
    protected static class PlayerBuffs{
        protected float bonusDamagePercent;
        protected int bonusDamage;

        protected float bonusMovementVelocityPercent;
        protected int bonusMovementVelocity;

        protected float baseCriticalChance;
        protected float baseCriticalChanceMultiplier;

        protected ArrayList<Buff> buffs;

        private final int idPlayer;
        public PlayerBuffs(int idPlayer){
            this.idPlayer = idPlayer;
            bonusDamage = 0;
            bonusDamagePercent = 0;

            bonusMovementVelocity = 0;
            bonusMovementVelocityPercent = 0;

            baseCriticalChance = 0.1f;
            baseCriticalChanceMultiplier = 1;

            buffs = new ArrayList<>();
        }

        public void add(Buff buff) {
            buff.apply();
            buffs.add(buff);
        }

        public void update() {
            for(int i = 0;i<buffs.size();i++){
                Buff b = buffs.get(i);
                if(b.shouldRemove()){
                    b.remove();
                    buffs.remove(i);
                    i--;
                }
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
    }
    public boolean isActiveBuff(int type, int idPlayer){
        for(PlayerBuffs pb : playerBuffs){
            if(pb.idPlayer == idPlayer){
                return pb.isActiveBuff(type);
            }
        }
        return false;
    }
    // in secs
    public float getTimeLeft(int type, int idPlayer){
        for(PlayerBuffs pb : playerBuffs){
            if(pb.idPlayer == idPlayer){
                return pb.getTimeLeft(type);
            }
        }
        return -1;
    }
    private static BuffManagerMP buffManager;
    public static BuffManagerMP getInstance() {
        return buffManager;
    }
    public BuffManagerMP(){
        buffManager = this;
        playerBuffs = new ArrayList<>();
    }
    public void addPlayer(PlayerMP player){
        PlayerBuffs pb = new PlayerBuffs(player.getIdConnection());
        playerBuffs.add(pb);
    }
    public void removePlayer(PlayerMP player){
        for(int i = 0;i<playerBuffs.size();i++){
            if(playerBuffs.get(i).idPlayer == player.getIdConnection()){
                playerBuffs.remove(i);
                i--;
            }
        }
    }
    public void addBuff(Buff buff,int idPlayer){
        for(PlayerBuffs pb : playerBuffs){
            if(pb.idPlayer == idPlayer){
                pb.add(buff);
                break;
            }
        }
    }
    public void update(){
        for(PlayerBuffs pb : playerBuffs){
            pb.update();
        }
    }
    public int applyMovementBonuses(int velocity, int idPlayer){
        for(PlayerBuffs pb : playerBuffs){
            if(pb.idPlayer == idPlayer){
                return pb.applyMovementBonuses(velocity);
            }
        }
        return 0;
    }
    public int applyDamageBonuses(int damage, int idPlayer){
        for(PlayerBuffs pb : playerBuffs){
            if(pb.idPlayer == idPlayer){
                return pb.applyDamageBonuses(damage);
            }
        }
        return 0;
    }
    public float getCriticalChance(float bonusGunCritChance, int idPlayer){
        for(PlayerBuffs pb : playerBuffs){
            if(pb.idPlayer == idPlayer){
                return pb.getCriticalChance(bonusGunCritChance);
            }
        }
        return 0;
    }
    public PlayerBuffs getPlayerBuffs(int idPlayer){
        for(PlayerBuffs pb : playerBuffs){
            if(pb.idPlayer == idPlayer){
                return pb;
            }
        }
        return null;
    }
}

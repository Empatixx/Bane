package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;

import java.util.ArrayList;

public class MPStatistics {
    private ArrayList<PStats> playersStats;
    private long delay;
    public static class PStats{
        private int bulletShooted;
        private int enemiesKilled;
        private int bulletsHit;
        private long deathTime;

        private String username;
        private int idPlayer;

        public float getAccuracy(){
            return (float) bulletsHit / bulletShooted;
        }
        public boolean isThisPlayer(String username){
            return username.equalsIgnoreCase(this.username);
        }
        public PStats(String username, int idPlayer){
            bulletShooted = 0;
            enemiesKilled = 0;
            bulletsHit = 0;
            this.username = username;
            this.idPlayer = idPlayer;
        }

        public long getDeathTime() {
            return deathTime;
        }

        public int getBulletShooted() {
            return bulletShooted;
        }

        public int getEnemiesKilled() {
            return enemiesKilled;
        }
    }
    public MPStatistics(){
        playersStats = new ArrayList<>();
    }
    public void addPlayer(String username,int idPlayer){
        PStats pStats = new PStats(username,idPlayer);
        playersStats.add(pStats);
    }
    public void remove(String username){
        for(int i = 0;i<playersStats.size();i++){
            PStats pStats = playersStats.get(i);
            if(pStats.isThisPlayer(username)){
                playersStats.remove(i);
                i--;
            }
        }
    }
    // server side
    public void sentPackets(){
        if(System.currentTimeMillis() - delay > 500){
            Server server = MultiplayerManager.getInstance().server.getServer();
            for(PStats stats : playersStats){
                Network.PstatsUpdate pu = new Network.PstatsUpdate();
                pu.bulletsHit = (short)stats.bulletsHit;
                pu.shootShooted = (short)stats.bulletShooted;
                pu.enemiesKilled = (short)stats.enemiesKilled;
                pu.idPlayer = stats.idPlayer;
                server.sendToAllUDP(pu);
            }
            delay = System.currentTimeMillis();
        }
    }
    // client side
    public void reveicePackets(){
        Object[] packets = MultiplayerManager.getInstance().packetHolder.get(PacketHolder.PLAYERSSTATS);
        for(PStats pStats : playersStats){
            for(Object o : packets){
                Network.PstatsUpdate p = (Network.PstatsUpdate) o;
                if(p.idPlayer == pStats.idPlayer){
                    pStats.bulletsHit = p.bulletsHit;
                    pStats.bulletShooted = p.shootShooted;
                    pStats.deathTime = p.deathTime;
                    pStats.enemiesKilled = p.enemiesKilled;
                }
            }
        }
    }
    public void setTimeDeath(int idPlayer, long time){
        for(PStats stats : playersStats){
            if(idPlayer == stats.idPlayer){
                stats.deathTime = time;
                break;
            }
        }
    }
    public void addBulletShoot(int idPlayer){
        for(PStats stats : playersStats){
            if(idPlayer == stats.idPlayer){
                stats.bulletShooted++;
                break;
            }
        }
    }
    public void addBulletHit(int idPlayer){
        for(PStats stats : playersStats){
            if(idPlayer == stats.idPlayer){
                stats.bulletsHit++;
                break;
            }
        }
    }
    public void addEnemiesKill(int idPlayer){
        for(PStats stats : playersStats){
            if(idPlayer == stats.idPlayer){
                stats.enemiesKilled++;
                break;
            }
        }
    }
    public PStats getPlayerStats(int idPlayer){
        for(PStats stats : playersStats){
            if(idPlayer == stats.idPlayer){
                return stats;
            }
        }
        return null;
    }
}

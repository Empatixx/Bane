package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;

import java.util.ArrayList;

public class MPStatistics {
    private ArrayList<PStats> playersStats;
    public static class PStats{
        private int bulletShooted;
        private int enemiesKilled;
        private int bulletsHit;
        private long deathTime;

        private String username;

        public float getAccuracy(){
            return (float) bulletsHit / bulletShooted;
        }
        public boolean isThisPlayer(String username){
            return username.equalsIgnoreCase(this.username);
        }
        public PStats(String username){
            bulletShooted = 0;
            enemiesKilled = 0;
            bulletsHit = 0;
            this.username = username;
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
    public void addPlayer(String username){
        PStats pStats = new PStats(username);
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
        Server server = MultiplayerManager.getInstance().server.getServer();
        for(PStats stats : playersStats){
            Network.PstatsUpdate pu = new Network.PstatsUpdate();
            pu.bulletsHit = stats.bulletsHit;
            pu.shootShooted = stats.bulletShooted;
            pu.deathTime = stats.deathTime;
            pu.enemiesKilled = stats.enemiesKilled;
            pu.username = stats.username;
            server.sendToAllUDP(pu);
        }
    }
    // client side
    public void reveicePackets(){
        Object[] packets = MultiplayerManager.getInstance().packetHolder.get(PacketHolder.PLAYERSSTATS);
        for(PStats pStats : playersStats){
            for(Object o : packets){
                Network.PstatsUpdate p = (Network.PstatsUpdate) o;
                if(p.username.equalsIgnoreCase(pStats.username)){
                    pStats.bulletsHit = p.bulletsHit;
                    pStats.bulletShooted = p.shootShooted;
                    pStats.deathTime = p.deathTime;
                    pStats.enemiesKilled = p.enemiesKilled;
                    pStats.username = p.username;
                }
            }
        }
    }
    public void setTimeDeath(String username, long time){
        for(PStats stats : playersStats){
            if(stats.username.equalsIgnoreCase(username)){
                stats.deathTime = time;
                break;
            }
        }
    }
    public void addBulletShoot(String username){
        for(PStats stats : playersStats){
            if(stats.username.equalsIgnoreCase(username)){
                stats.bulletShooted++;
                break;
            }
        }
    }
    public void addBulletHit(String username){
        for(PStats stats : playersStats){
            if(stats.username.equalsIgnoreCase(username)){
                stats.bulletsHit++;
                break;
            }
        }
    }
    public void addEnemiesKill(String username){
        for(PStats stats : playersStats){
            if(stats.username.equalsIgnoreCase(username)){
                stats.enemiesKilled++;
                break;
            }
        }
    }
    public PStats getPlayerStats(String username){
        for(PStats stats : playersStats){
            if(stats.username.equalsIgnoreCase(username)){
                return stats;
            }
        }
        return null;
    }
}

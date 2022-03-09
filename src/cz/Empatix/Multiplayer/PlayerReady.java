package cz.Empatix.Multiplayer;

public class PlayerReady {
    private final int idPlayer;
    private final String username; // only for alert purpose
    private boolean ready;
    public PlayerReady(String username, int idPlayer){
        this.ready = false;
        this.idPlayer = idPlayer;
        this.username = username;
    }
    public boolean isEqual(int id) {return id == idPlayer;}
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }

    public String getUsername() {
        return username;
    }
}

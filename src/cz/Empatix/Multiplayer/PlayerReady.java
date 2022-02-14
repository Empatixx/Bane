package cz.Empatix.Multiplayer;

public class PlayerReady {
    private final String username;
    private boolean ready;
    public PlayerReady(String username){
        this.ready = false;
        this.username = username;
    }
    public String getUsername() {
            return username;
    }
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }
}

package cz.Empatix.Entity;

import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Render.Graphics.Sprites.Sprite;

public class Animation{

    private Sprite[] frames;
    private int currentFrame;

    private long startTime;
    private long delay;

    private boolean playedOnce;
    private boolean reverse;

    private long syncElapsedMP;
    private boolean syncMP;

    public Animation() {
        playedOnce = false;
    }
    // server animation
    public Animation(int frames) {
        playedOnce = false;
        Sprite[] fakeSprites = new Sprite[frames];
        setFrames(fakeSprites);
    }
    public void setFrames(Sprite[] frames) {
        this.frames = frames;
        currentFrame = 0;
        startTime = System.currentTimeMillis() - InGame.deltaPauseTime();
        playedOnce = false;
        reverse = false;
    }
    public void updateFrames(Sprite[] frames) {
        this.frames = frames;
    }
    public void setDelay(long d) { delay = d; }
    public void setFrame(int i) { currentFrame = i; }

    public void reverse() {
        reverse = true;
        playedOnce = false;
        currentFrame = frames.length-1;
    }
    public void unreverse() {
        reverse = false;
        playedOnce = false;
        currentFrame = 0;
    }
    public void update() {

        if(delay == -1) return;

        long elapsed = System.currentTimeMillis() - InGame.deltaPauseTime() - startTime;
        if(syncMP){
            elapsed = syncElapsedMP;
        }
        if(reverse){
            if (elapsed > delay) {
                currentFrame--;
                startTime = System.currentTimeMillis() - InGame.deltaPauseTime();
            }
            if (currentFrame == -1) {
                currentFrame = frames.length-1;
                playedOnce = true;
            }
        } else {
            if (elapsed > delay) {
                currentFrame++;
                startTime = System.currentTimeMillis() - InGame.deltaPauseTime();
            }
            if (currentFrame == frames.length) {
                currentFrame = 0;
                playedOnce = true;
            }
        }
    }

    public Sprite getFrame() { return frames[currentFrame]; }
    public boolean hasPlayedOnce() { return playedOnce; }

    /**
     *
     * @return index of current frame
     */
    public int getIndexOfFrame(){
        return currentFrame;
    }
    public boolean isPlayingLastFrame(){return currentFrame == frames.length-1;}

    /**
     * only for multiplayer purpose
     * @return elapsed time from start of current frame to next
     */
    public long getTime() {
        return System.currentTimeMillis() - InGame.deltaPauseTime() - startTime;
    }
    public void setTime(long time){
        syncMP = true;
        float latency = MultiplayerManager.getInstance().client.getClient().getReturnTripTime()/2f;
        syncElapsedMP = (long) (time+latency);
    }
}
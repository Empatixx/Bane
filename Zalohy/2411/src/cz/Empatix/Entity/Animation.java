package cz.Empatix.Entity;

public class Animation {

    private Sprite[] frames;
    private int currentFrame;

    private long startTime;
    private long delay;

    private boolean playedOnce;

    public Animation() {
        playedOnce = false;
    }

    public void setFrames(Sprite[] frames) {
        this.frames = frames;
        currentFrame = 0;
        startTime = System.nanoTime();
        playedOnce = false;
    }

    public void setDelay(long d) { delay = d; }
    public void setFrame(int i) { currentFrame = i; }

    public void update() {

        if(delay == -1) return;

        long elapsed = (System.nanoTime() - startTime) / 1000000;
        if(elapsed > delay) {
            currentFrame++;
            startTime = System.nanoTime();
        }
        if(currentFrame == frames.length) {
            currentFrame = 0;
            playedOnce = true;
        }

    }

    public int getFrame() { return currentFrame; }
    public float[] getTexCoords() { return frames[currentFrame].getTexCoords();}
    public int getBind() { return frames[currentFrame].getId(); }
    public boolean hasPlayedOnce() { return playedOnce; }
    @Override
    // finalize method is called on object once
    // before garbage collecting it
    protected void finalize()
    {
        System.out.println("Object garbage collected : " + this);
    }
}
package cz.Empatix.AudioManager;


import cz.Empatix.Main.Settings;
import org.lwjgl.openal.AL10;

public class Source {

    private int sourceId;

    private int type;
    public float volume;

    private boolean deleted;
    private long timeStop;
    private boolean stopping;

    public static final int MUSIC = 0;
    public static final int EFFECTS = 1;

    public Source(int type, float volume){
        if(!AudioManager.hasAudio()) return;
        sourceId = AL10.alGenSources();
        AL10.alSourcef(sourceId,AL10.AL_GAIN,1);
        AL10.alSourcef(sourceId,AL10.AL_PITCH,1);
        this.type = type;
        this.volume = volume;
        setVolume(volume);
    }

    public void play(int buffer){
        if(!AudioManager.hasAudio()) return;
        stopping = false;
        AL10.alSourceStop(sourceId);
        setVolume(volume);
        AL10.alSourcei(sourceId,AL10.AL_BUFFER,buffer);
        resume();
    }
    public void setLooping(boolean b){
        if(!AudioManager.hasAudio()) return;
        AL10.alSourcei(sourceId,AL10.AL_LOOPING, b ? AL10.AL_TRUE : AL10.AL_FALSE);
    }
    public boolean isPlaying(){
        if(!AudioManager.hasAudio()) return true;
        return AL10.alGetSourcei(sourceId,AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }
    public void setVolume(float volume){
        if(!AudioManager.hasAudio()) return;
        if(type == MUSIC) AL10.alSourcef(sourceId,AL10.AL_GAIN,volume* Settings.OVERALL * Settings.MUSIC);
        if(type == EFFECTS) AL10.alSourcef(sourceId,AL10.AL_GAIN,volume* Settings.OVERALL * Settings.EFFECTS);
    }
    public void setPitch(float pitch) { AL10.alSourcef(sourceId,AL10.AL_PITCH,pitch);}
    public void pause(){
        if(!AudioManager.hasAudio()) return;
        AL10.alSourcePause(sourceId);
    }
    public void resume(){
        if(!AudioManager.hasAudio()) return;
        AL10.alSourcePlay(sourceId);
    }
    public void stop(){
        timeStop = System.currentTimeMillis();
        stopping = true;
    }
    public void setPosition(float x, float y){
        AL10.alSource3f(sourceId,AL10.AL_POSITION,x,y,0);
    }


    public void delete() {
        if(!AudioManager.hasAudio()) return;
        stop();
        AL10.alDeleteSources(sourceId);
        deleted = true;
    }
    public void update(){
        if(!AudioManager.hasAudio()) return;

        float time = (float)(System.currentTimeMillis() - timeStop);

        if(stopping){
            if(time < 750){
                setVolume(volume * (1-time/750));
            }else {
                stopping=false;
                AL10.alSourceStop(sourceId);
            }
        }
    }

    public boolean isDeleted() {
        return deleted;
    }
}

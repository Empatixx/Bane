package cz.Empatix.AudioManager;


import org.lwjgl.openal.AL10;

public class Source {

    private final int sourceId;

    public Source(){
        sourceId = AL10.alGenSources();
        AL10.alSourcef(sourceId,AL10.AL_GAIN,1);
        AL10.alSourcef(sourceId,AL10.AL_PITCH,1);
    }

    public void play(int buffer){
        stop();
        AL10.alSourcei(sourceId,AL10.AL_BUFFER,buffer);
        resume();
    }

    public void delete(){
        stop();
        AL10.alDeleteSources(sourceId);
    }
    public void setLooping(boolean b){
        AL10.alSourcei(sourceId,AL10.AL_LOOPING, b ? AL10.AL_TRUE : AL10.AL_FALSE);
    }
    public boolean isPlaying(){
        return AL10.alGetSourcei(sourceId,AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }
    public void setVolume(float volume){
        AL10.alSourcef(sourceId,AL10.AL_GAIN,volume);
    }
    public void setPitch(float pitch) { AL10.alSourcef(sourceId,AL10.AL_PITCH,pitch);}
    public void pause(){
        AL10.alSourcePause(sourceId);
    }
    public void resume(){
        AL10.alSourcePlay(sourceId);
    }
    public void stop(){
        AL10.alSourceStop(sourceId);
    }
    public void setPosition(float x, float y){
        AL10.alSource3f(sourceId,AL10.AL_POSITION,x,y,0);
    }
}

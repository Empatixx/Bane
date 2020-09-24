package cz.Empatix.AudioManager;

import cz.Empatix.Main.Settings;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class AudioManager {

    // array of sounds (ids)
    private static HashMap<String,Integer> buffers;
    private static ArrayList<Soundtrack> soundtracks;

    private static ArrayList<Source> sources;


    private static double lastVolume;

    private static long timeSoundtrackChange;

    // soundtrack
    private static int currentSoundtrack = -1;
    private static int previousSoundtrack;

    private static boolean hasAudio;


    public static void playSoundtrack(int soundtrack){
        if (hasAudio){
            if(previousSoundtrack != -1) soundtracks.get(previousSoundtrack).pause();
            timeSoundtrackChange = System.currentTimeMillis();

            previousSoundtrack = currentSoundtrack;

            currentSoundtrack = soundtrack;

            soundtracks.get(currentSoundtrack).start();
        }

    }

    public static void init(){
        lastVolume = Settings.MUSIC * Settings.OVERALL;

        soundtracks = new ArrayList<>();
        sources = new ArrayList<>();
        buffers = new HashMap<>();
        String defaultDeviceName = alcGetString(0, ALC_DEVICE_SPECIFIER);

        hasAudio = false;

        if(defaultDeviceName != null){
            long device  = alcOpenDevice(defaultDeviceName);
            if(device  == 0) return;

            int[] attributes = {0};
            long  context    = alcCreateContext(device, attributes);
            alcMakeContextCurrent(context);

            ALCCapabilities alcCapabilities = ALC.createCapabilities(device);

            AL.createCapabilities(alcCapabilities);

            hasAudio = true;

            addSoundtracks();
        }

    }
    private static void addSoundtracks(){
        Soundtrack soundtrack = new Soundtrack("Sounds\\audio.ogg",false);
        soundtracks.add(soundtrack);

        Soundtrack soundtrack2 = new Soundtrack("Sounds\\boss.ogg",false);
        soundtracks.add(soundtrack2);

        Soundtrack soundtrack3 = new Soundtrack("Sounds\\intro.ogg",false);
        soundtracks.add(soundtrack3);

        Soundtrack soundtrack4 = new Soundtrack("Sounds\\ambient.ogg",false);
        soundtracks.add(soundtrack4);
    }
    public static int loadSound(String filename){
        if(!hasAudio)return -1;
        if(buffers.containsKey(filename)){
            return buffers.get(filename);
        }

        ShortBuffer rawAudioBuffer;

        int channels;
        int sampleRate;

        try (MemoryStack stack = stackPush()) {
            //Allocate space to store return information from the function
            IntBuffer channelsBuffer   = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);

            rawAudioBuffer = stb_vorbis_decode_filename("Sounds\\"+filename, channelsBuffer, sampleRateBuffer);

            //Retreive the extra information that was stored in the buffers by the functfion
            channels = channelsBuffer.get(0);
            sampleRate = sampleRateBuffer.get(0);
        }

        //Find the correct OpenAL format
        int format = -1;
        if (channels == 1) {
            format = AL_FORMAT_MONO16;
        } else if (channels == 2) {
            format = AL_FORMAT_STEREO16;
        }
        //Request space for the buffer
        int bufferPointer = alGenBuffers();

    //Send the data to OpenAL
        alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

    //Free the memory allocated by STB
        free(rawAudioBuffer);

        buffers.put(filename,bufferPointer);
        return bufferPointer;
    }
    public static void setListenerData(float x, float y){
        if (hasAudio){
            AL10.alListener3f(AL_POSITION,x,y,0);
            AL10.alListener3f(AL_VELOCITY,0,0,0);
        }

    }
    public static void cleanUp(){
        buffers.forEach((k,v) -> alDeleteSources(v));

        ALC.destroy();
        AL.setCurrentProcess(null);
    }

    public static void update(){
        if(!hasAudio) return;
        double newVol = Settings.OVERALL*Settings.MUSIC;
        if(newVol != lastVolume){
            for(Soundtrack track :soundtracks){
                track.setVolume(0.2f);
            }
        }
        float time = (float)(System.currentTimeMillis() - timeSoundtrackChange);
        if(time < 750) {
            soundtracks.get(currentSoundtrack).setVolume(0.2f * time / 750);

            if(previousSoundtrack != -1){
                soundtracks.get(previousSoundtrack).setVolume(0.2f * (1-time/750));
            }
        } else {
            if(previousSoundtrack != -1){
                Soundtrack soundtrack = soundtracks.get(previousSoundtrack);
                if(soundtrack.isPlaying()) soundtrack.pause();
            }
        }
        for(int i = 0;i<sources.size();i++){
            Source source = sources.get(i);
            if(source.isDeleted()){
                sources.remove(i);
                i--;
            } else {
                source.update();
            }
        }
    }

    public static Source createSource(int type,float volume){
        Source source = new Source(type,volume);
        sources.add(source);
        return source;
    }

    public static void cleanUpAllSources(){
        if(hasAudio) {
            for (Source s : sources) {
                s.delete();
            }
        }
    }
    static boolean hasAudio(){ return hasAudio;}
}

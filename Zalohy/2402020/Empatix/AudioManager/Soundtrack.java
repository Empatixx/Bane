package cz.Empatix.AudioManager;

import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class Soundtrack {
    public static final int IDLE = 0;
    public static final int BOSS = 1;
    public static final int MENU = 2;
    // source
    private final Source source;
    // if soundtrack after pause should start on 0 second
    private final boolean refresh;
    // buffer of sound
    private final int buffer;
    // volume of soundtrack starting on 0.5f
    private float volume;
    // if it didn't even start
    private boolean played;

    /**
     *
     * @param file - path to the sound
     * @param refresh - if you should start sound after pause at first second(not saving time)
     */
    Soundtrack(String file, boolean refresh){
        played = false;
        this.refresh = refresh;
        volume = 0.2f;
        ShortBuffer rawAudioBuffer;

        int channels;
        int sampleRate;

        try (MemoryStack stack = stackPush()) {
            //Allocate space to store return information from the function
            IntBuffer channelsBuffer   = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);

            rawAudioBuffer = stb_vorbis_decode_filename(file, channelsBuffer, sampleRateBuffer);

            //Retreive the extra information that was stored in the buffers by the function
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

        buffer = bufferPointer;

        source = new Source();
        source.setVolume(volume);
        source.setLooping(true);
        //source.pause();

    }
    void start(){
        if (!played){
            source.play(buffer);
            played = true;
        }
        else source.resume();
    }
    void pause(){
        if (refresh){
            source.stop();
            played = false;
        }
        else{
            source.pause();
        }
    }
}

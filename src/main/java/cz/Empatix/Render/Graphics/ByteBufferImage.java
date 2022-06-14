package cz.Empatix.Render.Graphics;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;

public class ByteBufferImage {
    private int width;
    private int height;
    private int channels;
    private ByteBuffer buffer;

    public ByteBuffer decodeImage(String filepath){

        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);


            /* Load image */
            buffer = stbi_load(filepath, w, h, channels, 0);

            if (buffer == null) {
                throw new RuntimeException("Failed to load a texture file!"
                        + System.lineSeparator() + stbi_failure_reason());
            }
            this.width = w.get(0);
            this.height = h.get(0);
            this.channels = channels.get(0);

            buffer.flip();

            return buffer;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getChannels() {
        return channels;
    }
}

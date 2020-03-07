package cz.Empatix.Render;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;

public class ByteBufferImage {
    private int width;
    private int height;

    public ByteBuffer decodeImage(String filepath){
        ByteBuffer buffer;

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

            buffer.flip();

            return buffer;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}

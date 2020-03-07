package cz.Empatix.Render;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.*;

public class ByteBufferImage {
    private String filepath;

    public ByteBufferImage(String filepath){
        this.filepath = filepath;
    }
    public ByteBuffer decodeImage(){
        ByteBuffer buffer;

        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);


            /* Load image */
            buffer = stbi_load(filepath, x, y, channels, 4);
            if (buffer == null) {
                throw new RuntimeException("Failed to load a texture file!"
                        + System.lineSeparator() + stbi_failure_reason());
            }

            buffer.flip();

            return buffer;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

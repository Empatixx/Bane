package cz.Empatix.Render;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
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
            stbi_set_flip_vertically_on_load(true);
            buffer = stbi_load(filepath, x, y, channels, STBI_rgb_alpha);
            if (buffer == null) {
                throw new RuntimeException("Failed to load a texture file!"
                        + System.lineSeparator() + stbi_failure_reason());
            }
            return buffer;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    protected void finalize()
    {
        System.out.println("Object garbage collected : " + this);
    }
}

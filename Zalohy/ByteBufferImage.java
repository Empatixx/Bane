package cz.Empatix.Render;

import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class ByteBufferImage {
    private BufferedImage image;

    public ByteBufferImage(BufferedImage image){
        this.image = image;
    }
    public ByteBuffer decodeImage(){
        try {
            int[] pixels;

            int h = image.getHeight();
            int w = image.getWidth();

            pixels = image.getRGB(0, 0, w, h, null, 0, w);

            ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4); //4 for RGBA, 3 for RGB

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int pixel = pixels[y * w + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                    buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                    buffer.put((byte) (pixel & 0xFF));               // Blue component
                    buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
                }
            }
            buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

            // You now have a ByteBuffer filled with the color data of each pixel.
            // Now just create a texture ID and bind it. Then you can load it using
            // whatever OpenGL method you want, for example:
            return buffer;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

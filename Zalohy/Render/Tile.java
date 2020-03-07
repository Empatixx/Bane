package cz.Empatix.Render;

import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Tile {
	
	private int id;
	private int type;
	
	// tile types
	public static final int NORMAL = 0;
	public static final int BLOCKED = 1;
	
	Tile(BufferedImage image, int type) {
		this.type = type;
		try{
			int[] pixels;

			int h = image.getHeight();
			int w = image.getWidth();

			pixels = image.getRGB(0, 0, w, h, null, 0, w);

			ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4); //4 for RGBA, 3 for RGB

			for (int y = 0; y < w; y++) {
				for (int x = 0; x < h; x++) {
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

			id = glGenTextures();

			glBindTexture(GL_TEXTURE_2D, id);

			glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);


			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	int getType() { return type; }
	void bind(){
		glBindTexture(GL_TEXTURE_2D,id);
	}
}

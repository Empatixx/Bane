package cz.Empatix.Render;


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

			int h = image.getHeight();
			int w = image.getWidth();

			ByteBuffer buffer = new ByteBufferImage(image).decodeImage();

			id = glGenTextures();

			glBindTexture(GL_TEXTURE_2D, id);

			glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);


			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

			//free(buffer);

		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	int getType() { return type; }
	void bind(){
		glBindTexture(GL_TEXTURE_2D,id);
	}
}

package cz.Empatix.Render;


import org.lwjgl.BufferUtils;

import java.nio.DoubleBuffer;

import static org.lwjgl.opengl.GL15.*;

public class Tile {

	private final int type;
	private final int vboTextures;

	// tile types
	public static final int NORMAL = 0;
	public static final int BLOCKED = 1;
	
	Tile(double[] texCoords, int type) {
		this.type = type;
		DoubleBuffer buffer = BufferUtils.createDoubleBuffer(texCoords.length);
		buffer.put(texCoords);
		buffer.flip();
		vboTextures = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
		glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER,0);
	}
	
	int getType() { return type; }

	public int getVbo() {
		return vboTextures;
	}

}

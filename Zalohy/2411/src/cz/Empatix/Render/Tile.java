package cz.Empatix.Render;


public class Tile {

	private float[] texCoords;
	private int type;
	
	// tile types
	public static final int NORMAL = 0;
	public static final int BLOCKED = 1;
	
	Tile(float[] texCoords, int type) {
		this.type = type;
		this.texCoords = texCoords;
	}
	
	int getType() { return type; }

	public float[] getTexCoords() { return texCoords; }
}

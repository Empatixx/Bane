package cz.Empatix.Render;


public class Tile {

	private final int type;
	private double[] texCoords;

	// tile types
	public static final int NORMAL = 0;
	public static final int BLOCKED = 1;
	
	public Tile(double[] texCoords, int type) {
		this.type = type;
		this.texCoords = texCoords;
	}

	public int getType() { return type; }

	public double[] getTexCoords() {
		return texCoords;
	}
}

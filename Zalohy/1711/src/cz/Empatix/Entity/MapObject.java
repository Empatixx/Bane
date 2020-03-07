package cz.Empatix.Entity;



import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public abstract class MapObject {
	
	// tile stuff
	protected TileMap tileMap;
	private int tileSize;
	protected double xmap;
	protected double ymap;
	
	// position and vector
	protected double x;
	protected double y;
	protected double dx;
	protected double dy;
	
	// dimensions
	protected int width;
	protected int height;
	
	// collision box
	protected int cwidth;
	protected int cheight;
	protected int collisionY;
	
	// collision
	private int currRow;
	private int currCol;
	protected double xdest;
	protected double ydest;
	protected double xtemp;
	protected double ytemp;
	protected boolean topLeft;
	protected boolean topRight;
	protected boolean bottomLeft;
	protected boolean bottomRight;
	
	// animation
	protected Animation animation;
	protected int currentAction;
	protected int previousAction;

	// movement
	protected boolean left;
	protected boolean right;
	protected boolean up;
	protected boolean down;
	protected boolean facingRight;

	// movement attributes
	protected double moveSpeed;
	protected double maxSpeed;
	protected double stopSpeed;

	//
	protected boolean flinching;
	protected long flinchingTimer;
	
	// constructor
	MapObject(TileMap tm) {
		tileMap = tm;
		tileSize = tm.getTileSize(); 
	}
	
	boolean intersects(MapObject o) {
		Rectangle r1 = getRectangle();
		Rectangle r2 = o.getRectangle();
		return r1.intersects(r2);
	}
	
	private Rectangle getRectangle() {
		return new Rectangle(
				(int)x-cwidth/2,
				(int)y-cheight/2 + collisionY,
				cwidth,
				cheight-collisionY
		);
	}
	
	private void calculateCorners(double x, double y) {
		// getting number of tile (row,collumn)

		int leftTile = (int) ((x - cwidth / 2) / tileSize);
		int rightTile = (int) ((x + cwidth / 2 - 1) / tileSize);
		int topTile = (int) ((y - cheight / 2) / tileSize);
		int bottomTile = (int) ((y + cheight / 2 - 1) / tileSize);


		// getting type of tile
		int tl = tileMap.getType(topTile, leftTile);
		int tr = tileMap.getType(topTile, rightTile);
		int bl = tileMap.getType(bottomTile, leftTile);
		int br = tileMap.getType(bottomTile, rightTile);

		// pokud tile má hodnotu 1 - možnost kolize
		topLeft = tl == Tile.BLOCKED;
		topRight = tr == Tile.BLOCKED;
		bottomLeft = bl == Tile.BLOCKED;
		bottomRight = br == Tile.BLOCKED;

	}
	// popis fce v Render.TileMap row: 195
	public boolean isNotOnScrean(){
		return (
				x - width/2 > 960-xmap || x+width/2 < -xmap
				||
				y - height/2 > 540-ymap || y+height/2 < -ymap
		);
	}
	
	public void checkTileMapCollision() {

		currCol = (int)x / tileSize;
		currRow = (int)y / tileSize;
		
		xdest = x + dx;
		ydest = y + dy;
		
		xtemp = x;
		ytemp = y;

		calculateCorners(x, ydest);
		if(dy < 0) {
			if(topLeft || topRight) {
				dy = 0;
				ytemp = currRow * tileSize + cheight / 2;
			}
			else {
				ytemp += dy;
			}
		}
		if(dy > 0) {
			if(bottomLeft || bottomRight) {
				dy = 0;
				ytemp = (currRow + 1) * tileSize - cheight / 2;
			}
			else {
				ytemp += dy;
			}
		}
		
		calculateCorners(xdest, y);
		if(dx < 0) {
			if(topLeft || bottomLeft) {
				dx = 0;
				xtemp = currCol * tileSize + cwidth / 2;
			}
			else {
				xtemp += dx;
			}
		}
		if(dx > 0) {
			if(topRight || bottomRight) {
				dx = 0;
				xtemp = (currCol + 1) * tileSize - cwidth / 2;
			}
			else {
				xtemp += dx;
			}
		}
	}
	
	int getx() { return (int)x; }
	int gety() { return (int)y; }

	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getCWidth() { return cwidth; }
	public int getCHeight() { return cheight; }
	
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public void setVector(double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	public void setMapPosition() {
		xmap = tileMap.getx();
		ymap = tileMap.gety();
	}
	
	void setLeft(boolean b) { left = b; }
	void setRight(boolean b) { right = b; }
	void setUp(boolean b) { up = b; }
	void setDown(boolean b) { down = b; }

	public void draw() {
		// pokud neni object na obrazovce - zrusit
		if (isNotOnScrean()){
			return;
		}
		// blikání - po hitu - hráč
		if (flinching){
			long elapsed = (System.nanoTime() - flinchingTimer) / 1000000;
			if (elapsed / 100 % 2 == 0){
				return;
			}
		}
		if (facingRight) {

			glBindTexture(GL_TEXTURE_2D, animation.getBind());

			glBegin(GL_TRIANGLES);
			// first triangle

			// BOTTOM LEFT
			glTexCoord2i(0, 0);
			glVertex2i((int)(x+xmap-width/2), (int)(y+ymap-height/2));

			// TOP RIGHT
			glTexCoord2i(1,1);
			glVertex2i((int)(x+xmap+width/2), (int)(y+ymap+height/2));

			// TOP LEFT
			glTexCoord2i(0,1);
			glVertex2i((int)(x+xmap-width/2), (int)(y+ymap+height/2));


			// second triangle

			// TOP RIGHT
			glTexCoord2i(1,1);
			glVertex2i((int)(x+xmap+width/2), (int)(y+ymap+height/2));

			// TOP LEFT
			glTexCoord2i(1,0);
			glVertex2i((int)(x+xmap+width/2), (int)(y+ymap-height/2));


			// BOTTOM RIGHT
			glTexCoord2i(0, 0);
			glVertex2i((int)(x+xmap-width/2), (int)(y+ymap-height/2));


			glEnd();

		} else {
			glBindTexture(GL_TEXTURE_2D, animation.getBind());

			glBegin(GL_TRIANGLES);
			// first triangle

			// BOTTOM LEFT
			glTexCoord2i(0, 0);
			glVertex2i((int)(x+xmap-width/2), (int)(y+ymap-height/2));

			// TOP RIGHT
			glTexCoord2i(-1,1);
			glVertex2i((int)(x+xmap+width/2), (int)(y+ymap+height/2));

			// TOP LEFT
			glTexCoord2i(0,1);
			glVertex2i((int)(x+xmap-width/2), (int)(y+ymap+height/2));


			// second triangle

			// TOP RIGHT
			glTexCoord2i(-1,1);
			glVertex2i((int)(x+xmap+width/2), (int)(y+ymap+height/2));

			// TOP LEFT
			glTexCoord2i(-1,0);
			glVertex2i((int)(x+xmap+width/2), (int)(y+ymap-height/2));

			// BOTTOM RIGHT
			glTexCoord2i(0, 0);
			glVertex2i((int)(x+xmap-width/2), (int)(y+ymap-height/2));

			glEnd();
		}
	}
}

















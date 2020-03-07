package Entity;



import Main.Gamepanel;
import Render.Tile;
import Render.TileMap;

import java.awt.*;

public abstract class MapObject {
	
	// tile stuff
	protected TileMap tileMap;
	protected int tileSize;
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
	protected int currRow;
	protected int currCol;
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
	
	public boolean intersects(MapObject o) {
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

		int leftTile = (int)((x - cwidth/2) / tileSize);
		int rightTile = (int)((x + cwidth/2 - 1) / tileSize);
		int topTile = (int)((y - cheight/2) / tileSize);
		int bottomTile = (int)((y + cheight/2 - 1) / tileSize);

		// getting type of tile
		int tl = tileMap.getType(topTile, leftTile);
		int tr = tileMap.getType(topTile, rightTile);
		int bl = tileMap.getType(bottomTile, leftTile);
		int br = tileMap.getType(bottomTile, rightTile);
		
		topLeft = tl == Tile.BLOCKED;
		topRight = tr == Tile.BLOCKED;
		bottomLeft = bl == Tile.BLOCKED;
		bottomRight = br == Tile.BLOCKED;
		
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
	
	public int getx() { return (int)x; }
	public int gety() { return (int)y; }
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
	public void setUp(boolean b) { up = b; }
	void setDown(boolean b) { down = b; }

	public boolean notOnScreen() {
		return x + xmap + width < 0 ||
			x + xmap - width > Gamepanel.WIDTH |
			y + ymap + height < 0 ||
			y + ymap - height > Gamepanel.HEIGHT;
	}
	public void draw(java.awt.Graphics2D g) {
		if (flinching){
			long elapsed = (System.nanoTime() - flinchingTimer) / 1000000;
			if (elapsed / 100 % 2 == 0){
				return;
			}
		}
		g.setColor(Color.black);
		if (facingRight) {
			g.drawImage(
					animation.getImage(),
					(int) (x + xmap - width / 2),
					(int) (y + ymap) - height / 2,
					null
			);
		} else {
			g.drawImage(
					animation.getImage(),
					(int) (x + xmap + width / 2),
					(int) (y + ymap) - height / 2,
					-width,
					height,
					null
			);
		}
		if (Gamepanel.displayCollisions) {
			g.drawRect((int) (x + xmap - cwidth / 2), (int) (y + ymap - cheight / 2 +collisionY) , cwidth, cheight-collisionY);
		}
		g.setColor(Color.red);
		g.drawRect((int) (x + xmap), (int) (y + ymap), 0, 0);
	}
}

















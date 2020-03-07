package cz.Empatix.Entity;



import cz.Empatix.Graphics.Shaders.Shader;
import cz.Empatix.Graphics.Sprites.Spritesheet;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;


import java.awt.*;

import static org.lwjgl.opengl.GL20.*;

public abstract class MapObject {
	
	// tile stuff
	protected final TileMap tileMap;
	protected final int tileSize;
	protected float xmap;
	protected float ymap;
	
	// position and vector
	protected final Vector3f position;
	protected final Vector3f speed;

	// dimensions
	protected int width;
	protected int height;
	// graphics
	protected int spriteSheetCols;
	protected int spriteSheetRows;

	// collision box
	protected int cwidth;
	protected int cheight;

	// collision
	private int currRow;
	private int currCol;
	private final Vector2f dest;
	protected final Vector2f temp;
	private boolean topLeft;
	private boolean topRight;
	private boolean bottomLeft;
	private boolean bottomRight;
	
	// animation
	protected Animation animation;
	protected int currentAction;


	// movement
	protected boolean left;
	protected boolean right;
	protected boolean up;
	protected boolean down;
	protected boolean facingRight;

	// movement attributes
	protected float moveSpeed;
	protected float maxSpeed;
	protected float stopSpeed;

	//
	protected boolean flinching;
	protected long flinchingTimer;
	// 2.0 modern opengl rework
	protected int vboVertexes;
	protected Shader shader;
	protected Spritesheet spritesheet;


	// constructor
	MapObject(TileMap tm) {
		tileMap = tm;
		tileSize = tm.getTileSize();

		temp = new Vector2f(0,0);
		dest = new Vector2f(0,0);
		speed = new Vector3f(0,0,0);
		position = new Vector3f(0,0,0);

	}
	
	boolean intersects(MapObject o) {
		Rectangle r1 = getRectangle();
		Rectangle r2 = o.getRectangle();
		return r1.intersects(r2);
	}

	
	private Rectangle getRectangle() {
		return new Rectangle(
				(int)position.x-cwidth/2,
				(int)position.y-cheight/2,
				cwidth,
				cheight
		);
	}

	/**
	 * Checking for tile collisions
	 * @param x new player X position
	 * @param y new player Y position
	 */
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

		// pokud tile má hodnotu 1 = collision
		topLeft = tl == Tile.BLOCKED;
		topRight = tr == Tile.BLOCKED;
		bottomLeft = bl == Tile.BLOCKED;
		bottomRight = br == Tile.BLOCKED;

	}
	// popis fce v Render.TileMap row: 195

	/**
	 *
	 * @return if MapObject can be shown on screan of monitor
	 */
	private boolean isNotOnScrean(){
		return (
				position.x - width/2 > Game.WIDTH-xmap || position.x+width/2 < -xmap
				||
				position.y - height/2 > Game.HEIGHT-ymap || position.y+height/2 < -ymap
		);
	}

	/**
	 * Calculating destinations and checking if there is any collision
	 */
	public void checkTileMapCollision() {

		currCol = (int)position.x / tileSize;
		currRow = (int)position.y / tileSize;
		
		dest.x = position.x + speed.x;
		dest.y = position.y + speed.y;

		temp.x = position.x;
		temp.y = position.y;

		calculateCorners(position.x, dest.y);
		if(speed.y < 0) {
			if(topLeft || topRight) {
				speed.y = 0;
				temp.y = currRow * tileSize + cheight / 2;
			}
			else {
				temp.y += speed.y;
			}
		}
		if(speed.y > 0) {
			if(bottomLeft || bottomRight) { speed.y
						= 0;
				temp.y = (currRow + 1) * tileSize - cheight / 2;
			}
			else {
				temp.y += speed.y;
			}
		}
		
		calculateCorners(dest.x, position.y);
		if(speed.x < 0) {
			if(topLeft || bottomLeft) {
				speed.x = 0;
				temp.x = currCol * tileSize + cwidth / 2;
			}
			else {
				temp.x += speed.x;
			}
		}
		if(speed.x > 0) {
			if(topRight || bottomRight) {
				speed.x = 0;
				temp.x = (currCol + 1) * tileSize - cwidth / 2;
			}
			else {
				temp.x += speed.x;
			}
		}
	}
	
	int getx() { return (int)position.x; }
	int gety() { return (int)position.y; }

	/**
	 * @param x X location of MapObject
	 * @param y Y location of MapObject
	 */
	public void setPosition(float  x, float y) {
		position.x = x;
		position.y = y;
	}
	/**
	 * Setting vector2 speed of MapObject
	 * @param x speed in direction x
	 * @param y speed in direction y
	 */
	public void setSpeed(float x, float y) {
		this.speed.x = x;
		this.speed.y = y;
	}

	/**
	 * Getting shifts of tilemap
	 */
	public void setMapPosition() {
		xmap = tileMap.getx();
		ymap = tileMap.gety();
	}
	
	void setLeft(boolean b) { left = b; }
	void setRight(boolean b) { right = b; }
	void setUp(boolean b) { up = b; }
	void setDown(boolean b) { down = b; }

	public void draw(Camera camera) {
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
		Matrix4f target;
		if (facingRight) {
			target = new Matrix4f().translate(position)
					.scale(2);
		} else {
			target = new Matrix4f().translate(position)
					.scale(2)
					.rotateY(3.14f);

		}
		camera.projection().mul(target,target);

		shader.bind();
		shader.setUniform("sampler",0);
		shader.setUniform("projection",target);
		glActiveTexture(GL_TEXTURE0);
		spritesheet.bindTexture();

		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);


		glBindBuffer(GL_ARRAY_BUFFER,vboVertexes);
		glVertexAttribPointer(0,2,GL_INT,false,0,0);

		glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());
		glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

		glDrawArrays(GL_QUADS, 0, 4);

		glBindBuffer(GL_ARRAY_BUFFER,0);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);

		shader.unbind();
		glBindTexture(GL_TEXTURE_2D,0);
		if (Game.displayCollisions){
			glColor3i(255,255,255);
			glBegin(GL_LINE_LOOP);
			// BOTTOM LEFT
			glVertex2f(position.x+xmap-cwidth/2,position.y+ymap-cheight/2);
			// TOP LEFT
			glVertex2f(position.x+xmap-cwidth/2, position.y+ymap+cheight/2);
			// TOP RIGHT
			glVertex2f(position.x+xmap+cwidth/2, position.y+ymap+cheight/2);
			// BOTTOM RIGHT
			glVertex2f(position.x+xmap+cwidth/2, position.y+ymap-cheight/2);
			glEnd();

			glPointSize(10);
			glColor3i(255,0,0);
			glBegin(GL_POINTS);
			glVertex2f(position.x+xmap,position.y+ymap);
			glEnd();


		}
	}

}









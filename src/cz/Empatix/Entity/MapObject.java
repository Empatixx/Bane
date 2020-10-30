package cz.Empatix.Entity;


import cz.Empatix.AudioManager.Source;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightPoint;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;

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

	// optimalization for rendering
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
	// 3.0 modern opengl
	protected int vboVertices;
	protected Shader shader;
	protected Spritesheet spritesheet;
	public float scale;

	// audio
	protected Source source;

	// lightning
	public LightPoint light;
	// shadow
	public Spritesheet shadowSprite;
    public int shadowVboVertices;
    public boolean shadow;


	// constructor
	public MapObject(TileMap tm) {
		tileMap = tm;
		tileSize = tm.getTileSize();

		temp = new Vector2f(0,0);
		dest = new Vector2f(0,0);
		speed = new Vector3f(0,0,0);
		position = new Vector3f(0,0,0);
	}
	
	public boolean intersects(MapObject o) {
		Rectangle r1 = getRectangle();
		Rectangle r2 = o.getRectangle();
		return r1.intersects(r2);
	}

	
	public Rectangle getRectangle() {
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
	/**
	 *
	 * @return if MapObject can be shown on screan of monitor
	 */
	public boolean isNotOnScrean(){
		return (
				position.x - width/2 > Camera.getWIDTH()-xmap || position.x+width/2 < -xmap
				||
				position.y - height/2 > Camera.getHEIGHT()-ymap || position.y+height/2 < -ymap
		);
	}

	/**
	 * Calculating destinations and checking if there is any collision
	 */
	protected void checkTileMapCollision() {
		
		dest.x = position.x + speed.x;
		dest.y = position.y + speed.y;

		temp.x = position.x;
		temp.y = position.y;

		calculateCorners(position.x, dest.y);
		if(speed.y < 0) {
			if(topLeft || topRight) {
				speed.y = 0;
				if(tileSize < cheight/2) currRow = ((int)position.y - cheight / 2) / tileSize;
				else currRow = (int)position.y / tileSize;
				temp.y = currRow * tileSize + cheight / 2;
			}
			else {
				temp.y += speed.y;
			}
		}
		if(speed.y > 0) {
			if(bottomLeft || bottomRight) {
				speed.y = 0;
				if(tileSize < cheight/2) currRow = ((int)position.y + cheight / 2 - 1) / tileSize;
				else currRow = (int)position.y / tileSize;
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
				if(tileSize < cwidth/2) currCol = ((int)position.x - cwidth / 2) / tileSize;
				else currCol = (int)position.x / tileSize;

				temp.x = currCol * tileSize + cwidth / 2;
			}
			else {
				temp.x += speed.x;
			}
		}
		if(speed.x > 0) {
			if(topRight || bottomRight) {
				speed.x = 0;
				if(tileSize < cwidth/2) currCol = ((int)position.x + cwidth / 2 - 1) / tileSize;
				else currCol = (int)position.x / tileSize;

				temp.x = (currCol + 1) * tileSize - cwidth / 2;
			}
			else {
				temp.x += speed.x;
			}
		}
	}

	public void checkRoomObjectsCollision(){
		ArrayList<RoomObject> mapObjects = tileMap.getRoomMapObjects();
		boolean[] collisionCheck = new boolean[mapObjects.size()];
		for(int i = 0;i<mapObjects.size();i++){
			RoomObject obj = mapObjects.get(i);
			boolean touchEvent = false;

			boolean y = (int)dest.y()+cheight/2- 1 > (int)obj.getY()-obj.cheight/2 && (int)dest.y()-cheight/2 < (int)obj.getY()+obj.cheight/2- 1;
			boolean x = (int)position.x()-cwidth/2 < (int)obj.getX()+obj.cwidth/2- 1 && (int)position.x()+cwidth/2- 1 > (int)obj.getX()-obj.cwidth/2;

			if(x && y){
				touchEvent = true;
				if (speed.y > 0 && obj.collision) {
					if(obj.moveable){
						obj.setSpeedY(speed.y*obj.getSpeedMoveBoost());
						speed.y -= stopSpeed*2*obj.getSpeedMoveBoost();
						if(speed.y < 0) speed.y = 0;
						temp.y = position.y+speed.y;
						obj.checkTileMapCollision();
						obj.checkRoomObjectsCollision(obj,collisionCheck);
						// if map object is blocked by tile collision - block player by map object collision
						if(obj.speed.y <= speed.y){
							temp.y = obj.getY() - obj.cheight / 2 - cheight / 2+ 1;
						}
					} else {
						speed.y=0;
						temp.y = obj.getY() - obj.cheight / 2 - cheight / 2;
					}
				} else if (speed.y < 0 && obj.collision) {
					if(obj.moveable){
						obj.setSpeedY(speed.y*obj.getSpeedMoveBoost());
						speed.y += stopSpeed*2*obj.getSpeedMoveBoost();
						if(speed.y > 0) speed.y = 0;
						temp.y = position.y+speed.y;
						obj.checkTileMapCollision();
						obj.checkRoomObjectsCollision(obj,collisionCheck);
						// if map object is blocked by tile collision - block player by map object collision
						if(obj.speed.y >= speed.y){
							temp.y = obj.getY() + obj.cheight / 2 + cheight / 2 - 1;
						}
					} else {
						speed.y=0;
						temp.y = obj.getY() + obj.cheight / 2 + cheight / 2 - 1;
					}
				}
			}

			y = (int)position.y()+cheight/2- 1 > (int)obj.getY()-obj.cheight/2 && (int)position.y()-cheight/2 < (int)obj.getY()+obj.cheight/2- 1;
			x = (int)dest.x()-cwidth/2 < (int)obj.getX()+obj.cwidth/2 - 1 && (int)dest.x()+cwidth/2 - 1 > (int)obj.getX()-obj.cwidth/2;

			if (y && x) {

				touchEvent = true;

				if (speed.x > 0 && obj.collision) {
					if(obj.moveable){
						obj.setSpeedX(speed.x*obj.getSpeedMoveBoost());
						speed.x -= stopSpeed*2*obj.getSpeedMoveBoost();
						if(speed.x < 0) speed.x = 0;
						temp.x = position.x+speed.x;
						obj.checkTileMapCollision();
						obj.checkRoomObjectsCollision(obj,collisionCheck);
						// if map object is blocked by tile collision - block player by map object collision

						if(obj.speed.x <= speed.x){
							temp.x=obj.getX()-obj.cwidth/2-cwidth/2+ 1;
						}
					} else {
						speed.x=0;
						temp.x=obj.getX()-obj.cwidth/2-cwidth/2;
					}
				} else if (speed.x < 0 && obj.collision) {
					if(obj.moveable){
						obj.setSpeedX(speed.x*obj.getSpeedMoveBoost());
						speed.x += stopSpeed*2*obj.getSpeedMoveBoost();
						if(speed.x > 0) speed.x = 0;
						temp.x = position.x+speed.x;
						obj.checkTileMapCollision();
						obj.checkRoomObjectsCollision(obj,collisionCheck);
						// if map object is blocked by tile collision - block player by map object collision
						if(obj.speed.x >= speed.x){
							temp.x=obj.getX()+obj.cwidth/2+cwidth/2- 1;
						}
					} else {
						speed.x=0;
						temp.x=obj.getX()+obj.cwidth/2+cwidth/2- 1;
					}
				}
			}

			if(touchEvent){

				obj.touchEvent();
			}
		}
	}
	public void checkRoomObjectsCollision(MapObject previousObject, boolean[] collisionCheck){
		ArrayList<RoomObject> mapObjects = tileMap.getRoomMapObjects();
		for(int i = 0;i<mapObjects.size();i++){
			RoomObject obj = mapObjects.get(i);
			if(previousObject == obj) collisionCheck[i] = true;
			if(collisionCheck[i]) continue;

			boolean y = (int)dest.y()+cheight/2-1 > (int)obj.getY()-obj.cheight/2 && (int)dest.y()-cheight/2 < (int)obj.getY()+obj.cheight/2-1;
			boolean x = (int)position.x()-cwidth/2 < (int)obj.getX()+obj.cwidth/2-1 && (int)position.x()+cwidth/2-1 > (int)obj.getX()-obj.cwidth/2;

			if(x && y){
				if (speed.y > 0 && obj.collision) {
					if(obj.moveable){
						obj.setSpeedY(speed.y);
						if(speed.y < 0) speed.y = 0;
						temp.y = position.y+speed.y;
						obj.checkTileMapCollision();
						obj.checkRoomObjectsCollision(this,collisionCheck);
						// if map object is blocked by tile collision - block player by map object collision
						if(obj.speed.y <= speed.y) {
							temp.y = obj.getY() - obj.cheight / 2 - cheight / 2+ 1;
						}
						if(obj.speed.y == 0){
							speed.y = 0;
						}
					} else {
						speed.y=0;
						temp.y = obj.getY() - obj.cheight / 2 - cheight / 2;
					}
				} else if (speed.y < 0 && obj.collision) {
					if(obj.moveable){
						obj.setSpeedY(speed.y);
						if(speed.y > 0) speed.y = 0;
						temp.y = position.y+speed.y;
						obj.checkTileMapCollision();
						obj.checkRoomObjectsCollision(this,collisionCheck);
						// if map object is blocked by tile collision - block player by map object collision
						if(obj.speed.y >= speed.y) {
							temp.y = obj.getY() + obj.cheight / 2 + cheight / 2- 1;
						}
						if(obj.speed.y == 0){
							speed.y = 0;
						}
					} else {
						speed.y=0;
						temp.y = obj.getY() + obj.cheight / 2 + cheight / 2- 1;
					}
				}
			}

			y = (int)position.y()+cheight/2-1 > (int)obj.getY()-obj.cheight/2 && (int)position.y()-cheight/2 < (int)obj.getY()+obj.cheight/2-1;
			x = (int)dest.x()-cwidth/2 < (int)obj.getX()+obj.cwidth/2-1 && (int)dest.x()+cwidth/2-1 > (int)obj.getX()-obj.cwidth/2;

			if (y && x) {
				if (speed.x > 0 && obj.collision) {
					if(obj.moveable){
						obj.setSpeedX(speed.x);
						if(speed.x < 0) speed.x = 0;
						temp.x = position.x+speed.x;
						obj.checkTileMapCollision();
						obj.checkRoomObjectsCollision(this,collisionCheck);
						// if map object is blocked by tile collision - block player by map object collision

						if(obj.speed.x <= speed.x){
							temp.x=obj.getX()-obj.cwidth/2-cwidth/2+ 1;
						}
						if(obj.speed.x == 0){
							speed.x = 0;
						}
					} else {
						speed.x=0;
						temp.x=obj.getX()-obj.cwidth/2-cwidth/2;
					}
				} else if (speed.x < 0 && obj.collision) {
					if(obj.moveable){
						obj.setSpeedX(speed.x);
						if(speed.x > 0) speed.x = 0;
						temp.x = position.x+speed.x;
						obj.checkTileMapCollision();
						obj.checkRoomObjectsCollision(this,collisionCheck);
						// if map object is blocked by tile collision - block player by map object collision
						if(obj.speed.x >= speed.x){
							temp.x=obj.getX()+obj.cwidth/2+cwidth/2-1;
						}
						if(obj.speed.x == 0){
							speed.x = 0;
						}
					} else {
						speed.x=0;
						temp.x=obj.getX()+obj.cwidth/2+cwidth/2-1;
					}
				}
			}
		}
	}
	public float getX() { return position.x; }
	public float getY() { return position.y; }

	/**
	 * @param x X location of MapObject
	 * @param y Y location of MapObject
	 */
	public void setPosition(float  x, float y) {
		position.x = x;
		position.y = y;
		temp.x = x;
		temp.y = y;
		if(light != null){
			light.update();
		}
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
	public void setSpeedX(float x) {
		this.speed.x = x;
	}
	public void setSpeedY(float y) {
		this.speed.y = y;
	}
	/**
	 * Getting shifts of tilemap
	 */
	public void setMapPosition() {
		xmap = tileMap.getX();
		ymap = tileMap.getY();
	}
	
	void setLeft(boolean b) { left = b; }
	void setRight(boolean b) { right = b; }
	void setUp(boolean b) { up = b; }
	void setDown(boolean b) { down 	= b; }
	public void drawShadow() {
	}
	public void draw() {
		// pokud neni object na obrazovce - zrusit
		if (isNotOnScrean()){
			return;
		}

		// blikání - po hitu - hráč

		if (flinching){
			long elapsed = (System.nanoTime() - flinchingTimer) / 1000000;
			if (elapsed / 100 % 2 == 0){
				shader.unbind();
				glBindTexture(GL_TEXTURE_2D,0);
				glActiveTexture(0);
				return;
			}
		}

        Matrix4f target;
        if (facingRight) {
            target = new Matrix4f().translate(position)
                    .scale(scale);
        } else {
            target = new Matrix4f().translate(position)
                    .scale(scale)
                    .rotateY(3.14f);

        }
        Camera.getInstance().projection().mul(target,target);

        shader.bind();
        shader.setUniformi("sampler",0);
        glActiveTexture(GL_TEXTURE0);
		shader.setUniformm4f("projection",target);

		spritesheet.bindTexture();

		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);


		glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
		glVertexAttribPointer(0,2,GL_INT,false,0,0);


		glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());
		glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

		glDrawArrays(GL_QUADS, 0, 4);

		glBindBuffer(GL_ARRAY_BUFFER,0);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);

		shader.unbind();
		glBindTexture(GL_TEXTURE_2D,0);
		glActiveTexture(0);
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
	public void drawShadow(float scale){
        if (isNotOnScrean()){
            return;
        }
        Vector3f shadowPos = new Vector3f(position.x,position.y+height/2,0);

        Matrix4f shadowMatrixTarget = new Matrix4f().translate(shadowPos).scale(scale);
        Camera.getInstance().projection().mul(shadowMatrixTarget,shadowMatrixTarget);
        shader.bind();
        shader.setUniformi("sampler",0);
        shader.setUniformm4f("projection",shadowMatrixTarget);
        glActiveTexture(GL_TEXTURE0);

        shadowSprite.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, shadowVboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);


        glBindBuffer(GL_ARRAY_BUFFER,shadowSprite.getSprites(0)[0].getVbo());
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);
    }

	public void updateLight(){
		light.setPos(position.x+xmap,position.y+ymap);
	}

	public int getCheight() {
		return cheight;
	}

	public int getCwidth() {
		return cwidth;
	}

	public boolean isFlinching(){
		return flinching;
	}

	public Vector3f getSpeed() {
		return speed;
	}

	public void createShadow(){
		shadow = true;
		shadowSprite = SpritesheetManager.getSpritesheet("Textures\\shadow.tga");
		if(shadowSprite == null){
			shadowSprite = SpritesheetManager.createSpritesheet("Textures\\shadow.tga");
		}
		double[] texCoords =
				{
						0,0,
						0,1,
						1,1,
						1,0
				};
		Sprite[] sprites = new Sprite[]{new Sprite(texCoords)};


		shadowSprite.addSprites(sprites);

		shadowVboVertices = ModelManager.getModel(32,16);
		if (shadowVboVertices == -1){
			shadowVboVertices = ModelManager.createModel(32,16);
		}
	}

	public boolean isMovingRight() {
		return right;
	}

	public boolean isMovingLeft() {
		return left;
	}

	public boolean isMovingDown() {
		return down;
	}

	public boolean isMovingUp() {
		return up;
	}

	public Vector3f getPosition() {
		return position;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}









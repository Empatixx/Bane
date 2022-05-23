package cz.Empatix.Entity;


import cz.Empatix.AudioManager.Source;
import cz.Empatix.Java.Loader;
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
import java.util.concurrent.atomic.AtomicInteger;

import static cz.Empatix.Main.Game.deltaTimeUpdate;
import static org.lwjgl.opengl.GL20.*;

public abstract class MapObject {

	public static void load(){
		Loader.loadImage("Textures\\shadow.tga");
	}
	
	// tile stuff
	protected final TileMap tileMap;
	protected int tileSize;
	protected float xmap;
	protected float ymap;
	
	// position and vector
	protected Vector3f position;
	protected final Vector3f speed;
	protected final Vector2f acceleration;

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
	protected int currRow;
	protected int currCol;
	protected final Vector2f dest;
	protected final Vector2f temp;
	protected boolean topLeft;
	protected boolean topRight;
	protected boolean bottomLeft;
	protected boolean bottomRight;
	
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
	@Deprecated protected float maxSpeed;
	@Deprecated protected float moveSpeed;
	@Deprecated protected float stopSpeed;

	protected int movementVelocity; // distance per second
	protected float moveAcceleration; // percent gain per second
	protected float stopAcceleration; // percent lose per second
	//
	protected boolean flinching;
	protected long flinchingTimer;
	// 2.0 modern opengl
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

    private static final AtomicInteger atomicInteger = new AtomicInteger();
	public int id;

	// constructor
	public MapObject(TileMap tm) {
		tileMap = tm;
		tileSize = tm.getTileSize();

		temp = new Vector2f(0,0);
		dest = new Vector2f(0,0);
		speed = new Vector3f(0,0,0);
		acceleration = new Vector2f(0,0);
		position = new Vector3f(0,0,0);
		if(tileMap.isServerSide()){
			id = atomicInteger.incrementAndGet();
		}
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
	protected void calculateCorners(double x, double y) {
		// getting number of tile (row,collumn)

		int leftTile = (int) ((x - cwidth / 2) / tileSize);
		int rightTile = (int) ((Math.round(x) + cwidth / 2 - 1) / tileSize);
		int topTile = (int) ((y - cheight / 2) / tileSize);
		int bottomTile = (int) ((Math.round(y) + cheight / 2 - 1) / tileSize);


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
				acceleration.y = 0;
				if(tileSize < cheight/2) currRow = (int)(position.y - cheight / 2) / tileSize;
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
				acceleration.y = 0;
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
				acceleration.x = 0;
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
				acceleration.x = 0;
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
		ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
		for(ArrayList<RoomObject> objects : objectsArray) {
			if (objects == null) continue;
			for (RoomObject obj : objects) {
				if(this instanceof RoomObject && this == obj) continue;
				if(!obj.collision && intersects(obj) && this instanceof Player) obj.touchEvent(this);
				if(obj.collision){
					dest.x = position.x + speed.y;
					dest.y = position.y + speed.y;

					boolean xCollision = obj.getX() - obj.getCwidth() / 2 < dest.x + cwidth / 2 - 1
							&&
							obj.getX() + obj.getCwidth() / 2 > dest.x - cwidth / 2 + 1;

					boolean yCollision = obj.getY() - obj.getCheight() / 2f < position.y + cheight / 2 - 1
							&&
							obj.getY() + obj.getCheight() / 2 > position.y - cheight / 2 + 1;

					if(xCollision && yCollision) {
						if (speed.x > 0 && obj.collision) {
							if (obj.moveable) {
								float maxSpeed = obj.getMovementVelocity() * deltaTimeUpdate;
								if (this instanceof Player || this instanceof Enemy) {
									if (speed.x > maxSpeed) {
										acceleration.x = maxSpeed / (movementVelocity * deltaTimeUpdate);
										speed.x = maxSpeed;
									}
									temp.x = position.x + speed.x;

								}
								obj.acceleration.x = 1;
								obj.speed.x = maxSpeed;
								obj.checkTileMapCollision();
								obj.checkRoomObjectsCollision();
								if (obj.speed.x == 0) {
									speed.x = 0;
									acceleration.x = 0;
									temp.x = obj.getX() - obj.cwidth / 2 - cwidth / 2;
								}
							} else {
								speed.x = 0;
								acceleration.x = 0;
								temp.x = obj.getX() - obj.cwidth / 2 - cwidth / 2;
							}
						} else if (speed.x < 0 && obj.collision) {
							if (obj.moveable) {
								float maxSpeed = -obj.getMovementVelocity() * deltaTimeUpdate;
								if (this instanceof Player || this instanceof Enemy) {
									if (speed.x < maxSpeed) {
										acceleration.x = maxSpeed / (movementVelocity * deltaTimeUpdate);
										speed.x = maxSpeed;
									}
									temp.x = position.x + speed.x;
								}
								obj.acceleration.x = -1;
								obj.speed.x = maxSpeed;
								obj.checkTileMapCollision();
								obj.checkRoomObjectsCollision();
								if (obj.speed.x == 0) {
									speed.x = 0;
									acceleration.x = 0;
									temp.x = obj.getX() + obj.cwidth / 2 + cwidth / 2;
								}
							} else {
								speed.x = 0;
								acceleration.x = 0;
								temp.x = obj.getX() + obj.cwidth / 2 + cwidth / 2;
							}
						}
						obj.touchEvent(this);
					}
					xCollision = obj.getX() - obj.getCwidth() / 2 < position.x + cwidth / 2 - 1
							&&
							obj.getX() + obj.getCwidth() / 2 > position.x - cwidth / 2 + 1;

					yCollision = obj.getY() - obj.getCheight() / 2 < dest.y + cheight / 2 - 1
							&&
							obj.getY() + obj.getCheight() / 2 > dest.y - cheight / 2 + 1;
					if(xCollision && yCollision){
						if (speed.y > 0 && obj.collision) {
							if (obj.moveable) {
								float maxSpeed = obj.getMovementVelocity() * deltaTimeUpdate;
								if (this instanceof Player || this instanceof Enemy) {
									if (speed.y > maxSpeed) {
										acceleration.y = maxSpeed / (movementVelocity * deltaTimeUpdate);
										speed.y = maxSpeed;
									}
									temp.y = position.y + speed.y;
								}
								obj.acceleration.y = 1;
								obj.speed.y = maxSpeed;
								obj.checkTileMapCollision();
								obj.checkRoomObjectsCollision();
								if (obj.speed.y == 0) {
									speed.y = 0;
									acceleration.y = 0;
									temp.y = obj.getY() - obj.cheight / 2 - cheight / 2;
								}

							} else {
								speed.y = 0;
								acceleration.y = 0;
								temp.y = obj.getY() - obj.cheight / 2 - cheight / 2;
							}
						} else if (speed.y < 0 && obj.collision) {
							if (obj.moveable) {
								float maxSpeed = -obj.getMovementVelocity() * deltaTimeUpdate;
								if (this instanceof Player || this instanceof Enemy) {
									if (speed.y < maxSpeed) {
										acceleration.y = maxSpeed / (movementVelocity * deltaTimeUpdate);
										speed.y = maxSpeed;
									}
									temp.y = position.y + speed.y;

								}
								obj.acceleration.y = -1;
								obj.speed.y = maxSpeed;
								obj.setSpeedY(speed.y);
								obj.checkTileMapCollision();
								obj.checkRoomObjectsCollision();
								if (obj.speed.y == 0) {
									speed.y = 0;
									acceleration.y = 0;
									temp.y = obj.getY() + obj.cheight / 2 + cheight / 2;
								}
							} else {
								speed.y = 0;
								acceleration.y = 0;
								temp.y = obj.getY() + obj.cheight / 2 + cheight / 2;
							}
						}
						obj.touchEvent(this);
					}
				}
			}
		}
	}

	/*public void checkRoomObjectsCollision(){
		ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
		for(ArrayList<RoomObject> objects : objectsArray) {
			if (objects == null) continue;
			for(RoomObject obj : objects) {
				if (this instanceof RoomObject) {
					if (this == obj) continue;
				}
				if (!obj.collision) {
					if (intersects(obj)) {
						if (this instanceof Player) {
							obj.touchEvent(this);
						}
					}
				} else {
					dest.x = position.x + speed.x;
					dest.y = position.y + speed.y;

					boolean x = obj.getX() - obj.getCwidth() / 2 < dest.x + cwidth / 2 - 1
							&&
							obj.getX() + obj.getCwidth() / 2 > dest.x - cwidth / 2 + 1;

					boolean y = obj.getY() - obj.getCheight() / 2 < position.y + cheight / 2 - 1
							&&
							obj.getY() + obj.getCheight() / 2 > position.y - cheight / 2 + 1;


					if (x && y) {
						if (speed.x > 0 && obj.collision) {
							if (obj.moveable) {
								if (this instanceof Player || this instanceof Enemy) {
									int maxObjSpeed = (int) (obj.getMaxMovement() * maxSpeed);
									if (speed.x > maxObjSpeed) {
										setSpeedX(maxObjSpeed);
									}
									temp.x = position.x + speed.x;

								}
								obj.setSpeedX(speed.x);
								obj.checkTileMapCollision();
								obj.checkRoomObjectsCollision();
								if (obj.speed.x == 0) {
									speed.x = 0;
									temp.x = obj.getX() - obj.cwidth / 2 - cwidth / 2;
								}
							} else {
								speed.x = 0;
								temp.x = obj.getX() - obj.cwidth / 2 - cwidth / 2;
							}
						} else if (speed.x < 0 && obj.collision) {
							if (obj.moveable) {
								if (this instanceof Player || this instanceof Enemy) {
									int maxObjSpeed = (int) (obj.getMaxMovement() * maxSpeed);
									if (speed.x < -maxObjSpeed) {
										setSpeedX(-maxObjSpeed);
									}
									temp.x = position.x + speed.x;
								}
								obj.setSpeedX(speed.x);
								obj.checkTileMapCollision();
								obj.checkRoomObjectsCollision();
								if (obj.speed.x == 0) {
									speed.x = 0;
									temp.x = obj.getX() + obj.cwidth / 2 + cwidth / 2;
								}
							} else {
								speed.x = 0;
								temp.x = obj.getX() + obj.cwidth / 2 + cwidth / 2;
							}
						}
						obj.touchEvent(this);
					}

					x = obj.getX() - obj.getCwidth() / 2 < position.x + cwidth / 2 - 1
							&&
							obj.getX() + obj.getCwidth() / 2 > position.x - cwidth / 2 + 1;

					y = obj.getY() - obj.getCheight() / 2 < dest.y + cheight / 2 - 1
							&&
							obj.getY() + obj.getCheight() / 2 > dest.y - cheight / 2 + 1;

					if (x && y) {
						if (speed.y > 0 && obj.collision) {
							if (obj.moveable) {
								if (this instanceof Player || this instanceof Enemy) {
									int maxObjSpeed = (int) (obj.getMaxMovement() * maxSpeed);
									if (speed.y > maxObjSpeed) {
										setSpeedY(maxObjSpeed);
									}
									temp.y = position.y + speed.y;
								}
								obj.setSpeedY(speed.y);
								obj.checkTileMapCollision();
								obj.checkRoomObjectsCollision();
								if (obj.speed.y == 0) {
									speed.y = 0;
									temp.y = obj.getY() - obj.cheight / 2 - cheight / 2;
								}

							} else {
								speed.y = 0;
								temp.y = obj.getY() - obj.cheight / 2 - cheight / 2;
							}
						} else if (speed.y < 0 && obj.collision) {
							if (obj.moveable) {
								if (this instanceof Player || this instanceof Enemy) {
									int maxObjSpeed = (int) (obj.getMaxMovement() * maxSpeed);
									if (speed.y < -maxObjSpeed) {
										setSpeedY(-maxObjSpeed);
									}
									temp.y = position.y + speed.y;

								}
								obj.setSpeedY(speed.y);
								obj.checkTileMapCollision();
								obj.checkRoomObjectsCollision();
								if (obj.speed.y == 0) {
									speed.y = 0;
									temp.y = obj.getY() + obj.cheight / 2 + cheight / 2;
								}
							} else {
								speed.y = 0;
								temp.y = obj.getY() + obj.cheight / 2 + cheight / 2;
							}
						}
						obj.touchEvent(this);
					}

				}
			}
		}
	}*/
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
			light.setPos(position.x+xmap,position.y+ymap);
		}
	}
	/**
	 * Setting vector2 speed of MapObject
	 * @param x speed in direction x
	 * @param y speed in direction y
	 */
	@Deprecated public void setSpeed(float x, float y) {
		this.speed.x = x;
		this.speed.y = y;
	}
	@Deprecated public void setSpeedX(float x) {
		this.speed.x = x;
	}
	@Deprecated public void setSpeedY(float y) {
		this.speed.y = y;
	}
	public void move(Vector2f acceleration, int velocity){ //TODO: make sum of more velocities, self-velocity + move velocity
		this.acceleration.set(acceleration);
	}
	/**
	 * Getting shifts of tilemap
	 */
	public void setMapPosition() {
		if(tileMap.isServerSide()) return;
		xmap = tileMap.getX();
		ymap = tileMap.getY();
	}
	
	public void setLeft(boolean b) { left = b; }
	public void setRight(boolean b) { right = b; }
	public void setUp(boolean b) { up = b; }
	public void setDown(boolean b) { down 	= b; }
	public void drawShadow() {
	}
	public void draw() {
		setMapPosition();
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
				glActiveTexture(GL_TEXTURE0);

				return;
			}
		}

        Matrix4f target;
        if (facingRight) {
            target = new Matrix4f().translate(position)
                    .scale(scale);
        } else {
            target = new Matrix4f().translate(position)
					.rotateY((float) Math.PI)
                    .scale(scale);

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
		glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

		glDrawArrays(GL_QUADS, 0, 4);

		glBindBuffer(GL_ARRAY_BUFFER,0);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);

		shader.unbind();
		glBindTexture(GL_TEXTURE_2D,0);
		glActiveTexture(GL_TEXTURE0);

		if (Game.displayCollisions){
			glColor3i(255,255,255);
			glBegin(GL_LINE_STRIP);
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
        glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
		glActiveTexture(GL_TEXTURE0);

	}
	public void drawShadow(float scale, int height){
		if (isNotOnScrean()){
			return;
		}
		Vector3f shadowPos = new Vector3f(position.x,position.y+this.scale*height/2,0);

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
		glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

		glDrawArrays(GL_QUADS, 0, 4);

		glBindBuffer(GL_ARRAY_BUFFER,0);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);

		shader.unbind();
		glBindTexture(GL_TEXTURE_2D,0);
		glActiveTexture(GL_TEXTURE0);

	}

	/**
	 * sets location of light binded to this MapObject
	 */
	public void updateLight(){
		light.setPos(position.x+xmap,position.y+ymap);
	}

	/**
	 * sets location of specified light to MapObject location
	 * @param light
	 */
	public void updateLight(LightPoint light){
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

			float[] texCoords =
					{
							0,0,
							0,1,
							1,1,
							1,0
					};
			Sprite[] sprites = new Sprite[]{new Sprite(texCoords)};

			shadowSprite.addSprites(sprites);
		}
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

	public float getScale() {
		return scale;
	}

	public void setFacingRight(boolean facingRight) {
		this.facingRight = facingRight;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public float getTempX(){return temp.x;}
	public float getTempY(){return temp.y;}
	public void getMovementSpeed() {
		float moveSpeed;
		// MAKING CHARACTER MOVE
		if (right){
			acceleration.x += moveAcceleration * deltaTimeUpdate;
			if(acceleration.x > 1f) acceleration.x = 1f;

			moveSpeed = movementVelocity * acceleration.x * deltaTimeUpdate;
			speed.x = moveSpeed;
		}
		else if (left){
			acceleration.x -= moveAcceleration * deltaTimeUpdate;
			if(acceleration.x < -1f) acceleration.x = -1f;

			moveSpeed = movementVelocity * acceleration.x * deltaTimeUpdate;
			speed.x = moveSpeed;
		}
		else {
			if (acceleration.x < 0){
				acceleration.x += stopAcceleration * deltaTimeUpdate;
				moveSpeed = movementVelocity * acceleration.x * deltaTimeUpdate;
				speed.x = moveSpeed;
				if (speed.x > 0){
					acceleration.x = 0;
					speed.x = 0;
				}
			} else if (acceleration.x > 0){
				acceleration.x -= stopAcceleration * deltaTimeUpdate;
				moveSpeed = movementVelocity * acceleration.x * deltaTimeUpdate;
				speed.x = moveSpeed;
				if (speed.x < 0){
					acceleration.x = 0;
					speed.x = 0;
				}
			}
		}

		if (up){
			acceleration.y -= moveAcceleration * deltaTimeUpdate;
			if(acceleration.y < -1f) acceleration.y = -1f;

			moveSpeed = movementVelocity * acceleration.y * deltaTimeUpdate;
			speed.y = moveSpeed;
		}
		else if (down){
			acceleration.y += moveAcceleration * deltaTimeUpdate;
			if(acceleration.y > 1f) acceleration.y = 1f;

			moveSpeed = movementVelocity * acceleration.y * deltaTimeUpdate;
			speed.y = moveSpeed;
		}
		else {
			if (acceleration.y < 0){
				acceleration.y += stopAcceleration * deltaTimeUpdate;
				moveSpeed = movementVelocity * acceleration.y * deltaTimeUpdate;
				speed.y = moveSpeed;
				if (speed.y > 0){
					acceleration.y = 0;
					speed.y = 0;
				}
			} else if (acceleration.y > 0){
				acceleration.y -= stopAcceleration * deltaTimeUpdate;
				moveSpeed = movementVelocity * acceleration.y * deltaTimeUpdate; // delty na casy, postupne zrychlovani pomocí nasobení, čas postupného zrychlení a postupné zpomalení odečítání pak
				speed.y = moveSpeed;
				if (speed.y < 0){
					acceleration.y = 0;
					speed.y = 0;
				}
			}
		}

	}

	public int getMovementVelocity() {
		return movementVelocity;
	}
	public void setMovementVelocity(int movementVelocity) {
		this.movementVelocity = movementVelocity;
	}

	public Vector2f getAcceleration() {
		return acceleration;
	}

	public void setMoveAcceleration(float moveAcceleration) {
		this.moveAcceleration = moveAcceleration;
	}

	public float getMoveAcceleration() {
		return moveAcceleration;
	}
}

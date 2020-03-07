package cz.Empatix.Render;



import cz.Empatix.Main.Game;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class TileMap {
	
	// position
	private double x;
	private double y;
	
	// bounds
	private int xmin;
	private int ymin;
	private int xmax;
	private int ymax;
	
	private double tween;
	
	// map
	private int[][] map;
	private int tileSize;
	private int numRows;
	private int numCols;
	private int width;
	private int height;
	
	// tileset
	private int numTilesAcross;
	private Tile[][] tiles;
	
	// drawing
	private int rowOffset;
	private int colOffset;
	private int numRowsToDraw;
	private int numColsToDraw;

	// rooms
	private ArrayList<Room> roomArrayList;
	private int[][] map

	public TileMap(int tileSize) {
		this.tileSize = tileSize;
		numRowsToDraw = Game.HEIGHT / tileSize + 2;
		numColsToDraw = Game.WIDTH / tileSize + 2;
		tween = 0.07;
	}
	
	public void loadTiles(String s) {
		try {

			BufferedImage tileset = ImageIO.read(
				getClass().getResourceAsStream(s)
			);
			numTilesAcross = tileset.getWidth() / tileSize;
			tiles = new Tile[2][numTilesAcross];
			
			BufferedImage subimage;
			for(int col = 0; col < numTilesAcross; col++) {
				subimage = tileset.getSubimage(
							col * tileSize,
							0,
							tileSize,
							tileSize
						);
				tiles[0][col] = new Tile(subimage, Tile.NORMAL);
				subimage = tileset.getSubimage(
							col * tileSize,
							tileSize,
							tileSize,
							tileSize
						);
				tiles[1][col] = new Tile(subimage, Tile.BLOCKED);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void loadMap() {
		roomArrayList = new ArrayList<>();



		width = numCols * tileSize;
		height = numRows * tileSize;


		xmin = Game.WIDTH - width;
		xmax = 0;
		ymin = Game.HEIGHT - height;
		ymax = 0;
	}
	
	public int getTileSize() { return tileSize; }
	public double getx() { return x; }
	public double gety() { return y; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	
	public int getType(int row, int col) {
		int rc = map[row][col];
		int r = rc / numTilesAcross;
		int c = rc % numTilesAcross;
		// POKUD TILE NEEXISTUJE TAK RETURN BEZ KOLIZE
		if (map[row][col] == -1) return Tile.NORMAL;
		return tiles[r][c].getType();
	}
	
	public void setTween(double d) { tween = d; }
	
	public void setPosition(double x, double y) {
		//System.out.println(this.x);
		//System.out.println((x - this.x) * tween);
		
		this.x += (x - this.x) * tween;
		this.y += (y - this.y) * tween;
		
		//System.out.println(this.x + "\n==========");
		
		fixBounds();
		colOffset = (int)-this.x / tileSize;
		rowOffset = (int)-this.y / tileSize;
		
	}
	
	private void fixBounds() {
		if(x < xmin) x = xmin;
		if(y < ymin) y = ymin;
		if(x > xmax) x = xmax;
		if(y > ymax) y = ymax;
	}
	
	public void draw() {
		for(
			int row = rowOffset;
			row < rowOffset + numRowsToDraw;
			row++) {

			if(row >= numRows) break;

			for(
				int col = colOffset;
				col < colOffset + numColsToDraw;
				col++) {

				if(col >= numCols) break;

				if(map[row][col] == -1) continue;

				int rc = map[row][col];
				int r = rc / numTilesAcross;
				int c = rc % numTilesAcross;

				// CHECKNUTI CI TILE NENI NA OBRAZU CI NENI TAK ZRUSIT
				// collumn - sloupec X , row - radek po Y
				// Posouvá ymax o ymapu ymin+jeden tile o ymin to samé u x
				/*
				 * momentalni vykresleni tilu X > Xmax na obrazovce
				 * col * tileSize > 960-x
				 *
				 * momentalni vykresleni tilu X < Xmin na obrazovce + pocatecni tile
				 * col * tileSize < -(tileSize+x)
				 *
				 * to same u o Y
				 */
				if (	col * tileSize > 960-x || col * tileSize < -(tileSize+x)
						||
						row * tileSize > 540-y || row * tileSize < -(tileSize+y)

				) {
					continue;
				}


				// BINDING TEXTURE
				tiles[r][c].bind();

				glBegin(GL_QUADS);

				glTexCoord2i(0,1);
				glVertex2i((int)x + col * tileSize,(int)y + (row+1) * tileSize);

				glTexCoord2i(1,1);
				glVertex2i((int)x + (col+1) * tileSize,(int)y + (row+1) * tileSize);

				glTexCoord2i(1,0);
				glVertex2i((int)x + (col+1) * tileSize,(int)y + row * tileSize);


				glTexCoord2i(0, 0);
				glVertex2i((int)x + col * tileSize,(int)y + row * tileSize);

				


				glEnd();
			}

		}
		
	}
	
}



/*	public void loadMap() {
		roomArrayList = new ArrayList<>();


		// 0 - down 1 - up 2 - left 3 - right
		int randomDirection;

		int[][] tempMap = new Room(Room.Classic).getRoomMap();

		numCols = 0;
		numRows = 0;

		final int maxRooms = 25;

		for(int i=0;maxRooms > i;i++){

			randomDirection = (int) (3.0 * Math.random());

			Room room = new Room(Room.Classic);
			roomArrayList.add(room);

			if (randomDirection == 0 || randomDirection == 1){
				numRows += room.getNumRows();
				numCols = (tempMap[0].length > room.getNumCols())
						? tempMap[0].length : room.getNumCols();

				tempMap = combineMaps(room,tempMap,randomDirection);
			// Je-li direction 2 nebo 3 (left,right)
			} else {

				numRows = (tempMap.length > room.getNumRows())
						? tempMap.length : room.getNumRows();
				numCols += room.getNumCols();

				tempMap = combineMaps(room,tempMap,randomDirection);
			}
			// DEBUG EXPORT MAP
			System.out.print(i+". "+"================================\n");
			for(int k = 0;tempMap.length > k;k++){
				for(int l =0;tempMap[0].length > l ;l++){
					System.out.print(tempMap[k][l]+" ");

				}
				System.out.print("\n");
			}
			System.out.print("================================\n");

		}

		map = tempMap;

		width = numCols * tileSize;
		height = numRows * tileSize;


		xmin = Game.WIDTH - width;
		xmax = 0;
		ymin = Game.HEIGHT - height;
		ymax = 0;
	}
	private int[][] combineMaps(Room room,int[][] map, int direction){
		int[][] combinedMap = new int[numRows][numCols];

		// room map rows / collumns
		int rmRows = room.getNumRows();
		int rmCols = room.getNumCols();

		// origin map rows / collumns
		int mapRows = map.length;
		int mapCols = map[0].length;

		int[][] roomMap = room.getRoomMap();

		if (direction == 0) {
			for(int row = 0;row < numRows;row++){
				for(int col = 0;col < numCols;col++){
					// pokud je radek na origin mape
					if (row < mapRows) {
						if (col < mapCols){
							combinedMap[row][col] = map[row][col];
						// pokud ma origin mapa mene sloupcu nez room mapa
						} else {
							combinedMap[row][col] = -1;
						}
					// pokud uz je radek na roomampe
					} else{
						if (col < rmCols){
							combinedMap[row][col] = roomMap[row-mapRows][col];
						// pokud ma room mapa mene sloupcu nez origin mapa
						} else {
							combinedMap[row][col] = -1;
						}
					}
				}
			}
		} else if (direction == 1){
			for(int row = 0;row < numRows;row++){
				for(int col = 0;col < numCols;col++){
					// pridani room mapy pred origin mapu
					if (row < rmRows) {
						if (col < rmCols){
							combinedMap[row][col] = roomMap[row][col];
						// pokud ma room mapa mene sloupcu nez origin mapa
						} else {
							combinedMap[row][col] = -1;
						}
					// pokud uz je radek na mape
					} else {
						if (col < rmCols){
							combinedMap[row][col] = map[row-rmRows][col];
							// pokud ma room mapa mene sloupcu nez origin mapa
						} else {
							combinedMap[row][col] = -1;
						}
					}
				}
			}
		} else if (direction == 2){
			for(int row = 0;row < numRows;row++){
				for(int col = 0;col < numCols;col++){
					if (row < rmRows){
						if (col < rmCols){
							combinedMap[row][col] = roomMap[row][col];
						} else {
							combinedMap[row][col] = map[row][col-rmCols];
						}
					} else {
						if (col < rmCols){
							combinedMap[row][col] = -1;
						} else {
							combinedMap[row][col] = map[row-rmRows][col-rmCols];
						}
					}
				}
			}
		} else if (direction == 3){
			for(int row = 0;row < numRows;row++){
				for(int col = 0;col < numCols;col++){
					if (row < rmRows){
						if (col < rmCols){
							combinedMap[row][col] = roomMap[row][col];
						} else {
							combinedMap[row][col] = map[row][col-rmCols];
						}
					} else {
						if (col < rmCols){
							combinedMap[row][col] = -1;
						} else {
							combinedMap[row][col] = map[row-rmRows][col-rmCols];
						}
					}
				}
			}
		}

		return combinedMap;
	}*/














package cz.Empatix.Render;



// knihovny
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Main.Game;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class TileMap {
	
	// position
	private double x;
	private double y;
	
	// bounds
	private double xmin;
	private double ymin;
	private double xmax;
	private double ymax;
	
	private double tween;
	
	// map
	private int[][] map;
	private int tileSize;
	private int numRows;
	private int numCols;
	
	// tileset
	private int numTilesAcross;
	private Tile[][] tiles;
	
	// drawing
	private int rowOffset;
	private int colOffset;
	private int numRowsToDraw;
	private int numColsToDraw;

	// opengl id of texture (tileset)
	private int tilesetId;

	// rooms
	private Room[] roomArrayList;
	private int[][] roomMap;
	private int idGen;

	// room orientation
	private int roomY;
	private int roomX;

	// starting XY room
	private double playerStartX;
	private double playerStartY;

	// gamestate
	private InGame gs;

	public TileMap(int tileSize, InGame gameState) {
		this.tileSize = tileSize;
		numRowsToDraw = Game.HEIGHT / tileSize + 2;
		numColsToDraw = Game.WIDTH / tileSize + 2;

		this.gs = gameState;

	}
	
	public void loadTiles(String s) {
		ByteBufferImage decoder = new ByteBufferImage();
		ByteBuffer tileset = decoder.decodeImage(s);

		tilesetId = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, tilesetId);

		glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, tileset);

		numTilesAcross = decoder.getWidth() / tileSize;
		tiles = new Tile[2][numTilesAcross];
			
		for(int col = 0; col < numTilesAcross; col++) {
			float[] texCoords =
					{
							(float)col/numTilesAcross,0,
							(float)col/numTilesAcross,1f/2,
							(col+1f)/numTilesAcross,1f/2,
							(col+1f)/numTilesAcross,0
					};
			tiles[0][col] = new Tile(texCoords, Tile.NORMAL);
			float[] texCoordsCol =
					{
							(float)col/numTilesAcross,1f/2,
							(float)col/numTilesAcross,1,
							(col+1f)/numTilesAcross,1,
							(col+1f)/numTilesAcross,1f/2
					};
			tiles[1][col] = new Tile(texCoordsCol, Tile.BLOCKED);
		}
		STBImage.stbi_image_free(tileset);
		
	}
	private int getIdGen(){
		idGen++;
		return idGen;
	}
	public void loadMap() {
		// room generation
		generateRooms();

		// cutting map (removing useless xy where is not anything)
		decreaseSizeOfMap();

		// converting rooms into 1 big tile map
		formatMap();

		for (int x = 0;x < numRows;x++){
			for(int y = 0;y < numCols;y++){
				System.out.print(map[x][y]+" ");
			}
			System.out.print("\n");
		}
	}

	public void updateCurrentRoom(int x, int y){
		for (Room room : roomArrayList){
			// getting X max/min of room
			int xMax = room.getxMax();
			int xMin = room.getxMin();

			// getting Y max/min of room
			int yMax = room.getyMax();
			int yMin = room.getyMin();

			if (x > xMin && x < xMax){
				if (y > yMin && y < yMax){

					// CORNERS OF MAP ( ROOM ) + tween to make it more sync (plus max = min; min = max) bcs of x / y of tilemap is negative

					xmin += (Game.WIDTH - xMax - xmin) * tween;
					xmax += -(xMin + xmax) * tween;

					ymin += (Game.HEIGHT - yMax - ymin) * tween;
					ymax += -(+yMin + ymax) * tween;

					if (!room.hasEntered()){
						// event trigger on entering a new room
						room.entered();
					}
					break;
				}
			}
		}
	}

	private void generateRooms(){
		// id generator
		idGen = 0;

		int maxRooms = 8;
		int currentRooms = 0;

		// boolean loop
		boolean mapCompleted = false;

		// room xy map
		roomMap = new int[30][30];

		//arraylist filled with rooms
		roomArrayList = new Room[maxRooms];

		// just random
		Random rnd = new Random();

		while(!mapCompleted){
			if (currentRooms == 0){
				// starting room
				int id = getIdGen();

				Room mistnost = new Room(Room.Classic,id,15,15, gs);

				roomArrayList[currentRooms] = mistnost;
				roomMap[15][15] = id;

				// counting how many rooms are already created.
				currentRooms++;
			} else {
				int newRooms = 0;
				// looping each rooms
				for(int i = 0;i < currentRooms;i++){

					// get number of paths currently coming from that room
					int paths = roomArrayList[i].countPaths();

					float chance = rnd.nextFloat();

					// Creates a new path from old to new room, less chance for rooms with more paths
					if (chance > 0.2+0.2*paths){

						int roomX = roomArrayList[i].getX();
						int roomY = roomArrayList[i].getY();


						// checking each side if there's already room
						boolean[] blockedPaths = new boolean[4];//roomArrayList[i].getPaths();
						blockedPaths[0] = roomMap[roomY-1][roomX] != 0;
						blockedPaths[1] = roomMap[roomY+1][roomX] != 0;
						blockedPaths[2] = roomMap[roomY][roomX-1] != 0;
						blockedPaths[3] = roomMap[roomY][roomX+1] != 0;


						// random between <0,4)
						int rndDirection = rnd.nextInt(4);

						// Checking if room haven't that path yet
						while(blockedPaths[rndDirection]){
							rndDirection = rnd.nextInt(4);
						}


						// TOP Direction
						if (rndDirection == 0){
							int id = getIdGen();

							Room mistnost = new Room(Room.Classic,id,roomX,roomY-1,gs);

							roomArrayList[i].setTop(true);
							mistnost.setBottom(true);

							roomArrayList[currentRooms+newRooms] = mistnost;
							roomMap[roomY-1][roomX] = id;
						}
						// BOTTOM Direction
						else if (rndDirection == 1){
							int id = getIdGen();

							Room mistnost = new Room(Room.Classic,id,roomX,roomY+1, gs);

							roomArrayList[i].setBottom(true);
							mistnost.setTop(true);

							roomArrayList[currentRooms+newRooms] = mistnost;
							roomMap[roomY+1][roomX] = id;
						}
						// LEFT Direction
						else if (rndDirection == 2){
							int id = getIdGen();

							Room mistnost = new Room(Room.Classic,id,roomX-1,roomY, gs);

							mistnost.setRight(true);
							roomArrayList[i].setLeft(true);

							roomArrayList[currentRooms+newRooms] = mistnost;
							roomMap[roomY][roomX-1] = id;
						}
						// RIGHT Direction
						else {
							int id = getIdGen();

							Room mistnost = new Room(Room.Classic,id,roomX+1,roomY, gs);

							mistnost.setLeft(true);
							roomArrayList[i].setRight(true);

							roomArrayList[currentRooms+newRooms] = mistnost;
							roomMap[roomY][roomX+1] = id;
						}
						newRooms++;


					}
					if (currentRooms+newRooms == maxRooms){
						// random generator of map is done
						mapCompleted = true;
						break;
					}
				}
				currentRooms += newRooms;
			}
		}
	}


	private void formatMap() {
		// max collumns in collumn of room
		int[] maxCols;
		maxCols = new int[roomX];

		for(int loop = 0;loop < 4;loop++) {
			// load every room maps
			if (loop == 0){
				for(Room room : roomArrayList){
					room.loadMap();
				}
			}
			// getting max collumns in collumn of room
			if (loop == 1){
				for (int x = 0; x < roomX; x++) {
					for (int y = 0; y < roomY; y++) {

						// getting room of xy room map
						Room mistnost = getRoom(roomMap[y][x]);

						// room must be created there
						if (mistnost != null) {
							int cols = mistnost.getNumCols();
							if (maxCols[x] < cols){
								maxCols[x] = cols;
							}
						}
					}
				}
			}
			// finding maxCols and maxRows of final tile map
			else if (loop == 2) {

				for (int y = 0; y < roomY; y++) {

					// getting maxCols+Rows of Room
					int previousMaxRows = 0;
					int previousMaxCols = 0;


					for (int x = 0; x < roomX; x++) {
						// getting room of xy room map
						Room mistnost = getRoom(roomMap[y][x]);

						// if there is not any room
						if (mistnost == null) {
							int cols = maxCols[x];
							previousMaxCols += cols;

						// if there is room
						} else {
							int cols = mistnost.getNumCols();
							int rows = mistnost.getNumRows();

							if (previousMaxRows < rows) {
								previousMaxRows = rows;
							}
							previousMaxCols += cols;
						}
					}
					numRows += previousMaxRows;

					if (previousMaxCols > numCols) {
						numCols = previousMaxCols;
					}
					System.out.print("Row of room: "+y+" R:"+numRows+" S:"+numCols+"\n");
				}
				System.out.print("RADKY/SLOUPCE "+numRows+" "+numCols+"\n");
			}
			else if (loop == 3){
				// posuny behem loopu
				int nextShiftRows;
				int shiftRows = 0;
				int shiftCols;

				// final tilemap
				map = new int[numRows][numCols];

				//fill array all indexes with -1
				for (int x2 = 0; x2 < numRows;x2++){
					for (int y2 = 0; y2 < numCols;y2++){
						map[x2][y2] = -1;
					}
				}

				for (int y = 0; y < roomY; y++) {

					shiftCols = 0;
					nextShiftRows = 0;

					for (int x = 0; x < roomX; x++) {
						System.out.print("XY: "+x+" "+y+"\n");
						System.out.print("SHIFTS XY: "+shiftCols+" "+shiftRows+"\n");

						Room mistnost = getRoom(roomMap[y][x]);
						// POKUD ZDE ZADNA MISTNOST NENI
						if (mistnost == null){

							// getting max cols of current collumn (roomX) of rooms
							int cols = maxCols[x];
							shiftCols+=cols;
							System.out.print("NEXT SHIFTS XY: "+shiftCols+" "+shiftRows+"\n");
						} else {

							int cols = mistnost.getNumCols();
							int rows = mistnost.getNumRows();
							System.out.print("NEXT SHIFTS XY: "+(shiftCols+cols)+" "+(shiftRows+rows)+"\n");

							if (nextShiftRows < rows){
								nextShiftRows = rows;
							}

							int[][] roomMap = mistnost.getRoomMap();

							for(int xtile = 0;xtile < rows;xtile++){

								for(int ytile = 0;ytile < cols;ytile++){

									map[xtile+shiftRows][ytile+shiftCols] = roomMap[xtile][ytile];
								}
							}

							int xMin = shiftCols*tileSize;
							int xMax = (shiftCols+cols)*tileSize;
							int yMin = shiftRows*tileSize;
							int yMax = (shiftRows+rows)*tileSize;

							//Setting corners of room
							mistnost.setCorners(
									xMin,
									xMax,
									yMin,
									yMax
							);

							// starting room
							if (mistnost.getId() == 1){

								playerStartX = xMin+(double)(xMax-xMin)/2;
								playerStartY = yMin+(double)(yMax-yMin)/2;

							}


							shiftCols+=cols;
						}
					}
					shiftRows+=nextShiftRows;
				}
			}
		}

	}
	private Room getRoom(int id){
		for(Room room : roomArrayList){
			if (room.getId() == id){
				return room;
			}
		}
		return null;
	}



	private void decreaseSizeOfMap(){
		for(int j = 0;j < 30;j++){
			for (int i = 0;i < 30;i++){
				System.out.print(roomMap[j][i]+" ");
			}
			System.out.print("\n");
		}
		// zkracovani mapy
		roomX = 30;
		roomY = 30;
		int right = 0;
		int left = 0;
		int top = 0;
		int bottom = 0;
		// radky od shora dolu
		A: for (int i = 0;i < 30;i++){
			for (int j = 0;j < 30;j++){
				if (roomMap[i][j] != 0){
					break A;
				}
			}
			top++;
		}
		// radky od dola nahoru
		A: for (int i = 29;i >= 0;i--){
			for (int j = 29;j >= 0;j--){
				if (roomMap[i][j] != 0){
					break A;
				}
			}
			bottom++;
		}
		// sloupce od leva do prava
		A: for (int i = 0;i < 30;i++){
			for (int j = 0;j < 30;j++){
				if (roomMap[j][i] != 0){
					break A;
				}
			}
			left++;
		}
		// sloupce od prava do leva
		A: for (int i = 29;i >= 0;i--){
			for (int j = 29;j >= 0;j--){
				if (roomMap[j][i] != 0){
					break A;
				}
			}
			right++;
		}

		roomY -=bottom+top;
		roomX -=left+right;

		int[][] newRoomMap = new int[roomY][roomX];
		for (int i = 0; i < roomY; i++){
			for (int j = 0; j < roomX; j++){
				newRoomMap[i][j] = roomMap[top+i][left+j];
			}
		}

		roomMap = newRoomMap;

		for(int j = 0; j < roomY; j++){
			for (int i = 0; i < roomX; i++){
				System.out.print(roomMap[j][i]+" ");
			}
			System.out.print("\n");
		}
	}




	public int getTileSize() { return tileSize; }
	public double getx() { return x; }
	public double gety() { return y; }

	/**
	 * rc = number of tile in tileset
	 * r (row in tileset); first row is normal : second row is blocked (collision)
	 * c (collumns in tileset)
	 */
	public int getType(int row, int col) {
		int rc = map[row][col];

		int r = rc / numTilesAcross;
		int c = rc % numTilesAcross;

		// If tile doesn't exist - return that there is not any collision
		if (map[row][col] == -1) return Tile.NORMAL;
		return tiles[r][c].getType();
	}
	
	public void setTween(double d) { tween = d; }
	
	public void setPosition(double x, double y) {

		this.x += (x - this.x) * tween;
		this.y += (y - this.y) * tween;

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
				 * to same o Y
				 */
				if (	col * tileSize > 960-x || col * tileSize < -(tileSize+x)
						||
						row * tileSize > 540-y || row * tileSize < -(tileSize+y)

				) {
					continue;
				}


				// BINDING TEXTURE
				glBindTexture(GL_TEXTURE_2D,tilesetId);
				float[] texCoords = tiles[r][c].getTexCoords();

				glBegin(GL_QUADS);

				// BOTTOM LEFT
				glTexCoord2f(texCoords[0], texCoords[1]);
				glVertex2i((int)x + col * tileSize,(int)y + row * tileSize);

				glTexCoord2f(texCoords[2],texCoords[3]);
				// TOP LEFT
				glVertex2i((int)x + col * tileSize,(int)y + (row+1) * tileSize);

				// TOP RIGHT
				glTexCoord2f(texCoords[4],texCoords[5]);
				glVertex2i((int)x + (col+1) * tileSize,(int)y + (row+1) * tileSize);

				// BOTTOM RIGHT
				glTexCoord2f(texCoords[6],texCoords[7]);
				glVertex2i((int)x + (col+1) * tileSize,(int)y + row * tileSize);
				


				glEnd();
			}

		}

	}

	public double getPlayerStartX() { return playerStartX; }

	public double getPlayerStartY(){ return playerStartY; }

	public int getNumRows() { return numRows; }

	public int getNumCols() { return numCols; }
}










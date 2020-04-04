package cz.Empatix.Render;



import cz.Empatix.Graphics.ByteBufferImage;
import cz.Empatix.Graphics.Shaders.Shader;
import cz.Empatix.Graphics.Shaders.ShaderManager;
import cz.Empatix.Java.Random;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL43.*;

public class TileMap {
	// position
	private final Vector3f position;
	private final Camera camera;
	private Shader shader;



	// bounds
	private float xmin;
	private float ymin;
	private float xmax;
	private float ymax;
	
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
	private final int numRowsToDraw;
	private final int numColsToDraw;

	// opengl id of texture (tileset)
	private int tilesetId;
	private int vboVertexes;
	//matrix4f opengl
	private Matrix4f target;

	// rooms
	private Room[] roomArrayList;
	private Room currentRoom;
	private int[][] roomMap;
	private int idGen;

	// room orientation
	private int roomY;
	private int roomX;

	// starting XY room
	private float playerStartX;
	private float playerStartY;


	public TileMap(int tileSize, Camera camera) {
		this.tileSize = tileSize;
		// 2x scale
		numRowsToDraw = Camera.getHEIGHT() / (tileSize*2) + 2;
		numColsToDraw = Camera.getWIDTH() / (tileSize*2) + 2;

		position = new Vector3f(0,0,0);

		this.camera = camera;
		tween = 1;

		target = new Matrix4f();

	}

	public int getNumRows() {
		return numRows;
	}
	public int getNumCols(){
		return numCols;
	}

	public void loadTiles(String s) {
		ByteBufferImage decoder = new ByteBufferImage();
		ByteBuffer tileset = decoder.decodeImage(s);

		tilesetId = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, tilesetId);

		glTexStorage2D(GL_TEXTURE_2D, 5, GL_RGBA8, decoder.getWidth(), decoder.getHeight());
		glTexSubImage2D(GL_TEXTURE_2D,0,0,0,decoder.getWidth(),decoder.getHeight(),GL_RGBA,GL_UNSIGNED_BYTE,tileset);
		glGenerateMipmap(GL_TEXTURE_2D);  //Generate num_mipmaps number of mipmaps here.

		//glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, tileset);

		glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);


		numTilesAcross = decoder.getWidth() / tileSize;
		tiles = new Tile[2][numTilesAcross];
			
		for(int col = 0; col < numTilesAcross; col++) {
			double[] texCoords =
					{
							(double)col/numTilesAcross,0,
							(double)col/numTilesAcross,1.0/2,
							(col+1.0)/numTilesAcross,1.0/2,
							(col+1.0)/numTilesAcross,0
					};
			tiles[0][col] = new Tile(texCoords, Tile.NORMAL);
			double[] texCoordsCol =
					{
							(double)col/numTilesAcross,1.0/2,
							(double)col/numTilesAcross,1,
							(col+1.0)/numTilesAcross,1,
							(col+1.0)/numTilesAcross,1.0/2
					};
			tiles[1][col] = new Tile(texCoordsCol, Tile.BLOCKED);
		}
		STBImage.stbi_image_free(tileset);

        int[] vertices =
                {
                        0,0, // BOTTOM LEFT
						0,tileSize, // BOTTOM TOP
                        tileSize,tileSize, // RIGHT TOP
                        tileSize,0 // BOTTOM RIGHT



                };

        IntBuffer buffer = BufferUtils.createIntBuffer(vertices.length);
        buffer.put(vertices);
        buffer.flip();

        vboVertexes = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboVertexes);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);


		shader = ShaderManager.getShader("shaders\\shader");
		if (shader == null){
			shader = ShaderManager.createShader("shaders\\shader");
		}

        // because we are scaling image by 2x we must increase size of tileSize
        tileSize *=2;

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

		for(int i = 0;i<numRows;i++){
			for(int j = 0;j<numCols;j++){
				System.out.print(map[i][j]+" ");
			}
			System.out.print("\n");
		}
		System.out.println("\n");

		// converting 1 and 0 into tiles id textures
		autoTile();

		// getting XY max/min
		for (Room room : roomArrayList){
			// getting X max/min of room
			int xMax = room.getxMax();
			int xMin = room.getxMin();

			// getting Y max/min of room
			int yMax = room.getyMax();
			int yMin = room.getyMin();

			if (playerStartX > xMin && playerStartX < xMax){
				if (playerStartY > yMin && playerStartY < yMax){

					// CORNERS OF MAP ( ROOM ) + tween to make it more sync (plus max = min; min = max) bcs of x / y of tilemap is negative

					xmin += Camera.getWIDTH() - xMax - xmin;
					xmax += -xMin - xmax;

					ymin += Camera.getHEIGHT() - yMax - ymin;
					ymax += -yMin - ymax;

					break;
				}
			}

		}
		setPosition(Camera.getWIDTH() / 2f - playerStartX,Camera.getHEIGHT() / 2f - playerStartY);


	}

	public void updateCurrentRoom(int x, int y){
		boolean foundRoom = false;
		for (RoomPath room : currentRoom.getRoomPaths()){
			if(room == null) continue;
			// getting X max/min of room
			int xMax = room.getRealXMax();
			int xMin = room.getRealXMin();
			// getting Y max/min of room
			int yMax = room.getRealYMax();
			int yMin = room.getRealYMin();

			if (x > xMin && x < xMax){
				if (y > yMin && y < yMax){
					xMax = room.getxMax();
					xMin = room.getxMin();

					// getting Y max/min of room
					yMax = room.getyMax();
					yMin = room.getyMin();

					// CORNERS OF MAP ( ROOM ) + tween to make it more sync (plus max = min; min = max) bcs of x / y of tilemap is negative

					xmin += (Camera.getWIDTH() - xMax - xmin) * tween;
					xmax += (-xMin - xmax) * tween;

					ymin += (Camera.getHEIGHT() - yMax - ymin) * tween;
					ymax += (-yMin - ymax) * tween;
					//foundRoom = true;
					break;
				}
			}
		}

		if(!foundRoom) {
			for (Room room : roomArrayList) {
				// getting X max/min of room
				int xMax = room.getxMax();
				int xMin = room.getxMin();

				// getting Y max/min of room
				int yMax = room.getyMax();
				int yMin = room.getyMin();

				if (x > xMin && x < xMax) {
					if (y > yMin && y < yMax) {

						// CORNERS OF MAP ( ROOM ) + tween to make it more sync (plus max = min; min = max) bcs of x / y of tilemap is negative

						xmin += (Camera.getWIDTH() - xMax - xmin) * tween;
						xmax += (-xMin - xmax) * tween;

						ymin += (Camera.getHEIGHT() - yMax - ymin) * tween;
						ymax += (-yMin - ymax) * tween;

						if (!room.hasEntered()) {
							// event trigger on entering a new room
							room.entered();
						}
						currentRoom = room;
						break;
					}
				}
			}
		}
	}

	private void generateRooms(){
		// id generator
		idGen = 0;

		int maxRooms = 7;
		int currentRooms = 0;

		// boolean loop
		boolean mapCompleted = false;

		// room xy map
		roomY = roomX = 20;
		roomMap = new int[roomX][roomY];


		//arraylist filled with rooms
		roomArrayList = new Room[maxRooms];

		while(!mapCompleted){
			if (currentRooms == 0){
				// starting room
				int id = getIdGen();

				int x = roomX/2;
				int y = roomY/2;

				Room mistnost = new Room(Room.Classic,id,x,y,tileSize);

				roomArrayList[currentRooms] = mistnost;
				roomMap[y][x] = id;


				// counting how many rooms are already created.
				currentRooms++;
			} else {
				int newRooms = 0;
				// looping each rooms
				for(int i = 0;i < currentRooms;i++){

					// get number of paths currently coming from that room
					//int paths = roomArrayList[i].countPaths();

					int roomX = roomArrayList[i].getX();
					int roomY = roomArrayList[i].getY();
					boolean[] blockedPaths = new boolean[4];//roomArrayList[i].getPaths();
					blockedPaths[0] = roomMap[roomY-1][roomX] != 0;
					blockedPaths[1] = roomMap[roomY+1][roomX] != 0;
					blockedPaths[2] = roomMap[roomY][roomX-1] != 0;
					blockedPaths[3] = roomMap[roomY][roomX+1] != 0;

					int paths = 0;
					// get number of paths currently coming from that room
					for(boolean path:blockedPaths){
						if(path) paths++;
					}

					double chance = Random.nextDouble();

					// Creates a new path from old to new room, less chance for rooms with more paths
					if (chance > 0.2+0.2*paths){
						// random between <0,4)
						int rndDirection = Random.nextInt(4);

						// Checking if room haven't that path yet
						while(blockedPaths[rndDirection]){
							rndDirection = Random.nextInt(4);
						}


						// TOP Direction
						if (rndDirection == 0){
							int id = getIdGen();

							Room mistnost = new Room(Room.Classic,id,roomX,roomY-1,tileSize);

							roomArrayList[i].setTop(true);
							mistnost.setBottom(true);

							roomArrayList[currentRooms+newRooms] = mistnost;
							roomMap[roomY-1][roomX] = id;
						}
						// BOTTOM Direction
						else if (rndDirection == 1){
							int id = getIdGen();

							Room mistnost = new Room(Room.Classic,id,roomX,roomY+1,tileSize);

							roomArrayList[i].setBottom(true);
							mistnost.setTop(true);

							roomArrayList[currentRooms+newRooms] = mistnost;
							roomMap[roomY+1][roomX] = id;
						}
						// LEFT Direction
						else if (rndDirection == 2){
							int id = getIdGen();

							Room mistnost = new Room(Room.Classic,id,roomX-1,roomY,tileSize);

							mistnost.setRight(true);
							roomArrayList[i].setLeft(true);

							roomArrayList[currentRooms+newRooms] = mistnost;
							roomMap[roomY][roomX-1] = id;
						}
						// RIGHT Direction
						else {
							int id = getIdGen();

							Room mistnost = new Room(Room.Classic,id,roomX+1,roomY,tileSize);

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
		int[] maxCols = new int[roomX];

		// paths informations
		int[] shiftsCols = new int[roomX*roomY];

		for(int loop = 0;loop < 6;loop++) {
			// load every room maps
			if (loop == 0){
				for(Room room : roomArrayList){
					room.loadMap();
				}
			}
				// getting max collumns in collumn of room
			else if (loop == 1){
				for (int x = 0; x < roomX; x++) {
					for (int y = 0; y < roomY; y++) {

						// getting room of xy room map
						Room mistnost = getRoom(roomMap[y][x]);

						// room must be created there
						if (mistnost != null) {
							int cols = mistnost.getNumCols();

							cols+=2;

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

							cols+=2;




							if (previousMaxRows < rows) {
								previousMaxRows = rows;
							}
							previousMaxCols += cols;
						}
					}
					numRows += previousMaxRows;

					numRows+=2;

					if (previousMaxCols > numCols) {
						numCols = previousMaxCols;
					}
				}
			} else if (loop == 3) {
				// posuny behem loopu
				int nextShiftRows;
				int shiftRows = 0;
				int shiftCols;

				boolean bottom;
				boolean right;

				// final tilemap
				map = new int[numRows][numCols];

				//fill array all indexes with 1
				for (int x2 = 0; x2 < numRows; x2++) {
					for (int y2 = 0; y2 < numCols; y2++) {
						map[x2][y2] = 1;
					}
				}

				for (int y = 0; y < roomY; y++) {

					// bottom if we should offset all rooms on new row of roomMap by 2 rows
					bottom = false;
					right = false;
					shiftCols = 0;
					nextShiftRows = 0;

					for (int x = 0; x < roomX; x++) {

						Room mistnost = getRoom(roomMap[y][x]);
						// POKUD ZDE ZADNA MISTNOST NENI
						if (mistnost == null) {

							// getting max cols of current collumn (roomX) of rooms
							int cols = maxCols[x];
							shiftCols += cols;
						} else {

							int cols = mistnost.getNumCols();
							int rows = mistnost.getNumRows();

							if (!bottom) {
								if (mistnost.isBottom()) bottom = true;
							}

							if (right) {
								shiftCols += 2;

							} else {
								right = true;
							}


							int[][] roomMap = mistnost.getRoomMap();

							shiftsCols[y*roomX+x] = shiftCols;

							for (int xtile = 0; xtile < rows; xtile++) {

								if (cols >= 0)
									System.arraycopy(roomMap[xtile], 0, map[xtile + shiftRows], shiftCols, cols);
							}

							int xMin = shiftCols * tileSize;
							int xMax = (shiftCols + cols) * tileSize;
							int yMin = shiftRows * tileSize;
							int yMax = (shiftRows + rows) * tileSize;

							//Setting corners of room
							mistnost.setCorners(
									xMin,
									xMax,
									yMin,
									yMax
							);

							// starting room
							if (mistnost.getId() == 1) {

								playerStartX = xMin + (float) (xMax - xMin) / 2;
								playerStartY = yMin + (float) (yMax - yMin) / 2;

								currentRoom = mistnost;
							}

							if (nextShiftRows < rows) {
								nextShiftRows = rows;
							}

							shiftCols += cols;
						}
					}
					shiftRows += nextShiftRows;
					if (bottom) shiftRows += 2;
				}
			} else if (loop == 4) {
				// posuny behem loopu
				int nextShiftRows;
				int shiftRows = 0;
				int shiftCols;

				for (int y = 0; y < roomY; y++) {

					shiftCols = 0;
					nextShiftRows = 0;

					for (int x = 0; x < roomX; x++) {

						Room mistnost = getRoom(roomMap[y][x]);
						// POKUD ZDE ZADNA MISTNOST NENI
						if (mistnost == null) {

							// getting max cols of current collumn (roomX) of rooms
							int cols = maxCols[x];
							shiftCols += cols;
						} else {

							int cols = mistnost.getNumCols();
							int rows = mistnost.getNumRows();


							Room bottomRoom = null;
							if(roomY > y+1) bottomRoom = getRoom(roomMap[y+1][x]);

							// bottom direction
							if(bottomRoom != null && mistnost.isBottom()){
								int paths = 0;
								// if bottom room cols starts on another cols
								int fixX = shiftsCols[(y+1)*roomX+x]-shiftCols > 0 ? shiftsCols[(y+1)*roomX+x]-shiftCols : 0;
								int fixXMax = cols-bottomRoom.getNumCols()-(shiftsCols[(y+1)*roomX+x]-shiftCols)> 0 ? cols-bottomRoom.getNumCols()-(shiftsCols[(y+1)*roomX+x]-shiftCols) : 0;
								B:for(int j = 1+shiftCols+fixX+(cols-fixXMax-2-fixX)/2;j<cols+shiftCols-1-fixXMax;j++){
									for(int i = rows-1+shiftRows;i<numRows;i++){
								 		if(paths >= 2) break B;
										if(map[i][j] == Tile.NORMAL) break;
										map[i][j] = Tile.NORMAL;
									}
									 paths++;
								}
								RoomPath bottomPath = new RoomPath();

								bottomPath.setCorners(
										mistnost.getxMin(),
										mistnost.getxMax(),
										mistnost.getyMin(),
										bottomRoom.getyMax()
								);

								bottomPath.setRealCorners(
										mistnost.getxMin(),
										mistnost.getxMax(),
										mistnost.getyMax()-tileSize,
										bottomRoom.getyMin()+tileSize
								);

								mistnost.setBottomRoomPath(bottomPath);
								bottomRoom.setTopRoomPath(bottomPath);


							}
							// right direction
							Room sideRoom = null;
							if(roomX > x+1) sideRoom = getRoom(roomMap[y][x+1]);

							if(sideRoom != null && mistnost.isRight()) {
								int paths = 0;
								B:for (int j = 1 + shiftRows + (sideRoom.getNumRows()) / 2 - 2; j < sideRoom.getNumRows() - 1 + shiftRows; j++) {
									for (int i = cols - 1 + shiftCols; i < numCols; i++) {
										if (paths >= 2) break B;
										if (map[j][i] == Tile.NORMAL) {
											break;
										}
										map[j][i] = Tile.NORMAL;
									}
									paths++;
								}
								RoomPath sidePath = new RoomPath();

								sidePath.setCorners(
										mistnost.getxMin(),
										sideRoom.getxMax(),
										mistnost.getyMin(),
										mistnost.getyMax()
								);

								sidePath.setRealCorners(
										mistnost.getxMax()-tileSize,
										sideRoom.getxMin()+tileSize,
										mistnost.getyMin(),
										mistnost.getyMax()
								);

								mistnost.setRightRoomPath(sidePath);
								sideRoom.setLeftRoomPath(sidePath);

							}


							shiftCols += 2;




							if (nextShiftRows < rows) {
								nextShiftRows = rows;
							}

							shiftCols += cols;
						}
					}
					shiftRows += nextShiftRows;
					shiftRows += 2;
				}
			} else{
				for(Room room : roomArrayList){
					room.unload();
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
		int right = 0;
		int left = 0;
		int top = 0;
		int bottom = 0;
		// radky od shora dolu
		A: for (int i = 0;i < roomY;i++){
			for (int j = 0;j < roomX;j++){
				if (roomMap[i][j] != 0){
					break A;
				}
			}
			top++;
		}
		// radky od dola nahoru
		A: for (int i = roomY-1;i >= 0;i--){
			for (int j = roomX-1;j >= 0;j--){
				if (roomMap[i][j] != 0){
					break A;
				}
			}
			bottom++;
		}
		// sloupce od leva do prava
		A: for (int i = 0;i < roomY;i++){
			for (int j = 0;j < roomX;j++){
				if (roomMap[j][i] != 0){
					break A;
				}
			}
			left++;
		}
		// sloupce od prava do leva
		A: for (int i = roomY-1;i >= 0;i--){
			for (int j = roomX-1;j >= 0;j--){
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
			if (roomX >= 0) System.arraycopy(roomMap[top + i], left, newRoomMap[i], 0, roomX);
		}

		roomMap = newRoomMap;

	}

	public int getTileSize() { return tileSize; }
	public float getX() { return position.x; }
	public float getY() { return position.y; }

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

		position.x += (x - position.x) * tween;
		position.y += (y - position.y) * tween;

		fixBounds();

		colOffset = (int)-position.x / tileSize;
		rowOffset = (int)-position.y / tileSize;

		camera.setPosition(Math.round(position.x),Math.round(position.y));


	}
	
	private void fixBounds() {
		if(position.x < xmin) position.x = xmin;
		if(position.y < ymin) position.y = ymin;
		if(position.x > xmax) position.x = xmax;
		if(position.y > ymax) position.y = ymax;
	}
	
	public void draw() {
		target = new Matrix4f()
				.translate(colOffset*tileSize,rowOffset*tileSize,0)
				.scale(2);
		Matrix4f projection = camera.projection();
		shader.bind();
		shader.setUniformi("sampler",0);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, tilesetId);

		for(
			int row = rowOffset;
			row < rowOffset + numRowsToDraw;
			row++
		) {

			if(row >= numRows) break;

			for(
				int col = colOffset;
				col < colOffset + numColsToDraw;
				col++
			) {

				if(col >= numCols) break;

				if(map[row][col] == -1) continue;

				int rc = map[row][col];
				int r = rc / numTilesAcross;
				int c = rc % numTilesAcross;

				target = new Matrix4f()
							.translate(col * tileSize,row * tileSize,0) // shift
							.scale(2); // scaling image by 2

				projection.mul(target,target);

				shader.setUniformm4f("projection",target);

				glEnableVertexAttribArray(0);
				glEnableVertexAttribArray(1);

				glBindBuffer(GL_ARRAY_BUFFER,vboVertexes);
				glVertexAttribPointer(0,2,GL_INT,false,0,0);

				glBindBuffer(GL_ARRAY_BUFFER,tiles[r][c].getVbo());
				glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

				glDrawArrays(GL_QUADS, 0, 4);

				glBindBuffer(GL_ARRAY_BUFFER,0);

				glDisableVertexAttribArray(0);
				glDisableVertexAttribArray(1);

			}


		}

		shader.unbind();
		glBindTexture(GL_TEXTURE_2D,0);
		glActiveTexture(0);

	}

	public float getPlayerStartX() { return playerStartX; }

	public float getPlayerStartY(){ return playerStartY; }

	public void autoTile(){
		int[][] newMap = new int[numRows][numCols];

		for(int i = 0;i < numRows;i++){
			for(int j = 0;j<numCols;j++){
				int tile = map[i][j];
				int topTile,bottomTile,rightTile,leftTile;
				// diagonal tiles
				int bottomRightTile,bottomLeftTile;
				int topRightTile,topLeftTile;


				if(i-1 < 0) topTile = 1;
				else topTile = map[i-1][j];

				if(i+1 >= numRows) bottomTile = 1;
				else bottomTile = map[i+1][j];

				if(j-1 < 0) leftTile = 1;
				else leftTile = map[i][j-1];


				if(j+1 >= numCols) rightTile = 1;
				else rightTile = map[i][j+1];

				// diagnonals
				if(i+1 >= numRows || j-1 < 0)  bottomLeftTile = 1;
				else bottomLeftTile = map[i+1][j-1];

				if(i+1 >= numRows || j+1 >= numCols)  bottomRightTile = 1;
				else bottomRightTile = map[i+1][j+1];

				if(i-1 < 0 || j-1 < 0)  topLeftTile = 1;
				else topLeftTile = map[i-1][j-1];

				if(i-1 < 0 || j+1 >= numCols)  topRightTile = 1;
				else topRightTile = map[i-1][j+1];

				if(tile == 1 && topTile == 1 && bottomTile == 1 && leftTile == 1 && rightTile == 0) {
					int random = Random.nextInt(2);
					if(random == 1) newMap[i][j] = 27;
					else newMap[i][j] = 39;
				} else if (tile == 1 && topTile == 1 && bottomTile == 1 && leftTile == 0 && rightTile == 1){
					int random = Random.nextInt(2);
					if(random == 1)newMap[i][j] = 28;
					else newMap[i][j] = 40;
				} else if (tile == 1 && topTile == 1 && bottomTile == 0 && leftTile == 1 && rightTile == 1){
					newMap[i][j] = 22;
				} else if (tile == 1 && topTile == 0 && bottomTile == 1 && leftTile == 1 && rightTile == 1) {
					int random = Random.nextInt(3);
					if(random == 1)newMap[i][j] = 31;
					else newMap[i][j] = 30;
				} else if(tile == 1 && topTile == 0 && bottomTile == 1 && leftTile == 1 && rightTile == 0) {
					newMap[i][j] = 38;
				} else if(tile == 1 && topTile == 0 && bottomTile == 1 && leftTile == 0 && rightTile == 1) {
					newMap[i][j] = 35;
				} else if(tile == 1 && topTile == 1 && bottomTile == 0) {
					newMap[i][j] = 22;
				} else if (tile == 1 && topTile == 1 && bottomTile == 1 && leftTile == 1 && rightTile == 1
						&& bottomLeftTile == 1 && bottomRightTile == 1 && topRightTile == 0 && topLeftTile == 1) {
					newMap[i][j] = 29;
				} else if (tile == 1 && topTile == 1 && bottomTile == 1 && leftTile == 1 && rightTile == 1
						&& bottomLeftTile == 1 && bottomRightTile == 1 && topRightTile == 1 && topLeftTile == 0) {
					newMap[i][j] = 34;
				} else if (tile == 1 && topTile == 1 && bottomTile == 1 && leftTile == 1 && rightTile == 1
						&& bottomLeftTile == 1 && bottomRightTile == 0 && topRightTile == 1 && topLeftTile == 1) {
					newMap[i][j] = 21;
				} else if (tile == 1 && topTile == 1 && bottomTile == 1 && leftTile == 1 && rightTile == 1
						&& bottomLeftTile == 0 && bottomRightTile == 1 && topRightTile == 1 && topLeftTile == 1) {
					newMap[i][j] = 26;
				} else if(tile == 1 && topTile == 1 && bottomTile == 1 && leftTile == 1 && rightTile == 1) {
					newMap[i][j] = 41;
				}
				else if (tile == 0 && topTile == 0 && bottomTile == 0 && leftTile == 1 && rightTile == 0){
					newMap[i][j] = 4;
				}
				else if (tile == 0 && topTile == 0 && bottomTile == 1 && leftTile == 1 && rightTile == 0){
					newMap[i][j] = 8;
				} else if (tile == 0 && topTile == 1 && bottomTile == 0 && leftTile == 0 && rightTile == 1){
					newMap[i][j] = 3;
				} else if (tile == 0 && topTile == 1 && bottomTile == 0 && leftTile == 0 && rightTile == 0){
					int random = Random.nextInt(2);
					if(random == 1) newMap[i][j] = 1;
					else newMap[i][j] = 2;
				} else if (tile == 0 && topTile == 0 && bottomTile == 0 && leftTile == 0 && rightTile == 1){
					newMap[i][j] = 7;
				} else if (tile == 0 && topTile == 0 && bottomTile == 1 && leftTile == 0 && rightTile == 0){
					int random = Random.nextInt(2);
					if(random == 1) newMap[i][j] = 9;
					else newMap[i][j] = 10;
				} else if (tile == 0 && topTile == 0 && bottomTile == 1 && leftTile == 0 && rightTile == 1){
					newMap[i][j] = 11;
				} else if (tile == 0 && topTile == 0 && bottomTile == 0 && leftTile == 0 && rightTile == 0){
					int random = Random.nextInt(14);
					if(random == 1) newMap[i][j] = 5;
					else if(random == 2) newMap[i][j] = 12;
					else if(random == 3) newMap[i][j] = 13;
					else if(random == 4) newMap[i][j] = 14;
					else if(random == 5) newMap[i][j] = 15;
					else if(random == 6) newMap[i][j] = 16;
					else if(random == 7) newMap[i][j] = 17;
					else if(random == 8) newMap[i][j] = 18;
					else newMap[i][j] = 6;
				}

			}
		}
		this.map = newMap;
	}
}

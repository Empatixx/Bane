package cz.Empatix.Render;


import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.Artefacts.ArtefactManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.Shopkeeper;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Java.RomanNumber;
import cz.Empatix.Main.DiscordRP;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.*;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Hud.Minimap.MMRoom;
import cz.Empatix.Render.Hud.Minimap.MiniMap;
import cz.Empatix.Render.RoomObjects.*;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL45.*;

public class TileMap {
	private Room sideRoom;

	public static void load(){
		Loader.loadImage("Textures\\tileset64.tga");
	}
	// gamestate
	private int floor;

	// instances
	private MiniMap miniMap;
	private Player[] player;

	// position
	private Vector3f position;
	private Camera camera;
	private Shader shader;


	// bounds
	private float xmin;
	private float ymin;
	private float xmax;
	private float ymax;
	
	private double tween;
	
	// map
	private byte[][] map;
	private int tileSize;
	private int numRows;
	private int numCols;
	
	// tileset
	private int numTilesAcross;
	private Tile[][] tiles;
	
	// drawing
	private int rowOffset;
	private int colOffset;

	private int previousrowOffset;
	private int previouscolOffset;

	private int numRowsToDraw;
	private int numColsToDraw;

	// opengl id of texture (tileset)
	private int tilesetId;
	private int[] vboVertices;
	private int[] vboTexCoords;
	//matrix4f opengl
	private Matrix4f target;

	// rooms
	private Room[] roomArrayList;
	private Room[] currentRoom;
	private Room[] sideRooms;

	private int[][] roomMap;
	private int idGen;

	// room orientation
	private int roomY;
	private int roomX;

	// starting XY room
	private float playerStartX;
	private float playerStartY;

	private long nextFloorEnterTime;
	private TextRender[] title;

	// mutliplayer
	private List<Network.ObjectInteract> objectInteractPackets;

	// if tilemap is server-side
	private boolean serverSide;
	public TileMap(int tileSize, MiniMap miniMap) {
		this.tileSize = tileSize;
		this.miniMap = miniMap;
		// 2x scale
		numRowsToDraw = Camera.getHEIGHT() / (tileSize*2) + 2;
		numColsToDraw = Camera.getWIDTH() / (tileSize*2) + 2;

		position = new Vector3f(0,0,0);

		this.camera = Camera.getInstance();
		tween = 1;

		target = new Matrix4f();
		floor = 0;

		sideRooms = new Room[4];
		title = new TextRender[2];
		for(int i = 0;i<2;i++){
			title[i] = new TextRender();
		}
		player = new Player[1];
		currentRoom = new Room[1];
		sideRooms = new Room[4];
	}
	public TileMap(int tileSize) {
		this.tileSize = tileSize;
		// 2x scale
		numRowsToDraw = Camera.getHEIGHT() / (tileSize*2) + 2;
		numColsToDraw = Camera.getWIDTH() / (tileSize*2) + 2;

		position = new Vector3f(0,0,0);

		this.camera = Camera.getInstance();
		tween = 1;

		target = new Matrix4f();
		floor = 0;
		// singleplayer - creating only one instance of player
		player = new Player[1];
		currentRoom = new Room[1];
		sideRooms = new Room[4];

	}
	// constructor for multiplayer
	public TileMap(int tileSize,MiniMap miniMap, int playerCount) {
		this.tileSize = tileSize;
		this.miniMap = miniMap;

		floor = 0;
		player = new Player[playerCount];
		currentRoom = new Room[playerCount];
		sideRooms = new Room[playerCount*4];
		objectInteractPackets = Collections.synchronizedList(new ArrayList<>());

		serverSide = true;
	}
	public void setPlayer(Player p){
		this.player[0] = p;
	}
	public void setPlayers(Player[] p){
		for(int i = 0;i<player.length && i < p.length;i++){
			this.player[i] = p[i];
		}
	}
	public int getNumRows() {
		return numRows;
	}
	public int getNumCols(){
		return numCols;
	}
	public void loadTiles(String s) {
		vboTexCoords = new int[]{glGenBuffers(),glGenBuffers()};
		vboVertices = new int[]{glGenBuffers(),glGenBuffers()};

		ByteBufferImage decoder = Loader.getImage(s);
		ByteBuffer tileset = decoder.getBuffer();

		tilesetId = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, tilesetId);

		// support for devices without GL4.2+
		if(Game.GL_MAJOR_VERSION >= 4 && Game.GL_MINOR_VERSION >= 2){
			glTexStorage2D(GL_TEXTURE_2D, 5, GL_RGBA8, decoder.getWidth(), decoder.getHeight());
			glTexSubImage2D(GL_TEXTURE_2D,0,0,0,decoder.getWidth(),decoder.getHeight(),GL_RGBA,GL_UNSIGNED_BYTE,tileset);
			glGenerateMipmap(GL_TEXTURE_2D);  //Generate num_mipmaps number of mipmaps here.
		} else {
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, tileset);
			if(Game.GL_MAJOR_VERSION >= 3){
				glGenerateMipmap(GL_TEXTURE_2D);  //Generate num_mipmaps number of mipmaps here.
			}
		}
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

		shader = ShaderManager.getShader("shaders\\shader");
		if (shader == null){
			shader = ShaderManager.createShader("shaders\\shader");
		}

		// because we are scaling image by 2x we must increase size of tileSize
        tileSize *=2;

	}
	// loading tiles in multiplayer
	public void loadTilesMP(String s) {
		ByteBufferImage decoder = Loader.getImage(s);
		numTilesAcross = decoder.getWidth() / tileSize;
		tiles = new Tile[2][numTilesAcross];
		for(int col = 0; col < numTilesAcross; col++) {
			// texCoords is null, because we never render in multiplayer
			tiles[0][col] = new Tile(null, Tile.NORMAL);
			tiles[1][col] = new Tile(null, Tile.BLOCKED);
		}
		// because we are scaling image by 2x we must increase size of tileSize
		tileSize *=2;

	}
	private int getIdGen(){
		idGen++;
		return idGen;
	}
	public void loadProgressRoom(){
		// id generator
		idGen = 0;

		int maxRooms = 1;

		// room xy map
		roomY = roomX = 1;
		roomMap = new int[1][1];

		//arraylist filled with rooms
		roomArrayList = new Room[maxRooms];

		int id = getIdGen();

		int x = roomX/2;
		int y = roomY/2;

		Room room = new Room(Room.Progress,id,x,y);
		Arrays.fill(currentRoom, room);

		MMRoom mmRoom = new MMRoom(room.getType(),room.getX(),room.getY());
		room.setMinimapRoom(mmRoom);

		roomArrayList[0] = room;
		roomMap[y][x] = id;

		map = room.getRoomMap();
		numCols = room.getNumCols();
		numRows = room.getNumRows();


		playerStartX = numCols/2*tileSize;
		playerStartY = numRows/2*tileSize;


		xmin = (Camera.getWIDTH() - room.getNumCols()*tileSize);
		xmax = 0;

		ymin = (Camera.getHEIGHT() - room.getNumRows()*tileSize);
		ymax = 0;

		autoTile();


		room.createObjects(this,null);
	}
	public void loadMap() {
		// room generation
		generateRooms();

		// cutting map (removing useless xy where is not anything)
		decreaseSizeOfMap();

		// converting rooms into 1 big tile map
		formatMap();

		// converting 1 and 0 into tiles id textures
		autoTile();

		// server created map -> send packet to all clients
		if(serverSide){
			Server server = MultiplayerManager.getInstance().server.getServer();
			Network.MapLoaded mapLoaded = new Network.MapLoaded();
			server.sendToAllTCP(mapLoaded);
		}

		// create map objects into all rooms
		for(Room room : roomArrayList){
			room.createObjects(this,player);
		}
		if(serverSide) return;


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
	public void loadMapViaPackets() {
		Object[] roomPackets = MultiplayerManager.getInstance().packetHolder.get(PacketHolder.TRANSFERROOM);
		Object[] roomMapPacket = MultiplayerManager.getInstance().packetHolder.get(PacketHolder.TRANSFERROOMMAP);

		handleRoomMapPacket((Network.TransferRoomMap) roomMapPacket[0]);
		for(Object o : roomPackets){
			Network.TransferRoom transferRoom = (Network.TransferRoom) o;
			handleRoomPacket(transferRoom);
		}

		// converting rooms into 1 big tile map
		formatMap();

		A: for(int i = 0;i < roomY;i++){
			for(int j = 0;j <roomX;j++){
				if(roomMap[i][j] == currentRoom[0].getId()){
					if(roomY > i+1 && currentRoom[0].isBottom()){
						sideRooms[0] = getRoom(roomMap[i+1][j]);
					}
					else sideRooms[0] = null;
					if(0 <= i-1 && currentRoom[0].isTop()){
						sideRooms[1] = getRoom(roomMap[i-1][j]);
					}
					else sideRooms[1] = null;
					if(roomX > j+1 && currentRoom[0].isRight()){
						sideRooms[2] = getRoom(roomMap[i][j+1]);
					}
					else sideRooms[2] = null;
					if(0 <= j-1 && currentRoom[0].isLeft()){
						sideRooms[3] = getRoom(roomMap[i][j-1]);
					}
					else sideRooms[3] = null;
					break A;
				}
			}
		}

		// converting 1 and 0 into tiles id textures
		autoTile();


		createRoomObjectsViaPackets();


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
	/**
	 * Method for singleplayer, checking if player entered some new rooms, only works in singleplayer
	 */
	public void updateCurrentRoom(int x, int y){
		for (RoomPath room : currentRoom[0].getRoomPaths()){
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
					break;
				}
			}
		}
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


					xMax -= tileSize*2;
					xMin += tileSize*2;

					// getting Y max/min of room
					yMax -= tileSize*2;
					yMin += tileSize*2;
					if (!room.hasEntered() && y > yMin && y < yMax && x > xMin && x < xMax) {
						if(!MultiplayerManager.multiplayer){
							// event trigger on entering a new room
							room.entered(this);
						}
						room.showRoomOnMinimap();
					}
					if(currentRoom[0] != room){
						currentRoom[0] = room;
						A: for(int i = 0;i < roomY;i++){
							for(int j = 0;j <roomX;j++){
								if(roomMap[i][j] == room.getId()){
									if(roomY > i+1 && currentRoom[0].isBottom()){
										sideRooms[0] = getRoom(roomMap[i+1][j]);
									}
									else sideRooms[0] = null;
									if(0 <= i-1 && currentRoom[0].isTop()){
										sideRooms[1] = getRoom(roomMap[i-1][j]);
									}
									else sideRooms[1] = null;
									if(roomX > j+1 && currentRoom[0].isRight()){
										sideRooms[2] = getRoom(roomMap[i][j+1]);
									}
									else sideRooms[2] = null;
									if(0 <= j-1 && currentRoom[0].isLeft()){
										sideRooms[3] = getRoom(roomMap[i][j-1]);
									}
									else sideRooms[3] = null;
									break A;
								}
							}
						}
					}
 					break;

				}
			}
		}
	}
	/**
	 * Method for multiplayer, checking if players entered some new rooms, only works in multiplayer!
	 */
	public void updateCurrentRoom() {
		for (Room room : roomArrayList) {
			// getting X max/min of room
			int xMax = room.getxMax();
			int xMin = room.getxMin();

			// getting Y max/min of room
			int yMax = room.getyMax();
			int yMin = room.getyMin();

			for (int k = 0;k<player.length;k++) {
				Player player = this.player[k];
				if (player != null) {
					int x = (int)player.getX();
					int y = (int)player.getY();

					if (x > xMin && x < xMax) {
						if (y > yMin && y < yMax) {
							xMax -= tileSize * 2;
							xMin += tileSize * 2;
							// getting Y max/min of room
							yMax -= tileSize * 2;
							yMin += tileSize * 2;

							if (!room.hasEntered() && y > yMin && y < yMax && x > xMin && x < xMax) {
								// event trigger on entering a new room
								room.entered(this, player);
							}
							boolean duplicate = false;
							for(int c = 0;c<currentRoom.length;c++){
								if(c == k) continue;
								if (currentRoom[c] == room) {
									duplicate = true;
									break;
								}
							}
							if(!duplicate){
								currentRoom[k] = room;
								A: for(int i = 0;i < roomY;i++){
									for(int j = 0;j <roomX;j++){
										if(roomMap[i][j] == room.getId()){
											if(roomY > i+1 && currentRoom[k].isBottom()){
												duplicate = false;
												Room sideRoom = getRoom(roomMap[i+1][j]);
												for(int c = 0;c<currentRoom.length;c++){
													if(c == k) continue;
													if (sideRooms[c*4] == sideRoom || sideRoom == currentRoom[k]) {
														duplicate = true;
														break;
													}
												}
												if(duplicate){
													sideRooms[k*4] = null;
												} else {
													sideRooms[k*4] = sideRoom;
												}
											}
											else sideRooms[k*4] = null;
											if(0 <= i-1 && currentRoom[k].isTop()){
												duplicate = false;
												Room sideRoom = getRoom(roomMap[i-1][j]);
												for(int c = 0;c<currentRoom.length;c++){
													if(c == k) continue;
													if (sideRooms[c*4+1] == sideRoom || sideRoom == currentRoom[k]) {
														duplicate = true;
														break;
													}
												}
												if(duplicate){
													sideRooms[k*4+1] = null;
												} else {
													sideRooms[k*4+1] = sideRoom;
												}
											}
											else sideRooms[k*4+1] = null;
											if(roomX > j+1 && currentRoom[k].isRight()){
												duplicate = false;
												sideRoom = getRoom(roomMap[i][j+1]);
												for(int c = 0;c<currentRoom.length;c++){
													if(c == k) continue;
													if (sideRooms[c*4+2] == sideRoom || sideRoom == currentRoom[k]) {
														duplicate = true;
														break;
													}
												}
												if(duplicate){
													sideRooms[k*4+2] = null;
												}
												sideRooms[k*4+2] = sideRoom;
											}
											else sideRooms[k*4+2] = null;
											if(0 <= j-1 && currentRoom[k].isLeft()){
												duplicate = false;
												sideRoom = getRoom(roomMap[i][j-1]);
												for(int c = 0;c<currentRoom.length;c++){
													if(c == k) continue;
													if (sideRooms[c*4+3] == sideRoom || sideRoom == currentRoom[k]) {
														duplicate = true;
														break;
													}
												}
												if(duplicate){
													sideRooms[k*4+3] = null;
												} else {
													sideRooms[k*4+3] = getRoom(roomMap[i][j-1]);
												}
											}
											else sideRooms[k*4+3] = null;
											break A;
										}
									}
								}
							} else {
								currentRoom[k] = null;
								for(int s = 0;s<4;s++) sideRooms[k*4+s] = null;
							}
							break;
						}
					}
				}
			}
		}
	}
	public void updateObjects(){
		for(int j = 0;j<currentRoom.length;j++){
			Room currentRoom = this.currentRoom[j];
			if(currentRoom == null) continue;
			currentRoom.updateObjects(this);
			ArrayList<RoomObject> objects = currentRoom.getMapObjects();
			for(int k = j*4;k<j*4+4;k++){
				Room r = sideRooms[k];
				if(r != null){
					r.updateObjects(this);
					for(int i = 0;i<objects.size();i++){
						for (Player p : player) {
							if (p != null) {
								RoomObject object = objects.get(i);

								int x = (int) object.getX();
								int y = (int) object.getY();

								int cwidth = object.getCwidth() / 2;
								int cheight = object.getCheight() / 2;

								int pcheight = p.getCheight() / 2;
								int pcwidth = p.getCwidth() / 2;

								if (x - cwidth - pcwidth >= r.getxMin() && x + cwidth + pcwidth <= r.getxMax() && y - cheight - pcheight >= r.getyMin() && y + cheight + pcheight <= r.getyMax()) {
									currentRoom.removeObject(object);
									r.addObject(object);
									i--;
								}
							}
						}
					}
				}
			}
			if(!serverSide && MultiplayerManager.multiplayer){
				Object[] packets = MultiplayerManager.getInstance().packetHolder.get(PacketHolder.OPENCHEST);
				for(Object o : packets){
					Network.OpenChest openChest = (Network.OpenChest) o;
					for (RoomObject object : objects) {
						if (object.getId() == openChest.id) {
							((Chest) object).open();
						}
					}
				}
			}
		}

	}
	public ArrayList<RoomObject>[] getRoomMapObjects(){
		@SuppressWarnings("unchecked")
		ArrayList<RoomObject>[] arrayLists = new ArrayList[currentRoom.length];
		for(int i = 0;i<currentRoom.length;i++){
			if(currentRoom[i] == null) arrayLists[i] = null;
			else arrayLists[i] = currentRoom[i].getMapObjects();
		}
		return arrayLists;
	}
	private void generateRooms(){
		// id generator
		idGen = 0;

		int maxRooms = 9;
		int currentRooms = 0;

		// boolean loop
		boolean mapCompleted = false;

		// room xy map
		roomY = roomX = 20;
		roomMap = new int[roomX][roomY];


		//arraylist filled with rooms
		roomArrayList = new Room[maxRooms];

		// multiplayer
		MultiplayerManager mpManager = null;
		if(MultiplayerManager.multiplayer){
			mpManager = MultiplayerManager.getInstance();
		}

		boolean lootRoomCreated = false;
		boolean shopRoomCreated = false;
		while(!mapCompleted){
			if (currentRooms == 0){
				// starting room
				int id = getIdGen();

				int x = roomX/2;
				int y = roomY/2;

				Room room = new Room(Room.Starter,id,x,y);
				Arrays.fill(currentRoom,room);
				MMRoom mmRoom = new MMRoom(room.getType(),room.getX()-10,room.getY()-10);
				room.setMinimapRoom(mmRoom);
				miniMap.addRoom(mmRoom,currentRooms);

				roomArrayList[currentRooms] = room;
				roomMap[y][x] = id;
				// multiplayer
				if(mpManager != null){
					Network.TransferRoom transferRoom = new Network.TransferRoom();
					transferRoom.type = Room.Starter;
					transferRoom.mapFilepath = room.getMapFilepath();
					transferRoom.id = id;
					transferRoom.x = x;
					transferRoom.y = y;
					transferRoom.index = currentRooms;

					Server server = MultiplayerManager.getInstance().server.getServer();
					server.sendToAllTCP(transferRoom);
				}
				// counting how many rooms have been created.
				currentRooms++;
			} else {
				int newRooms = 0;

				// looping each room
				for(int i = 0;i < currentRooms;i++){

					// type of room
					int type;
					if(currentRooms+newRooms >= 4 && !lootRoomCreated) {
						type = Room.Loot;
					} else if(currentRooms+newRooms >= 6 && !shopRoomCreated) {
						type = Room.Shop;
					} else if (maxRooms-1==currentRooms+newRooms) {
						type = Room.Boss;
					} else{
						type = Room.Classic;
					}

					int currentRoomType = roomArrayList[i].getType();

					// cancel if we would connect new room to loot room
					if(currentRoomType == Room.Loot || currentRoomType == Room.Shop) {
						continue;
					} else if(currentRoomType == Room.Starter){
						if(type == Room.Loot || type == Room.Boss){
							continue;
						}
					}

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
						if(type == Room.Loot){
							lootRoomCreated = true;
						}
						else if(type == Room.Shop){
							shopRoomCreated = true;
						}
						// random between <0,4)
						int rndDirection = Random.nextInt(4);

						// Checking if room haven't that path yet
						while(blockedPaths[rndDirection]){
							rndDirection = Random.nextInt(4);
						}

						Room room;
						int id = getIdGen();


						// TOP Direction
						if (rndDirection == 0){
							room = new Room(type,id,roomX,roomY-1);

							// adding to minimap
							MMRoom mmRoom = new MMRoom(room.getType(),room.getX()-10,room.getY()-10);
							roomArrayList[i].getMinimapRoom().addSideRoom(mmRoom,0);
							room.setMinimapRoom(mmRoom);
							miniMap.addRoom(mmRoom,currentRooms+newRooms);

							roomArrayList[i].setTop(true);
							room.setBottom(true);

							roomArrayList[currentRooms+newRooms] = room;
							roomMap[roomY-1][roomX] = id;
						}
						// BOTTOM Direction
						else if (rndDirection == 1){
							room = new Room(type,id,roomX,roomY+1);

							// adding to minimap
							MMRoom mmRoom = new MMRoom(room.getType(),room.getX()-10,room.getY()-10);
							roomArrayList[i].getMinimapRoom().addSideRoom(mmRoom,1);
							room.setMinimapRoom(mmRoom);
							miniMap.addRoom(mmRoom,currentRooms+newRooms);

							roomArrayList[i].setBottom(true);
							room.setTop(true);

							roomArrayList[currentRooms+newRooms] = room;
							roomMap[roomY+1][roomX] = id;
						}
						// LEFT Direction
						else if (rndDirection == 2){
							room = new Room(type,id,roomX-1,roomY);

							// adding to minimap
							MMRoom mmRoom = new MMRoom(room.getType(),room.getX()-10,room.getY()-10);
							roomArrayList[i].getMinimapRoom().addSideRoom(mmRoom,2);
							room.setMinimapRoom(mmRoom);
							miniMap.addRoom(mmRoom,currentRooms+newRooms);

							room.setRight(true);
							roomArrayList[i].setLeft(true);

							roomArrayList[currentRooms+newRooms] = room;
							roomMap[roomY][roomX-1] = id;
						}
						// RIGHT Direction
						else {
							room = new Room(type,id,roomX+1,roomY);

							// adding to minimap
							MMRoom mmRoom = new MMRoom(room.getType(),room.getX()-10,room.getY()-10);
							roomArrayList[i].getMinimapRoom().addSideRoom(mmRoom,3);
							room.setMinimapRoom(mmRoom);
							miniMap.addRoom(mmRoom,currentRooms+newRooms);

							room.setLeft(true);
							roomArrayList[i].setRight(true);

							roomArrayList[currentRooms+newRooms] = room;
							roomMap[roomY][roomX+1] = id;
						}
						// multiplayer support - sending new rooms
						if(mpManager != null){
							Network.TransferRoom transferRoom = new Network.TransferRoom();
							transferRoom.type = type;
							transferRoom.mapFilepath = room.getMapFilepath();
							transferRoom.id = id;
							transferRoom.x = room.getX();
							transferRoom.y = room.getY();
							transferRoom.index = currentRooms+newRooms;
							transferRoom.previousIndex = i;
							transferRoom.top = rndDirection == 1;
							transferRoom.bottom = rndDirection == 0;
							transferRoom.left = rndDirection == 3;
							transferRoom.right = rndDirection == 2;

							Server server = MultiplayerManager.getInstance().server.getServer();
							server.sendToAllTCP(transferRoom);
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
		A: for(int i = 0;i < roomY;i++){
			for(int j = 0;j <roomX;j++){
				if(roomMap[i][j] == currentRoom[0].getId()){
					sideRooms[0] = getRoom(roomMap[i+1][j]);
					sideRooms[1] = getRoom(roomMap[i-1][j]);
					sideRooms[2] = getRoom(roomMap[i][j+1]);
					sideRooms[3] = getRoom(roomMap[i][j-1]);
					break A;
				}
			}
		}
	}
	private void formatMap() {
		// max collumns in collumn of room
		int[] maxCols = new int[roomX];

		// paths informations
		int[] shiftsCols = new int[roomX*roomY];

		for(int loop = 0;loop < 4;loop++) {
			// getting max collumns in collumn of room
			if (loop == 0){
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
			else if (loop == 1) {

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
			} else if (loop == 2) {
				// shifts while loop
				int nextShiftRows;
				int shiftRows = 0;
				int shiftCols;

				boolean bottom;
				boolean right;

				// final tilemap
				map = new byte[numRows][numCols];

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
						// if there is not any room
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


							byte[][] roomMap = mistnost.getRoomMap();

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
			} else {
				// shifts while loop
				int nextShiftRows;
				int shiftRows = 0;
				int shiftCols;

				for (int y = 0; y < roomY; y++) {

					shiftCols = 0;
					nextShiftRows = 0;

					for (int x = 0; x < roomX; x++) {

						Room mistnost = getRoom(roomMap[y][x]);
						// if there is not any room
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
								int fixX = Math.max(shiftsCols[(y + 1) * roomX + x] - shiftCols, 0);
								int fixXMax = Math.max(cols - bottomRoom.getNumCols() - (shiftsCols[(y + 1) * roomX + x] - shiftCols), 0);
								B:for(int j = 1+shiftCols+fixX+(cols-fixXMax-2-fixX)/2;j<cols+shiftCols-1-fixXMax;j++){
									for(int i = rows-1+shiftRows;i<numRows;i++){
								 		if(paths >= 2) break B;
										if(map[i][j] == Tile.NORMAL){
											mistnost.addWall(this,j*tileSize+tileSize/2,(rows-1+shiftRows)*tileSize+tileSize/2, PathWall.BOTTOM);
											bottomRoom.addWall(this,j*tileSize+tileSize/2,(i-1)*tileSize+tileSize/2,PathWall.TOP);

											break;
										}
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
											mistnost.addWall(this,(cols - 1 + shiftCols)*tileSize+tileSize/2,j*tileSize+tileSize/2,PathWall.RIGHT);
											sideRoom.addWall(this,(i-1)*tileSize+tileSize/2,j*tileSize+tileSize/2,PathWall.LEFT);
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
		// sending packet of room map to other players
		MultiplayerManager mpManager = null;
		if(MultiplayerManager.multiplayer){
			mpManager = MultiplayerManager.getInstance();
		}
		if(mpManager != null){
			Network.TransferRoomMap transferRoomMap = new Network.TransferRoomMap();
			transferRoomMap.roomMap = roomMap;
			transferRoomMap.roomX = roomX;
			transferRoomMap.roomY = roomY;

			Server server = mpManager.server.getServer();
			server.sendToAllTCP(transferRoomMap);
		}
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
		if(serverSide) return;

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
	public void draw(int tileType) {
		Matrix4f projection = camera.projection();
		shader.bind();
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, tilesetId);
		target = new Matrix4f()
				.scale(2); // scaling image by 2

		projection.mul(target,target);
		shader.setUniformm4f("projection",target);

		int count = 0;
		for(int row = rowOffset; row < rowOffset + numRowsToDraw; row++) {
			if (row >= numRows) break;
			for (int col = colOffset; col < colOffset + numColsToDraw; col++) {
				if (col >= numCols) break;
				if(getType(row,col) == tileType){
					count++;
				}
			}
		}

		if(previouscolOffset != colOffset || previousrowOffset != rowOffset){
			IntBuffer verticesBuffer = BufferUtils.createIntBuffer(count*8);
			DoubleBuffer texCoordsBuffer = BufferUtils.createDoubleBuffer(count*8);

			for(int row = rowOffset; row < rowOffset + numRowsToDraw; row++) {
				if (row >= numRows) break;
				for (int col = colOffset; col < colOffset + numColsToDraw; col++) {
					if (col >= numCols) break;
					if(getType(row,col) != tileType ) continue;
					int[] vertices =
							{
									col*tileSize/2,row*tileSize/2, // BOTTOM LEFT
									col*tileSize/2,tileSize/2+row*tileSize/2, // BOTTOM TOP
									tileSize/2+col*tileSize/2,tileSize/2+row*tileSize/2, // RIGHT TOP
									tileSize/2+col*tileSize/2,row*tileSize/2 // BOTTOM RIGHT
							};

					int rc = map[row][col];
					int r = rc / numTilesAcross;
					int c = rc % numTilesAcross;

					verticesBuffer.put(vertices);
					texCoordsBuffer.put(tiles[r][c].getTexCoords());
				}
			}
			texCoordsBuffer.flip();
			verticesBuffer.flip();

			glBindBuffer(GL_ARRAY_BUFFER, vboVertices[tileType]);
			glBufferData(GL_ARRAY_BUFFER,verticesBuffer,GL_DYNAMIC_DRAW);
			glBindBuffer(GL_ARRAY_BUFFER,0);

			glBindBuffer(GL_ARRAY_BUFFER, vboTexCoords[tileType]);
			glBufferData(GL_ARRAY_BUFFER,texCoordsBuffer,GL_DYNAMIC_DRAW);
			glBindBuffer(GL_ARRAY_BUFFER,0);

			if(tileType == Tile.BLOCKED){
				previouscolOffset = colOffset;
				previousrowOffset = rowOffset;
			}
		}




		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);

		glBindBuffer(GL_ARRAY_BUFFER, vboVertices[tileType]);
		glVertexAttribPointer(0,2,GL_INT,false,0,0);

		glBindBuffer(GL_ARRAY_BUFFER,vboTexCoords[tileType]);
		glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

		glDrawArrays(GL_QUADS, 0, count*4);

		glBindBuffer(GL_ARRAY_BUFFER,0);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);

		shader.unbind();
		glBindTexture(GL_TEXTURE_2D,0);
		glActiveTexture(GL_TEXTURE0);


	}
	public void drawObjects(){
        currentRoom[0].drawObjects(this);
		for(Room r : sideRooms){
			if(r != null) {
				r.drawObjects(this);
			}
		}

	}
	/**
	 *
	 * @param behindCollision - if pre-draw objects should be drawn behind collision tiles
	 */
	public void preDrawObjects(boolean behindCollision){
		currentRoom[0].preDrawObjects(behindCollision);
		for(Room r : sideRooms){
			if(r != null) {
				r.preDrawObjects(behindCollision);
			}
		}
	}
	public float getPlayerStartX() { return playerStartX; }
	public float getPlayerStartY(){ return playerStartY; }
	public void autoTile(){
		byte[][] newMap = new byte[numRows][numCols];

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
					newMap[i][j] = (byte)(22+Random.nextInt(4));
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
	public void addObject(RoomObject obj,int idRoom){
		Room currentRoom = getRoom(idRoom);
		currentRoom.addObject(obj);
		if(serverSide){
			if(obj instanceof Chest){
				Network.AddRoomObject roomObject = new Network.AddRoomObject();
				roomObject.x = (int)obj.getX();
				roomObject.y = (int)obj.getY();
				roomObject.type = Network.TypeRoomObject.CHEST;
				roomObject.id = obj.getId();
				roomObject.idRoom = currentRoom.getId();
				Server server = MultiplayerManager.getInstance().server.getServer();
				server.sendToAllTCP(roomObject);
			}
		}
	}
	public void addLadder(){
		Room currentRoom = null;
		for(Room r : roomArrayList){
			if(r.getType() == Room.Boss){
				currentRoom = r;
			}
		}
		if(currentRoom == null) return;
		int xMin = currentRoom.getxMin();
		int xMax = currentRoom.getxMax();

		int yMin = currentRoom.getyMin();
		int yMax = currentRoom.getyMax();

		Ladder ladder = new Ladder(this);
		ladder.setPosition(xMin + (float) (xMax - xMin) / 2, yMin + (float) (yMax - yMin) / 2);
		addObject(ladder, currentRoom.getId());

		if(serverSide){
			Network.AddRoomObject roomObject = new Network.AddRoomObject();
			roomObject.x = (int)ladder.getX();
			roomObject.y = (int)ladder.getY();
			roomObject.type = Network.TypeRoomObject.LADDER;
			roomObject.id = ladder.getId();
			roomObject.idRoom = currentRoom.getId();
			Server server = MultiplayerManager.getInstance().server.getServer();
			server.sendToAllTCP(roomObject);
		}
	}
	public void newMap(){
		tween = 1;
		ItemManager itemManager = ItemManager.getInstance();
		EnemyManager enemyManager = EnemyManager.getInstance();
		ArtefactManager artefactManager = ArtefactManager.getInstance();
		itemManager.clear();
		enemyManager.clear();
		artefactManager.clear();
		// deleting old room objects from previous floor
		for(Room r: roomArrayList) {
			for(RoomObject obj : r.getMapObjects()){
				obj.delete();
			}
		}
		loadMap();

		fillMiniMap();
		player[0].setPosition(playerStartX, playerStartY);
		setTween(0.10);

		floor++;
		nextFloorEnterTime = System.currentTimeMillis() - InGame.deltaPauseTime();

		DiscordRP.getInstance().update("In-Game","Floor "+RomanNumber.toRoman(floor+1));
	}
	//TODO: multiplayer newMap
	public void newMapMP(){
		floor++;
		Server server = MultiplayerManager.getInstance().server.getServer();
		Network.NextFloor nextFloor = new Network.NextFloor();
		nextFloor.floor = floor;
		server.sendToAllTCP(nextFloor);

		ItemManagerMP itemManager = ItemManagerMP.getInstance();
		EnemyManagerMP enemyManager = EnemyManagerMP.getInstance();
		ArtefactManagerMP artefactManager = ArtefactManagerMP.getInstance();

		artefactManager.clear();
		itemManager.clear();
		enemyManager.clear();

		// deleting old room objects from previous floor
		for(Room r: roomArrayList) {
			for(RoomObject obj : r.getMapObjects()){
				obj.delete();
			}
		}
		loadMap();
		for(Player p : player){
			if(p != null) p.setPosition(playerStartX, playerStartY);
		}
	}
	public void drawTitle(){
		if(System.currentTimeMillis() - InGame.deltaPauseTime() - nextFloorEnterTime < 1750){
			float time = (float)Math.sin(System.currentTimeMillis() % 2000 / 600f)+(1-(float)Math.cos((System.currentTimeMillis() % 2000 / 600f) +0.5f));

			 title[0].draw("Floor "+RomanNumber.toRoman(floor+1),new Vector3f(TextRender.getHorizontalCenter(0,1920,"Floor "+RomanNumber.toRoman(floor+1),5),240,0),5,
					 new Vector3f((float)Math.sin(time),(float)Math.cos(0.5f+time),1f));

			 title[1].draw("Enemies health is increased by "+12*floor*floor+"%",new Vector3f(TextRender.getHorizontalCenter(0,1920,"Enemies health is increased by "+12*floor*floor+"%",2),350,0),2,
					 new Vector3f((float)Math.sin(time),(float)Math.cos(0.5f+time),1f));

		}
	}
	public int getFloor() {
		return floor;
	}
	public void fillMiniMap(){
		currentRoom[0].showRoomOnMinimap();
	}
	// singleplayer
	public Room getCurrentRoom() {
		return currentRoom[0];
	}

	public void keyPressed(int k, Player p){
		if(serverSide){
			for(Room currentRoom : currentRoom)currentRoom.keyPressed(k,p);
		} else {
			currentRoom[0].keyPressed(k,p);
		}
	}
	public void handleRoomPacket(Network.TransferRoom room) {
		int maxRooms = 9;

		Room createdRoom = new Room(room.type,room.id,room.x,room.y,room.mapFilepath);

		if(room.type == Room.Starter) currentRoom[0] = createdRoom;
		if(roomArrayList == null) roomArrayList = new Room[maxRooms];

		MMRoom mmRoom = new MMRoom(room.type,room.x-10,room.y-10);
		createdRoom.setMinimapRoom(mmRoom);

		createdRoom.setBottom(room.bottom);
		createdRoom.setTop(room.top);
		createdRoom.setLeft(room.left);
		createdRoom.setRight(room.right);

		Room previousRoom = roomArrayList[room.previousIndex];

		if(room.type != Room.Starter) {
			MMRoom previousMMRoom  = roomArrayList[room.previousIndex].getMinimapRoom();
			if (room.top) {
				previousRoom.setBottom(true);
				previousMMRoom.addSideRoom(mmRoom, 1);
			} else if (room.bottom) {
				previousRoom.setTop(true);
				previousMMRoom.addSideRoom(mmRoom, 0);
			} else if (room.left) {
				previousRoom.setRight(true);
				previousMMRoom.addSideRoom(mmRoom, 3);
			} else if (room.right) {
				previousRoom.setLeft(true);
				previousMMRoom.addSideRoom(mmRoom, 2);
			}
		}

		miniMap.addRoom(mmRoom,room.index);
		roomArrayList[room.index] = createdRoom;

	}
	public void handleRoomMapPacket(Network.TransferRoomMap map){
		roomMap = map.roomMap;
		roomX = map.roomX;
		roomY = map.roomY;
	}
	public boolean isServerSide() {
		return serverSide;
	}
	public void createRoomObjectsViaPackets() {
		PacketHolder packetHolder = MultiplayerManager.getInstance().packetHolder;
		for(Object o:packetHolder.get(PacketHolder.ADDROOMOBJECT)){
			Network.AddRoomObject addRoomPacket = (Network.AddRoomObject) o;
			for(Room room : roomArrayList){
				if(addRoomPacket.idRoom != room.getId()) continue;
				RoomObject roomObject;
				switch (addRoomPacket.type){
					case POT:{
						roomObject = new Pot(this);
						break;
					}
					case FLAG:{
						roomObject = new Flag(this);
						break;
					}
					case BONES:{
						roomObject = new Bones(this);
						break;
					}
					case CHEST:{
						roomObject = new Chest(this);
						break;
					}
					case SPIKE:{
						roomObject = new Spike(this);
						break;
					}
					case BARREL:{
						roomObject = new Barrel(this);
						break;
					}
					case LADDER:{
						roomObject = new Ladder(this);
						break;
					}
					case SHOPKEEPER:{
						roomObject = new Shopkeeper(this);
						break;
					}
					case SHOPTABLE:{
						roomObject = new ShopTable(this);
						((ShopTable) roomObject).createItem();
						break;
					}
					case TORCH:{
						roomObject = new Torch(this);
						((Torch)roomObject).setType(addRoomPacket.objectType);
						break;
					}
					case ARROWTRAP:{
						roomObject = new ArrowTrap(this,player);
						((ArrowTrap)roomObject).setType(addRoomPacket.objectType);
						break;
					}
					case FLAMETHROWER:{
						roomObject = new Flamethrower(this,player);
						((Flamethrower)roomObject).setType(addRoomPacket.objectType);
						break;
					}
					default:{
						roomObject = null;
						break;
					}
				}
				roomObject.setPosition(addRoomPacket.x,addRoomPacket.y);
				roomObject.setId(addRoomPacket.id);
				room.addObject(roomObject);
			}
		}
	}
	public void handleRoomMovePacket(Network.MoveRoomObject movePacket) {
		ArrayList<RoomObject>[] objects = getRoomMapObjects();
		for(ArrayList<RoomObject> roomObjects : objects){
			for(RoomObject roomObject : roomObjects){
				if(roomObject.getId() == movePacket.id){
					roomObject.setPosition(movePacket.x,movePacket.y);
					return;
				}
			}
		}
		for(Room sideRoom : sideRooms){
			if(sideRoom == null) continue;
			ArrayList<RoomObject> roomObjects = sideRoom.getMapObjects();
			for(RoomObject roomObject : roomObjects){
				if(roomObject.getId() == movePacket.id){
					roomObject.setPosition(movePacket.x,movePacket.y);
					return;
				}
			}
		}
	}

	/**
	 * method for checking packets, if someone from players entered a new room
	 */
	public void checkingRoomLocks() {
		Object[] packets = MultiplayerManager.getInstance().packetHolder.get(PacketHolder.LOCKROOM);
		for(Object o : packets){
			Network.LockRoom p = (Network.LockRoom) o;
			for(Room room : roomArrayList){
				if(p.idRoom == room.getId()){
					room.lockRoom(p.lock);
					if(!p.lock) {
						ArtefactManager artefactManager = ArtefactManager.getInstance();
						artefactManager.charge();
					}
					room.showRoomOnMinimap();
				}
			}
		}
	}
	public Room getRoomByCoords(float x, float y){
		for(Room room : roomArrayList){
			if(room.getxMax() > x && room.getxMin() < x && room.getyMax() > y && y > room.getyMin()) return room;
		}
		return null;
	}

	public void handleNextFloorPacket(Network.NextFloor nextFloor) {
		ItemManager itemManager = ItemManager.getInstance();
		EnemyManager enemyManager = EnemyManager.getInstance();
		itemManager.clear();
		enemyManager.clear();
		// deleting old room objects from previous floor
		for(Room r: roomArrayList) {
			for(RoomObject obj : r.getMapObjects()){
				obj.delete();
			}
		}

		floor = nextFloor.floor;
		nextFloorEnterTime = System.currentTimeMillis() - InGame.deltaPauseTime();

		DiscordRP.getInstance().update("In-Game","Floor "+RomanNumber.toRoman(floor+1));
	}

	public void handleObjectInteractPacket(Network.ObjectInteract object) {
		objectInteractPackets.add(object);
	}
	public void checkObjectsInteractions(ItemManagerMP.InteractionAcknowledge[] acknowledges){
		A: for(Network.ObjectInteract objectInteract : objectInteractPackets){
			for(Player p : player){
				if(p == null) continue;
				String pname = ((PlayerMP)p).getUsername();
				if(pname.equalsIgnoreCase(objectInteract.username) && !p.isDead()){
					for(ItemManagerMP.InteractionAcknowledge ack : acknowledges){
						if(ack.isThisAckOfPlayer(pname)){
							if (ack.didInteract()) continue A;
						}
					}
					Room room = getRoomByCoords(p.getX(),p.getY());
					if(room == null) continue A;
					for(RoomObject object : room.getMapObjects()){
						if(p.intersects(object)){
							object.keyPress();
						}
					}
				}
			}
		}
		objectInteractPackets.clear();
	}
}
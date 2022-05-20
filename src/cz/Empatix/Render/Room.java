package cz.Empatix.Render;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.Artefacts.ArtefactManager;
import cz.Empatix.Entity.ItemDrops.ItemDrop;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.Shopkeeper;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Multiplayer.ArtefactManagerMP;
import cz.Empatix.Multiplayer.EnemyManagerMP;
import cz.Empatix.Multiplayer.ItemManagerMP;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Hud.Minimap.MMRoom;
import cz.Empatix.Render.RoomObjects.*;
import cz.Empatix.Render.RoomObjects.ProgressRoom.Armorstand;
import cz.Empatix.Render.RoomObjects.ProgressRoom.Bookshelf;
import cz.Empatix.Render.RoomObjects.ProgressRoom.Portal;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Room {
    // if player has entered room yet
    private boolean entered;
    private boolean closed;
    private MMRoom minimapRoom;

    // indicator for setting map
    private final int id;
    private final int x;
    private final int y;

    // info about room
    private int numCols;
    private int numRows;
    private final int type;
    private String mapFilepath;

    // types of rooms
    public final static int Starter = 0;
    public final static int Classic = 1;
    public final static int Loot = 2;
    public final static int Shop = 3;
    public final static int Boss = 4;
    public final static int Progress = 5;

    // orientation about paths
    private boolean bottom;
    private boolean top;
    private boolean left;
    private boolean right;

    // tiles of room
    private byte[][] roomMap;

    // corners of room
    private int yMin;
    private int yMax;
    private int xMin;
    private int xMax;

    private final RoomPath[] roomPaths;

    private final ArrayList<RoomObject> mapObjects;

    transient private TextRender[] texts;



    public Room(int type, int id, int x, int y){
        entered = false;
        closed = false;

        this.id = id;

        this.x = x;
        this.y = y;

        this.type = type;

        roomPaths = new RoomPath[4];

        mapObjects = new ArrayList<>();

        chooseMap();
        loadRoom(mapFilepath);
    }

    public Room(int type, int id, int x, int y, String mapFilepath){
        entered = false;
        closed = false;

        this.id = id;

        this.x = x;
        this.y = y;

        this.type = type;

        roomPaths = new RoomPath[4];

        mapObjects = new ArrayList<>();

        loadRoom(mapFilepath);
    }

    private void loadRoom(String mapFilepath){
        try{
            InputStream in = getClass().getResourceAsStream(mapFilepath);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(in)
            );

            numCols = Integer.parseInt(br.readLine());
            numRows = Integer.parseInt(br.readLine());

            roomMap = new byte[numRows][numCols];

            String delims = "\\s+";
            for (int row = 0; row < numRows; row++) {
                String line = br.readLine();
                String[] tokens = line.split(delims);
                for (int col = 0; col < numCols; col++) {
                    roomMap[row][col] = Byte.parseByte(tokens[col]);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    void chooseMap(){
        if(type == Classic) {
            mapFilepath = "/Map/currentmap" + (new Random().nextInt(4) + 1) + ".map";
        } else if (type == Loot){
            mapFilepath = "/Map/lootroom.map";
        } else if (type == Shop){
            mapFilepath = "/Map/shoproom.map";
        }else if(type == Starter || type == Boss) {
            mapFilepath = "/Map/currentmap2.map";
        } else if(type == Progress){
            mapFilepath = "/Map/progressroom.map";
        }
    }

    public byte[][] getRoomMap() {
        return roomMap;
    }

    public int getType() {
        return type;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getId(){ return id;}

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public void setTop(boolean top) {
        minimapRoom.setTop(top);
        this.top = top;
    }

    public void setLeft(boolean left) {
        minimapRoom.setLeft(left);
        this.left = left;
    }

    public void setBottom(boolean bottom) {
        minimapRoom.setBottom(bottom);
        this.bottom = bottom;
    }

    public void setRight(boolean right) {
        minimapRoom.setRight(right);
        this.right = right;
    }

    public void setCorners(int xMin,int xMax,int yMin, int yMax){
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.yMin = yMin;
    }

    public int getyMin() { return yMin; }

    public int getxMax() { return xMax; }

    public int getxMin() { return xMin; }

    public int getyMax() { return yMax; }

    boolean hasEntered(){ return entered; }

    /**
     * entering room in singleplayer
     * @param tileMap
     */
    public void entered(TileMap tileMap){
        entered = true;
        if (type == Room.Classic){
            int maxMobs = cz.Empatix.Java.Random.nextInt(4) + 2+tileMap.getFloor();
            int tileSize = tileMap.getTileSize();
            for (int i = 0; i < maxMobs;i++){
                EnemyManager enemyManager = EnemyManager.getInstance();
                enemyManager.addEnemy(xMin+tileSize,xMax-tileSize,yMin+tileSize,yMax-tileSize);
            }
            lockRoom(true);
        } else if (type == Room.Boss){

            int y=yMin + (yMax - yMin) / 2;
            int x=xMin + (xMax - xMin) / 2;
            EnemyManager enemyManager = EnemyManager.getInstance();
            enemyManager.spawnBoss(x,y);

            AudioManager.playSoundtrack(Soundtrack.BOSS);

            lockRoom(true);
        }
    }

    /**
     * entering room in multiplayer
     * @param tileMap
     */
    public void enteredMP(TileMap tileMap){
        entered = true;
        if (type == Room.Classic){
            int maxMobs = cz.Empatix.Java.Random.nextInt(4) + 2+tileMap.getFloor();
            for (int i = 0; i < maxMobs;i++){
                EnemyManagerMP enemyManager = EnemyManagerMP.getInstance();
                enemyManager.addEnemy(xMin,xMax,yMin,yMax);
            }
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Network.LockRoom enteringRoom = new Network.LockRoom();
            mpManager.server.requestACK(enteringRoom,enteringRoom.idPacket);
            enteringRoom.idRoom = (byte)id;
            enteringRoom.lock = true;
            Server server = mpManager.server.getServer();
            server.sendToAllUDP(enteringRoom);
            lockRoom(true);
        } else if (type == Room.Boss){

            int y=yMin + (yMax - yMin) / 2;
            int x=xMin + (xMax - xMin) / 2;
            EnemyManagerMP enemyManager = EnemyManagerMP.getInstance();
            enemyManager.spawnBoss(x,y);

            AudioManager.playSoundtrack(Soundtrack.BOSS);

            Network.LockRoom enteringRoom = new Network.LockRoom();
            enteringRoom.idRoom = (byte)id;
            enteringRoom.lock = true;
            Server server = MultiplayerManager.getInstance().server.getServer();
            server.sendToAllUDP(enteringRoom);
            lockRoom(true);
        }
    }
    public void lockRoom(boolean b){
        closed = b;
        for(RoomObject obj:mapObjects){
            if(obj instanceof PathWall) obj.collision=b;
        }
    }


    public boolean isRight() {
        return right;
    }

    public boolean isBottom() {
        return bottom;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isTop() {
        return top;
    }


    public RoomPath[] getRoomPaths() {
        return roomPaths;
    }
    public void setTopRoomPath(RoomPath roomPath){
        roomPaths[0] = roomPath;
    }
    public void setBottomRoomPath(RoomPath roomPath){
        roomPaths[1] = roomPath;
    }
    public void setLeftRoomPath(RoomPath roomPath){
        roomPaths[2] = roomPath;
    }
    public void setRightRoomPath(RoomPath roomPath){
        roomPaths[3] = roomPath;
    }

    public void preDrawObjects(boolean behindCollision){
        for(RoomObject object : mapObjects){
            if(object.isPreDraw() && object.isBehindCollision() == behindCollision){
                object.draw();
            }
        }
    }
    public void drawObjects(TileMap tm){
        for(RoomObject object : mapObjects){
            if(!object.isPreDraw())object.draw();
        }
        if(type == Starter && tm.getFloor() == 0) {
            if(texts == null){
                texts = new TextRender[5];
                for(int i = 0;i<5;i++){
                    texts[i] = new TextRender();
                }
            }
            if(tm.getCurrentRoom() == this){
                int y = yMin + (yMax - yMin) / 2;
                int x = xMin + (xMax - xMin) / 2;

                float time = (float) Math.sin(System.currentTimeMillis() % 2000 / 600f) + (1 - (float) Math.cos((System.currentTimeMillis() % 2000 / 600f) + 0.5f));
                texts[0].drawMap("WASD - Movement", new Vector3f(x, y, 0), 2,
                        new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
                texts[1].drawMap("Mouse click - shoot", new Vector3f(x, y + 50, 0), 2,
                        new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
                texts[2].drawMap("1 and 2 - weapon slots", new Vector3f(x, y + 100, 0), 2,
                        new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
                texts[3].drawMap("E/Q - pickup/drop gun", new Vector3f(x, y + 150, 0), 2,
                        new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));
                texts[4].drawMap("F - use artefact", new Vector3f(x, y + 200, 0), 2,
                        new Vector3f((float) Math.sin(time), (float) Math.cos(0.5f + time), 1f));

            }
        }
    }
    public void updateObjects(TileMap tm){
        for(int i = 0;i<mapObjects.size();i++){
            RoomObject object = mapObjects.get(i);
            if(object instanceof DestroyableObject){
                if(((DestroyableObject) object).canDrop()){
                    ((DestroyableObject)object).itemDropped();
                    if(Math.random() > 0.6){
                        if(!MultiplayerManager.multiplayer){
                            ItemManager itemManager = ItemManager.getInstance();
                            ItemDrop drop = itemManager.createDrop(object.getX(),object.getY());
                            if(((DestroyableObject)object).isPreventItemDespawn()){
                                drop.preventDespawn();
                            }
                        } else if(tm.isServerSide()){
                            ItemManagerMP itemManager = ItemManagerMP.getInstance();
                            ItemDrop drop = itemManager.createDrop(object.getX(),object.getY());
                            if(((DestroyableObject)object).isPreventItemDespawn()){
                                drop.preventDespawn();
                            }
                        }
                    }
                }
            }
            if(object.shouldRemove()){
                mapObjects.remove(i);
                i--;
                continue;
            }
            object.update();
        }
        if(type == Boss || type == Classic) {
            // multiplayer - handling client/server side locking of rooms via packets
            if(tm.isServerSide()){
                EnemyManagerMP enemyManager = EnemyManagerMP.getInstance();
                if (enemyManager.areEnemiesDeadInCoords(xMin,xMax,yMin,yMax) && closed) {
                    Network.LockRoom lockRoom = new Network.LockRoom();
                    lockRoom.idRoom = (byte)id;
                    lockRoom.lock = false;
                    Server server = MultiplayerManager.getInstance().server.getServer();
                    server.sendToAllUDP(lockRoom);
                    lockRoom(false);
                    // adds 1 point to artifact for cleared room
                    ArtefactManagerMP am = ArtefactManagerMP.getInstance();
                    am.charge();
                }
            } else {
                // singleplayer
                if(!MultiplayerManager.multiplayer){
                    EnemyManager enemyManager = EnemyManager.getInstance();
                    if (enemyManager.areEnemiesDead() && closed) {
                        lockRoom(false);
                        // adds 1 point to artifact for cleared room
                        ArtefactManager am = ArtefactManager.getInstance();
                        am.charge();
                    }
                }
            }
        }
    }
    public void createObjects(TileMap tm, Player[] player) {
        if(type == Classic){
            int num = cz.Empatix.Java.Random.nextInt(3);

            // spikes
            for(int i = 0;i<num;i++){
                addSpike(tm);
            }
            // flags
            num = cz.Empatix.Java.Random.nextInt(3);
            for(int i = 0;i<num;i++) {
                addFlag(tm);
            }
            for(int i = 0;i<3;i++){
                if(Math.random() > 0.5){
                    addBones(tm);
                }
            }
            for(int i = 0;i<2;i++){
                if(Math.random() > 0.5){
                    addBarrel(tm);
                }
            }
            // arrows traps
            for(int i = 0; i< cz.Empatix.Java.Random.nextInt(2); i++) {
                addArrowTrap(tm,player);
            }
            // torches
            for(int i = 0;i<5;i++) {
                addTorch(tm);
            }

            if(Math.random() < 0.2){
                if(left){
                    addFlamethrower(tm,player);
                }
            }
        }
        if (type == Loot) {
            Chest chest = new Chest(tm);
            chest.enableDropWeapon();
            chest.setPosition(xMin + (float) (xMax - xMin) / 2, yMin + (float) (yMax - yMin) / 2);
            mapObjects.add(chest);
            sendAddRoomObjectPacket(chest,tm);

            int tileSize = tm.getTileSize();

            Torch torch = new Torch(tm);
            int type = Torch.TOP;
            torch.setType(type);
            torch.setPosition(xMin + 4 * tileSize + tileSize / 2, yMin + 4 * tileSize + tileSize / 2);
            mapObjects.add(torch);
            sendAddRoomObjectPacket(torch,tm);

            torch = new Torch(tm);
            type = Torch.TOP;
            torch.setType(type);
            torch.setPosition(xMin + 4 * tileSize + tileSize / 2, yMin + 10 * tileSize + tileSize / 2);
            mapObjects.add(torch);
            sendAddRoomObjectPacket(torch,tm);

            torch = new Torch(tm);
            torch.setType(type);
            torch.setPosition(xMin + 15 * tileSize + tileSize / 2, yMin + 4 * tileSize + tileSize / 2);
            mapObjects.add(torch);
            sendAddRoomObjectPacket(torch,tm);

            torch = new Torch(tm);
            type = Torch.TOP;
            torch.setType(type);
            torch.setPosition(xMin + 15 * tileSize + tileSize / 2, yMin + 10 * tileSize + tileSize / 2);
            mapObjects.add(torch);
            sendAddRoomObjectPacket(torch,tm);
        }
        if(type == Shop){
            for(int i = 1;i<=3;i++){
                ShopTable table = new ShopTable(tm);
                table.setPosition(xMin+ (float) (xMax - xMin) / 4 * i,yMin + (float) (yMax - yMin) / 2);
                table.createItem();
                mapObjects.add(table);
                sendAddRoomObjectPacket(table,tm);

                if(i == 2){
                    Shopkeeper shopkeeper = new Shopkeeper(tm);
                    shopkeeper.setPosition(xMin+ (float) (xMax - xMin) / 4 * i,yMin + (float) (yMax - yMin) / 2 - 300);
                    this.addObject(shopkeeper);
                    sendAddRoomObjectPacket(shopkeeper,tm);

                }
            }
            int tileSize = tm.getTileSize();
            Pot pot = new Pot(tm);
            pot.setPosition(xMin+2*tileSize+tileSize/2,yMax-2*tileSize-tileSize/2);
            addObject(pot);
            sendAddRoomObjectPacket(pot,tm);

            pot = new Pot(tm);
            pot.setPosition(xMax-2*tileSize-tileSize/2,yMax-2*tileSize-tileSize/2);
            addObject(pot);
            sendAddRoomObjectPacket(pot,tm);

            Crystal crystal = new Crystal(tm);
            crystal.setPosition(xMin+4*tileSize+tileSize/2,yMin+3*tileSize+tileSize/2);
            addObject(crystal);
            sendAddRoomObjectPacket(crystal,tm);

            crystal = new Crystal(tm);
            crystal.setPosition(xMax-4*tileSize-tileSize/2,yMin+3*tileSize+tileSize/2);
            addObject(crystal);
            sendAddRoomObjectPacket(crystal,tm);
        }
        if(type == Boss){
            int tileSize = tm.getTileSize();
            for(int i = 4;i<6;i++){
                for(int j = 4;j<6;j++){
                    if(i == 4 && j == 4) continue;
                    Barrel barrel = new Barrel(tm);
                    barrel.setPosition(xMin + i*tileSize, yMin + j*tileSize);
                    barrel.setPreventItemDespawn(true);
                    mapObjects.add(barrel);
                    sendAddRoomObjectPacket(barrel,tm);

                    barrel = new Barrel(tm);
                    barrel.setPosition(xMax - i*tileSize, yMin + j*tileSize);
                    barrel.setPreventItemDespawn(true);
                    mapObjects.add(barrel);
                    sendAddRoomObjectPacket(barrel,tm);

                    barrel = new Barrel(tm);
                    barrel.setPosition(xMax - i*tileSize, yMax - j*tileSize);
                    barrel.setPreventItemDespawn(true);
                    mapObjects.add(barrel);
                    sendAddRoomObjectPacket(barrel,tm);

                    barrel = new Barrel(tm);
                    barrel.setPosition(xMin + i*tileSize, yMax - j*tileSize);
                    barrel.setPreventItemDespawn(true);
                    mapObjects.add(barrel);
                    sendAddRoomObjectPacket(barrel,tm);

                }
            }
        }
        if(type == Progress){
            int tileSize = tm.getTileSize();
            Portal portal = new Portal(tm);
            portal.setPosition((float) (numCols*tileSize) / 2,  2*tileSize);
            mapObjects.add(portal);
            // flags
            Flag flag = new Flag(tm);
            flag.setPosition(12*tileSize+tileSize/2,3*tileSize-tileSize/2);
            mapObjects.add(flag);
            flag = new Flag(tm);
            flag.setPosition(16*tileSize+tileSize/2,3*tileSize-tileSize/2);
            mapObjects.add(flag);

            // torches
            Torch torch = new Torch(tm);
            torch.setType(Torch.TOP);
            torch.setPosition(11*tileSize+tileSize/2,3*tileSize-tileSize/2);
            mapObjects.add(torch);
            torch = new Torch(tm);
            torch.setType(Torch.TOP);
            torch.setPosition(17*tileSize+tileSize/2,3*tileSize-tileSize/2);
            mapObjects.add(torch);
            torch = new Torch(tm);
            torch.setType(Torch.SIDELEFT);
            torch.setPosition(27*tileSize+tileSize/2,3*tileSize-tileSize/2);
            mapObjects.add(torch);
            torch = new Torch(tm);
            torch.setType(Torch.SIDERIGHT);
            torch.setPosition(tileSize+tileSize/2,3*tileSize-tileSize/2);
            mapObjects.add(torch);
            // bookshelfs
            // barrels
            for(int i = 13;i<16;i++){
                Barrel barrel = new Barrel(tm);
                barrel.setPosition(i*tileSize+tileSize/2,7*tileSize+tileSize/2);
                barrel.setMoveable(false);
                this.addObject(barrel);
            }

            Bookshelf bookshelf = new Bookshelf(tm);
            bookshelf.setPosition(21*tileSize,tileSize+48);
            mapObjects.add(bookshelf);
            bookshelf = new Bookshelf(tm);
            bookshelf.setPosition(23*tileSize,tileSize+48);
            mapObjects.add(bookshelf);
            bookshelf = new Bookshelf(tm);
            bookshelf.setPosition(25*tileSize,tileSize+48);
            mapObjects.add(bookshelf);

            Armorstand armorstand = new Armorstand(tm);
            armorstand.setPosition(19*tileSize,tileSize+tileSize/2);
            mapObjects.add(armorstand);
            armorstand = new Armorstand(tm);
            armorstand.setPosition(27*tileSize,tileSize+tileSize/2);
            mapObjects.add(armorstand);
        }

    }

    private void addFlamethrower(TileMap tm, Player[] player) {
        int tileSize = tm.getTileSize();

        int xMinTile = xMin/tileSize - 2;
        int yMinTile = yMin/tileSize + 1;

        int xMaxTile = xMin/tileSize - 1;
        int yMaxTile = yMax/tileSize - 1;

        int x;
        int y;
        Flamethrower flamethrower = new Flamethrower(tm,player);
        flamethrower.setType(Flamethrower.VERTICAL);
        do{
            x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
            y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
            flamethrower.setPosition(x*tileSize+tileSize/2,y*tileSize);
        } while ((tm.getType(y - 1, x) != Tile.BLOCKED || tm.getType(y, x) == Tile.BLOCKED) ||
                intersectsObjects(flamethrower)) ;
        addObject(flamethrower);
        sendAddRoomObjectPacket(flamethrower,tm);
    }

    private void addTorch(TileMap tm) {
        int tileSize = tm.getTileSize();

        int xMinTile = xMin / tileSize + 1;
        int yMinTile = yMin / tileSize + 1;

        int xMaxTile = xMax / tileSize - 1;
        int yMaxTile = yMax / tileSize - 1;

        Torch torch = new Torch(tm);

        int x;
        int y;
        do{
            x = cz.Empatix.Java.Random.nextInt(xMaxTile - xMinTile + 1) + xMinTile;
            y = cz.Empatix.Java.Random.nextInt(yMaxTile - yMinTile + 1) + yMinTile;
            if (tm.getType(y - 1, x) == Tile.BLOCKED && tm.getType(y, x) != Tile.BLOCKED){
                int type = Torch.TOP;
                torch.setType(type);
                torch.setPosition(x * tileSize + tileSize / 2, y * tileSize - tileSize / 2);
            } else if (tm.getType(y, x - 1) == Tile.BLOCKED && tm.getType(y, x) != Tile.BLOCKED){
                int type = Torch.SIDERIGHT;
                torch.setType(type);
                torch.setPosition(x * tileSize + tileSize / 2, y * tileSize + tileSize / 2);
            } else if (tm.getType(y, x + 1) == Tile.BLOCKED && tm.getType(y, x) != Tile.BLOCKED){
                int type = Torch.SIDELEFT;
                torch.setType(type);
                torch.setPosition(x * tileSize + tileSize / 2, y * tileSize + tileSize / 2);
            }
        } while ((tm.getType(y - 1, x) != Tile.BLOCKED || tm.getType(y, x) == Tile.BLOCKED) &&
                (tm.getType(y, x - 1) != Tile.BLOCKED || tm.getType(y, x) == Tile.BLOCKED) &&
                (tm.getType(y, x + 1) != Tile.BLOCKED || tm.getType(y, x) == Tile.BLOCKED) ||
                intersectsObjects(torch)) ;
        addObject(torch);
        sendAddRoomObjectPacket(torch,tm);
    }

    private void addArrowTrap(TileMap tm,Player[] player) {
        int tileSize = tm.getTileSize();

        int xMinTile = xMin/tileSize + 1;
        int yMinTile = yMin/tileSize + 1;

        int xMaxTile = xMax/tileSize - 2;
        int yMaxTile = yMax/tileSize - 2;

        int x;
        int y;
        ArrowTrap arrowTrap = new ArrowTrap(tm,player);
        do{
            x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
            y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
            if(tm.getType(y-1,x) == Tile.BLOCKED && tm.getType(y,x) != Tile.BLOCKED){
                arrowTrap.setPosition(x*tileSize+tileSize/2,y*tileSize-tileSize/2);
                arrowTrap.setType(ArrowTrap.TOP);
            } else if (tm.getType(y, x - 1) == Tile.BLOCKED && tm.getType(y +1, x - 1) == Tile.BLOCKED && tm.getType(y, x) != Tile.BLOCKED){
                arrowTrap.setType(ArrowTrap.LEFT);
                arrowTrap.setPosition(x * tileSize - tileSize/6, y * tileSize + tileSize / 2);
            } else if (tm.getType(y, x + 1) == Tile.BLOCKED && tm.getType(y +1, x + 1) == Tile.BLOCKED && tm.getType(y, x) != Tile.BLOCKED){
                arrowTrap.setType(ArrowTrap.RIGHT);
                arrowTrap.setPosition(x * tileSize + 6*tileSize/5 , y * tileSize + tileSize / 2);
            }
        }while ((tm.getType(y-1,x) != Tile.BLOCKED || tm.getType(y,x) == Tile.BLOCKED) &&
                (tm.getType(y, x - 1) != Tile.BLOCKED || tm.getType(y +1, x - 1) != Tile.BLOCKED || tm.getType(y, x) == Tile.BLOCKED) &&
                (tm.getType(y, x + 1) != Tile.BLOCKED || tm.getType(y +1, x + 1) != Tile.BLOCKED || tm.getType(y, x) == Tile.BLOCKED)  ||
                intersectsObjects(arrowTrap));
        this.addObject(arrowTrap);
        sendAddRoomObjectPacket(arrowTrap,tm);
    }

    public ArrayList<RoomObject> getMapObjects(){return mapObjects;}

    public void addWall(TileMap tm, float x, float y,int dir){

        PathWall roomPath = new PathWall(tm);
        roomPath.setDirection(dir);
        roomPath.setPosition(x,y);
        mapObjects.add(roomPath);
    }
    public void addObject(RoomObject obj){
        mapObjects.add(obj);
    }
    public void removeObject(RoomObject obj){
        mapObjects.remove(obj);
    }

    public void setMinimapRoom(MMRoom minimapRoom) {
        this.minimapRoom = minimapRoom;
    }
    public void showRoomOnMinimap() {
        minimapRoom.setDiscovered(true);
        minimapRoom.entered();
    }

    public MMRoom getMinimapRoom() {
        return minimapRoom;
    }
    public void keyPressed(int k, Player p){
        if(k == ControlSettings.getValue(ControlSettings.OBJECT_INTERACT)){
            for(RoomObject object : mapObjects){
                if(object.intersects(p)){
                    object.keyPress();
                }
            }
        }
    }
    private boolean intersectsObjects(RoomObject object){
        for(RoomObject roomObject: mapObjects){
            if(roomObject.intersects(object)){
                return true;
            }
        }
        return false;
    }

    public String getMapFilepath() {
        return mapFilepath;
    }

    public void sendAddRoomObjectPacket(RoomObject object, TileMap tm){
        if(!tm.isServerSide()) return;
        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        Network.AddRoomObject roomObject = new Network.AddRoomObject();
        mpManager.server.requestACK(roomObject,roomObject.idPacket);
        if(object instanceof Spike){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.SPIKE;
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof Flag){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.FLAG;
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof Bones){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.BONES;
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof Barrel){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.BARREL;
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof ArrowTrap){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.ARROWTRAP;
            roomObject.objectType = (byte)((ArrowTrap) object).getType();
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof Torch){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.TORCH;
            roomObject.objectType = (byte)((Torch) object).getType();
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof Flamethrower){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.FLAMETHROWER;
            roomObject.objectType = (byte)((Flamethrower) object).getType();
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof Pot){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.POT;
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof Chest){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.CHEST;
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof Shopkeeper){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.SHOPKEEPER;
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof ShopTable){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.SHOPTABLE;
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        } else if (object instanceof Crystal){
            roomObject.x = (int)object.getX();
            roomObject.y = (int)object.getY();
            roomObject.type = Network.TypeRoomObject.CRYSTAL;
            roomObject.id = object.getId();
            roomObject.idRoom = this.id;
        }
        Server server = mpManager.server.getServer();
        server.sendToAllUDP(roomObject);
    }

    public void resetEntering() {
        entered = false;
    }
    public boolean hasBeenEntered(){return entered;}

    private void addSpike(TileMap tm){
        int tileSize = tm.getTileSize();

        int xMinTile = xMin/tileSize + 3;
        int yMinTile = yMin/tileSize + 3;

        int xMaxTile = xMax/tileSize - 3;
        int yMaxTile = yMax/tileSize - 3;

        int x;
        int y;

        Spike spike = new Spike(tm);

        boolean done = false;
        while(!done){
            boolean collision = false;

            x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
            y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
            A: for(int j = -1;j<2;j++){
                for(int k = -1;k<2;k++){
                    if(tm.getType(y+j,x+k) == Tile.BLOCKED){
                        collision=true;
                        break A;
                    }
                }
            }

            spike.setPosition(x*tileSize+tileSize/2,y*tileSize+tileSize/2);
            if(!collision) done = true;
            if(hasRoomObjectCollision(spike)) done = false;
        }
        addObject(spike);
        sendAddRoomObjectPacket(spike,tm);
    }
    private void addFlag(TileMap tm) {
        int tileSize = tm.getTileSize();

        int xMinTile = xMin/tileSize + 1;
        int yMinTile = yMin/tileSize + 1;

        int xMaxTile = xMax/tileSize - 1;
        int yMaxTile = yMax/tileSize - 1;

        int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
        int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
        while(tm.getType(y-1,x) != Tile.BLOCKED || tm.getType(y,x) == Tile.BLOCKED){
            x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
            y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
        }
        Flag flag = new Flag(tm);
        flag.setPosition(x*tileSize+tileSize/2,y*tileSize-tileSize/2);
        this.addObject(flag);
        sendAddRoomObjectPacket(flag,tm);
    }
    private void addBones(TileMap tm) {
        int tileSize = tm.getTileSize();

        int xMinTile = xMin/tileSize + 1;
        int yMinTile = yMin/tileSize + 1;

        int xMaxTile = xMax/tileSize - 1;
        int yMaxTile = yMax/tileSize - 1;

        Bones bones = new Bones(tm);

        int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
        int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
        bones.setPosition(x*tileSize+tileSize/2,y*tileSize+tileSize/2);

        while(tm.getType(y,x) == Tile.BLOCKED || hasRoomObjectCollision(bones)){
            x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
            y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
            bones.setPosition(x*tileSize+tileSize/2,y*tileSize+tileSize/2);
        }
        this.addObject(bones);
        sendAddRoomObjectPacket(bones,tm);
    }
    private void addBarrel(TileMap tm) {
        int tileSize = tm.getTileSize();

        int xMinTile = xMin/tileSize + 3;
        int yMinTile = yMin/tileSize + 3;

        int xMaxTile = xMax/tileSize - 3;
        int yMaxTile = yMax/tileSize - 3;

        Barrel barrel = new Barrel(tm);

        int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
        int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
        barrel.setPosition(x*tileSize+tileSize/2,y*tileSize+tileSize/2);

        while(tm.getType(y,x) == Tile.BLOCKED || hasRoomObjectCollision(barrel)){
            x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
            y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
            barrel.setPosition(x*tileSize+tileSize/2,y*tileSize+tileSize/2);
        }
        this.addObject(barrel);
        sendAddRoomObjectPacket(barrel,tm);
    }

    /**
     * checks defined room object if it has collided with some others room objects
     * @return true if collision is detected, false if not
     */
    public boolean hasRoomObjectCollision(RoomObject o){
        for(RoomObject othObj: mapObjects){
            if(o.intersects(othObj)) return true;
        }
        return false;
    }

}

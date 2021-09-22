package cz.Empatix.Render;

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
import cz.Empatix.Render.Hud.Minimap.MMRoom;
import cz.Empatix.Render.RoomObjects.*;
import cz.Empatix.Render.RoomObjects.ProgressRoom.Portal;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Room implements Serializable {
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



    Room(int type, int id, int x, int y){
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

    Room(int type, int id, int x, int y, String mapFilepath){
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
    public void loadSave(){
        texts = new TextRender[5];
        for(int i = 0;i<5;i++){
            texts[i] = new TextRender();
        }
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

    byte[][] getRoomMap() {
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

    int getId(){ return id;}

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    void setTop(boolean top) {
        minimapRoom.setTop(top);
        this.top = top;
    }

    void setLeft(boolean left) {
        minimapRoom.setLeft(left);
        this.left = left;
    }

    void setBottom(boolean bottom) {
        minimapRoom.setBottom(bottom);
        this.bottom = bottom;
    }

    void setRight(boolean right) {
        minimapRoom.setRight(right);
        this.right = right;
    }

    void setCorners(int xMin,int xMax,int yMin, int yMax){
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

    void entered(TileMap tileMap){
        entered = true;
        if (type == Room.Classic){

            int maxMobs = cz.Empatix.Java.Random.nextInt(4) + 2+tileMap.getFloor();


            for (int i = 0; i < maxMobs;i++){
                EnemyManager enemyManager = EnemyManager.getInstance();

                // multiplayer
                if(MultiplayerManager.multiplayer){
                    MultiplayerManager mpManager = MultiplayerManager.getInstance();
                    if(mpManager.isHost()) enemyManager.addEnemy(xMin,xMax,yMin,yMax);
                }
                // singleplayer
                else {
                    enemyManager.addEnemy(xMin,xMax,yMin,yMax);
                }
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
    private void lockRoom(boolean b){
        closed = b;
        for(RoomObject obj:mapObjects){
            if(obj instanceof PathWall) obj.collision=b;
        }
    }


    boolean isRight() {
        return right;
    }

    boolean isBottom() {
        return bottom;
    }

    boolean isLeft() {
        return left;
    }

    boolean isTop() {
        return top;
    }


    RoomPath[] getRoomPaths() {
        return roomPaths;
    }
    void setTopRoomPath(RoomPath roomPath){
        roomPaths[0] = roomPath;
    }
    void setBottomRoomPath(RoomPath roomPath){
        roomPaths[1] = roomPath;
    }
    void setLeftRoomPath(RoomPath roomPath){
        roomPaths[2] = roomPath;
    }
    void setRightRoomPath(RoomPath roomPath){
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
    public void updateObjects(){
        for(int i = 0;i<mapObjects.size();i++){
            RoomObject object = mapObjects.get(i);
            if(object instanceof DestroyableObject){
                if(((DestroyableObject) object).canDrop()){
                    ((DestroyableObject)object).itemDropped();
                    if(Math.random() > 0.6){
                        ItemManager itemManager = ItemManager.getInstance();
                        ItemDrop drop = itemManager.createDrop(object.getX(),object.getY());
                        if(((DestroyableObject)object).isPreventItemDespawn()){
                            drop.preventDespawn();
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
            EnemyManager enemyManager = EnemyManager.getInstance();
            if (enemyManager.areEnemiesDead() && closed) {
                lockRoom(false);
                // adds 1 point to artifact for cleared room
                ArtefactManager am = ArtefactManager.getInstance();
                am.charge();
            }
        }
    }
    public void createObjects(TileMap tm, Player player) {
        if(type == Classic){
            int num = cz.Empatix.Java.Random.nextInt(3);

            // spikes
            for(int i = 0;i<num;i++){
                int tileSize = tm.getTileSize();

                int xMinTile = xMin/tileSize + 3;
                int yMinTile = yMin/tileSize + 3;

                int xMaxTile = xMax/tileSize - 3;
                int yMaxTile = yMax/tileSize - 3;

                int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;

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
                    if(!collision) done = true;
                }
                Spike spike = new Spike(tm);
                spike.setPosition(x*tileSize+tileSize/2,y*tileSize+tileSize/2);
                addObject(spike);
            }
            // flags
            num = cz.Empatix.Java.Random.nextInt(3);
            for(int i = 0;i<num;i++) {
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
            }
            for(int i = 0;i<3;i++){
                if(Math.random() > 0.5){
                    int tileSize = tm.getTileSize();

                    int xMinTile = xMin/tileSize + 1;
                    int yMinTile = yMin/tileSize + 1;

                    int xMaxTile = xMax/tileSize - 1;
                    int yMaxTile = yMax/tileSize - 1;

                    int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                    int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
                    while(tm.getType(y,x) == Tile.BLOCKED){
                        x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                        y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
                    }
                    Bones bones = new Bones(tm);
                    bones.setPosition(x*tileSize+tileSize/2,y*tileSize+tileSize/2);
                    this.addObject(bones);
                }
            }
            for(int i = 0;i<2;i++){
                if(Math.random() > 0.5){
                    int tileSize = tm.getTileSize();

                    int xMinTile = xMin/tileSize + 3;
                    int yMinTile = yMin/tileSize + 3;

                    int xMaxTile = xMax/tileSize - 3;
                    int yMaxTile = yMax/tileSize - 3;

                    int x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                    int y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
                    while(tm.getType(y,x) == Tile.BLOCKED){
                        x = cz.Empatix.Java.Random.nextInt(xMaxTile-xMinTile+1)+xMinTile;
                        y = cz.Empatix.Java.Random.nextInt(yMaxTile-yMinTile+1)+yMinTile;
                    }
                    Barrel barrel = new Barrel(tm);
                    barrel.setPosition(x*tileSize+tileSize/2,y*tileSize+tileSize/2);
                    this.addObject(barrel);
                }
            }
            // arrows traps
            for(int i = 0; i< cz.Empatix.Java.Random.nextInt(2); i++) {
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
            }
            // torches
            for(int i = 0;i<5;i++) {
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
            }

            if(Math.random() < 0.2){
                if(left){
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
                }
            }
        }
        if (type == Loot) {
            Chest chest = new Chest(tm);
            chest.enableDropWeapon();
            chest.setPosition(xMin + (float) (xMax - xMin) / 2, yMin + (float) (yMax - yMin) / 2);
            mapObjects.add(chest);

            int tileSize = tm.getTileSize();

            Torch torch = new Torch(tm);
            int type = Torch.TOP;
            torch.setType(type);
            torch.setPosition(xMin + 4 * tileSize + tileSize / 2, yMin + 4 * tileSize + tileSize / 2);
            mapObjects.add(torch);

            torch = new Torch(tm);
            type = Torch.TOP;
            torch.setType(type);
            torch.setPosition(xMin + 4 * tileSize + tileSize / 2, yMin + 10 * tileSize + tileSize / 2);
            mapObjects.add(torch);

            torch = new Torch(tm);
            torch.setType(type);
            torch.setPosition(xMin + 15 * tileSize + tileSize / 2, yMin + 4 * tileSize + tileSize / 2);
            mapObjects.add(torch);

            torch = new Torch(tm);
            type = Torch.TOP;
            torch.setType(type);
            torch.setPosition(xMin + 15 * tileSize + tileSize / 2, yMin + 10 * tileSize + tileSize / 2);
            mapObjects.add(torch);
        }
        if(type == Shop){
            for(int i = 1;i<=3;i++){
                ShopTable table = new ShopTable(tm);
                table.setPosition(xMin+ (float) (xMax - xMin) / 4 * i,yMin + (float) (yMax - yMin) / 2);
                table.createItem();
                mapObjects.add(table);

                if(i == 2){
                    Shopkeeper shopkeeper = new Shopkeeper(tm);
                    shopkeeper.setPosition(xMin+ (float) (xMax - xMin) / 4 * i,yMin + (float) (yMax - yMin) / 2 - 300);
                    this.addObject(shopkeeper);
                }
            }
            int tileSize = tm.getTileSize();
            Pot pot = new Pot(tm);
            pot.setPosition(xMin+2*tileSize+tileSize/2,yMax-2*tileSize-tileSize/2);
            addObject(pot);

            pot = new Pot(tm);
            pot.setPosition(xMax-2*tileSize-tileSize/2,yMax-2*tileSize-tileSize/2);
            addObject(pot);
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

                    barrel = new Barrel(tm);
                    barrel.setPosition(xMax - i*tileSize, yMin + j*tileSize);
                    barrel.setPreventItemDespawn(true);
                    mapObjects.add(barrel);

                    barrel = new Barrel(tm);
                    barrel.setPosition(xMax - i*tileSize, yMax - j*tileSize);
                    barrel.setPreventItemDespawn(true);
                    mapObjects.add(barrel);

                    barrel = new Barrel(tm);
                    barrel.setPosition(xMin + i*tileSize, yMax - j*tileSize);
                    barrel.setPreventItemDespawn(true);
                    mapObjects.add(barrel);
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
        }

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
    public boolean intersectsObjects(RoomObject object){
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
}

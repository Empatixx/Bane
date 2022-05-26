package cz.Empatix.Entity;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.AI.Path;
import cz.Empatix.Entity.AI.PathNode;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.GameServer;
import cz.Empatix.Multiplayer.Interpolator;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Damageindicator.CombatIndicator;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Room;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public abstract class Enemy extends MapObject{
    protected int health;
    protected int maxHealth;
    protected boolean dead;
    protected int damage;

    // AI vars
    protected int type;
    protected static final  int melee = 0;
    protected static final  int shooter = 1;
    protected static final  int hybrid = 2;

    private Path[] path;

    protected final Player[] player;
    public int[] px;
    public int[] py;

    public int[] playerTileX;
    public int[] playerTileY;

    protected boolean itemDropped;

    transient private Shader outlineShader;
    transient private Shader spawnShader;

    protected long lastTimeDamaged;
    protected long spawnTime;
    protected boolean reflectBullets;

    private long lastTimeSync = -1;

    private boolean ragingActivated;

    private long lastReg;

    public Enemy(TileMap tm, Player player) {
        super(tm);
        this.player = new Player[1];
        playerTileX = new int[1];
        playerTileY = new int[1];
        px = new int[1];
        px = new int[1];
        py = new int[1];

        path = new Path[1];

        this.player[0] = player;
        itemDropped=false;
        reflectBullets = false;

        outlineShader = ShaderManager.getShader("shaders\\outline");
        if (outlineShader == null){
            outlineShader = ShaderManager.createShader("shaders\\outline");
        }

        spawnShader = ShaderManager.getShader("shaders\\spawn");
        if (spawnShader == null){
            spawnShader = ShaderManager.createShader("shaders\\spawn");
        }

        spawnTime=System.currentTimeMillis()-InGame.deltaPauseTime();
    }

    public Enemy(TileMap tm, Player[] player) {
        super(tm);
        this.player = player;
        playerTileX = new int[2];
        playerTileY = new int[2];
        px = new int[2];
        py = new int[2];

        path = new Path[2];

        itemDropped=false;
        reflectBullets = false;

        if(!tm.isServerSide()){
            outlineShader = ShaderManager.getShader("shaders\\outline");
            if (outlineShader == null){
                outlineShader = ShaderManager.createShader("shaders\\outline");
            }

            spawnShader = ShaderManager.getShader("shaders\\spawn");
            if (spawnShader == null){
                spawnShader = ShaderManager.createShader("shaders\\spawn");
            }
        }
        spawnTime=System.currentTimeMillis()-InGame.deltaPauseTime();

        if(MultiplayerManager.multiplayer && !tm.isServerSide()) interpolator = new Interpolator(this,1/30f);

    }

    public boolean isSpawning(){
        return System.currentTimeMillis()-InGame.deltaPauseTime()-spawnTime < 1000;
    }
    @Override
    public void draw() {
        if(Game.displayCollisions) {
            for(int i = 0;i<player.length;i++){
                if(player[i] != null) {
                    if (path[i] != null) {
                        glBegin(GL_LINE_STRIP);
                        for (PathNode node : path[i].getPathNodes()) {
                            glPointSize(10);
                            glColor3i(255, 0, 0);
                            glVertex2f(node.getX() + xmap, node.getY() + ymap);
                        }
                        glEnd();

                    }
                }
            }
        }
        // pokud neni object na obrazovce - zrusit
        if (isNotOnScrean()){
            return;
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

        if(isSpawning()){
            spawnShader.bind();
            spawnShader.setUniformi("sampler",0);
            spawnShader.setUniformm4f("projection",target);
            float spawnTime = (float)(System.currentTimeMillis()-InGame.deltaPauseTime()-this.spawnTime)/1000;
            spawnShader.setUniformf("spawn",spawnTime);

            glActiveTexture(GL_TEXTURE0);
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

            spawnShader.unbind();
            glBindTexture(GL_TEXTURE_2D,0);
            glActiveTexture(GL_TEXTURE0);


        } else {
            super.draw();
        }

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
        long time = System.currentTimeMillis() - lastTimeDamaged - InGame.deltaPauseTime();
        if (time < 500) {
            outlineShader.bind();
            outlineShader.setUniformi("sampler", 0);
            outlineShader.setUniformm4f("projection", target);
            int maxWidth = (spriteSheetCols + 1) * 5 + spriteSheetCols * width;
            int maxHeight = (spriteSheetRows + 1) * 5 + spriteSheetRows * height;
            outlineShader.setUniform2f("stepSize", new Vector2f(2f / maxWidth, 2f / maxHeight));
            float alpha = 1 - (float) (System.currentTimeMillis() - lastTimeDamaged - InGame.deltaPauseTime()) / 500;
            outlineShader.setUniformf("outlineAlpha", alpha);
            outlineShader.setUniform3f("color", new Vector3f(1.0f,0f,0f));

            glActiveTexture(GL_TEXTURE0);
            spritesheet.bindTexture();

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);


            glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);


            glBindBuffer(GL_ARRAY_BUFFER, animation.getFrame().getVbo());
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            outlineShader.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(GL_TEXTURE0);

        }
    }

    public boolean isDead() { return dead; }

    public int getDamage() { return damage; }

    public void hit(int damage) {
        if(dead) return;
        lastTimeDamaged=System.currentTimeMillis()-InGame.deltaPauseTime();
        health -= damage;
        if(health < 0) health = 0;
        if(health == 0){
            dead = true;
        }
    }

    /**
     * instant death of enemy without drop
     */
    public void setDead(){
        dead = true;
        itemDropped = true;
    }
    protected void EnemyAI() {

        updatePlayerCords();

        if (type == melee) {
            // if loc of any player was changed
            boolean locPlayerChange = false;
            for(int i = 0;i<player.length;i++){
                if(player[i] != null){
                    int pTileX = px[i] / tileSize;
                    int pTileY = py[i] / tileSize;

                    if(playerTileX[i] != pTileX || playerTileY[i] != pTileY){
                        locPlayerChange = true;
                    }
                }
            }
            for (int k = 0; k < player.length; k++) {
                if (player[k] != null && !player[k].isDead()) {

                    Room eroom = tileMap.getRoomByCoords(position.x,position.y);
                    Room proom = tileMap.getRoomByCoords(px[k],py[k]);

                    // player is path room or in another room
                    if(proom == null) continue;
                    if(eroom.getId() != proom.getId()) continue;

                    int enemyTileX = (int) position.x / tileSize;
                    int enemyTileY = (int) position.y / tileSize;
                    int pTileX = px[k] / tileSize;
                    int pTileY = py[k] / tileSize;

                    if (pTileX == enemyTileX && pTileY == enemyTileY) {
                        if (py[k] > position.y) {
                            setDown(true);
                            setUp(false);
                        } else if (py[k] < position.y) {
                            setUp(true);
                            setDown(false);
                        }
                        if (px[k] > position.x) {
                            setRight(true);
                            setLeft(false);
                        } else if (px[k] < position.x) {
                            setRight(false);
                            setLeft(true);
                        }
                        return;
                    } else if (locPlayerChange || path[k] == null) {

                        playerTileX[k] = pTileX;
                        playerTileY[k] = pTileY;


                        ArrayList<PathNode> closed = new ArrayList<>(50);
                        ArrayList<PathNode> opened = new ArrayList<>(50);

                        boolean endFound = false;

                        // reducing math operations
                        final int startingPointX = enemyTileX * tileSize + tileSize / 2;
                        final int startingPointY = enemyTileY * tileSize + tileSize / 2;

                        for (int i = -1; i < 2; i++) {
                            for (int j = -1; j < 2; j++) {
                                if (tileMap.getType(enemyTileY + i, enemyTileX + j) == Tile.NORMAL) {
                                    PathNode node;
                                    if (j == 0 && i == 0) {
                                        closed.add(new PathNode(startingPointX, startingPointY, 0, 0, null));
                                        continue;
                                    } else if (j == 0 || i == 0) {
                                        node = createNode(startingPointX + j * tileSize, startingPointY + i * tileSize, px[k], py[k], null, 10);
                                    } else {
                                        node = createNode(startingPointX + j * tileSize, startingPointY + i * tileSize, px[k], py[k], null, 14);
                                    }

                                    if (i == -1) {
                                        node.setUp(true);
                                    } else if (i == 1) {
                                        node.setDown(true);
                                    }
                                    if (j == -1) {
                                        node.setLeft(true);
                                    } else if (j == 1) {
                                        node.setRight(true);
                                    }
                                    opened.add(node);
                                }
                            }
                        }
                        PathNode theClosest;

                        // setting max X/Y for pathnodes
                        final int maxX = tileMap.getNumCols() * tileSize;
                        final int maxY = tileMap.getNumRows() * tileSize;

                        while (!opened.isEmpty() && !endFound) {
                            theClosest = null;
                            for (PathNode currentNode : opened) {
                                final int x = currentNode.getX() / tileSize;
                                final int y = currentNode.getY() / tileSize;
                                if (x == playerTileX[k] && y == playerTileY[k]) {
                                    endFound = true;
                                    path[k] = new Path(currentNode);
                                    break;
                                } else {
                                    if (theClosest != null) {
                                        if (theClosest.getF() > currentNode.getF()) theClosest = currentNode;
                                    } else {
                                        theClosest = currentNode;
                                    }
                                }
                            }
                            if (theClosest != null && !endFound) {
                                opened.remove(theClosest);
                                closed.add(theClosest);
                                final int x = theClosest.getX();
                                final int y = theClosest.getY();

                                final int xTile = x / tileSize;
                                final int yTile = y / tileSize;

                                boolean right = false, left = false, up = false, down = false;

                                // collision checks
                                if (x - tileSize >= 0) left = tileMap.getType(yTile, xTile - 1) == Tile.NORMAL;
                                if (x + tileSize < maxX) right = tileMap.getType(yTile, xTile + 1) == Tile.NORMAL;
                                if (y - tileSize >= 0) up = tileMap.getType(yTile - 1, xTile) == Tile.NORMAL;
                                if (y + tileSize < maxY) down = tileMap.getType(yTile + 1, xTile) == Tile.NORMAL;

                                PathNode temp;
                                // LEFT
                                temp = createNode(x - tileSize, y, px[k], py[k], theClosest, 10);
                                if (left && doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                    temp.setLeft(true);
                                    opened.add(temp);
                                }
                                // RIGHT
                                temp = createNode(x + tileSize, y, px[k], py[k], theClosest, 10);
                                if (right && doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                    opened.add(temp);
                                    temp.setRight(true);

                                }

                                // DOWN
                                if (down) {
                                    temp = createNode(x, y + tileSize, px[k], py[k], theClosest, 10);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                        temp.setDown(true);
                                        opened.add(temp);
                                    }
                                    // DOWN RIGHT
                                    if (right && tileMap.getType(yTile + 1, xTile + 1) == Tile.NORMAL) {
                                        temp = createNode(x + tileSize, y + tileSize, px[k], py[k], theClosest, 14);
                                        if (doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                            temp.setDown(true);
                                            temp.setRight(true);
                                            opened.add(temp);
                                        }

                                    }
                                    // DOWN LEFT
                                    if (left && tileMap.getType(yTile + 1, xTile - 1) == Tile.NORMAL) {
                                        temp = createNode(x - tileSize, y + tileSize, px[k], py[k], theClosest, 14);
                                        if (doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                            temp.setDown(true);
                                            temp.setLeft(true);
                                            opened.add(temp);
                                        }
                                    }
                                }
                                // UP
                                if (up) {
                                    temp = createNode(x, y - tileSize, px[k], py[k], theClosest, 10);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                        temp.setUp(true);
                                        opened.add(temp);
                                    }
                                    // UP RIGHT
                                    if (right && tileMap.getType(yTile - 1, xTile + 1) == Tile.NORMAL) {
                                        temp = createNode(x + tileSize, y - tileSize, px[k], py[k], theClosest, 14);
                                        if (doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                            temp.setUp(true);
                                            temp.setRight(true);
                                            opened.add(temp);
                                        }
                                    }
                                    // UP LEFT
                                    if (left && tileMap.getType(yTile - 1, xTile - 1) == Tile.NORMAL) {
                                        temp = createNode(x - tileSize, y - tileSize, px[k], py[k], theClosest, 14);
                                        if (doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                            temp.setUp(true);
                                            temp.setLeft(true);
                                            opened.add(temp);
                                        }
                                    }

                                }

                            }
                        }

                    }
                } else {
                    path[k] = null;
                }
            }
            int closestPathIndex = -1;
            for(int i = 0;i<path.length;i++){
                if(path[i] == null) continue;
                if(closestPathIndex == -1) closestPathIndex = i;
                else if(path[closestPathIndex].returnSize() > path[i].returnSize()) closestPathIndex = i;
            }
            if(closestPathIndex == -1){
                setDown(false);
                setUp(false);
                setLeft(false);
                setRight(false);
                return; // all players are dead
            }
            if (px[closestPathIndex] > position.x) facingRight = true;
            else if (px[closestPathIndex] < position.x) facingRight = false;
            if (path[closestPathIndex] != null) {
                PathNode pathNode = path[closestPathIndex].getPathNode();

                setDown(false);
                setUp(false);
                setLeft(false);
                setRight(false);

                // when entity has next pathnode
                boolean redo;
                // booleans if entity passed current x/y coords
                boolean preX, preY;

                do {
                    preX = false;
                    preY = false;
                    int x = pathNode.getX();
                    int y = pathNode.getY();
                    redo = false;
                    // diagonals
                    if (pathNode.isDown() && pathNode.isRight()) {
                        setDown(true);
                        setRight(true);
                        if ((int) position.y >= y) {
                            preY = true;
                            setDown(false);
                        }
                        if ((int) position.x >= x) {
                            preX = true;
                            setRight(false);
                        }
                    } else if (pathNode.isDown() && pathNode.isLeft()) {
                        setDown(true);
                        setLeft(true);
                        if ((int) position.y >= y) {
                            preY = true;
                            setDown(false);
                        }
                        if ((int) position.x <= x) {
                            preX = true;
                            setLeft(false);
                        }
                    } else if (pathNode.isUp() && pathNode.isRight()) {
                        setUp(true);
                        setRight(true);
                        if ((int) position.y <= y) {
                            preY = true;
                            setUp(false);
                        }
                        if ((int) position.x >= x) {
                            preX = true;
                            setRight(false);
                        }
                    } else if (pathNode.isUp() && pathNode.isLeft()) {
                        setUp(true);
                        setLeft(true);
                        if ((int) position.y <= y) {
                            preY = true;
                            setUp(false);
                        }
                        if ((int) position.x <= x) {
                            preX = true;
                            setLeft(false);
                        }
                    }
                    //main directions
                    else if (pathNode.isDown()) {
                        preX = true;
                        setDown(true);
                        setUp(false);
                        if ((int) position.y >= y) {
                            preY = true;
                        }
                        int leftTile = (int) ((position.x - cwidth / 2) / tileSize);
                        int rightTile = (int) ((position.x + cwidth / 2 - 1) / tileSize);
                        int bottomTile = (int) ((position.y + 1 + cheight / 2 - 1) / tileSize);
                        // getting type of tile
                        int bl = tileMap.getType(bottomTile, leftTile);
                        int br = tileMap.getType(bottomTile, rightTile);

                        // pokud tile má hodnotu 1 = collision
                        boolean bottomLeft = bl == Tile.BLOCKED;
                        boolean bottomRight = br == Tile.BLOCKED;
                        if (bottomRight) {
                            setLeft(true);
                            setRight(false);
                        } else if (bottomLeft) {
                            setLeft(false);
                            setRight(true);
                        } else {
                            setLeft(false);
                            setRight(false);
                        }
                    } else if (pathNode.isUp()) {
                        preX = true;
                        setUp(true);
                        setDown(false);
                        if ((int) position.y <= y) {
                            preY = true;
                        }
                        int leftTile = (int) ((position.x - cwidth / 2) / tileSize);
                        int rightTile = (int) ((position.x + cwidth / 2 - 1) / tileSize);
                        int topTile = (int) ((position.y - 1 - cheight / 2) / tileSize);

                        // getting type of tile
                        int tl = tileMap.getType(topTile, leftTile);
                        int tr = tileMap.getType(topTile, rightTile);

                        // pokud tile má hodnotu 1 = collision
                        boolean topLeft = tl == Tile.BLOCKED;
                        boolean topRight = tr == Tile.BLOCKED;
                        if (topRight) {
                            setLeft(true);
                            setRight(false);
                        } else if (topLeft) {
                            setLeft(false);
                            setRight(true);
                        } else {
                            setLeft(false);
                            setRight(false);
                        }
                    } else if (pathNode.isRight()) {
                        preY = true;
                        setRight(true);
                        setLeft(false);
                        if ((int) position.x >= x) {
                            preX = true;
                        }
                        int rightTile = (int) ((position.x + 1 + cwidth / 2 - 1) / tileSize);
                        int topTile = (int) ((position.y - cheight / 2) / tileSize);
                        int bottomTile = (int) ((position.y + cheight / 2 - 1) / tileSize);

                        // getting type of tile
                        int tr = tileMap.getType(topTile, rightTile);
                        int br = tileMap.getType(bottomTile, rightTile);

                        // pokud tile má hodnotu 1 = collision
                        boolean topRight = tr == Tile.BLOCKED;
                        boolean bottomRight = br == Tile.BLOCKED;
                        if (topRight) {
                            setUp(false);
                            setDown(true);
                        } else if (bottomRight) {
                            setDown(false);
                            setUp(true);
                        } else {
                            setDown(false);
                            setUp(false);
                        }
                    } else if (pathNode.isLeft()) {
                        preY = true;
                        setLeft(true);
                        setRight(false);
                        if ((int) position.x <= x) {
                            preX = true;
                        }
                        int leftTile = (int) ((position.x - 1 - cwidth / 2) / tileSize);
                        int topTile = (int) ((position.y - cheight / 2) / tileSize);
                        int bottomTile = (int) ((position.y + cheight / 2 - 1) / tileSize);

                        // getting type of tile
                        int tl = tileMap.getType(topTile, leftTile);
                        int bl = tileMap.getType(bottomTile, leftTile);

                        // pokud tile má hodnotu 1 = collision
                        boolean topLeft = tl == Tile.BLOCKED;
                        boolean bottomLeft = bl == Tile.BLOCKED;
                        if (topLeft) {
                            setUp(false);
                            setDown(true);
                        } else if (bottomLeft) {
                            setDown(false);
                            setUp(true);
                        } else {
                            setDown(false);
                            setUp(false);
                        }
                    }
                    if (preX && preY) {
                        path[closestPathIndex].nextPathNode();
                        if (!path[closestPathIndex].hasLastNode()) {
                            pathNode = path[closestPathIndex].getPathNode();
                        } else {
                            path[closestPathIndex] = null;
                            setDown(false);
                            setUp(false);
                            setRight(false);
                            setLeft(false);
                            return;
                        }
                        redo = true;
                    }
                } while (redo);
            }
        }
    }

    private boolean doesntContain(List<PathNode> closedNodes, PathNode checkNode){
        for (PathNode closedNode: closedNodes
        ) {
            if (closedNode.equals(checkNode)){
                return false;
            }
        }
        return true;
    }
    public PathNode createNode(int x, int y,int pX,int pY,PathNode parent,int moveCost){
        PathNode node;
        if(parent != null){
            node = new PathNode(x,y, Math.abs((x - pX - tileSize / 2) / tileSize) + Math.abs((y - pY - tileSize / 2) / tileSize),parent.getG()+moveCost,parent);
        } else {
            node = new PathNode(x,y, Math.abs((x - pX - tileSize / 2) / tileSize) + Math.abs((y - pY - tileSize / 2) / tileSize),moveCost,parent);
        }
        return node;


    }

    public void update() {
        if(!MultiplayerManager.multiplayer || tileMap.isServerSide()){
            tryEnrage();
            tryRegen();
            // ENEMY AI
            EnemyAI();

            // update position
            getMovementSpeed();
            checkTileMapCollision();
            setPosition(temp.x, temp.y);
        } else {
            interpolator.update(position.x,position.y);
        }
    }
    public void movePacket(){
        if(tileMap.isServerSide()){
            if(GameServer.tick % 2 == 0){ // every second tick send location
                Server server = MultiplayerManager.getInstance().server.getServer();
                Network.MoveEnemy moveEnemy = new Network.MoveEnemy();
                moveEnemy.tick = GameServer.tick;
                moveEnemy.x = position.x;
                moveEnemy.y = position.y;
                moveEnemy.id = id;
                moveEnemy.down = down;
                moveEnemy.up = up;
                moveEnemy.left = left;
                moveEnemy.right = right;
                moveEnemy.facingRight = facingRight;
                server.sendToAllUDP(moveEnemy);
            }
        }
    }

    // ENEMY AI

    private void updatePlayerCords(){
        for(int i = 0;i<player.length;i++){
            if(player[i] != null){
                px[i] = (int)player[i].getX();
                py[i] = (int)player[i].getY();
            }
        }
    }
    public boolean shouldRemove(){
        return animation.hasPlayedOnce() && isDead();
    }

    public boolean canDropItem() {
        return animation.hasPlayedOnce() && isDead() && !itemDropped;
    }

    public void setItemDropped() {
        this.itemDropped = true;
    }

    public boolean canReflect(){return reflectBullets;}

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
     *
     * @return index of the closest player to enemy
     */
    public int theClosestPlayerIndex(){
        int theClosest = 0;
        float dist = position.distance(player[0].getPosition());
        for(int i = 1;i<player.length;i++){
            if(player[i] == null) continue;
            if(player[i].isDead()) continue;
            float newDist = position.distance(player[i].getPosition());
            if(dist > newDist){
                theClosest = i;
                dist = newDist;
            }
        }
        return theClosest;
    }
    /**
     *
     * @return index of the farthest player to enemy
     */
    public int theFarthestPlayerIndex(){
        int theClosest = -1;
        float dist = -1;
        for(int i = 0;i<player.length;i++){
            if(player[i] == null) continue;
            if(player[i].isDead()) continue;
            Room eroom = tileMap.getRoomByCoords(position.x,position.y);
            Room proom = tileMap.getRoomByCoords(player[i].getX(),player[i].getY());

            // player is path room or in another room
            if(proom == null) continue;
            if(eroom.getId() != proom.getId()) continue;

            float newDist = position.distance(player[i].getPosition());
            if(dist < newDist){
                theClosest = i;
                dist = newDist;
            }
        }
        return theClosest;
    }
    public abstract void handleAddEnemyProjectile(Network.AddEnemyProjectile o);
    public abstract void handleMoveEnemyProjectile(Network.MoveEnemyProjectile o);
    public abstract void handleHitEnemyProjectile(Network.HitEnemyProjectile hitPacket);

    public void handleSync(Network.EnemySync sync){
        // packet sync is not old
        if(lastTimeSync < sync.idPacket){
            animation.setTime(sync.time);
            currentAction = sync.currAction;
            animation.setFrames(spritesheet.getSprites(currentAction));
            animation.setFrame(sync.sprite);
            lastTimeSync = sync.idPacket;
        }
    }
    // increase health of enemy
    public void heal(int amount){
        health+=amount;
        if(health > maxHealth) health = maxHealth;
        else {
            if(!tileMap.isServerSide()){
                int cwidth = getCwidth();
                int cheight = getCheight();
                int x = -cwidth/4+ Random.nextInt(cwidth/2);
                CombatIndicator.addHealShow(amount,(int)getX()-x,(int)getY()-cheight/3
                        ,new Vector2f(-x/10f,-30f));
            } else {
                Network.EnemyHealthHeal healPacket = new Network.EnemyHealthHeal();
                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                mpManager.server.requestACK(healPacket,healPacket.idPacket);
                healPacket.id = id;
                healPacket.amount = (short) amount;
                Server server = mpManager.server.getServer();
                server.sendToAllUDP(healPacket);
            }
        }
    }

    public void addInterpolationPosition(Network.MoveEnemy p){
        interpolator.newUpdate(p.tick,new Vector3f(p.x,p.y,0));
    }

    public void setHealth(short health) {
        this.health = health;
    }

    // raging affix
    public void tryEnrage(){
        if(tileMap.isActiveAffix(TileMap.BERSERKS)){
            if((float)health/maxHealth <= 0.2f){
                if(!ragingActivated){
                    moveAcceleration *= 1.3f;
                    movementVelocity *= 1.3f;
                    ragingActivated = true;
                }
            }
        }
    }
    // fortified affix
    public void tryBoostHealth(){
        if(tileMap.isActiveAffix(TileMap.BOOSTHP)){
            maxHealth *= 1.4f;
            health *= 1.4;
        }
    }
    public void tryRegen(){
        if(tileMap.isActiveAffix(TileMap.ENEMYREGEN)){
            if(System.currentTimeMillis() - lastReg - InGame.deltaPauseTime() > 5000 && !isDead()){
                lastReg = System.currentTimeMillis() - InGame.deltaPauseTime();
                int healAmount = (int)Math.ceil(maxHealth * 0.02);
                if(healAmount <= 0) healAmount = 1;
                heal(healAmount);
            }
        }
    }
    public abstract void forceRemove();

    public boolean isRagingActivated() {
        return ragingActivated;
    }
}
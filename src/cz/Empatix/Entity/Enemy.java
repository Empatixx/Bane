package cz.Empatix.Entity;

import cz.Empatix.Entity.AI.Path;
import cz.Empatix.Entity.AI.PathNode;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Damageindicator.DamageIndicator;
import cz.Empatix.Render.Graphics.Shaders.Shader;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public abstract class  Enemy extends MapObject {
    private static final int IDLE = 0;
    private static final int DEAD = 1;

    protected int health;
    protected int maxHealth;
    protected boolean dead;
    protected int damage;

    // AI vars
    protected int type;
    protected static final  int melee = 0;
    protected static final  int shooter = 1;
    protected static final  int hybrid = 2;

    private Path path;

    protected final Player player;
    public int px;
    public int py;

    public int playerTileX;
    public int playerTileY;

    protected boolean itemDropped;

    //protected boolean flinching;
    //protected long flinchTimer;

    private Shader outlineShader;
    private Shader spawnShader;

    protected long lastTimeDamaged;

    protected long spawnTime;


    public Enemy(TileMap tm, Player player) {
        super(tm);
        this.player = player;
        itemDropped=false;

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

    public boolean isSpawning(){
        return System.currentTimeMillis()-InGame.deltaPauseTime()-spawnTime < 1000;
    }
    @Override
    public void draw() {
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


            glBindBuffer(GL_ARRAY_BUFFER, vboVerticles);
            glVertexAttribPointer(0,2,GL_INT,false,0,0);


            glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());
            glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER,0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            spawnShader.unbind();
            glBindTexture(GL_TEXTURE_2D,0);
            glActiveTexture(0);

        } else {
            shader.bind();
            shader.setUniformi("sampler",0);
            shader.setUniformm4f("projection",target);
            glActiveTexture(GL_TEXTURE0);
            spritesheet.bindTexture();

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);


            glBindBuffer(GL_ARRAY_BUFFER, vboVerticles);
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


            glBindBuffer(GL_ARRAY_BUFFER, vboVerticles);
            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);


            glBindBuffer(GL_ARRAY_BUFFER, animation.getFrame().getVbo());
            glVertexAttribPointer(1, 2, GL_DOUBLE, false, 0, 0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            outlineShader.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(0);
        }
        if(Game.displayCollisions) {
            if (path != null) {
                for (PathNode node : path.getPathNodes()) {
                    glPointSize(10);
                    glColor3i(255, 0, 0);
                    glBegin(GL_POINTS);
                    glVertex2f(node.getX() + xmap, node.getY() + ymap);
                    glEnd();
                }
            }
        }
    }

    public boolean isDead() { return dead; }

    int getDamage() { return damage; }

    public void hit(int damage) {
        if(dead) return;
        health -= damage;
        if(health < 0) health = 0;
        if(health == 0){
            dead = true;
        }

        //flinching = true;
        //flinchTimer = System.nanoTime();
        int x = -cwidth/4+ Random.nextInt(cwidth/2);
        DamageIndicator.addDamageShow(damage,(int)position.x-x,(int)position.y-cheight/2
                ,new Vector2f(-x/25f,-1.5f));
    }
    protected void EnemyAI() {

        updatePlayerCords();

        if (type == melee) {
            int enemyTileX = (int)position.x/tileSize;
            int enemyTileY = (int)position.y/tileSize;
            int pTileX = px/tileSize;
            int pTileY = py/tileSize;

            if(pTileX == enemyTileX && pTileY == enemyTileY) {
                if(py > position.y){
                    setDown(true);
                    setUp(false);
                } else if (py < position.y){
                    setUp(true);
                    setDown(false);
                }
                if (px > position.x){
                    setRight(true);
                    setLeft(false);
                } else if (px < position.x){
                    setRight(false);
                    setLeft(true);
                }
                return;
            }
            else if((playerTileX != pTileX || playerTileY != pTileY) || path == null){

                playerTileX = pTileX;
                playerTileY = pTileY;


                ArrayList<PathNode> closed = new ArrayList<>();
                ArrayList<PathNode> opened = new ArrayList<>();

                boolean endFound = false;

                // reducing math operations
                final int startingPointX = enemyTileX*tileSize+tileSize/2;
                final int startingPointY = enemyTileY*tileSize+tileSize/2;

                for (int i = -1; i < 2; i++){
                    for (int j = -1; j < 2; j++) {
                        if (tileMap.getType(enemyTileY + i, enemyTileX + j) == Tile.NORMAL) {
                            PathNode node;
                            if (j == 0 && i == 0) {
                                closed.add(new PathNode(startingPointX, startingPointY, 0, 0, null));
                                continue;
                            }
                            else if (j == 0 || i == 0) {
                                node = createNode(startingPointX + j * tileSize, startingPointY + i * tileSize, px, py, null, 10);
                            } else {
                                node = createNode(startingPointX + j * tileSize, startingPointY + i * tileSize, px, py, null, 14);
                            }

                            if(i==-1){
                                node.setUp(true);
                            } else if(i == 1){
                                node.setDown(true);
                            }
                            if(j==-1){
                                node.setLeft(true);
                            }else if (j == 1){
                                node.setRight(true);
                            }
                            opened.add(node);
                        }
                    }
                }

                PathNode theClosest;

                // setting max X/Y for pathnodes
                final int maxX = tileMap.getNumCols()*tileSize;
                final int maxY = tileMap.getNumRows()*tileSize;

                while (!opened.isEmpty() && !endFound) {
                    theClosest = null;
                    for (PathNode currentNode : opened) {
                        final int x = currentNode.getX()/tileSize;
                        final int y = currentNode.getY()/tileSize;
                        if (x == playerTileX && y == playerTileY) {
                            endFound = true;
                            path = new Path(currentNode);
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
                        if (x - tileSize >= 0) left = tileMap.getType(yTile,xTile - 1) == Tile.NORMAL;
                        if (x + tileSize < maxX) right = tileMap.getType(yTile,xTile + 1) == Tile.NORMAL;
                        if (y - tileSize >= 0) up = tileMap.getType(yTile - 1,xTile) == Tile.NORMAL;
                        if (y + tileSize < maxY) down = tileMap.getType(yTile + 1,xTile) == Tile.NORMAL;


                        PathNode temp;
                        // LEFT
                        temp = createNode(x - tileSize, y, px, py, theClosest, 10);
                        if (left && doesntContain(closed, temp) && doesntContain(opened, temp)) {
                            temp.setLeft(true);
                            opened.add(temp);
                        }
                        // RIGHT
                        temp = createNode(x + tileSize, y, px, py, theClosest, 10);
                        if (right && doesntContain(closed, temp) && doesntContain(opened, temp)) {
                            opened.add(temp);
                            temp.setRight(true);

                        }

                        // DOWN
                        if (down) {
                            temp = createNode(x, y + tileSize, px, py, theClosest, 10);
                            if (doesntContain(closed, temp) && doesntContain(opened, temp)){
                                temp.setDown(true);
                                opened.add(temp);
                            }
                            // DOWN RIGHT
                            if (right && tileMap.getType(yTile + 1,xTile + 1) == Tile.NORMAL) {
                                temp = createNode(x + tileSize, y + tileSize, px, py, theClosest, 14);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)){
                                    temp.setDown(true);
                                    temp.setRight(true);
                                    opened.add(temp);
                                }

                            }
                            // DOWN LEFT
                            if (left && tileMap.getType(yTile + 1,xTile - 1) == Tile.NORMAL) {
                                temp = createNode(x - tileSize, y + tileSize, px, py, theClosest, 14);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)){
                                    temp.setDown(true);
                                    temp.setLeft(true);
                                    opened.add(temp);
                                }
                            }
                        }
                        // UP
                        if (up) {
                            temp = createNode(x, y - tileSize, px, py, theClosest, 10);
                            if (doesntContain(closed, temp) && doesntContain(opened, temp)){
                                temp.setUp(true);
                                opened.add(temp);
                            }
                            // UP RIGHT
                            if (right && tileMap.getType(yTile - 1,xTile + 1) == Tile.NORMAL) {
                                temp = createNode(x + tileSize, y - tileSize, px, py, theClosest, 14);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)){
                                    temp.setUp(true);
                                    temp.setRight(true);
                                    opened.add(temp);
                                }
                            }
                            // UP LEFT
                            if (left && tileMap.getType(yTile - 1,xTile - 1) == Tile.NORMAL) {
                                temp = createNode(x - tileSize, y - tileSize, px, py, theClosest, 14);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)){
                                    temp.setUp(true);
                                    temp.setLeft(true);
                                    opened.add(temp);
                                }
                            }

                        }

                    }
                }

            }
            if (px > position.x) facingRight = true;
            else if (px < position.x) facingRight = false;
            if(path != null) {
                PathNode pathNode = path.getPathNode();

                setDown(false);
                setUp(false);
                setLeft(false);
                setRight(false);

                boolean redo;
                boolean preX,preY;

                do {
                    preX=false;
                    preY=false;
                    int x = pathNode.getX();
                    int y = pathNode.getY();
                    redo = false;
                    // diagonals
                    if(pathNode.isDown() && pathNode.isRight()){
                        setDown(true);
                        setRight(true);
                        if ((int) position.y >= y) {
                            preY=true;
                            setDown(false);
                        }
                        if ((int) position.x >= x) {
                            preX = true;
                            setRight(false);
                        }
                    }
                    else if(pathNode.isDown() && pathNode.isLeft()){
                        setDown(true);
                        setLeft(true);
                        if ((int) position.y >= y) {
                            preY=true;
                            setDown(false);
                        }
                        if ((int) position.x <= x) {
                            preX = true;
                            setLeft(false);
                        }
                    }
                    else if(pathNode.isUp() && pathNode.isRight()){
                        setUp(true);
                        setRight(true);
                        if ((int) position.y <= y) {
                            preY=true;
                            setUp(false);
                        }
                        if ((int) position.x >= x) {
                            preX = true;
                            setRight(false);
                        }
                    }
                    else if(pathNode.isUp() && pathNode.isLeft()){
                        setUp(true);
                        setLeft(true);
                        if ((int) position.y <= y) {
                            preY=true;
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
                            preY=true;
                        }

                        int leftTile = (int) ((position.x - cwidth / 2) / tileSize);
                        int rightTile = (int) ((position.x + cwidth / 2 - 1) / tileSize);
                        int bottomTile = (int) ((position.y+1 + cheight / 2 - 1) / tileSize);


                        // getting type of tile
                        int bl = tileMap.getType(bottomTile, leftTile);
                        int br = tileMap.getType(bottomTile, rightTile);

                        // pokud tile má hodnotu 1 = collision
                        boolean bottomLeft = bl == Tile.BLOCKED;
                        boolean bottomRight = br == Tile.BLOCKED;
                        if(bottomRight){
                            setLeft(true);
                            setRight(false);
                        } else if(bottomLeft){
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
                        int rightTile = (int) ((position.x+1 + cwidth / 2 - 1) / tileSize);
                        int topTile = (int) ((position.y - cheight / 2) / tileSize);
                        int bottomTile = (int) ((position.y + cheight / 2 - 1) / tileSize);


                        // getting type of tile
                        int tr = tileMap.getType(topTile, rightTile);
                        int br = tileMap.getType(bottomTile, rightTile);

                        // pokud tile má hodnotu 1 = collision
                        boolean topRight = tr == Tile.BLOCKED;
                        boolean bottomRight = br == Tile.BLOCKED;
                        if(topRight){
                            setUp(false);
                            setDown(true);
                        } else if (bottomRight){
                            setDown(false);
                            setUp(true);
                        } else{
                            setDown(false);
                            setUp(false);
                        }
                    } else if (pathNode.isLeft()) {
                        preY=true;
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
                    if(preX && preY){
                        path.nextPathNode();
                        if(!path.hasLastNode()){
                            pathNode = path.getPathNode();
                        } else {
                            path = null;
                            setDown(false);
                            setUp(false);
                            setRight(false);
                            setLeft(false);
                            return;
                        }
                        redo = true;
                    }
                } while(redo);
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
    public void update() { }
    // ENEMY AI

    private void updatePlayerCords(){
        px = (int)player.getx();
        py = (int)player.gety();
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
}
package cz.Empatix.Entity;

import cz.Empatix.Entity.AI.Path;
import cz.Empatix.Entity.AI.PathNode;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;
import java.util.List;

public abstract class  Enemy extends MapObject {
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

    //protected boolean flinching;
    //protected long flinchTimer;

    public Enemy(TileMap tm, Player player) {
        super(tm);
        this.player = player;

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
    }

    public void update() { }
    // ENEMY AI
    protected void EnemyAI(){

        updatePlayerCords();

        if (type == melee){

            final int tileTargetX = px/tileSize;
            final int tileTargetY = py/tileSize;

            final int tileEnemyX = (int)position.x/tileSize;
            final int tileEnemyY = (int)position.y/tileSize;

            if ((tileEnemyX != tileTargetX || tileEnemyY != tileTargetY)) {
                if (Math.abs(tileEnemyX - tileTargetX) <= 8 && Math.abs(tileEnemyY - tileTargetY) <= 8) {
                    boolean endFound = false;

                    final List<PathNode> opened = new ArrayList<>();
                    final List<PathNode> closed = new ArrayList<>();

                    // reducing math operations
                    final int startingPointX = tileEnemyX*tileSize+tileSize/2;
                    final int startingPointY = tileEnemyY*tileSize+tileSize/2;



                    for (int i = -1; i < 2; i++){
                        for (int j = -1; j < 2; j++) {
                            if (tileMap.getType(tileEnemyY + i, tileEnemyX + j) == Tile.NORMAL) {
                                if (j == 0 && i == 0) {
                                    closed.add(new PathNode(startingPointX, startingPointY, 0, 0, null));
                                } else if (j == 0 || i == 0) {
                                    opened.add(createNode(startingPointX + j * tileSize, startingPointY + i * tileSize, px, py, null, 10)); //  horizontal/vertical directions
                                } else {
                                    opened.add(createNode(startingPointX + j * tileSize, startingPointY + i * tileSize, px, py, null, 14)); //  diagonals
                                }
                            }
                        }
                    }



                    PathNode theClosest;

                    // setting max X/Y for pathnodes
                    final int maxX = tileMap.getNumCols()*tileSize;
                    final int maxY = tileMap.getNumRows()*tileSize;

                    while (!opened.isEmpty() && !endFound) {
                        theClosest = null;
                        for (PathNode currentNode : opened
                        ) {
                            final int x = currentNode.getX()/tileSize;
                            final int y = currentNode.getY()/tileSize;
                            if (x == tileTargetX &&
                                    y == tileTargetY) {
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
                                opened.add(temp);
                            }
                            // RIGHT
                            temp = createNode(x + tileSize, y, px, py, theClosest, 10);
                            if (right && doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                opened.add(temp);
                            }

                            // DOWN
                            if (down) {
                                temp = createNode(x, y + tileSize, px, py, theClosest, 10);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                // DOWN RIGHT
                                if (right && tileMap.getType(yTile + 1,xTile + 1) == Tile.NORMAL) {
                                    temp = createNode(x + tileSize, y + tileSize, px, py, theClosest, 14);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);

                                }
                                // DOWN LEFT
                                if (left && tileMap.getType(yTile + 1,xTile - 1) == Tile.NORMAL) {
                                    temp = createNode(x - tileSize, y + tileSize, px, py, theClosest, 14);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                }
                            }
                            // UP
                            if (up) {
                                temp = createNode(x, y - tileSize, px, py, theClosest, 10);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                // UP RIGHT
                                if (right && tileMap.getType(yTile - 1,xTile + 1) == Tile.NORMAL) {
                                    temp = createNode(x + tileSize, y - tileSize, px, py, theClosest, 14);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                }
                                // UP LEFT
                                if (left && tileMap.getType(yTile - 1,xTile - 1) == Tile.NORMAL) {
                                    temp = createNode(x - tileSize, y - tileSize, px, py, theClosest, 14);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                }

                            }

                        }
                    }
                }
                if (px > position.x) facingRight = true;
                else if (px < position.x) facingRight = false;
            }
            if (path != null) {
                PathNode pathNode = path.getPathNode();
                int x = pathNode.getX();
                int y = pathNode.getY();
                if (x == (int)position.x && y == (int)position.y) {
                    if (this.path.hasLastNode()) {
                        setLeft(false);
                        setRight(false);
                        setUp(false);
                        setDown(false);
                        path = null;
                        return;
                    }
                    path.nextPathNode();
                    pathNode = path.getPathNode();
                    x = pathNode.getX();
                    y = pathNode.getY();
                }
                if (x < (int)position.x) {
                    setLeft(true);
                    setRight(false);
                } else if (x > (int)position.x) {
                    setRight(true);
                    setLeft(false);
                } else {
                    setRight(false);
                    setLeft(false);
                }
                if (y < (int)position.y) {
                    setUp(true);
                    setDown(false);
                } else if (y > (int)position.y) {
                    setDown(true);
                    setUp(false);
                } else {
                    setDown(false);
                    setUp(false);
                }
            } else {
                setDown(false);
                setUp(false);
                setRight(false);
                setLeft(false);
            }
        }
    }
    /**
     *
     * @param x - X position of PathNode
     * @param y - Y position of PathNode
     * @param parent -  previous PathNode from that we can get to this one
     * @return - a new created PathNode
     */
    private PathNode createNode(int x,
                                int y,
                                int eX,
                                int eY,
                                PathNode parent,
                                int moveCost) {
        //Math.abs((eX-x-tileSize/2)/tileSize)+Math.abs((eY-y-tileSize/2)/tileSize)
        if (parent != null){
            return new PathNode(
                    x,
                    y,
                    Math.abs((eX-x-tileSize/2)/tileSize)+Math.abs((eY-y-tileSize/2)/tileSize),
                    parent.getH()+moveCost,
                    parent
            );
        }
        return new PathNode
                (
                        x,
                        y,
                        Math.abs((eX-x-tileSize/2)/tileSize)+Math.abs((eY-y-tileSize/2)/tileSize),
                        moveCost,
                        parent
                );
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

    private void updatePlayerCords(){
        px = player.getx();
        py = player.gety();
    }
    public boolean shouldRemove(){
        return animation.hasPlayedOnce() && isDead();
    }
}
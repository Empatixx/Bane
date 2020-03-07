package cz.Empatix.Entity;

import cz.Empatix.Entity.AI.Path;
import cz.Empatix.Entity.AI.PathNode;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;
import java.util.List;

public abstract class Enemy extends MapObject {

    protected int health;
    protected int maxHealth;
    boolean dead;
    protected int damage;

    // AI vars
    protected int type;
    protected static final  int melee = 0;
    protected static final  int shooter = 1;
    protected static final  int hybrid = 2;

    private Path path;

    private final Player player;
    private int px;
    private int py;

    //protected boolean flinching;
    //protected long flinchTimer;

    public Enemy(TileMap tm, Player player) {
        super(tm);
        this.player = player;
    }

    public boolean isDead() { return dead; }

    int getDamage() { return damage; }

    void hit(int damage) {
        if(dead) return;
        health -= damage;
        if(health < 0) health = 0;
        if(health == 0) dead = true;

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

            boolean isLast = false;
            if (path != null){
                isLast = path.hasLastNode();
            }
            if ((tileEnemyX != tileTargetX || tileEnemyY != tileTargetY) && !isLast) {
                if (Math.abs(tileEnemyX - tileTargetX) <= 4 && Math.abs(tileEnemyY - tileTargetY) <= 4) {
                    boolean endFound = false;

                    final List<PathNode> opened = new ArrayList<>();
                    final List<PathNode> closed = new ArrayList<>();
                    final ArrayList<PathNode> removing = new ArrayList<>();


                    // start
                    final PathNode start = new PathNode(tileEnemyX, tileEnemyY, 0, 0, null,0);
                    closed.add(start);

                    // main diretions
                    opened.add(createNode(tileEnemyX, tileEnemyY - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 10, PathNode.UP)); //  UP
                    opened.add(createNode(tileEnemyX, tileEnemyY + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 10, PathNode.DOWN)); // DOWN
                    opened.add(createNode(tileEnemyX - 1, tileEnemyY, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 10, PathNode.LEFT)); // LEFT
                    opened.add(createNode(tileEnemyX + 1, tileEnemyY, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 10, PathNode.RIGHT)); // RIGHT

                    // diagonals
                    opened.add(createNode(tileEnemyX - 1, tileEnemyY - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 14, PathNode.UPLEFT)); //  UP LEFT
                    opened.add(createNode(tileEnemyX - 1, tileEnemyY + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 14, PathNode.BOTTOMLEFT)); // DOWN LEFT
                    opened.add(createNode(tileEnemyX + 1, tileEnemyY - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 14, PathNode.UPRIGHT)); // UP RIGHT
                    opened.add(createNode(tileEnemyX + 1, tileEnemyY + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 14, PathNode.BOTTOMRIGHT)); // DOWN RIGHT

                    PathNode theClosest;
                    while (!opened.isEmpty() && !endFound) {
                        theClosest = null;
                        for (PathNode currentNode : opened
                        ) {
                            final int x = currentNode.getX();
                            final int y = currentNode.getY();
                            if (x == tileTargetX &&
                                    y == tileTargetY) {
                                endFound = true;
                                path = new Path(currentNode);
                                break;
                            } else {
                                if (tileMap.getType(y, x) != Tile.BLOCKED) {
                                    if (theClosest != null) {
                                        if (theClosest.getF() > currentNode.getF()) theClosest = currentNode;
                                    } else {
                                        theClosest = currentNode;
                                    }
                                    // if tile has collision move him to closed ones
                                } else {
                                    removing.add(currentNode);
                                }
                            }
                        }
                        opened.removeAll(removing);
                        closed.addAll(removing);
                        removing.clear();
                        if (theClosest != null && !endFound) {
                            opened.remove(theClosest);
                            closed.add(theClosest);
                            int x = theClosest.getX();
                            int y = theClosest.getY();
                            // LEFT
                            PathNode temp;
                            temp = createNode(x - 1, y, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 10,PathNode.LEFT);
                            if (x - 1 >= 0 && doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                opened.add(temp);
                            }
                            // RIGHT
                            temp = createNode(x + 1, y, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 10,PathNode.RIGHT);
                            if (x + 1 < tileMap.getNumCols() && doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                opened.add(temp);
                            }
                            // UP
                            if (y - 1 >= 0) {
                                temp = createNode(x, y - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 10,PathNode.UP);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                if (x + 1 < tileMap.getNumCols()) { // UP RIGHT
                                    temp = createNode(x + 1, y - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 14,PathNode.UPRIGHT);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                }
                                if (x - 1 >= 0) { // UP LEFT
                                    temp = createNode(x - 1, y - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 14,PathNode.UPLEFT);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                }
                            }
                            // DOWN
                            if (y + 1 < tileMap.getNumRows()) {
                                temp = createNode(x, y + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 10,PathNode.DOWN);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                if (x + 1 < tileMap.getNumCols()) { // DOWN RIGHT
                                    temp = createNode(x + 1, y + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 14,PathNode.BOTTOMRIGHT);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                }
                                if (x - 1 >= 0) { // DOWN LEFT
                                    temp = createNode(x - 1, y + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 14,PathNode.BOTTOMLEFT);
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
                PathNode node = path.getPathNode();
                int x;
                int y;

                boolean repeat;

                do{
                    System.out.print("direction: "+node.getDirection()+"\n");

                    setUp(false);
                    setDown(false);
                    setLeft(false);
                    setRight(false);

                    repeat = false;
                    x = node.getX();
                    y = node.getY();
                    switch (node.getDirection()) {
                        case PathNode.UP: {
                            if (y * tileSize + tileSize / 2 < position.y) {
                                setUp(true);
                            } else {
                                if (path.hasLastNode()) {
                                    path = null;
                                    setUp(false);
                                    setDown(false);
                                    setLeft(false);
                                    setRight(false);
                                    return;
                                }
                                path.nextPathNode();
                                node = path.getPathNode();
                                repeat = true;
                            }
                            break;
                        }
                        case PathNode.DOWN: {
                            if (y * tileSize + tileSize / 2 > position.y) {
                                setDown(true);
                            } else {
                                if (path.hasLastNode()) {
                                    path = null;
                                    setUp(false);
                                    setDown(false);
                                    setLeft(false);
                                    setRight(false);
                                    return;
                                }
                                path.nextPathNode();
                                node = path.getPathNode();
                                repeat = true;
                            }
                            break;
                        }
                        case PathNode.RIGHT: {
                            if (x * tileSize + tileSize / 2 > position.x) {
                                setRight(true);
                            } else {
                                if (path.hasLastNode()) {
                                    path = null;
                                    setUp(false);
                                    setDown(false);
                                    setLeft(false);
                                    setRight(false);
                                    return;
                                }
                                path.nextPathNode();
                                node = path.getPathNode();
                                repeat = true;
                            }
                            break;
                        }
                        case PathNode.LEFT: {
                            if (x * tileSize + tileSize / 2 < position.x) {
                                setLeft(true);
                            } else {
                                if (path.hasLastNode()) {
                                    path = null;
                                    setUp(false);
                                    setDown(false);
                                    setLeft(false);
                                    setRight(false);
                                    return;
                                }
                                path.nextPathNode();
                                node = path.getPathNode();
                                repeat = true;
                            }
                            break;
                        }
                        case PathNode.BOTTOMLEFT:{
                            if (y * tileSize + tileSize / 2 > position.y || x * tileSize + tileSize / 2 < position.x) {
                                setDown(true);
                                setLeft(true);
                            } else {
                                if (path.hasLastNode()) {
                                    path = null;
                                    setUp(false);
                                    setDown(false);
                                    setLeft(false);
                                    setRight(false);
                                    return;
                                }
                                path.nextPathNode();
                                node = path.getPathNode();
                                repeat = true;
                            }
                            break;
                        }
                        case PathNode.BOTTOMRIGHT:{
                            if (y * tileSize + tileSize / 2 > position.y || x * tileSize + tileSize / 2 > position.x) {
                                setDown(true);
                                setRight(true);
                            } else {
                                if (path.hasLastNode()) {
                                    path = null;
                                    setUp(false);
                                    setDown(false);
                                    setLeft(false);
                                    setRight(false);
                                    return;
                                }
                                path.nextPathNode();
                                node = path.getPathNode();
                                repeat = true;
                            }
                            break;
                        }
                        case PathNode.UPRIGHT:{
                            if (y * tileSize + tileSize / 2 < position.y || x * tileSize + tileSize / 2 > position.x) {
                                setUp(true);
                                setRight(true);
                            } else {
                                if (path.hasLastNode()) {
                                    path = null;
                                    setUp(false);
                                    setDown(false);
                                    setLeft(false);
                                    setRight(false);
                                    return;
                                }
                                path.nextPathNode();
                                node = path.getPathNode();
                                repeat = true;
                            }
                            break;
                        }
                        case PathNode.UPLEFT: {
                            if (y * tileSize + tileSize / 2 < position.y || x * tileSize + tileSize / 2 < position.x) {
                                setUp(true);
                                setLeft(true);
                            } else {
                                if (path.hasLastNode()) {
                                    path = null;
                                    setUp(false);
                                    setDown(false);
                                    setLeft(false);
                                    setRight(false);
                                    return;
                                }
                                path.nextPathNode();
                                node = path.getPathNode();
                                repeat = true;
                            }
                            break;
                        }
                    }
                } while (repeat);
            }
        }
    }

    /**
     *
     * @param x - X position of PathNode
     * @param y - Y position of PathNode
     * @param teX - position of tile X on enemy
     * @param teY - position of tile Y on enemy
     * @param ttX - position of tile X on target
     * @param ttY - position of tile Y on target
     * @param parent -  previous PathNode from that we can get to this one
     * @return - a new created PathNode
     */
    private PathNode createNode(int x,
                                int y,
                                int teX,
                                int teY,
                                int ttX,
                                int ttY,
                                PathNode parent,
                                int moveCost,
                                int direction){
        if (parent != null){
            return new PathNode(
                    x,
                    y,
                    Math.abs(ttX-x)+Math.abs(ttY-y),
                    parent.getH()+moveCost,
                    parent,
                    direction

            );
        }
        return new PathNode
                (
                        x,
                        y,
                        Math.abs(ttX-x)+Math.abs(ttY-y),
                        moveCost,
                        parent,
                        direction

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
}
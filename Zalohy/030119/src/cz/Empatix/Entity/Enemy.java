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
            if (tileEnemyX != tileTargetX || tileEnemyY != tileTargetY) {
                if (Math.abs(tileEnemyX - tileTargetX) <= 4 && Math.abs(tileEnemyY - tileTargetY) <= 4) {
                    boolean endFound = false;

                    final List<PathNode> opened = new ArrayList<>();
                    final List<PathNode> closed = new ArrayList<>();
                    final ArrayList<PathNode> removing = new ArrayList<>();


                    // start
                    final PathNode start = new PathNode(tileEnemyX, tileEnemyY, 0, 0, null);
                    closed.add(start);

                    opened.add(createNode(tileEnemyX, tileEnemyY - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 10)); //  UP
                    opened.add(createNode(tileEnemyX, tileEnemyY + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 10)); // DOWN
                    opened.add(createNode(tileEnemyX - 1, tileEnemyY, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 10)); // LEFT
                    opened.add(createNode(tileEnemyX + 1, tileEnemyY, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 10)); // RIGHT

                    // horizontal directions
                    opened.add(createNode(tileEnemyX - 1, tileEnemyY - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 14)); //  UP LEFT
                    opened.add(createNode(tileEnemyX - 1, tileEnemyY + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 14)); // UP RIGHT
                    opened.add(createNode(tileEnemyX + 1, tileEnemyY - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 14)); // DOWN LEFT
                    opened.add(createNode(tileEnemyX + 1, tileEnemyY + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, null, 14)); // DOWN RIGHT

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
                            temp = createNode(x - 1, y, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 10);
                            if (x - 1 >= 0 && doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                opened.add(temp);
                            }
                            // RIGHT
                            temp = createNode(x + 1, y, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 10);
                            if (x + 1 < tileMap.getNumCols() && doesntContain(closed, temp) && doesntContain(opened, temp)) {
                                opened.add(temp);
                            }
                            // UP
                            if (y - 1 >= 0) {
                                temp = createNode(x, y - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 10);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                if (x + 1 < tileMap.getNumCols()) { // UP RIGHT
                                    temp = createNode(x + 1, y - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 14);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                }
                                if (x - 1 >= 0) { // UP LEFT
                                    temp = createNode(x - 1, y - 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 14);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                }
                            }
                            // DOWN
                            if (y + 1 < tileMap.getNumRows()) {
                                temp = createNode(x, y + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 10);
                                if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                if (x + 1 < tileMap.getNumCols()) { // DOWN RIGHT
                                    temp = createNode(x + 1, y + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 14);
                                    if (doesntContain(closed, temp) && doesntContain(opened, temp)) opened.add(temp);
                                }
                                if (x - 1 >= 0) { // DOWN LEFT
                                    temp = createNode(x - 1, y + 1, tileEnemyX, tileEnemyY, tileTargetX, tileTargetY, theClosest, 14);
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
                PathNode path = this.path.getPathNode();
                int x = path.getX();
                int y = path.getY();
                if (x == tileEnemyX && y == tileEnemyY) {
                    if (this.path.hasLastNode()) {
                        this.path = null;
                        return;
                    }
                    this.path.nextPathNode();
                    path = this.path.getPathNode();
                    x = path.getX();
                    y = path.getY();
                }
                if (x < tileEnemyX) {
                    setLeft(true);
                    setRight(false);
                } else if (x > tileEnemyX) {
                    setRight(true);
                    setLeft(false);
                } else {
                    setRight(false);
                    setLeft(false);
                }
                if (y < tileEnemyY) {
                    setUp(true);
                    setDown(false);
                } else if (y > tileEnemyY) {
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
     * @param teX - position of tile X on enemy
     * @param teY - position of tile Y on enemy
     * @param ttX - position of tile X on target
     * @param ttY - position of tile Y on target
     * @param parent -  previous PathNode from that we can get to this one
     * @return - a new created PathNode
     */
    private PathNode createNode(int x, int y, int teX, int teY, int ttX, int ttY, PathNode parent, int moveCost){
        if (parent != null){
            return new PathNode(
                    x,
                    y,
                    Math.abs(ttX-x)+Math.abs(ttY-y),
                    parent.getH()+moveCost,
                    parent

            );
        }
        return new PathNode
                (
                        x,
                        y,
                        Math.abs(ttX-x)+Math.abs(ttY-y),
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
}
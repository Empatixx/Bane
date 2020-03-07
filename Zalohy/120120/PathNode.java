package cz.Empatix.Entity.AI;

public class PathNode {
    public final static int LEFT = 0;
    public final static int RIGHT = 1;
    public final static int UP = 2;
    public final static int DOWN = 3;
    public final static int UPLEFT = 4;
    public final static int UPRIGHT = 5;
    public final static int BOTTOMLEFT = 6;
    public final static int BOTTOMRIGHT = 7;
    // location of pathnode
    private final int x,y;
    // costs
    // g - from starting node
    // h - from end node
    private final int h;
    private final int g;

    //previous pathnode from that we got this one
    private final PathNode parent;
    private final int direction;

    public int getDirection() {
        return direction;
    }

    public PathNode(int x, int y, int h, int g, PathNode parent, int direction){
        this.x = x;
        this.y = y;
        this.h = h;
        this.g = g;
        this.parent = parent;
        this.direction = direction;
    }
    public int getX (){ return x;}
    public int getY() { return y;}

    public int getH() {
        return h;
    }

    public PathNode getParent() {
        return parent;
    }

    public double getF(){
        return h+g;
    }



    @Override
    public boolean equals(Object o){
        return (((PathNode)o).y == this.y && ((PathNode)o).x == this.x);
    }
}

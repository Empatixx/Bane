package cz.Empatix.Entity.AI;

public class PathNode {
    // location of pathnode
    private int x,y;
    // costs
    // g - from starting node
    // h - from end node
    private final int h;
    private final int g;

    //previous pathnode from that we got this one
    private final PathNode parent;


    public PathNode(int x, int y, int h, int g, PathNode parent){
        this.x = x;
        this.y = y;
        this.h = h;
        this.g = g;
        this.parent = parent;
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



    public boolean equals(PathNode o){
        return (o.y == this.y && o.x == this.x);
    }

    void setX(int x) {
        this.x = x;
    }

    void setY(int y) {
        this.y = y;
    }
}

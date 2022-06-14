package cz.Empatix.Entity.AI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Path {

    private final List<PathNode> pathNodes;


    public Path(PathNode endNode){
        pathNodes = new ArrayList<>();
        PathNode parent;
        pathNodes.add(endNode);
        parent = endNode.getParent();

        while (parent != null){
            pathNodes.add(parent);

            parent = parent.getParent();
        }

        Collections.reverse(pathNodes);

    }

    public List<PathNode> getPathNodes() {
        return pathNodes;
    }

    public PathNode getPathNode(){
        return pathNodes.get(0);
    }
    public void nextPathNode(){
        pathNodes.remove(0);
    }
    public int returnSize(){ return pathNodes.size();}
    public boolean hasLastNode(){ return (pathNodes.size() <= 1);}

}

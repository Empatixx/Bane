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
        for(int i = 1;i<pathNodes.size()-1;i++){
            PathNode pathNode = pathNodes.get(i);
            PathNode nextPathNode = pathNodes.get(i+1);
            PathNode previousPathNode = pathNodes.get(i-1);

            pathNode.setX((int)(pathNode.getX()+0.7*(nextPathNode.getX()+previousPathNode.getX()-2*pathNode.getX())));
            pathNode.setY((int)(pathNode.getY()+0.7*(nextPathNode.getY()+previousPathNode.getY()-2*pathNode.getY())));

        }


        Collections.reverse(pathNodes);

    }
    public PathNode getPathNode(){
        return pathNodes.get(0);
    }
    public void nextPathNode(){
        pathNodes.remove(0);
    }
    public int returnSize(){ return pathNodes.size();}
    public boolean hasLastNode(){ return (pathNodes.size() == 1);}

    /*
      pathNodes = new ArrayList<>();
        PathNode parent;
        pathNodes.add(endNode);
        parent = endNode.getParent();

        while (parent != null){
            pathNodes.add(parent);


            parent = parent.getParent();
        }
        Collections.reverse(pathNodes);
     */
}

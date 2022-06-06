package cz.Empatix.Multiplayer;

import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Main.Game;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;


public class Interpolator {
    private float timeElapsed = 0f;
    private float timeToReachTarget;

    private float constTimeToReachTarget;

    private MapObject object;

    private List<TransformUpdate> futureTransformUpdates;
    private TransformUpdate to;
    private TransformUpdate from;
    private TransformUpdate previous;
    public Interpolator(MapObject object, float timeToReachTarget){
        this.object = object;

        this.constTimeToReachTarget = timeToReachTarget;

        this.timeToReachTarget = constTimeToReachTarget;
        futureTransformUpdates = new ArrayList<>();
        GameClient client = MultiplayerManager.getInstance().client;
        to = new TransformUpdate(client.serverTick, object.getX(),object.getY());
        from = new TransformUpdate(client.interpolationTick, object.getX(),object.getY());
        previous = new TransformUpdate(client.interpolationTick, object.getX(),object.getY());
    }
    public void update(float x, float y){
        GameClient client = MultiplayerManager.getInstance().client;
        for(int i = 0;i<futureTransformUpdates.size();i++){
            if(client.serverTick >= futureTransformUpdates.get(i).tick && to.tick < futureTransformUpdates.get(i).tick){
                previous = to;
                to = futureTransformUpdates.get(i);
                from = new TransformUpdate(client.interpolationTick,x,y);

                futureTransformUpdates.remove(i);
                i--;
                timeElapsed = 0;
                timeToReachTarget = (to.tick - from.tick) * constTimeToReachTarget;
            }
        }
        timeElapsed += Game.deltaTime;
        interpolatePosition(timeElapsed / timeToReachTarget);
    }
    private void interpolatePosition(float lerpAmount){
        //GameClient client = MultiplayerManager.getInstance().client;
        // interpolation -> smooth trans
        if(true){ // TODO: packets are old, needed to extrapolation
            if(!to.pos.equals(from.pos)){
                Vector3f pos = lerpClamped(from.pos,to.pos,lerpAmount);
                object.setPosition(pos.x,pos.y);
                if(object instanceof PlayerMP){
                    //if(lerpAmount > 1) System.out.print("OVERFLOW ");
                    //System.out.println("DIFF: "+(pos.x-from.pos.x));
                    //System.out.println(to.pos.x);
                }
            }
            return;
        }
        if(lerpAmount < 3){ // too much predicting => false predict
            // extrapolation -> predicting movement
            Vector3f pos = lerpUnclamped(from.pos,to.pos,lerpAmount); // extrapolation
            object.setPosition(pos.x,pos.y);
        }



    }
    public void newUpdate(int tick, Vector3f pos){
        GameClient client = MultiplayerManager.getInstance().client;
        // old packet
        if(tick <= client.interpolationTick){
            return;
        }
        // checking if packet is older then some packets
        for(int i = 0;i<futureTransformUpdates.size();i++){
            if(tick < futureTransformUpdates.get(i).tick){
                futureTransformUpdates.add(i,new TransformUpdate(tick,pos.x,pos.y));
                return;
            }
        }
        futureTransformUpdates.add(new TransformUpdate(tick,pos.x,pos.y));
    }
    private Vector3f lerpClamped(Vector3f x, Vector3f y, float t) {
        Vector3f dest = new Vector3f();
        if(t > 1) t = 1;
        if(t < 0) t = 0;
        dest.x = x.x + (y.x() - x.x) * t;
        dest.y = x.y + (y.y() - x.y) * t;
        System.out.println(dest.x);
        return dest;
    }
    private Vector3f lerpUnclamped(Vector3f x, Vector3f y, float t) {
        Vector3f dest = new Vector3f();
        dest.x = x.x + (y.x() - x.x) * t;
        dest.y = x.y + (y.y() - x.y) * t;
        return dest;
    }
}

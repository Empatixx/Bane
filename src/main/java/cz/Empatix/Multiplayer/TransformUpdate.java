package cz.Empatix.Multiplayer;

import org.joml.Vector3f;

public class TransformUpdate {
    public int tick;
    public Vector3f pos;
    public TransformUpdate(int tick, float x, float y){
        this.tick = tick;
        this.pos = new Vector3f(x,y,0);
    }
}

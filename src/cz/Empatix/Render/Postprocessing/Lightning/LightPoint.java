package cz.Empatix.Render.Postprocessing.Lightning;

import cz.Empatix.Entity.MapObject;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class LightPoint {
    private boolean remove;
    private Vector2f pos;
    private Vector3f color;
    private float intensity;

    private MapObject object;
    private TileMap tm;


    public LightPoint(TileMap tm, Vector2f pos, Vector3f color, float intensity, MapObject object){
        this.tm = tm;
        this.pos = pos;
        this.color = color;
        this.intensity = intensity;
        this.object = object;
    }
    public void update(){
        if(!remove){
            setPos(object.getX()+tm.getX(),object.getY()+tm.getY());
        }
    }
    public float getIntensity() {
        return intensity;
    }

    public Vector3f getColor() {
        return color;
    }

    public Vector2f getPos() {
        return pos;
    }

    public void setPos(float x,float y) {
        if(!remove) {
            // resolution scaling
            pos.x = x * Settings.WIDTH / 1920f;
            pos.y = y * Settings.HEIGHT / 1080f;
        }
    }

    public void remove() {
        this.remove = true;
        object = null;
        color = null;
        pos = null;
    }

    public boolean shouldRemove() {
        return remove;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public boolean isNotOnScreen(){
        return (
                pos.x > Camera.getWIDTH() + 500 * intensity || pos.x < 0 - 500 * intensity
                        ||
                pos.y > Camera.getHEIGHT() + 500 * intensity|| pos.y < 0 - 500 * intensity
        );
    }
}

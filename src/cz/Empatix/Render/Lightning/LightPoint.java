package cz.Empatix.Render.Lightning;

import cz.Empatix.Main.Settings;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class LightPoint {
    private boolean remove;
    private Vector2f pos;
    private Vector3f color;
    private float intensity;

    public LightPoint(Vector2f pos, Vector3f color, float intensity){

        this.pos = pos;
        this.color = color;
        this.intensity = intensity;

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
        // resolution scaling
        pos.x = x * Settings.WIDTH/1920f;
        pos.y = y * Settings.HEIGHT/1080f;
    }

    public void remove() {
        this.remove = true;
    }

    public boolean shouldRemove() {
        return remove;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

}

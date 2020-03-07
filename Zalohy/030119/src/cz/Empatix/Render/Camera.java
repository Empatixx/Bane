package cz.Empatix.Render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Matrix4f projection;

    private int xCamera;
    private int yCamera;

    public Camera(int x, int y){
        this.xCamera = x;
        this.yCamera = y;
        position = new Vector3f(0,0,0);
        projection = new Matrix4f().setOrtho2D(0,x,y,0);
    }
    public void setPosition(float x, float y){
        position.x = x;
        position.y = y;

    }

    public Vector3f getPosition() {
        return position;
    }

    public Matrix4f projection(){
        Matrix4f target = new Matrix4f();
        Matrix4f pos = new Matrix4f().setTranslation(position.x,position.y,0);
        target = projection.mul(pos,target);
        return target;
    }

    public float getXCamera() {
        return xCamera;
    }

    public float getYCamera() {
        return yCamera;
    }
}

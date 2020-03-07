package cz.Empatix.Render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private final Vector3f position;
    private final Matrix4f projection;


    public Camera(int x, int y){
        position = new Vector3f(0,0,0);
        projection = new Matrix4f().setOrtho2D(0,x,y,0);
    }
    public void setPosition(float x, float y){
        position.x = x;
        position.y = y;

    }

    public Matrix4f projection(){
        Matrix4f target = new Matrix4f();
        Matrix4f pos = new Matrix4f().setTranslation(position.x,position.y,0);
        target = projection.mul(pos,target);
        return target;
    }

    /**
     * Hard projection means that it don't contain any shifting X/Y from tileMap
     * @return matrix4f orthographic for opengl
     */
    public Matrix4f hardProjection(){
        return projection;
    }

}

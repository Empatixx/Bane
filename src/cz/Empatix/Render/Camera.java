package cz.Empatix.Render;

import cz.Empatix.Main.Settings;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private final static Camera thisInstance = new Camera(1920,1080);

    private final Vector3f position;
    private final Matrix4f projection;

    private static int WIDTH;
    private static int HEIGHT;


    public static Camera getInstance(){ return thisInstance;}

    public Camera(int x, int y){
        position = new Vector3f(0,0,0);
        projection = new Matrix4f().setOrtho2D(0,x,y,0);

        WIDTH = x;
        HEIGHT = y;
    }
    public void setPosition(float x, float y){
        position.x = x;
        position.y = y;
        if(Settings.isFixedCameraSize()){
            // pixel perfect fix
            position.x-=0.5f;
            position.y-=0.5f;
        }

    }

    public Vector3f getPosition() {
        return position;
    }

    public Matrix4f projection(){
        Matrix4f target = new Matrix4f();
        Matrix4f pos = new Matrix4f().setTranslation(position);
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

    public static int getHEIGHT() {
        return HEIGHT;
    }

    public static int getWIDTH() {
        return WIDTH;
    }
}

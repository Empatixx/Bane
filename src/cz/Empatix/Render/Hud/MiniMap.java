package cz.Empatix.Render.Hud;

import cz.Empatix.Render.Camera;
import org.joml.Vector3f;

public class MiniMap {
    private Image minimapBorders;

    public MiniMap(Camera camera){
        minimapBorders = new Image("Textures\\minimap.tga",new Vector3f(1770,150,0),2,camera);
    }
    public void draw(){
        minimapBorders.draw();
    }
}

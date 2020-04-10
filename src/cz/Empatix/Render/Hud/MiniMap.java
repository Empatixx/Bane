package cz.Empatix.Render.Hud;

import org.joml.Vector3f;

public class MiniMap {
    private Image minimapBorders;

    public MiniMap(){
        minimapBorders = new Image("Textures\\minimap.tga",new Vector3f(1770,150,0),2);
    }
    public void draw(){
        minimapBorders.draw();
    }
}

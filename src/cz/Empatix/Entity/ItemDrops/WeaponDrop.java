package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.Guns.Weapon;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class WeaponDrop extends ItemDrop {

    private Weapon weapon;

    private Image img;

    public WeaponDrop(TileMap tm,Weapon weapon, Image img){
        super(tm);
        this.weapon = weapon;
        type = GUN;
        canDespawn = false;
        liveTime = System.currentTimeMillis();
        pickedUp = false;

        width = 16;
        height = 45;
        cwidth = 16;
        cheight = 45;
        scale = 1;
        facingRight = true;

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        // because of scaling image by 3x
        width *= scale;
        height *= scale;
        cwidth *= scale*3;
        cheight *= scale*2;

        light = LightManager.createLight(new Vector3f(1.0f,0.8274f,0.0f),new Vector2f(0,0),1.25f,this);
    }
    public void draw(Camera c){

        setMapPosition();
        super.draw();
    }
}

package cz.Empatix.Graphics.Sprites;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Spritesheet {
    private int idTexture;
    private ArrayList<Sprite[]> sprites;
    public Spritesheet(int idTexture) {
        sprites = new ArrayList<>();
        this.idTexture = idTexture;
    }
    public void addSprites(Sprite[] sprites ){
           this.sprites.add(sprites);
    }
    public Sprite[] getSprites(int index){
        return sprites.get(index);
    }

    public void bindTexture() {
        glBindTexture(GL_TEXTURE_2D, idTexture);
    }

}

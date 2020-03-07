package cz.Empatix.Entity;

public class Sprite {
    private int id;
    private float[] texCoords;
    public Sprite(float[] texCoords,int id){
        this.texCoords = texCoords;
        this.id = id;

    }

    public int getId() {
        return id;
    }

    public float[] getTexCoords() {
        return texCoords;
    }
}

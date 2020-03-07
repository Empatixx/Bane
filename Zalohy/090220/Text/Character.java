package cz.Empatix.Render.Text;

public class Character {
    private final char Char;

    private final int texVBO;
    private final int vexVBO;

    private double[] texcoords;
    private int[] vertexes;

    Character(char Char, int texVBO, int vexVBO){
        this.Char = Char;
        this.texVBO = texVBO;
        this.vexVBO = vexVBO;

    }
    public int bindTex(){return texVBO;}
    public int bindVex(){ return vexVBO; }

    public char getChar() {
        return Char;
    }
}

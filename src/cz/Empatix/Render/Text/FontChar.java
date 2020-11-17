package cz.Empatix.Render.Text;

class FontChar {
    private char c;
    private int width;


    private double[] texCoords;
    private int[] vertices;


    FontChar(char c,int width,int[] vertices,double[] texcoords){
        this.c = c;
        this.width = width;
        texCoords = texcoords;
        this.vertices = vertices;


    }

    char getChar() {
        return c;
    }

    public double[] getTexCoords() {
        return texCoords;
    }

    public int[] getVertices() {
        return vertices;
    }

    public int getWidth() {
        return width;
    }
}

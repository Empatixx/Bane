package cz.Empatix.Render.Text;

class FontChar {
    private char c;
    private int width;

    private int verticlesVBO;

    private int texcoordsVBO;

    FontChar(char c,int width,int verticles,int texcoords){
        this.c = c;
        this.width = width;

        this.verticlesVBO = verticles;
        this.texcoordsVBO = texcoords;
    }

    int getWidth() {
        return width;
    }

    int getTexcoordsVBO() {
        return texcoordsVBO;
    }

    int getVerticlesVBO() {
        return verticlesVBO;
    }

    char getChar() {
        return c;
    }
}

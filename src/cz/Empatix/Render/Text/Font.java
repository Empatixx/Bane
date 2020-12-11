package cz.Empatix.Render.Text;

import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.ByteBufferImage;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Font {
    // id texture
    private int idTexture;

    // stored chars
    private FontChar[] chars;

    private int cellWidth;
    private int cellHeight;

    // widts of chars

    Font(int width,int height,int cwidth,int cheight, String file){
        int size = width/cwidth*height/cheight;
        chars = new FontChar[size];

        ByteBufferImage decoder = Loader.getImage(file+".tga");
        ByteBuffer spritesheetImage = decoder.getBuffer();

        int idTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        this.idTexture = idTexture;
        this.cellWidth = cwidth;
        this.cellHeight = cheight;
    }
    void bindTexture(){
        glBindTexture(GL_TEXTURE_2D, idTexture);
    }
    int sizeOfChars(){ return chars.length;}

    void setChar(FontChar fontChar,int index) {
        chars[index] = fontChar;
    }

    FontChar[] getChars() {
        return chars;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public int getCellWidth() {
        return cellWidth;
    }
}

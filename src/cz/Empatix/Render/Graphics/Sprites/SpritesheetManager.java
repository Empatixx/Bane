package cz.Empatix.Render.Graphics.Sprites;

import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.ByteBufferImage;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL30.*;

public class SpritesheetManager {
    private final static HashMap<String,Spritesheet> spritesheets = new HashMap<>();

    public static Spritesheet getSpritesheet(String filepath){
        return spritesheets.get(filepath);
    }

    public static Spritesheet createSpritesheet(String filepath){
        ByteBufferImage decoder = Loader.getImage(filepath);
        ByteBuffer spritesheetImage = decoder.getBuffer();
        int channels = decoder.getChannels();
        if(channels == 4){
            int idTexture = glGenTextures();
            int width = decoder.getWidth();
            int height = decoder.getHeight();

            glBindTexture(GL_TEXTURE_2D, idTexture);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);
            glGenerateMipmap(GL_TEXTURE_2D);

            Spritesheet spritesheet = new Spritesheet(idTexture);
            spritesheets.put(filepath,spritesheet);

            return spritesheet;
        } else {
            int idTexture = glGenTextures();
            int width = decoder.getWidth();
            int height = decoder.getHeight();

            glBindTexture(GL_TEXTURE_2D, idTexture);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, spritesheetImage);
            glGenerateMipmap(GL_TEXTURE_2D);

            Spritesheet spritesheet = new Spritesheet(idTexture);
            spritesheets.put(filepath,spritesheet);

            return spritesheet;
        }
    }
}

package cz.Empatix.Render.Graphics.Sprites;

import cz.Empatix.Render.Graphics.ByteBufferImage;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

public class SpritesheetManager {
    private final static HashMap<String,Spritesheet> spritesheets = new HashMap<>();

    public static Spritesheet getSpritesheet(String filepath){
        return spritesheets.get(filepath);
    }

    public static Spritesheet createSpritesheet(String filepath){
        ByteBufferImage decoder = new ByteBufferImage();
        ByteBuffer spritesheetImage = decoder.decodeImage(filepath);

        int idTexture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, idTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, spritesheetImage);

        Spritesheet spritesheet = new Spritesheet(idTexture);
        spritesheets.put(filepath,spritesheet);

        STBImage.stbi_image_free(spritesheetImage);

        return spritesheet;
    }
}

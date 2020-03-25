package cz.Empatix.Graphics;

import cz.Empatix.Main.Settings;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Framebuffer {
    private int FBO;
    private int textureID;
    public Framebuffer(){
        FBO = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, FBO);

        // generate texture
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA12, Settings.WIDTH, Settings.HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE,(ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        // attach it to currently bound framebuffer object
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);

        if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.out.print("OpenGL ERROR: Framebuffer is not complete!\n");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bindFBO() {
        glBindFramebuffer(GL_FRAMEBUFFER, FBO);
    }

    public void bindTexture() {
        glBindTexture(GL_TEXTURE_2D,textureID);
    }
    public void unbindTexture(){
        glBindTexture(GL_TEXTURE_2D,0);

    }
    public void unbindFBO() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
}

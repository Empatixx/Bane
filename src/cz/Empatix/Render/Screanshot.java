package cz.Empatix.Render;


import cz.Empatix.Main.Settings;
import cz.Empatix.Utility.CopyImagetoClipBoard;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;

import static org.lwjgl.opengl.GL11.glReadBuffer;

public class Screanshot extends Thread{
    private boolean screaning;
    private CopyImagetoClipBoard copyImagetoClipBoard;

    public Screanshot(){
        copyImagetoClipBoard = new CopyImagetoClipBoard();
    }

    public void keyPressed() {
        if(screaning) return;
        screaning = true;
        glReadBuffer(GL11.GL_FRONT);
        int width = Settings.WIDTH;
        int height = Settings.HEIGHT;
        int bpp = 4;


        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );
        Thread t = new Thread(() -> {
            int screanNum = 0;
            File screensFolder = new File("Screenshots");
            if(!screensFolder.exists()){
                screensFolder.mkdir();
            }
            File file = new File("Screenshots\\"+LocalDate.now()+".png"); // The file to save to.
            while (file.exists()) {
                file = new File("Screenshots\\"+LocalDate.now()+"-"+screanNum+".png");
                screanNum++;
            }
            String format = "png"; // "PNG" or "JPG"
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for(int x = 0; x < width; x++)
            {
                for(int y = 0; y < height; y++)
                {
                    int i = (x + (width * y)) * bpp;
                    int r = buffer.get(i) & 0xFF;
                    int g = buffer.get(i + 1) & 0xFF;
                    int b = buffer.get(i + 2) & 0xFF;
                    image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }
            try {
                ImageIO.write(image, format, file);
            } catch (IOException e) { e.printStackTrace(); }

            copyImagetoClipBoard.copyImage(image);


            screaning=false;
        });
        t.start();
    }
}

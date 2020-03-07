package cz.Empatix.Main;

import org.lwjgl.glfw.GLFWVidMode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;


public class Settings {
    // grafics
    // viewport dimensions
    public static int WIDTH;
    public static int HEIGHT;

    // audio
    public static float OVERALL;
    public static float EFFECTS;
    public static float MUSIC;

    // CONTROLS

    public static char UP;
    public static char DOWN;
    public static char LEFT;
    public static char RIGHT;
    public static char RELOAD;

    public static void init(){
        // Load Settings
        Properties Props = new Properties();
        try{
            Props.loadFromXML(new FileInputStream("settings.xml"));

            WIDTH = Integer.valueOf(Props.getProperty("width"));
            HEIGHT = Integer.valueOf(Props.getProperty("height"));

            OVERALL = Float.valueOf(Props.getProperty("overall"));
            EFFECTS = Float.valueOf(Props.getProperty("effects"));
            MUSIC = Float.valueOf(Props.getProperty("music"));

        } catch (Exception e){
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            try {
                int width = vidmode.width();
                int height = vidmode.height();

                WIDTH = width;
                HEIGHT = height;

                Props.setProperty("width",Integer.toString(width));
                Props.setProperty("height",Integer.toString(height));

                Props.setProperty("overall",Float.toString(1f));
                Props.setProperty("effects",Float.toString(1f));
                Props.setProperty("music",Float.toString(1f));


                Props.storeToXML(new FileOutputStream("settings.xml"), "");

            } catch (Exception err){
                err.printStackTrace();
            }
        }

    }
    public static float scaleHeight(){
        return (float)HEIGHT / 1080;
    }
    public static float scaleWidth(){
        return (float)WIDTH / 1920;
    }

}

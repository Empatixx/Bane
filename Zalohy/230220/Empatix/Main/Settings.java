package cz.Empatix.Main;

import org.lwjgl.glfw.GLFWVidMode;

import java.io.FileOutputStream;
import java.util.Properties;

import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;


public class Settings {
    // grafics
    // viewport dimensions
    private static int WIDTH;
    private static int HEIGHT;

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

    public static void setDimensions(int width, int height){
        WIDTH = width;
        HEIGHT = height;
    }
    public static void init(){
        Properties defaultProps = new Properties();

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        try {
            int width = vidmode.width();
            int height = vidmode.height();

            WIDTH = width;
            HEIGHT = height;

            defaultProps.setProperty("width",Integer.toString(width));
            defaultProps.setProperty("height",Integer.toString(height));


            defaultProps.storeToXML(new FileOutputStream("settings.xml"), "");

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}

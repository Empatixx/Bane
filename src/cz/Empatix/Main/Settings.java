package cz.Empatix.Main;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL12;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import static org.lwjgl.glfw.GLFW.*;


public class Settings {
    // grafics
    private static boolean fixedCameraSize;
    // viewport dimensions
    private static int maxWIDTH;
    private static int maxHEIGHT;
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

    public static void init() {
        int[] supportedDimensions =
                {
                        1920, 1080,
                        1600, 900,
                        1440, 900,
                        1440, 720,
                        1366, 768,
                        1280, 720,
                        1024, 768,
                        800, 600,
                };

        // Load Settings
        Properties Props = new Properties();
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        maxHEIGHT = vidmode.height();
        maxWIDTH = vidmode.width();

        try {
            Props.loadFromXML(new FileInputStream("settings.xml"));

            WIDTH = Integer.valueOf(Props.getProperty("width"));
            HEIGHT = Integer.valueOf(Props.getProperty("height"));

            OVERALL = Float.valueOf(Props.getProperty("overall"));
            EFFECTS = Float.valueOf(Props.getProperty("effects"));
            MUSIC = Float.valueOf(Props.getProperty("music"));

        } catch (Exception e) {
            try {

                System.out.println("max "+maxWIDTH+" / "+maxHEIGHT);
                for (int i = 0; i < supportedDimensions.length - 1; i++) {
                    if (maxWIDTH >= supportedDimensions[i] && maxHEIGHT >= supportedDimensions[i + 1]) {
                        WIDTH = supportedDimensions[i];
                        HEIGHT = supportedDimensions[i + 1];
                        break;
                    }
                }

                OVERALL = 1f;
                EFFECTS = 1f;
                MUSIC = 1f;

                Props.setProperty("width", Integer.toString(WIDTH));
                Props.setProperty("height", Integer.toString(HEIGHT));

                Props.setProperty("overall", Float.toString(1f));
                Props.setProperty("effects", Float.toString(1f));
                Props.setProperty("music", Float.toString(1f));


                Props.storeToXML(new FileOutputStream("settings.xml"), "");

            } catch (Exception err) {
                err.printStackTrace();
            }
        }
        int[] fixedSize =
                {
                        1600, 900,
                        1440, 900,
                        800, 600,

                };
        fixedCameraSize = false;
        for (int j = 0; j < fixedSize.length; j += 2) {
            if (WIDTH == fixedSize[j] && HEIGHT == fixedSize[j + 1]) {
                fixedCameraSize = true;
            }
        }

    }

    public static boolean isFixedCameraSize() {
        return fixedCameraSize;
    }

    public static float scaleMouseX() {
        return (float) 1920 / WIDTH;
    }

    public static float scaleMouseY() {
        return (float) 1080 / HEIGHT;
    }

    public static void lowerResolution() {
        int[] supportedDimensions =
                {
                        1920, 1080,
                        1600, 900,
                        1440, 900,
                        1366, 768,
                        1280, 720,
                        1024, 768,
                        800, 600,
                };
        for (int i = 0; i < supportedDimensions.length; i += 2) {
            if (WIDTH == supportedDimensions[i] && HEIGHT == supportedDimensions[i + 1]) {
                if (i + 3 > supportedDimensions.length) return;
                HEIGHT = supportedDimensions[i + 3];
                WIDTH = supportedDimensions[i + 2];
                break;
            }
        }
        int[] fixedSize =
                {
                        1600, 900,
                        1440, 900,
                        800, 600,

                };
        fixedCameraSize = false;
        for (int j = 0; j < fixedSize.length; j += 2) {
            if (WIDTH == fixedSize[j] && HEIGHT == fixedSize[j + 1]) {
                fixedCameraSize = true;
            }
        }
        GL12.glViewport(0, 0, WIDTH, HEIGHT);
        glfwSetWindowSize(Game.window, WIDTH, HEIGHT);
    }

    public static void higherResolution() {

        int[] supportedDimensions =
                {
                        1920, 1080,
                        1600, 900,
                        1440, 900,
                        1366, 768,
                        1280, 720,
                        1024, 768,
                        800, 600,
                };
        for (int i = 0; i < supportedDimensions.length; i += 2) {
            if (WIDTH == supportedDimensions[i] && HEIGHT == supportedDimensions[i + 1]) {
                if (i - 2 < 0) return;
                int newHeight = supportedDimensions[i - 1];
                int newWidth = supportedDimensions[i - 2];
                if (maxWIDTH < newWidth || maxHEIGHT < newHeight) return;
                HEIGHT = newHeight;
                WIDTH = newWidth;
                break;
            }
        }
        int[] fixedSize =
                {
                        1600, 900,
                        1440, 900,
                        800, 600,
                };
        fixedCameraSize = false;
        for (int j = 0; j < fixedSize.length; j += 2) {
            if (WIDTH == fixedSize[j] && HEIGHT == fixedSize[j + 1]) {
                fixedCameraSize = true;
            }
        }
        GL12.glViewport(0, 0, WIDTH, HEIGHT);
        glfwSetWindowSize(Game.window, WIDTH, HEIGHT);
    }
    public static void save(){
        Properties Props = new Properties();
        Props.setProperty("width", Integer.toString(WIDTH));
        Props.setProperty("height", Integer.toString(HEIGHT));

        Props.setProperty("overall", Float.toString(OVERALL));
        Props.setProperty("effects", Float.toString(EFFECTS));
        Props.setProperty("music", Float.toString(MUSIC));

        try{
            Props.storeToXML(new FileOutputStream("settings.xml"), "");

        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
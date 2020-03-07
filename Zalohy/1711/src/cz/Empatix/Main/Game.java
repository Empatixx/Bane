package cz.Empatix.Main;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Render.ByteBufferImage;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game implements Runnable {
    public static boolean displayCollisions = false;

    public final static int WIDTH = 960;
    public final static int HEIGHT = 540;
    private final static int SCALE = 2;

    private boolean running;
    private Thread thread;

    private GameStateManager gsm;

    // The window handle
    private long window;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWKeyCallback keyCallback;

    private void start() {
        if (thread == null) {
            thread = new Thread(this,"Game");
            thread.start();
        }
    }

    /**
     * creating a main window of game
     */
    private void initWindow() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()){
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WIDTH*SCALE, HEIGHT*SCALE, "2D Game", glfwGetPrimaryMonitor(), NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        keyCallback = new KeyboardInput(this);
        mouseButtonCallback = new MouseInput(this);
        glfwSetKeyCallback(window, keyCallback); // keyboard input check
        glfwSetMouseButtonCallback(window, mouseButtonCallback);
        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // window image icon
        try {
            BufferedImage icon = ImageIO.read(new File("Textures\\Icon.png"));
            GLFWImage image = GLFWImage.malloc();
            GLFWImage.Buffer imagebf = GLFWImage.malloc(1);

            image.set(icon.getWidth(), icon.getHeight(), new ByteBufferImage(icon).decodeImage());
            imagebf.put(0, image);

            glfwSetWindowIcon(window,imagebf);

            image.free();
            imagebf.free();
        } catch (Exception e){
            e.printStackTrace();
        }
        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

    }

    /**
     * setting information
     */
    private void initGame(){

        running = true;

        gsm = new GameStateManager();

    }


    /**
     * FPS SYSTEM
     */
    @Override
    public void run() {
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);

        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        final double ns = 1000000000.0 / 60.0;

        double delta = 0;

        // UPS/FPS counter
        int frames = 0;
        int updates = 0;

        // WINDOW INIT
        initWindow();

        // Without this won't opengl work
        GL.createCapabilities();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // GAMESTATE / RUNNING
        initGame();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0,960,540,0, 1, -1);
        glMatrixMode(GL_MODELVIEW);

        while ( running ) {
            long now = System.nanoTime();
            delta += (now-lastTime) / ns;
            lastTime = now;

            while (delta >= 1){
                update();
                updates++;
                delta--;

            }
            frames++;
            draw();

            if (System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                System.out.print("UPS: "+updates+"   "+"FPS: "+frames+"\n");
                // GARBAGE COLLECTOR
                System.gc();

                frames = 0;
                updates = 0;
            }
        }
        keyCallback.free();
        mouseButtonCallback.free();
        GL.setCapabilities(null);
        AudioManager.cleanUp();
        glfwDestroyWindow(window);

    }
    private void update() {
        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents();

        // gamestate update
        gsm.update();
    }

    private void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        // gamestate draw
        gsm.draw();

        glfwSwapBuffers(window); // swap the color buffers

    }
    void keyPressed(int key) {
        gsm.keyPressed(key);
    }

    void keyReleased(int key) {
        gsm.keyReleased(key);
    }

    void mousePressed(int button) {
        gsm.mousePressed(button);
    }
    void mouseReleased(int button) {
        gsm.mouseReleased(button);
    }

    public static void main(String[] args){
        new Game().start();
    }
    void stopGame(){
        running = false;
    }
}

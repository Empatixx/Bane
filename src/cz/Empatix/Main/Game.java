package cz.Empatix.Main;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Graphics.ByteBufferImage;
import cz.Empatix.Render.LoadingScreen;
import cz.Empatix.Render.Text.TextRender;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game implements Runnable{
    public static int ARROW = 0;
    public static int CROSSHAIR = 1;
    private static long[] cursors;

    public static boolean displayCollisions = false;

    private static boolean running;
    private Thread thread;

    private static int FPS;

    private GameStateManager gsm;

    // The window hoandle
    public static long window;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWKeyCallback keyCallback;

    private TextRender text;

    private void start(){
        if(thread == null){
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

        // getting settings data
        Settings.init();

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_DECORATED,GLFW_FALSE);
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE); // the window will be focused by windows
        //glfwWindowHint( GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);


        // Create the window
        window = glfwCreateWindow(Settings.WIDTH, Settings.HEIGHT, "Bane", glfwGetPrimaryMonitor(), NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        keyCallback = new KeyboardInput(this);
        mouseButtonCallback = new MouseInput(this);
        // TODO: cursor callback is null
        glfwSetKeyCallback(window, keyCallback); // keyboard input check
        glfwSetMouseButtonCallback(window, mouseButtonCallback);
        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback(){
            @Override
            public void invoke(long window, double xpos, double ypos) {
                gsm.mousePos(xpos,ypos);
            }
        });
        glfwSetWindowIconifyCallback(window, new GLFWWindowIconifyCallback() {
            @Override
            public void invoke(long window, boolean iconified) {
                gsm.pause();
            }
        });

        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                gsm.mouseScroll(xoffset,yoffset);
            }
        });
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
            GLFWImage image = GLFWImage.malloc();
            GLFWImage.Buffer imagebf = GLFWImage.malloc(1);

            ByteBufferImage decoder = new ByteBufferImage();
            ByteBuffer buffer = decoder.decodeImage("Textures\\Icon.png");

            image.set(decoder.getWidth(), decoder.getHeight(), buffer);
            imagebf.put(0, image);

            glfwSetWindowIcon(window,imagebf);

            // free allocs
            image.free();
            imagebf.free();
            STBImage.stbi_image_free(buffer);
        } catch (Exception e){
            e.printStackTrace();
        }
        try {

            cursors = new long[2];
            //cursor - loading image
            for (int i = 0;i<2;i++) {
                int xhot,yhot;
                String img;
                if(i == 0) {
                    xhot = 12;
                    yhot = 7;
                    img = "Textures\\cursor.tga";
                }
                else {
                    xhot = 32;
                    yhot = 32;
                    img = "Textures\\crosshair.tga";
                }
                GLFWImage cursorImage = GLFWImage.malloc();

                ByteBufferImage cursorDecoder = new ByteBufferImage();
                ByteBuffer buffer = cursorDecoder.decodeImage(img);

                cursorImage.set(cursorDecoder.getWidth(), cursorDecoder.getHeight(), buffer);
                long cursor = GLFW.glfwCreateCursor(cursorImage, xhot, yhot);
                cursors[i] = cursor;


                if (cursor == MemoryUtil.NULL)
                    throw new RuntimeException("Error creating cursor");

                STBImage.stbi_image_free(buffer);
                cursorImage.free();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(Settings.VSYNC ? 1 : 0);

        glfwFocusWindow(window);



    }

    /**
     * setting information
     */
    private void initGame(){

        running = true;

        Random.init();

        gsm = new GameStateManager();

    }


    /**
     * FPS SYSTEM
     */
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

        loading();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0,1920,1080,0,1,-1);
        glViewport(0,0,Settings.WIDTH,Settings.HEIGHT);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_BLEND);

        /*glEnable(GL_DEBUG_OUTPUT);
        glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        //KHRDebug.glDebugMessageControl(GL_DONT_CARE, GL_DEBUG_TYPE_PERFORMANCE, GL_DEBUG_SEVERITY_HIGH, 0,
        //        true);
        KHRDebug.glDebugMessageCallback(new GLDebugMessageCallback() {
            @Override
            public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
                if(type == GL_DEBUG_TYPE_PERFORMANCE){
                    System.out.println(GLDebugMessageCallback.getMessage(length,message));

                }
            }
        },NULL);
*/

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glHint(GL_LINE_SMOOTH_HINT,GL_NICEST);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // GAMESTATE / RUNNING
        initGame();

        text = new TextRender();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while ( running ) {
            if(glfwWindowShouldClose(window)){
                running=false;
            }
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
                FPS = frames;
                // GARBAGE COLLECTOR
                System.gc();

                frames = 0;
                updates = 0;
            }
        }
        Settings.save();
        keyCallback.free();
        mouseButtonCallback.free();
        cursorPosCallback.free();
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

        if(displayCollisions){
            text.draw("FPS: "+FPS,new Vector3f(200, 400,0),2,new Vector3f(1.0f,1.0f,1.0f));
        }

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

    public static void stopGame(){
        running = false;
    }

    public static void setCursor(int type){
        GLFW.glfwSetCursor(window, cursors[type]);


    }
    public void loading(){
        LoadingScreen loadingScreen = new LoadingScreen();
        loadingScreen.draw();
        glfwSwapBuffers(window);

        // Make the window visible
        glfwShowWindow(window);
    }
}

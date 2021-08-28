package cz.Empatix.Main;

import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;

public class KeyboardInput extends GLFWKeyCallback {
    private final Game game;

    public static boolean capsLock;
    public static boolean numLock;

    private boolean pressedShift;

    KeyboardInput(Game game){
        this.game = game;
    }


    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action==GLFW_PRESS) game.keyPressed(key);
        if (action==GLFW_RELEASE) game.keyReleased(key);

        if(action == GLFW_PRESS && (key == GLFW_KEY_RIGHT_SHIFT || key == GLFW_KEY_LEFT_SHIFT)){
            capsLock = true;
            pressedShift = true;
        } else if (action == GLFW_RELEASE && (key == GLFW_KEY_RIGHT_SHIFT || key == GLFW_KEY_LEFT_SHIFT)){
            capsLock = false;
            pressedShift = false;
        } else if (!pressedShift) {
            capsLock = (mods & GLFW_MOD_CAPS_LOCK) == 16;
        }
        numLock = (mods & GLFW_MOD_NUM_LOCK) == 32;

    }
}

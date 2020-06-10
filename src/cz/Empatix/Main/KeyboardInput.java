package cz.Empatix.Main;

import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyboardInput extends GLFWKeyCallback {
    private final Game game;
    KeyboardInput(Game game){
        this.game = game;
    }


    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action==GLFW_PRESS) game.keyPressed(key);
        if (action==GLFW_RELEASE) game.keyReleased(key);
        /*if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS ){
            game.stopGame();
        }*/
    }
}

package Main;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

public class MouseInput extends GLFWMouseButtonCallback {
    private Game game;

    public MouseInput(Game game){
        this.game = game;
    }
    @Override
    public void invoke(long window, int button, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) game.mousePressed(button);
        if (action == GLFW.GLFW_RELEASE) game.mouseReleased(button);
    }
}

package cz.Empatix.Gamestates;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Render.Screanshot;
import cz.Empatix.Render.Text.TextRender;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class GameStateManager {

    private final ArrayList<GameState> gameStates;
    private int currentState;

    protected static final int MENU = 0;
    protected static final int INGAME = 1;

    private Screanshot screanshot;

    public void pause(){
        if(currentState == INGAME){
            ((InGame)gameStates.get(currentState)).pause();
        }
    }

    public GameStateManager() {
        // openGL matrix4f
        // audio
        AudioManager.init();
        AudioManager.setListenerData(0,0);

        //text render init
        TextRender.init();

        gameStates = new ArrayList<>();

        currentState = MENU;
        gameStates.add(new MenuState(this));
        gameStates.add(new InGame(this));

        screanshot = new Screanshot();
    }

    public void setState(int state) {
        currentState = state;
        gameStates.get(currentState).init();
    }

    public void update() {
        gameStates.get(currentState).update();
    }

    public void draw() {
        gameStates.get(currentState).draw();
    }

    public void keyPressed(int k) {
        gameStates.get(currentState).keyPressed(k);
        if(k == GLFW.GLFW_KEY_F2 || k == GLFW.GLFW_KEY_PRINT_SCREEN){
            screanshot.keyPressed();
        }
    }

    public void keyReleased(int k) {
        gameStates.get(currentState).keyReleased(k);
    }

    public void mousePressed(int button) {
        gameStates.get(currentState).mousePressed(button);
    }

    public void mouseReleased(int button) {
        gameStates.get(currentState).mouseReleased(button);
    }

    public void mouseScroll(double x, double y) {
        gameStates.get(currentState).mouseScroll(x,y);
    }

}

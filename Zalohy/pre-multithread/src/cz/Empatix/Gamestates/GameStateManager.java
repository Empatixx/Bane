package cz.Empatix.Gamestates;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Text.TextRender;

import java.util.ArrayList;

public class GameStateManager {

    private final ArrayList<GameState> gameStates;
    private int currentState;

    protected static final int MENU = 0;
    protected static final int INGAME = 1;

    public GameStateManager() {
        // openGL matrix4f
        Camera camera = new Camera(1920,1080);

        // audio
        AudioManager.init();
        AudioManager.setListenerData(0,0);

        //text render init
        TextRender.init();

        gameStates = new ArrayList<>();

        currentState = MENU;
        gameStates.add(new MenuState(this, camera));
        gameStates.add(new InGame(this, camera));

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

}

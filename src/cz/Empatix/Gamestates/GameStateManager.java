package cz.Empatix.Gamestates;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.Database.Database;
import cz.Empatix.Main.DataManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Render.Screanshot;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class GameStateManager {

    private final ArrayList<GameState> gameStates;
    private int currentState;

    protected static final int MENU = 0;
    protected static final int INGAME = 1;
    protected static final int PROGRESSROOM = 2;

    private Screanshot screenshot;
    private static Database db;

    float mouseX,mouseY;

    public void pause(){
        if(currentState == INGAME){
            ((InGame)gameStates.get(currentState)).pause();
        }
    }

    public static Database getDb() {
        return db;
    }

    public GameStateManager() {
        gameStates = new ArrayList<>();

        currentState = MENU;
        gameStates.add(new MenuState(this));
        gameStates.add(new InGame(this));
        gameStates.add(new ProgressRoom(this));
        screenshot = new Screanshot();
    }
    public static void loadDatabase(){
        db = new Database();
        db.load();
    }
    public static void loadAudio(){
        AudioManager.init();
        AudioManager.setListenerData(0,0);
    }

    public void setState(int state) {
        int previousState = currentState;
        currentState = state;
        gameStates.get(currentState).init();
        if(previousState == INGAME && currentState == PROGRESSROOM){
            ((ProgressRoom)gameStates.get(currentState)).transition();
        }
    }
    public void LoadGame() {
        gameStates.set(1, DataManager.load());
        ((InGame) gameStates.get(1)).loadGame(this);
        currentState = INGAME;

    }
    public void update() {
        gameStates.get(currentState).update();
    }

    public void draw() {
        gameStates.get(currentState).draw();
    }

    public void keyPressed(int k) {
        if(k == GLFW.GLFW_KEY_F2 || k == GLFW.GLFW_KEY_PRINT_SCREEN){
            screenshot.keyPressed();
        }
        if (k == GLFW.GLFW_KEY_F1){
            Game.displayCollisions = !Game.displayCollisions;
        }
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

    public void mouseScroll(double x, double y) {
        gameStates.get(currentState).mouseScroll(x,y);
    }

    public float getMouseX(){
        return mouseX;
    }
    public float getMouseY(){
        return mouseY;
    }
    public void mousePos(double xpos, double ypos){
        mouseX = (float)(xpos * Settings.scaleMouseX());
        mouseY = (float)(ypos * Settings.scaleMouseY());
    }
}

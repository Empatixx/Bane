package cz.Empatix.Gamestates;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.Database.Database;
import cz.Empatix.Gamestates.Multiplayer.InGameMP;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Multiplayer.ProgressRoomMP;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Gamestates.Singleplayer.ProgressRoom;
import cz.Empatix.Main.Game;
import cz.Empatix.Main.Settings;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PacketHolder;
import cz.Empatix.Render.Screenshot;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class GameStateManager {

    private final ArrayList<GameState> gameStates;
    private static int currentState;

    public static final int MENU = 0;
    public static final int INGAME = 1;
    public static final int PROGRESSROOM = 2;

    public static final int PROGRESSROOMMP = 3;
    public static final int INGAMEMP = 4;


    private Screenshot screenshot;
    private static Database db;
    private MultiplayerManager mpManager;

    public static long timeUpdate;

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

        gameStates.add(new ProgressRoomMP(this));
        gameStates.add(new InGameMP(this));

        screenshot = new Screenshot();
    }
    public static void loadDatabase(){
        db = new Database();
        db.load();
    }
    public void setState(int state) {
        int previousState = currentState;
        currentState = state;
        gameStates.get(currentState).init();
        if(previousState == INGAME && currentState == PROGRESSROOM){
            ((ProgressRoom)gameStates.get(currentState)).transition();
        }
    }

    /**
     * functions works same as setState, but it has one more parameter 'host' for multiplayer support
     * use only once for init multiplayer manager
     * @param state - gamestate
     * @param host - if user is host or not
     */
    public void setStateInitMP(int state, boolean host, String username, String ip) {
        int previousState = currentState;
        currentState = state;
        if (currentState == PROGRESSROOMMP) {

            mpManager = new MultiplayerManager(host,this,ip);

            Client client = mpManager.client.getClient();
            Network.Join join = new Network.Join();
            join.username = username;
            join.host = mpManager.isHost();
            client.sendTCP(join);

            while(client.isConnected()){
                Object[] objects = mpManager.packetHolder.get(PacketHolder.CANJOIN);
                if(objects.length >= 1){
                    Network.CanJoin canJoin = (Network.CanJoin) objects[0];
                    if(canJoin.can){
                        mpManager.setIdConnection(canJoin.idPlayer);
                        break;
                    }
                    else{
                        currentState = previousState;
                        mpManager.close();
                        return;
                    }
                }
            }
            // if packet never arrived and client was closed
            if(mpManager.isNotConnected()){
                currentState = previousState;
                mpManager.close();
                return;
            }
            mpManager.setUsername(username);

            gameStates.get(currentState).init();
            if (previousState == INGAME) {
                ((ProgressRoomMP) gameStates.get(currentState)).transition();
            }
        } else {
            gameStates.get(currentState).init();
        }
    }
    public void update() {
        timeUpdate = System.currentTimeMillis();
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
    public static void EnterGame() {
        if(currentState == PROGRESSROOM){
            ProgressRoom.enterGame = true;
        } else {
            ProgressRoomMP.ready = true;
        }
    }

    public GameState getCurrentGamestate(){return gameStates.get(currentState);}

}

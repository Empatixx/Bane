package cz.Empatix.Gamestates;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.ProgressNPC;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.DiscordRP;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.GameClient;
import cz.Empatix.Multiplayer.GameServer;
import cz.Empatix.Multiplayer.Packet00Login;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Framebuffer;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Postprocessing.Fade;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static cz.Empatix.Main.Game.ARROW;
import static org.lwjgl.opengl.GL11.*;

public class ProgressRoom extends GameState {
    private GameClient socketClient;
    private GameServer socketServer;

    public Player player[];
    public TileMap tileMap;

    private float mouseX;
    private float mouseY;

    private Framebuffer objectsFramebuffer;

    private LightManager lightManager;
    private Fade fade;
    private Framebuffer transitionFBO;
    private boolean transition;

    private cz.Empatix.Render.Hud.Image coin;

    private static boolean enterGame;

    private ProgressNPC progressNPC;

    private TextRender textRender;

    private AlertManager alertManager;

    ProgressRoom(GameStateManager gsm){
        this.gsm = gsm;

        textRender = new TextRender();

    }
    public void transition(){
        fade.setReverse();
        transition = true;
    }
    @Override
    void init() {
        // server host
        if(true) {
            socketServer = new GameServer(this);
            socketServer.start();
        }

        socketClient = new GameClient(this,"localhost");
        socketClient.start();

        Game.setCursor(ARROW);
        DiscordRP.getInstance().update("In-Game","Rest room");

        objectsFramebuffer = new Framebuffer();
        lightManager = new LightManager();

        fade = new Fade("shaders\\fade");
        transitionFBO = new Framebuffer();

        // Tile map
        tileMap = new TileMap(64);
        tileMap.loadTiles("Textures\\tileset64.tga");

        // player
        // create player object
        player = new Player[2];

        player[0] = new Player(tileMap);
        player[0].setCoins(GameStateManager.getDb().getValue("money","general"));

        Packet00Login loginPacket = new Packet00Login("User "+Random.nextInt(200));
        loginPacket.writeData(socketClient);

        // generate map + create objects which needs item manager & gun manager created
        tileMap.loadProgressRoom();
        // move player to starter room
        player[0].setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());
        tileMap.setPosition(
                Camera.getWIDTH() / 2f - player[0].getX(),
                Camera.getHEIGHT() / 2f - player[0].getY()
        );
        // make camera move smoothly
        tileMap.setTween(0.1);

        coin = new Image("Textures\\coin.tga",new Vector3f(75,1000,0),1.5f);

        progressNPC = new ProgressNPC(tileMap);
        progressNPC.setPosition(23*tileMap.getTileSize(),9*tileMap.getTileSize()/2);

        alertManager = new AlertManager();

        AudioManager.playSoundtrack(Soundtrack.PROGRESSROOM);
        int upgradesCount = progressNPC.getCountAvailableUpgrades(player[0]);
        if(upgradesCount > 0) AlertManager.add(AlertManager.INFORMATION,"You can buy "+upgradesCount+" upgrades");
        AlertManager.add(AlertManager.INFORMATION,"Go to the portal");
    }

    @Override
    void draw() {
        objectsFramebuffer.bindFBO();
        // clear framebuffer
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        tileMap.draw(Tile.NORMAL);
        for (Player p:player) {
            if(p != null) p.drawShadow();
        }

        tileMap.preDrawObjects(true);
        tileMap.draw(Tile.BLOCKED);

        tileMap.preDrawObjects(false);
        for (Player p:player) {
            if(p != null){
                if(p.getY() > progressNPC.getY()+40){
                    progressNPC.draw();
                    p.draw();
                } else {
                    p.draw();
                    progressNPC.draw();
                }
            }
        }

        // draw objects
        tileMap.drawObjects();

        objectsFramebuffer.unbindFBO();

        if(transition){
            transitionFBO.bindFBO();
            glClear(GL_COLOR_BUFFER_BIT);
        }

        lightManager.draw(objectsFramebuffer);


        progressNPC.drawHud();

        coin.draw();
        textRender.draw(""+player[0].getCoins(),new Vector3f(145,1019,0),3,new Vector3f(1.0f,0.847f,0.0f));

        alertManager.draw();

        if(transition){
            transitionFBO.unbindFBO();
            fade.draw(transitionFBO);
        }
    }
    @Override
    void update() {
        if(enterGame){
            enterGame=false;
            gsm.setState(GameStateManager.INGAME);
            return;
        }
        // loc of mouse
        mouseX = gsm.getMouseX();
        mouseY = gsm.getMouseY();

        // mouse location-moving direction of mouse of tilemap
        tileMap.setPosition(
                tileMap.getX()-(mouseX-960)/30,
                tileMap.getY()-(mouseY- 540)/30);

        // updating player
        // updating tilemap by player position
        tileMap.setPosition(
                Camera.getWIDTH() / 2f - player[0].getX(),
                Camera.getHEIGHT() / 2f - player[0].getY()
        );
        player[0].update();

        tileMap.updateObjects();

        progressNPC.update(mouseX,mouseY);
        progressNPC.touching(player[0]);

        alertManager.update();

        lightManager.update();
        AudioManager.update();
        if(transition){
            fade.update(transition);
            if(fade.isTransitionDone()) transition = false;
        }
    }
    @Override
    void keyPressed(int k) {
        if(k == GLFW.GLFW_KEY_ESCAPE && !progressNPC.isInteracting()){
            gsm.setState(GameStateManager.MENU);
        }
        player[0].keyPressed(k);
        tileMap.keyPressed(k,player[0]);
        progressNPC.keyPress(k);
    }

    @Override
    void keyReleased(int k) {
        player[0].keyReleased(k);
    }

    @Override
    void mousePressed(int button) {
        progressNPC.mousePressed(mouseX,mouseY,player[0]);
    }

    @Override
    void mouseReleased(int button) {
        progressNPC.mouseReleased(mouseX,mouseY);
    }

    @Override
    void mouseScroll(double x, double y) {
        progressNPC.mouseScroll(x,y);
    }

    public static void EnterGame() {
        ProgressRoom.enterGame = true;
    }
}

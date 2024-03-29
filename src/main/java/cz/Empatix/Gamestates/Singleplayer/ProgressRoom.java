package cz.Empatix.Gamestates.Singleplayer;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.ProgressRoom.MultiplayerNPC;
import cz.Empatix.Entity.ProgressRoom.ProgressNPC;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Main.DiscordRP;
import cz.Empatix.Main.Game;
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
    public Player player;
    public TileMap tileMap;

    private float mouseX;
    private float mouseY;

    private Framebuffer objectsFramebuffer;

    private LightManager lightManager;
    private Fade fade;
    private Framebuffer transitionFBO;
    private boolean transition;

    private Image coin;

    public static boolean enterGame;

    private ProgressNPC progressNPC;
    private MultiplayerNPC multiplayerNPC;

    private TextRender textRender;

    private AlertManager alertManager;

    public ProgressRoom(GameStateManager gsm){
        this.gsm = gsm;

        textRender = new TextRender();

    }
    public void transition(){
        fade.setReverse();
        transition = true;
    }
    @Override
    protected void init() {
        Game.setCursor(ARROW);
        DiscordRP.getInstance().update("Singplayer - In-Game","Rest room");

        objectsFramebuffer = new Framebuffer();

        fade = new Fade("shaders\\fade");
        transitionFBO = new Framebuffer();

        // Tile map
        tileMap = new TileMap(64);
        tileMap.loadTiles("Textures\\tileset64.tga");

        lightManager = new LightManager(tileMap);
        // player
        // create player object
        player = new Player(tileMap);

        player.setCoins(GameStateManager.getDb().getValue("money","general"));

        // generate map + create objects which needs item manager & gun manager created
        tileMap.loadProgressRoom();
        // move player to starter room
        player.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());
        tileMap.setPosition(
                Camera.getWIDTH() / 2f - player.getX(),
                Camera.getHEIGHT() / 2f - player.getY(),
                true
        );
        // make camera move smoothly
        tileMap.setTween(5);

        coin = new Image("Textures\\coin.tga",new Vector3f(75,1000,0),1.5f);

        progressNPC = new ProgressNPC(tileMap);
        progressNPC.setPosition(23*tileMap.getTileSize(),9*tileMap.getTileSize()/2);

        multiplayerNPC = new MultiplayerNPC(tileMap,gsm);
        multiplayerNPC.setPosition(6*tileMap.getTileSize(),8*tileMap.getTileSize()/2);


        alertManager = new AlertManager();

        AudioManager.playSoundtrack(Soundtrack.PROGRESSROOM);
        int upgradesCount = progressNPC.getCountAvailableUpgrades(player);
        if(upgradesCount > 0) AlertManager.add(AlertManager.INFORMATION,"You can buy "+upgradesCount+" upgrades");
        AlertManager.add(AlertManager.INFORMATION,"Go to the portal");
    }

    @Override
    protected void draw() {
        objectsFramebuffer.bindFBO();
        // clear framebuffer
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        tileMap.draw(Tile.NORMAL);
        player.drawShadow();

        tileMap.preDrawObjects(true);
        tileMap.draw(Tile.BLOCKED);

        tileMap.preDrawObjects(false);
        if(player.getY() > progressNPC.getY()+40){
            progressNPC.draw();
            player.draw();
        } else {
            player.draw();
            progressNPC.draw();
        }
        multiplayerNPC.draw();
        // draw objects
        tileMap.drawObjects();

        objectsFramebuffer.unbindFBO();

        if(transition){
            transitionFBO.bindFBO();
            glClear(GL_COLOR_BUFFER_BIT);
        }

        lightManager.draw(objectsFramebuffer);


        progressNPC.drawHud();
        multiplayerNPC.drawHud();

        coin.draw();
        textRender.draw(""+ player.getCoins(),new Vector3f(145,1019,0),3,new Vector3f(1.0f,0.847f,0.0f));

        alertManager.draw();

        if(transition){
            transitionFBO.unbindFBO();
            fade.draw(transitionFBO);
        }
    }
    @Override
    protected void update() {
        if(enterGame){
            enterGame=false;
            gsm.setState(GameStateManager.INGAME);
            return;
        }
        // loc of mouse
        mouseX = gsm.getMouseX();
        mouseY = gsm.getMouseY();


        tileMap.updateObjects();

        player.update();
        // set tilemap/camera with trying succeeding to center player in camera, this position is also affected my mouse
        tileMap.setPosition(
                Camera.getWIDTH() / 2f - player.getX() -(mouseX-960)/30,
                Camera.getHEIGHT() / 2f - player.getY() -(mouseY- 540)/30,
                false
        );

        progressNPC.update(mouseX,mouseY);
        progressNPC.touching(player);

        multiplayerNPC.update(mouseX,mouseY);
        multiplayerNPC.touching(player);

        alertManager.update();

        lightManager.update();
        AudioManager.update();
        if(transition){
            fade.update(true);
            if(fade.isTransitionDone()) transition = false;
        }
    }
    @Override
    protected void keyPressed(int k) {
        if(k == GLFW.GLFW_KEY_ESCAPE && !progressNPC.isInteracting()){
            gsm.setState(GameStateManager.MENU);
            transition = false;
        }
        if(!multiplayerNPC.isUsingInputBar()) player.keyPressed(k);
        tileMap.keyPressed(k, player);
        progressNPC.keyPress(k);
        multiplayerNPC.keyPress(k);
    }

    @Override
    protected void keyReleased(int k) {
        player.keyReleased(k);
        multiplayerNPC.keyReleased(k);
    }

    @Override
    protected void mousePressed(int button) {
        progressNPC.mousePressed(mouseX,mouseY, player);
        multiplayerNPC.mousePressed(mouseX,mouseY, player);
        System.out.println(mouseX+" "+mouseY);
    }

    @Override
    protected void mouseReleased(int button) {
        progressNPC.mouseReleased(mouseX,mouseY);
        multiplayerNPC.mouseReleased(mouseX,mouseY);
    }

    @Override
    protected void mouseScroll(double x, double y) {
        progressNPC.mouseScroll(x,y);
        multiplayerNPC.mouseScroll(x,y);
    }

}

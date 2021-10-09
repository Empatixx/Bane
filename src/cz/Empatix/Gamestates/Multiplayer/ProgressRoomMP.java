package cz.Empatix.Gamestates.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.ProgressNPC;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PlayerMP;
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

public class ProgressRoomMP extends GameState {

    public Player[] player;
    public PlayerReady[] playerReadies;

    public TileMap tileMap;

    private float mouseX;
    private float mouseY;

    private Framebuffer objectsFramebuffer;

    private LightManager lightManager;
    private Fade fade;
    private Framebuffer transitionFBO;
    private boolean transition;

    private cz.Empatix.Render.Hud.Image coin;

    // if player is ready to start game
    public static boolean ready;
    public boolean switchGamestate;

    public static int readyNumPlayers;

    private ProgressNPC progressNPC;

    private TextRender textRender;

    private AlertManager alertManager;

    private MultiplayerManager mpManager;

    public ProgressRoomMP(GameStateManager gsm){
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
        //DiscordRP.getInstance().update("In-Game","Rest room");

        mpManager = MultiplayerManager.getInstance();
        readyNumPlayers = 0;
        ready = false;
        switchGamestate = false;

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
        playerReadies = new PlayerReady[2];

        String username = mpManager.getUsername();
        player[0] = new PlayerMP(tileMap,username);
        ((PlayerMP)player[0]).setOrigin(true);
        playerReadies[0] = new PlayerReady(username);

        Client client = mpManager.client.getClient();
        Network.Join join = new Network.Join();
        join.username = username;
        join.host = mpManager.isHost();
        client.sendTCP(join);

        player[0].setCoins(GameStateManager.getDb().getValue("money","general"));

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
    protected void draw() {
        objectsFramebuffer.bindFBO();
        // clear framebuffer
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        tileMap.draw(Tile.NORMAL);
        for (Player p: player) {
            if(p != null) p.drawShadow();
        }

        tileMap.preDrawObjects(true);
        tileMap.draw(Tile.BLOCKED);

        tileMap.preDrawObjects(false);

        // drawing players by order by position.y
        boolean[] used = new boolean[player.length];
        for(int i = 0;i<player.length;i++){
            int index = -1;
            for(int j = 0;j < player.length;j++){
                if(player[j] == null || used[j]) continue;
                if(index == -1) index = j;
                else if(player[index].getY() > player[j].getY()) index = j;
            }
            if(index != -1){
                if(player[index].getY() <= progressNPC.getY()+40) player[index].draw();
                used[index] = true;
            }
        }

        progressNPC.draw();

        // drawing players by order by position.y
        used = new boolean[player.length];
        for(int i = 0;i<player.length;i++){
            int index = -1;
            for(int j = 0;j < player.length;j++){
                if(player[j] == null || used[j]) continue;
                if(index == -1) index = j;
                else if(player[index].getY() > player[j].getY()) index = j;
            }
            if(index != -1){
                if(player[index].getY() > progressNPC.getY()+40) player[index].draw();
                used[index] = true;
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
        textRender.draw(""+ player[0].getCoins(),new Vector3f(145,1019,0),3,new Vector3f(1.0f,0.847f,0.0f));

        alertManager.draw();

        if(transition){
            transitionFBO.unbindFBO();
            fade.draw(transitionFBO);
        }
    }
    @Override
    protected void update() {
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

        readyNumPlayers = 0;
        for(PlayerReady playerReady : playerReadies){
            if(playerReady != null){
                if(playerReady.ready) readyNumPlayers++;
            }
        }
        // total connected players to the host
        int totalConPlayers = 0;
        for(PlayerReady playerReady : playerReadies){
            if(playerReady != null) totalConPlayers++;
        }
        // all players are ready => enter game
        if(totalConPlayers == readyNumPlayers){
            mpManager.client.setNumPlayers(1);
            gsm.setState(GameStateManager.INGAMEMP);
        }

        tileMap.updateObjects();

        for(Player p : player){
            if(p != null)p.update();
        }


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
    protected void keyPressed(int k) {
        if(k == GLFW.GLFW_KEY_ESCAPE && !progressNPC.isInteracting()){
            gsm.setState(GameStateManager.MENU);
            mpManager.close();
        }
        player[0].keyPressed(k);
        tileMap.keyPressed(k, player[0]);
        progressNPC.keyPress(k);
    }

    @Override
    protected void keyReleased(int k) {
        player[0].keyReleased(k);
    }

    @Override
    protected void mousePressed(int button) {
        progressNPC.mousePressed(mouseX,mouseY, player[0]);
    }

    @Override
    protected void mouseReleased(int button) {
        progressNPC.mouseReleased(mouseX,mouseY);
    }

    @Override
    protected void mouseScroll(double x, double y) {
        progressNPC.mouseScroll(x,y);
    }

    public static class PlayerReady{
        private final String username;
        private boolean ready;
        public PlayerReady(String username){
            this.ready = false;
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setReady(boolean ready) {
            this.ready = ready;
        }

    }

}

package cz.Empatix.Gamestates.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.ProgressRoom.ProgressNPC;
import cz.Empatix.Entity.RoomObjects.ProgressRoom.Portal;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Main.DiscordRP;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.*;
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

    public PlayerMP[] player;
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

    private TextRender[] textRender;

    private AlertManager alertManager;

    private MultiplayerManager mpManager;

    private long connectionAlert;

    private int ping;
    private long pingTimer;

    public ProgressRoomMP(GameStateManager gsm){
        this.gsm = gsm;
        textRender = new TextRender[2];
        for(int i = 0;i<textRender.length;i++){
            textRender[i] = new TextRender();
        }

    }
    public void transition(){
        fade.setReverse();
        transition = true;
    }


    @Override
    protected void init() {
        Game.setCursor(ARROW);
        mpManager = MultiplayerManager.getInstance();
        readyNumPlayers = 0;
        ready = false;
        switchGamestate = false;

        objectsFramebuffer = new Framebuffer();
        fade = new Fade("shaders\\fade");
        transitionFBO = new Framebuffer();

        // Tile map
        tileMap = new TileMap(64);
        tileMap.loadTiles("Textures\\tileset64.tga");

        lightManager = new LightManager(tileMap);

        // player
        // create player object
        player = new PlayerMP[2];
        playerReadies = new PlayerReady[2];

        String username = mpManager.getUsername();
        player[0] = new PlayerMP(tileMap,username);
        player[0].setOrigin(true);
        player[0].setIdConnection(mpManager.getIdConnection());
        playerReadies[0] = new PlayerReady(username,mpManager.getIdConnection());

        player[0].setCoins(GameStateManager.getDb().getValue("money","general"));

        // generate map + create objects which needs item manager & gun manager created
        tileMap.loadProgressRoom();

        // move player to starter room
        player[0].setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());
        tileMap.setPosition(
                Camera.getWIDTH() / 2f - player[0].getX(),
                Camera.getHEIGHT() / 2f - player[0].getY(),
                true
        );
        // make camera move smoothly
        tileMap.setTween(5);

        coin = new Image("Textures\\coin.tga",new Vector3f(75,1000,0),1.5f);

        progressNPC = new ProgressNPC(tileMap);
        progressNPC.setPosition(23*tileMap.getTileSize(),9*tileMap.getTileSize()/2);

        alertManager = new AlertManager();

        AudioManager.playSoundtrack(Soundtrack.PROGRESSROOM);
        int upgradesCount = progressNPC.getCountAvailableUpgrades(player[0]);
        if(upgradesCount > 0) AlertManager.add(AlertManager.INFORMATION,"You can buy "+upgradesCount+" upgrades");
        AlertManager.add(AlertManager.INFORMATION,"Go to the portal");

        DiscordRP.getInstance().update("Multiplayer - In-Game","Lobby "+mpManager.client.getTotalPlayers()+"/2");

        Object[] joinPackets = mpManager.packetHolder.get(PacketHolder.JOINPLAYER);
        int index = mpManager.client.getTotalPlayers();
        for(Object object : joinPackets) {
            Network.AddPlayer player = (Network.AddPlayer) object;
            String packetUsername = player.username;
            PlayerMP playerMP = new PlayerMP(tileMap, packetUsername);
            playerMP.setPosition(tileMap.getPlayerStartX(),tileMap.getPlayerStartY());
            playerMP.setIdConnection(player.idPlayer);
            this.player[index] = playerMP;
            playerReadies[index] = new PlayerReady(packetUsername,player.idPlayer);
            index++;
            DiscordRP.getInstance().update("Multiplayer - In-Game", "Lobby " + index + "/2");

            for(PlayerReady r : playerReadies){
                r.setReady(false);
            }
            readyNumPlayers = 0;
            ready = false;
            Portal.packetChangeSent = false;
        }
        mpManager.client.setNumPlayers(index);
        pingTimer = System.currentTimeMillis();
        GameClient.lastTime = System.nanoTime();
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
        textRender[0].draw(""+ player[0].getCoins(),new Vector3f(145,1019,0),3,new Vector3f(1.0f,0.847f,0.0f));
        if(Game.displayCollisions) textRender[1].draw("Ping: "+ping+" ms",new Vector3f(200, 450,0),2,new Vector3f(1.0f,1.0f,1.0f));
        alertManager.draw();

        if(transition){
            transitionFBO.unbindFBO();
            fade.draw(transitionFBO);
        }
    }
    @Override
    protected void update() {
        long now = System.nanoTime();
        GameClient.deltaTick += (now - GameClient.lastTime) / MultiplayerManager.ns;
        GameClient.lastTime = now;
        while(GameClient.deltaTick >= 1){
            GameClient client = mpManager.client;
            client.serverTick++;
            client.setServerTick(client.serverTick);
            client.checkTickSyncs();
            GameClient.deltaTick--;
            //System.out.println("CURR TICK: "+client.serverTick+ " INTERPOLATED TICK: "+client.interpolationTick);
        }

        if(mpManager.isNotConnected()) {
            if (System.currentTimeMillis() - connectionAlert > 5000) {
                connectionAlert = System.currentTimeMillis();
                AlertManager.add(AlertManager.WARNING, "No connection!");
                MultiplayerManager.getInstance().client.tryReconnect();
            }
            for(Player p : player){
                if(p != null){
                    p.update();
                }
            }
            // loc of mouse
            mouseX = gsm.getMouseX();
            mouseY = gsm.getMouseY();

            // set tilemap/camera with trying succeeding to center player in camera, this position is also affected my mouse
            tileMap.setPosition(
                    Camera.getWIDTH() / 2f - player[0].getX() -(mouseX-960)/30,
                    Camera.getHEIGHT() / 2f - player[0].getY() -(mouseY- 540)/30,
                    false
            );

            progressNPC.update(mouseX,mouseY);
            progressNPC.touching(player[0]);
            Object[] AlertPackets = mpManager.packetHolder.get(PacketHolder.ALERT);
            for(Object o : AlertPackets){
                Network.Alert alert = (Network.Alert) o;
                if(mpManager.getIdConnection() == alert.idPlayer){
                    AlertManager.add(alert.warning ? AlertManager.WARNING : AlertManager.INFORMATION,alert.text);
                }
            }
            alertManager.update();

            lightManager.update();
            AudioManager.update();
            return;
        }
        // loc of mouse
        mouseX = gsm.getMouseX();
        mouseY = gsm.getMouseY();

        // set tilemap/camera with trying succeeding to center player in camera, this position is also affected my mouse
        tileMap.setPosition(
                Camera.getWIDTH() / 2f - player[0].getX() -(mouseX-960)/30,
                Camera.getHEIGHT() / 2f - player[0].getY() -(mouseY- 540)/30,
                false
        );

        tileMap.updateObjects();

        player[0].updateOrigin();
        // updating player
        for(PlayerMP p : player){
            if(p != null)p.update();
        }

        readyNumPlayers = 0;
        int totalConPlayers = 0;
        for(PlayerReady playerReady : playerReadies){
            if(playerReady != null){
                if(playerReady.isReady()) readyNumPlayers++;
                totalConPlayers++;
            }
        }
        // all players are ready => enter game
        if(totalConPlayers == readyNumPlayers && totalConPlayers != 1){
            mpManager.client.setNumPlayers(1);
            gsm.setState(GameStateManager.INGAMEMP);
            mpManager.packetHolder.clear(PacketHolder.MOVEPLAYER);
            mpManager.packetHolder.clear(PacketHolder.ORIGINMOVEPLAYER);
            Client client = mpManager.client.getClient();
            Network.RequestForPlayers request = new Network.RequestForPlayers();
            request.exceptIdPlayer = mpManager.getIdConnection();

            client.sendTCP(request);
            return;

        }

        Object[] objects = mpManager.packetHolder.get(PacketHolder.MOVEPLAYER);
        for(int i = 1;i<player.length;i++) {
            PlayerMP p = player[i];
            if(p == null) continue;
            for (Object o : objects) {
                Network.MovePlayer move = (Network.MovePlayer) o;
                if (p.getIdConnection() == move.idPlayer) {
                    p.addInterpolationPosition(move);
                    p.setUp(move.up);
                    p.setDown(move.down);
                    p.setRight(move.right);
                    p.setLeft(move.left);
                }
            }
        }

        progressNPC.update(mouseX,mouseY);
        progressNPC.touching(player[0]);
        Object[] AlertPackets = mpManager.packetHolder.get(PacketHolder.ALERT);
        for(Object o : AlertPackets){
            Network.Alert alert = (Network.Alert) o;
            if(mpManager.getIdConnection() == alert.idPlayer){
                AlertManager.add(alert.warning ? AlertManager.WARNING : AlertManager.INFORMATION,alert.text);
            }
        }
        alertManager.update();

        lightManager.update();
        AudioManager.update();
        if(transition){
            fade.update(transition);
            if(fade.isTransitionDone()) transition = false;
        }
        Object[] joinPackets = mpManager.packetHolder.get(PacketHolder.JOINPLAYER);
        int index = mpManager.client.getTotalPlayers();
        for(Object object : joinPackets) {
            Network.AddPlayer player = (Network.AddPlayer) object;
            String packetUsername = player.username;
            PlayerMP playerMP = new PlayerMP(tileMap, packetUsername);
            playerMP.setIdConnection(player.idPlayer);
            this.player[index] = playerMP;
            playerReadies[index] = new PlayerReady(packetUsername,player.idPlayer);
            index++;
            DiscordRP.getInstance().update("Multiplayer - In-Game", "Lobby " + index + "/2");

            for(PlayerReady r : playerReadies){
                r.setReady(false);
            }
            readyNumPlayers = 0;
            ready = false;
            Portal.packetChangeSent = false;
        }
        mpManager.client.setNumPlayers(index);

        Object[] disconnectPackets = mpManager.packetHolder.get(PacketHolder.DISCONNECTPLAYER);
        index = mpManager.client.getTotalPlayers();
        for(Object object : disconnectPackets){
            for(PlayerMP player : player){
                if(player != null){
                    Network.Disconnect packet = (Network.Disconnect) object;
                    if(packet.idPlayer == player.getIdConnection()){
                        index--;
                        int idOrigin = mpManager.getIdConnection();
                        this.player[index].remove();
                        if(idOrigin != packet.idPlayer){
                            AlertManager.add(AlertManager.WARNING,this.player[index].getUsername()+" has left the game!");
                        }
                        this.player[index] = null;
                        playerReadies[index] = null;
                        DiscordRP.getInstance().update("Multiplayer - In-Game","Lobby "+index+"/2");
                        for(PlayerReady pready : playerReadies){
                            if(pready != null) pready.setReady(false);
                        }
                        readyNumPlayers = 0;
                        ready = false;
                        Portal.packetChangeSent = false;
                        break;
                    }
                }
            }
        }
        mpManager.client.setNumPlayers(index);

        if(System.currentTimeMillis() - pingTimer > 1000){
            pingTimer+=1000;
            Client client = MultiplayerManager.getInstance().client.getClient();
            client.sendUDP(new Network.Ping());
            ping = client.getReturnTripTime()/2;
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

}

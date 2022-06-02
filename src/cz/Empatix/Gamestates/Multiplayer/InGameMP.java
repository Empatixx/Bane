package cz.Empatix.Gamestates.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Database.Database;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.Artefacts.ArtefactManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.RoomObjects.DestroyableObject;
import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Main.DiscordRP;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.*;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Damageindicator.CombatIndicator;
import cz.Empatix.Render.Graphics.Framebuffer;
import cz.Empatix.Render.Hud.ArmorBar;
import cz.Empatix.Render.Hud.HealthBar;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Hud.MenuBar;
import cz.Empatix.Render.Hud.Minimap.MiniMap;
import cz.Empatix.Render.Postprocessing.Fade;
import cz.Empatix.Render.Postprocessing.GaussianBlur;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Console;
import cz.Empatix.Utility.Loader;
import cz.Empatix.Utility.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static cz.Empatix.Main.Game.ARROW;
import static cz.Empatix.Main.Game.setCursor;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;


public class InGameMP extends GameState {
    private long deathTime;
    private float totalDelta;

    public static void load(){
        Loader.loadImage("Textures\\Menu\\pausemenu.tga");
        Loader.loadImage("Textures\\Menu\\bg.png");
        Loader.loadImage("Textures\\skull.tga");
        Loader.loadImage("Textures\\killslogo.tga");
        Loader.loadImage("Textures\\coinlogo.tga");
        Loader.loadImage("Textures\\accuracylogo.tga");
        Loader.loadImage("Textures\\timelogo.tga");

    }

    private boolean pause;
    private boolean endRewardEarned;
    private long gameStart;

    //
    // Main game
    //

    // death menu
    private Image skullPlayerdead;
    private Image[] logos;

    public PlayerMP[] player;
    public PlayerReady[] playerReadies;

    public TileMap tileMap;

    public GunsManager gunsManager;

    private float mouseX;
    private float mouseY;

    // ingame huds
    private HealthBar[] healthBar;
    private ArmorBar[] armorBar;
    private MiniMap miniMap;
    private CombatIndicator damageIndicator;
    private Image coin;
    private Image deathIcon;
    private Console console;
    private AlertManager alertManager;

    public EnemyManager enemyManager;

    // postprocessing
    private Framebuffer objectsFramebuffer;
    private Framebuffer pauseBlurFramebuffer;
    private Framebuffer fadeFramebuffer;
    private Fade fade;
    private boolean transitionContinue;
    private GaussianBlur gaussianBlur;
    private LightManager lightManager;


    public ItemManager itemManager;
    public ArtefactManager artefactManager;
    //
    // Paused game
    //
    private Background pauseBackground;
    private MenuBar[] pauseBars;
    // bars types
    private final static int PAUSEEXIT = 2;
    private final static int PAUSESETTINGS = 1;
    private final static int PAUSERESUME = 0;

    private int soundMenuClick;
    private Source source;

    private TextRender[] textRender;
    private TextRender[] barName;

    private MultiplayerManager mpManager;
    private MPStatistics mpStatistics;

    public boolean mapLoaded;
    public boolean postDeath;
    public int totalRoomsSynch;
    private int readyNumPlayers;
    private boolean ready;
    private long connectionAlert;
    private int ping;
    private long pingTimer;
    public InGameMP(GameStateManager gsm){
        this.gsm = gsm;
    }

    @Override
    protected void mouseReleased(int button) {
        if(pause){
            for(MenuBar bar : pauseBars){
                if(bar.intersects(mouseX,mouseY)){
                    int type = bar.getType();
                    source.play(soundMenuClick);
                    if(type == PAUSEEXIT){
                        player[0].cleanUp();
                        mpManager.close();
                        gsm.setState(GameStateManager.MENU);
                    } else if (type == PAUSERESUME){
                        resume();
                        gunsManager.stopShooting();
                        setCursor(Game.CROSSHAIR);
                    }
                }
            }
        } else{
            gunsManager.stopShooting();
        }
    }

    @Override
    protected void mousePressed(int button) {
        if(button == ControlSettings.getValue(ControlSettings.SHOOT)){
            if(!pause && !player[0].isDead())gunsManager.startShooting();
        }
    }
    @Override
    public void mouseScroll(double x, double y){
        gunsManager.changeGunScroll();
    }
    @Override
    protected void keyReleased(int k) {
        if (k == GLFW_KEY_ESCAPE && !postDeath){
            pause = !pause;
            if(pause){
                setCursor(ARROW);
            } else {
                resume();
            }
            gunsManager.stopShooting();
        }
        if(!player[0].isDead() || player[0].isGhost()){
            player[0].keyReleased(k);
            miniMap.keyReleased(k);
        }
        if(player[0].isDead()) return;
        if(console.isEnabled()){
            console.keyReleased(k);
        }

        if(pause) return;

        if (k == ControlSettings.getValue(ControlSettings.RELOAD)){
            Network.Reload reload = new Network.Reload();
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            reload.idPlayer = mpManager.getIdConnection();
            Client client = mpManager.client.getClient();
            client.sendUDP(reload);
            mpManager.client.requestACK(reload,reload.idPacket);
            gunsManager.reload();
        }
    }

    @Override
    protected void keyPressed(int k) {
        if(postDeath){
            if(k == ControlSettings.getValue(ControlSettings.OBJECT_INTERACT) && !ready && System.currentTimeMillis() - deathTime > 3000){
                Network.Ready ready = new Network.Ready();
                mpManager.client.requestACK(ready,ready.idPacket);
                ready.idPlayer = mpManager.getIdConnection();
                ready.state = true;
                Client client = mpManager.client.getClient();
                client.sendUDP(ready);
                this.ready = true;
            }
        } else {
            player[0].keyPressed(k);
            miniMap.keyPressed(k);
        }
        if(player[0].isDead()) return;
        if(pause) return;


        if (k == GLFW.GLFW_KEY_F3){
            console.setEnabled(!console.isEnabled());
        }

        if(console.isEnabled()){
            console.keyPressed(k);
            return;
        }
        float px = player[0].getX();
        float py = player[0].getY();
        float mx = tileMap.getX();
        float my = tileMap.getY();
        gunsManager.keyPressed(k,(int)(mouseX-mx-px),(int)(mouseY-my-py));

        //if(!interract){
        //    tileMap.keyPressed(k,player[0]);
        if(k == ControlSettings.getValue(ControlSettings.OBJECT_INTERACT)) {
            Client client = MultiplayerManager.getInstance().client.getClient();
            Network.DropInteract pickup = new Network.DropInteract();
            MultiplayerManager.getInstance().client.requestACK(pickup,pickup.idPacket);
            pickup.idPlayer = mpManager.getIdConnection();
            pickup.x = (int)(mouseX-mx-px);
            pickup.y = (int)(mouseY-my-py);
            client.sendUDP(pickup);

            Network.ObjectInteract interact = new Network.ObjectInteract();
            MultiplayerManager.getInstance().client.requestACK(pickup,interact.idPacket);
            interact.idPlayer = mpManager.getIdConnection();
            client.sendUDP(interact);
        }

        if(k == ControlSettings.getValue(ControlSettings.ARTEFACT_USE)){
            MultiplayerManager mpManager = MultiplayerManager.getInstance();
            Network.ArtefactActivate packetAActivate = new Network.ArtefactActivate();
            mpManager.client.requestACK(packetAActivate,packetAActivate.idPacket);
            packetAActivate.idPlayer = mpManager.getIdConnection();
            mpManager.client.getClient().sendUDP(packetAActivate);

        }

    }

    @Override
    protected void init() {
        DiscordRP.getInstance().update("Multiplayer - In-Game","Floor I");

        textRender = new TextRender[18];
        for(int i = 0;i<textRender.length;i++) textRender[i] = new TextRender();

        mpManager = MultiplayerManager.getInstance();
        mpStatistics = new MPStatistics();
        mapLoaded = false;
        postDeath = false;

        AudioManager.cleanUpAllSources();

        gameStart = System.currentTimeMillis();
        pause = false;


        endRewardEarned = false;

        objectsFramebuffer = new Framebuffer();
        pauseBlurFramebuffer = new Framebuffer();
        fadeFramebuffer = new Framebuffer();
        transitionContinue = false;
        fade = new Fade("shaders\\fade");
        gaussianBlur = new GaussianBlur("shaders\\blur");


        setCursor(Game.CROSSHAIR);

        miniMap = new MiniMap(false);

        // Tile map
        tileMap = new TileMap(64,miniMap);
        tileMap.loadTiles("Textures\\tileset64.tga");

        lightManager = new LightManager(tileMap);

        // player
        // create player object
        player = new PlayerMP[2];
        playerReadies = new PlayerReady[player.length];

        String username = mpManager.getUsername();
        player[0] = new PlayerMP(tileMap,username);
        player[0].setOrigin(true);
        player[0].setIdConnection(mpManager.getIdConnection());
        playerReadies[0] = new PlayerReady(username,mpManager.getIdConnection());
        mpStatistics.addPlayer(username,mpManager.getIdConnection());

        tileMap.setPlayer(player[0]);

        // weapons
        // load gun manager with tilemap object
        gunsManager = new GunsManager(tileMap,player);
        GunsManager.init(gunsManager);

        artefactManager = new ArtefactManager(tileMap,player);
        ArtefactManager.init(artefactManager);

        // items drops
        // load item manager with instances of objects
        itemManager = new ItemManager(tileMap,gunsManager,artefactManager,player[0]);
        ItemManager.init(itemManager);

        healthBar = new HealthBar[2];
        armorBar = new ArmorBar[2];
        barName = new TextRender[1];
        //health bar
        healthBar[0] = new HealthBar("Textures\\healthBar",new Vector3f(250,125,0),5,56,4);
        healthBar[0].enableHoverValuesShow();
        healthBar[0].setOffsetsBar(18,1);
        healthBar[0].enableHoverValuesShow();
        healthBar[0].initHealth(player[0].getHealth(),player[0].getMaxHealth());

        //health bar
        //armor bar
        armorBar[0] = new ArmorBar("Textures\\armorbar",new Vector3f(275,175,0),3,54,4);
        armorBar[0].enableHoverValuesShow();
        armorBar[0].setOffsetsBar(13f,2.5f);
        armorBar[0].initArmor(player[0].getArmor(),player[0].getMaxArmor());
        damageIndicator = new CombatIndicator();
        // coin
        coin = new Image("Textures\\coin.tga",new Vector3f(75,1000,0),1.5f);

        alertManager = new AlertManager();
        //audio
        AudioManager.playSoundtrack(Soundtrack.IDLE);

        enemyManager = new EnemyManager(player,tileMap);
        EnemyManager.init(enemyManager);



        // pause menu
        pauseBackground = new Background("Textures\\Menu\\pausemenu.tga");
        pauseBackground.setDimensions(500,800);
        pauseBars = new MenuBar[3];

        String defaultBar = "Textures\\Menu\\menu_bar.tga";
        MenuBar bar;
        bar = new MenuBar(defaultBar,new Vector3f(960,455,0),1.70f,200,100,true);
        bar.setType(PAUSERESUME);
        pauseBars[0] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(960,645,0),1.70f,200,100,true);
        bar.setType(PAUSESETTINGS);
        pauseBars[1] = bar;

        bar = new MenuBar(defaultBar,new Vector3f(960,835,0),1.70f,200,100,true);
        bar.setType(PAUSEEXIT);
        pauseBars[2] = bar;

        source = new Source(Source.EFFECTS,0.35f);
        soundMenuClick = AudioManager.loadSound("menuclick.ogg");

        skullPlayerdead = new Image("Textures\\skull.tga",new Vector3f(960,540,0),15f);
        logos = new Image[4];
        logos[0] = new Image("Textures\\killslogo.tga", new Vector3f(500,576,0),1.5f);
        logos[1] = new Image("Textures\\coinlogo.tga", new Vector3f(500,476,0),1.5f);
        logos[2] = new Image("Textures\\accuracylogo.tga", new Vector3f(500,676,0),1.5f);
        logos[3] = new Image("Textures\\timelogo.tga", new Vector3f(500,776,0),1.5f);

        skullPlayerdead.setAlpha(0f);

        console = new Console(gunsManager,player[0],itemManager,enemyManager);

        long delay = System.currentTimeMillis();
        int currentUnloadedRooms = 0;
        boolean mroomPacket = false;
        while(!mapLoaded || totalRoomsSynch != currentUnloadedRooms || !mroomPacket){
            if(System.currentTimeMillis() > delay) {
                delay+=1000;
            }
            Object[] packets = mpManager.packetHolder.get(PacketHolder.MAPLOADED);
            Object[] roomPackets = mpManager.packetHolder.getWithoutClear(PacketHolder.TRANSFERROOM);
            mroomPacket = mpManager.packetHolder.getWithoutClear(PacketHolder.TRANSFERROOMMAP).length == 1;

            if(packets.length >= 1){
                mapLoaded = true;
                totalRoomsSynch = ((Network.MapLoaded)packets[0]).totalRooms;
            }

            currentUnloadedRooms = roomPackets.length;

        }
        tileMap.loadMapViaPackets();
        tileMap.fillMiniMap();
        // move player to starter room
        player[0].setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());

        // make camera move smoothly
        tileMap.setTween(5);

        totalRoomsSynch = 999;
        ready = false;

        Object[] joinPackets = mpManager.packetHolder.get(PacketHolder.JOINPLAYER);
        int index = mpManager.client.getTotalPlayers();
        for(Object object : joinPackets) {
            Network.AddPlayer player = (Network.AddPlayer) object;
            String packetUsername = player.username;
            PlayerMP playerMP = new PlayerMP(tileMap, packetUsername);
            playerMP.setPosition(tileMap.getPlayerStartX(),tileMap.getPlayerStartY());
            playerMP.setIdConnection(player.idPlayer);
            this.player[index] = playerMP;
            healthBar[index] = new HealthBar("Textures\\mphealthBar",new Vector3f(200,300,0),4,35,3);
            healthBar[index].setOffsetsBar(3,0);
            healthBar[index].initHealth(this.player[index].getHealth(),this.player[index].getMaxHealth());

            armorBar[index] = new ArmorBar("Textures\\mparmorBar",new Vector3f(200,325,0),4,25,3);
            armorBar[index].initArmor(this.player[index].getArmor(),this.player[index].getMaxArmor());
            armorBar[index].setOffsetsBar(3,0);
            barName[index-1] = new TextRender();

            miniMap.addPlayerArrow(this.player[index]);

            deathIcon = new Image("Textures\\skull.tga",new Vector3f(100,315,0),2f);

            playerReadies[index] = new PlayerReady(packetUsername,player.idPlayer);
            index++;
        }
        mpManager.client.setNumPlayers(index);

        Object[] disconnectPackets = mpManager.packetHolder.get(PacketHolder.DISCONNECTPLAYER);
        for(Object object : disconnectPackets){
            index--;
            Network.Disconnect packet = (Network.Disconnect) object;
            int idOrigin = mpManager.getIdConnection();
            player[index].remove();
            miniMap.removePlayerArrow(player[index].getIdConnection());
            player[index] = null;
            playerReadies[index] = null;
            healthBar[index] = null;
            armorBar[index] = null;
            if(idOrigin != packet.idPlayer){
                AlertManager.add(AlertManager.WARNING,player[1].getUsername()+" has left the game!");
            }
        }
        mpManager.client.setNumPlayers(index);
        pingTimer = System.currentTimeMillis();
    }

    @Override
    protected void draw() {
        objectsFramebuffer.bindFBO();
        // clear framebuffer
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        tileMap.draw(Tile.NORMAL);

        for(Player player : player){
            if(player != null)player.drawShadow();
        }
        enemyManager.drawShadow();
        tileMap.preDrawObjects(true);

        tileMap.draw(Tile.BLOCKED);

        tileMap.preDrawObjects(false);

        itemManager.draw();

        artefactManager.preDraw();

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
                player[index].draw();
                used[index] = true;
            }
        }
        artefactManager.draw();


        tileMap.drawObjects();

        enemyManager.draw();
        
        gunsManager.draw();

        objectsFramebuffer.unbindFBO();
        if(postDeath){
            fadeFramebuffer.bindFBO();
            glClear(GL_COLOR_BUFFER_BIT);
        }

        if(pause){
            pauseBlurFramebuffer.bindFBO();
            glClear(GL_COLOR_BUFFER_BIT);
        }

        lightManager.draw(objectsFramebuffer);

        if (Game.displayCollisions){
            textRender[0].draw("X: "+(int)player[0].getX(),new Vector3f(200,550,0),3,new Vector3f(1.0f,1.0f,1.0f));
            textRender[1].draw("Y: "+(int)player[0].getY(),new Vector3f(200,600,0),3,new Vector3f(1.0f,1.0f,1.0f));
        }

        if(!postDeath)gunsManager.drawHud();
        artefactManager.drawHud();
        enemyManager.drawHud();

        player[0].drawVignette();

        tileMap.drawTitle();

        int otherBars = 0;
        for(int i =0;i<healthBar.length;i++){
            if(healthBar[i] != null){
                healthBar[i].draw();
                if(!player[i].isOrigin()){
                    Vector3f color = new Vector3f();
                    if(player[i].isGhost()){
                        color.set(0.760f);
                        deathIcon.draw();
                    } else {
                        color.set(0.874f,0.443f,0.149f);
                    }
                    barName[otherBars].draw(player[i].getUsername(),new Vector3f(125,280,0),1,color);
                    otherBars++;
                }
            }
        }
        for(ArmorBar armorBar : armorBar){
            if(armorBar != null) armorBar.draw();
        }
        miniMap.draw();
        damageIndicator.draw();
        console.draw();

        coin.draw();
        alertManager.draw();
        textRender[2].draw(""+player[0].getCoins(),new Vector3f(145,1019,0),3,new Vector3f(1.0f,0.847f,0.0f));
        if(Game.displayCollisions) textRender[17].draw("Ping: "+ping+" ms",new Vector3f(200, 450,0),2,new Vector3f(1.0f,1.0f,1.0f));

        // remake if all players are dead
        if(postDeath){
            fadeFramebuffer.unbindFBO();
            fade.draw(fadeFramebuffer);
            MPStatistics.PStats pStats = mpStatistics.getPlayerStats(player[0].getIdConnection());
            int totalReward = 0;
            int rewardAccuracy = 0;
            int rewardKilledEnemies = (int)Math.sqrt(pStats.getEnemiesKilled())*(int)((tileMap.getFloor()+1)/1.25);
            int rewardFloor = (int)Math.pow(tileMap.getFloor(),2);
            if(pStats.getBulletShooted() != 0){
                int accuracy = (int)(pStats.getAccuracy()*100);
                if(accuracy > 90){
                    rewardAccuracy= (int)(rewardKilledEnemies*0.8);
                } else if (accuracy > 75){
                    rewardAccuracy= rewardKilledEnemies/2;
                } else if (accuracy > 45){
                    rewardAccuracy= rewardKilledEnemies/4;
                }
            }
            totalReward+=rewardAccuracy+rewardKilledEnemies+rewardFloor;
            if(!endRewardEarned){
                endRewardEarned = true;
                Database db = GameStateManager.getDb();
                int storedMoney = db.getValue("money","general");
                db.setValue("money",totalReward+storedMoney);
            }
            if(transitionContinue) return;
            skullPlayerdead.draw();
            float time = System.currentTimeMillis()-deathTime;
            if(time > 1000){
                char[] gameOverTitle = "GAME OVER".toCharArray();
                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 0;time > 1000+i*95 && i < gameOverTitle.length;i++){
                    stringBuilder.append(gameOverTitle[i]);
                }
                textRender[3].draw(stringBuilder.toString(),new Vector3f( TextRender.getHorizontalCenter(0,1920,stringBuilder.toString(),5),340,0),5,new Vector3f(1f,0.25f,0f));
                glColor4f(1f,1f,1f,1f);
                glLineWidth(3f);
                glBegin(GL_LINES);
                float first = 960-(time-1000)/2.5f;
                float secondary = 960+(time-1000)/2.5f;
                if(first<480) first=480;
                if(secondary>1440) secondary=1440;
                glVertex2f(secondary, 380);
                glVertex2f(first, 380);
                glEnd();
                glLineWidth(1f);

            }
            if(time > 2350){
                for(Image img : logos){
                    img.draw();
                }
                textRender[4].draw("Floor: "+(tileMap.getFloor()+1),new Vector3f(600,500,0),3,new Vector3f(1f,1f,1f));
                textRender[5].draw("Enemies killed: "+pStats.getEnemiesKilled(),new Vector3f(600,600,0),3,new Vector3f(1f,1f,1f));
                if(pStats.getBulletShooted() == 0){
                    textRender[6].draw("Accuracy: 0%",new Vector3f(600,700,0),3,new Vector3f(1f,1f,1f));

                } else {
                    textRender[7].draw("Accuracy: "+(int)(pStats.getAccuracy()*100)+"%",new Vector3f(600,700,0),3,new Vector3f(1f,1f,1f));
                }
                long deathTime = player[0].getDeathTime();
                long sec = (deathTime-gameStart)/1000%60;
                long min = ((deathTime-gameStart)/1000/60)%60;
                long hours = (deathTime-gameStart)/1000/3600;

                String livetime = "Live time: ";
                if(hours>=1){
                    livetime=livetime+hours+" h  ";
                }
                if(min>=1){
                    livetime=livetime+min+" min  ";
                }
                livetime=livetime+sec+" sec";
                textRender[8].draw(livetime,new Vector3f(600,800,0),3,new Vector3f(1f,1f,1f));

                textRender[9].draw("+ "+rewardFloor,new Vector3f(1300,500,0),3,new Vector3f(0.831f, 0.658f, 0.031f));
                textRender[10].draw("+ "+rewardKilledEnemies,new Vector3f(1300,600,0),3,new Vector3f(0.831f, 0.658f, 0.031f));
                textRender[11].draw("+ "+rewardAccuracy,new Vector3f(1300,700,0),3,new Vector3f(0.831f, 0.658f, 0.031f));

            }
            int totalPlayers = 0;
            for(PlayerReady ready : playerReadies){
                if(ready != null) totalPlayers++;
            }
            if(time > 3000){
                if(System.currentTimeMillis() / 500 % 2 == 0) {
                    if(!ready){
                        String string = "Press "+ControlSettings.keyToChar(ControlSettings.getValue(ControlSettings.OBJECT_INTERACT))+" to continue...";
                        float xloc = TextRender.getHorizontalCenter(1400,1900,string,2);
                        textRender[12].draw(string,
                                new Vector3f(xloc,1000,0),2,new Vector3f(1f,1f,1f));
                    }
                    else{
                        String string = "Waiting for "+readyNumPlayers+"/"+totalPlayers+" players";
                        float xloc = TextRender.getHorizontalCenter(1400,1900,string,2);
                        textRender[12].draw(string,
                                new Vector3f(xloc,1000,0),2,new Vector3f(1f,1f,1f));
                    }
                }
            }

        }
        if(pause){
            pauseBlurFramebuffer.unbindFBO();

            gaussianBlur.draw(pauseBlurFramebuffer);

            pauseBackground.draw();
            for(MenuBar bar: pauseBars){
                bar.draw();
            }

            textRender[13].draw("Pause",new Vector3f(TextRender.getHorizontalCenter(795,1125,"Pause",7),300,0),7,new Vector3f(0.874f,0.443f,0.149f));
            textRender[14].draw("Resume",new Vector3f(TextRender.getHorizontalCenter(795,1125,"Resume",4),465,0),4,new Vector3f(0.874f,0.443f,0.149f));
            textRender[15].draw("Stats",new Vector3f(TextRender.getHorizontalCenter(795,1125,"Stats",4),655,0),4,new Vector3f(0.874f,0.443f,0.149f));
            textRender[16].draw("Exit",new Vector3f(TextRender.getHorizontalCenter(795,1125,"Exit",4),845,0),4,new Vector3f(0.874f,0.443f,0.149f));


        }

    }

    @Override
    protected void update() {
        if(mpManager.isNotConnected()){
            if(System.currentTimeMillis() - connectionAlert > 5000){
                connectionAlert = System.currentTimeMillis();
                AlertManager.add(AlertManager.WARNING,"No connection!");
                MultiplayerManager.getInstance().client.tryReconnect();
            }
            for(Player p : player){
                if(p != null){
                    p.update();
                }
            }

            mouseX = gsm.getMouseX();
            mouseY = gsm.getMouseY();
            // set tilemap/camera with trying succeeding to center player in camera, this position is also affected my mouse
            tileMap.setPosition(
                    Camera.getWIDTH() / 2f - player[0].getX() -(mouseX-960)/30,
                    Camera.getHEIGHT() / 2f - player[0].getY() -(mouseY- 540)/30,
                    false
            );
            itemManager.update();

            if (pause) {
                for (MenuBar hud : pauseBars) {
                    hud.setClick(false);
                    if (hud.intersects(mouseX, mouseY)) {
                        hud.setClick(true);
                    }
                }
            }
            for (PlayerMP player : player) {
                if (player != null) player.update();
            }
            Object[] playerHitPackets = mpManager.packetHolder.get(PacketHolder.PLAYERHIT);
            for(Object o : playerHitPackets){
                for(PlayerMP p : player) {
                    if (p != null) {
                        Network.PlayerHit playerHit = (Network.PlayerHit) o;
                        if(p.getIdConnection() == playerHit.idPlayer){
                            p.fakeHit(playerHit);
                        }
                    }
                }
            }
            tileMap.updateObjects();

            // updating if player entered some another room
            tileMap.updateCurrentRoom(
                    (int) player[0].getX(),
                    (int) player[0].getY()
            );

            // update player icon location by new room
            miniMap.update(tileMap);
            // update rooms like shop/loot if they were discovered, bcs locks are only for classic/boss
            miniMap.update(player,tileMap);
            miniMap.hover(mouseX,mouseY);

            Object[] hitBulletPackets = mpManager.packetHolder.get(PacketHolder.HITBULLET);
            enemyManager.update();
            artefactManager.update(hitBulletPackets);
            gunsManager.update(hitBulletPackets);
            handleExplosionDamage();
            handleTrapRODamage();

            damageIndicator.update();
            console.update();

            Object[] packets = mpManager.packetHolder.get(PacketHolder.PLAYERINFO);
            for(Object o : packets){
                Network.PlayerInfo info = (Network.PlayerInfo) o;
                for(PlayerMP p : player){
                    if(p == null) continue;
                    if(p.getIdConnection() == info.idPlayer){
                        p.setCoins(info.coins);
                        p.setHealth(info.health);
                        p.setMaxArmor(info.maxArmor);
                        p.setMaxHealth(info.maxHealth);
                        p.setArmor(info.armor);
                    }
                }
            }
            for(int i = 0;i<player.length;i++){
                if(player[i] != null){
                    healthBar[i].showDisplayValues(false);
                    armorBar[i].showDisplayValues(false);

                    healthBar[i].update(player[i].getHealth(), player[i].getMaxHealth());
                    if(healthBar[i].intersects(mouseX,mouseY)) healthBar[i].showDisplayValues(true);
                    armorBar[i].update(player[i].getArmor(),player[i].getMaxArmor());
                    if(armorBar[i].intersects(mouseX,mouseY)) armorBar[i].showDisplayValues(true);
                }
            }
            if(miniMap.isDisplayBigMap()){
                armorBar[0].showDisplayValues(true);
                healthBar[0].showDisplayValues(true);
            }
            alertManager.update();

            gaussianBlur.update(pause);
            lightManager.update();
            return;
        }
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
        if (postDeath){
            fade.update(transitionContinue);
            readyNumPlayers = 0;
            int totalConPlayers = 0;
            for(PlayerReady playerReady : playerReadies){
                if(playerReady != null){
                    if(playerReady.isReady()) readyNumPlayers++;
                    totalConPlayers++;
                }
            }
            // all players are ready => enter game
            if(totalConPlayers == readyNumPlayers){
                transitionContinue = true;
            }
            if(fade.isTransitionDone()){
                mpManager.client.setNumPlayers(1);

                gsm.setState(GameStateManager.PROGRESSROOMMP);
                glfwSetInputMode(Game.window,GLFW_CURSOR,GLFW_CURSOR_NORMAL);

                mpManager.packetHolder.clear(PacketHolder.MOVEPLAYER);
                mpManager.packetHolder.clear(PacketHolder.ORIGINMOVEPLAYER);

                Client client = mpManager.client.getClient();
                Network.RequestForPlayers request = new Network.RequestForPlayers();
                request.exceptIdPlayer = mpManager.getIdConnection();
                client.sendTCP(request);
                return;
            }
            float time = (System.currentTimeMillis()-deathTime);
            if(time > 500){ //TODO: test
                Vector3f pos = skullPlayerdead.getPos();
                float shift = (time-500)/500;
                if(shift > 1) shift = 1;
                pos.y += ((540 - 400 * shift) - pos.y);
                Vector3f newpos = new Vector3f(pos.x(),(int)pos.y(),0);
                skullPlayerdead.setPosition(newpos);
            }
            skullPlayerdead.setAlpha(time/3500f);
            player[0].updateOrigin();
            for(PlayerMP p : player){
                if(p != null)p.update();
            }
        } else {
            // entering new floor
            Object[] nextFloor = mpManager.packetHolder.get(PacketHolder.NEXTFLOOR);
            if(nextFloor.length >= 1){
                tileMap.handleNextFloorPacket((Network.NextFloor)nextFloor[0]);

                mapLoaded = false;
                long delay = System.currentTimeMillis();
                while(!mapLoaded){
                    if(System.currentTimeMillis() > delay) {
                        delay+=1000;
                        System.out.println("STILL NOT LOADED MAP");
                    }
                    Object[] packets = mpManager.packetHolder.get(PacketHolder.MAPLOADED);
                    if(packets.length >= 1) mapLoaded = true;
                }
                tileMap.setTween(1);
                tileMap.loadMapViaPackets();
                tileMap.fillMiniMap();
                // move player to starter room
                mpManager.packetHolder.clear(PacketHolder.MOVEPLAYER);
                player[0].setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());
                tileMap.setTween(5);
            }
            Object[] allPlayersDead = mpManager.packetHolder.get(PacketHolder.ALLPLAYERDEAD);
            if(allPlayersDead.length >= 1 && !postDeath){
                postDeath = true;
                pause = false;
                deathTime = System.currentTimeMillis();
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
            player[0].updateOrigin();
            for(PlayerMP p : player){
                if(p != null)p.update();
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
            Object[] playerHitPackets = mpManager.packetHolder.get(PacketHolder.PLAYERHIT);
            for(Object o : playerHitPackets){
                for(PlayerMP p : player) {
                    if (p != null) {
                        Network.PlayerHit playerHit = (Network.PlayerHit) o;
                        if(p.getIdConnection() == playerHit.idPlayer){
                            p.fakeHit(playerHit);
                        }
                    }
                }
            }
            tileMap.createRoomObjectsViaPackets();
        }
        AudioManager.update();
        tileMap.checkingRoomLocks();

        itemManager.update();

        if (pause) {
            for (MenuBar hud : pauseBars) {
                hud.setClick(false);
                if (hud.intersects(mouseX, mouseY)) {
                    hud.setClick(true);
                }
            }
        }
        tileMap.updateObjects();

        // updating if player entered some another room
        tileMap.updateCurrentRoom(
                (int) player[0].getX(),
                (int) player[0].getY()
        );

        // update player icon location by new room
        miniMap.update(tileMap);
        // update rooms like shop/loot if they were discovered, bcs locks are only for classic/boss
        miniMap.update(player,tileMap);
        miniMap.hover(mouseX,mouseY);

        // updating bullets(ammo)
        float px = player[0].getX();
        float py = player[0].getY();
        float mx = tileMap.getX();
        float my = tileMap.getY();
        if (!pause) {
            gunsManager.shoot(mouseX - mx - px, mouseY - my - py, px, py,player[0].getIdConnection());
        }
        // updating if player shoots any enemies
        enemyManager.update();

        Object[] hitBulletPackets = mpManager.packetHolder.get(PacketHolder.HITBULLET);
        artefactManager.update(hitBulletPackets);
        gunsManager.update(hitBulletPackets);

        Object[] moveBulletPackets = mpManager.packetHolder.get(PacketHolder.MOVEBULLET);
        gunsManager.handleBulletMovePacket(moveBulletPackets);
        artefactManager.handleBulletMovePacket(moveBulletPackets);

        handleExplosionDamage();
        handleTrapRODamage();

        damageIndicator.update();
        console.update();

        Object[] packets = mpManager.packetHolder.get(PacketHolder.PLAYERINFO);
        for(Object o : packets){
            Network.PlayerInfo info = (Network.PlayerInfo) o;
            for(PlayerMP p : player){
                if(p == null) continue;
                if(p.getIdConnection() == info.idPlayer){
                    p.setCoins(info.coins);
                    p.setHealth(info.health);
                    p.setMaxArmor(info.maxArmor);
                    p.setMaxHealth(info.maxHealth);
                    p.setArmor(info.armor);
                }
            }
        }

        for(int i = 0;i<player.length;i++){
            if(player[i] != null){
                healthBar[i].showDisplayValues(false);
                armorBar[i].showDisplayValues(false);

                healthBar[i].update(player[i].getHealth(), player[i].getMaxHealth());
                if(healthBar[i].intersects(mouseX,mouseY)) healthBar[i].showDisplayValues(true);
                armorBar[i].update(player[i].getArmor(),player[i].getMaxArmor());
                if(armorBar[i].intersects(mouseX,mouseY)) armorBar[i].showDisplayValues(true);
            }
        }
        if(miniMap.isDisplayBigMap()){
            armorBar[0].showDisplayValues(true);
            healthBar[0].showDisplayValues(true);
        }

        Object[] AlertPackets = mpManager.packetHolder.get(PacketHolder.ALERT);
        for(Object o : AlertPackets){
            Network.Alert alert = (Network.Alert) o;
            if(mpManager.getIdConnection() == alert.idPlayer){
                AlertManager.add(alert.warning ? AlertManager.WARNING : AlertManager.INFORMATION,alert.text);
            }
        }
        alertManager.update();

        gaussianBlur.update(pause);
        lightManager.update();

        mpStatistics.receivePackets();

        int index = mpManager.client.getTotalPlayers();
        Object[] disconnectPackets = mpManager.packetHolder.get(PacketHolder.DISCONNECTPLAYER);
        for(Object object : disconnectPackets){
            index--;
            Network.Disconnect packet = (Network.Disconnect) object;
            int idOrigin = mpManager.getIdConnection();
            player[index].remove();
            if(idOrigin != packet.idPlayer){
                AlertManager.add(AlertManager.WARNING,player[1].getUsername()+" has left the game!");
            }
            miniMap.removePlayerArrow(this.player[index].getIdConnection());
            player[index] = null;
            playerReadies[index] = null;
            healthBar[index] = null;
            armorBar[index] = null;
        }
        Object[] joinPackets = mpManager.packetHolder.get(PacketHolder.JOINPLAYER);
        for(Object object : joinPackets) {
            Network.AddPlayer player = (Network.AddPlayer) object;
            String packetUsername = player.username;
            PlayerMP playerMP = new PlayerMP(tileMap, packetUsername);
            playerMP.setPosition(tileMap.getPlayerStartX(),tileMap.getPlayerStartY());
            playerMP.setIdConnection(player.idPlayer);
            this.player[index] = playerMP;
            healthBar[index] = new HealthBar("Textures\\mphealthBar",new Vector3f(200,300,0),4,35,3);
            healthBar[index].setOffsetsBar(3,0);
            healthBar[index].initHealth(this.player[index].getHealth(),this.player[index].getMaxHealth());

            armorBar[index] = new ArmorBar("Textures\\mparmorBar",new Vector3f(200,325,0),4,25,3);
            armorBar[index].initArmor(this.player[index].getArmor(),this.player[index].getMaxArmor());
            armorBar[index].setOffsetsBar(3,-0.5f);
            barName[index-1] = new TextRender();

            miniMap.addPlayerArrow(this.player[index]);

            deathIcon = new Image("Textures\\skull.tga",new Vector3f(100,315,0),2f);

            playerReadies[index] = new PlayerReady(packetUsername,player.idPlayer);
            index++;
        }
        mpManager.client.setNumPlayers(index);

        mpManager.client.setNumPlayers(index);
        if(System.currentTimeMillis() - pingTimer > 1000){
            pingTimer+=1000;
            Client client = MultiplayerManager.getInstance().client.getClient();
            client.sendUDP(new Network.Ping());
            ping = client.getReturnTripTime()/2;
        }
    }
    public void pause(){
        if(!pause) {
            setCursor(ARROW);
            player[0].setLeft(false);
            player[0].setRight(false);
            player[0].setUp(false);
            player[0].setDown(false);
            pause = true;
        }
    }
    public void resume(){
        setCursor(Game.CROSSHAIR);
        pause=false;
    }
    public void handleExplosionDamage(){
        ArrayList<RoomObject>[] roomObjects = tileMap.getRoomMapObjects();
        Object[] explosionPackets = mpManager.packetHolder.get(PacketHolder.EXPLOSIONDAMAGE);
        ArrayList<Enemy> enemies = enemyManager.getEnemies();
        for(Object o : explosionPackets){
            Network.ExplosionDamage packet = (Network.ExplosionDamage) o;
            A: for(int i = 0;i<packet.idHit.length;i++){
                if(packet.typeHit[i]){ // typehit is enemy
                    for(Enemy e : enemies){
                        if(e.id == packet.idHit[i]){
                            int cwidth = e.getCwidth();
                            int cheight = e.getCheight();
                            int x = -cwidth/4+ Random.nextInt(cwidth/2);
                            if(packet.critical){
                                CombatIndicator.addCriticalDamageShow(packet.damage,(int)e.getX()-x,(int)e.getY()-cheight/3
                                        ,new Vector2f(-x/10f,-30f));
                            } else {
                                CombatIndicator.addDamageShow(packet.damage,(int)e.getX()-x,(int)e.getY()-cheight/3
                                        ,new Vector2f(-x/10f,-30f));
                            }
                            e.hit(packet.damage);
                            continue A;
                        }
                    }
                } else { // typehit is room object
                    for(ArrayList<RoomObject> objects : roomObjects) {
                        if (objects == null) continue;
                        for (RoomObject roomObject : objects) {
                            if (roomObject instanceof DestroyableObject) {
                                if(roomObject.id == packet.idHit[i]){
                                    ((DestroyableObject) roomObject).setHit(packet.damage);
                                    continue A;

                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void handleTrapRODamage(){
        Object[] damagePackets = mpManager.packetHolder.get(PacketHolder.TRAPRODAMAGE);
        ArrayList<RoomObject>[] roomObjects = tileMap.getRoomMapObjects();
        A: for(Object o : damagePackets){
            Network.TrapRoomObjectDamage packet = (Network.TrapRoomObjectDamage) o;
            for(ArrayList<RoomObject> objects : roomObjects) {
                if (objects == null) continue;
                for (RoomObject roomObject : objects) {
                    if (roomObject instanceof DestroyableObject) {
                        if(roomObject.id == packet.idHit){
                            ((DestroyableObject) roomObject).setHit(1);
                            continue A;
                        }
                    }
                }
            }
        }
    }

}

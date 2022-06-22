package cz.Empatix.Gamestates.Singleplayer;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Buffs.BuffManager;
import cz.Empatix.Database.Database;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.Artefacts.ArtefactManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameState;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Main.DiscordRP;
import cz.Empatix.Main.Game;
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
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static cz.Empatix.Main.Game.ARROW;
import static cz.Empatix.Main.Game.setCursor;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;


public class InGame extends GameState {

    private boolean pause;
    private boolean endRewardEarned;
    private long gameStart;
    private long gameTimeSave;

    //
    // Main game
    //

    // death menu
    private Image skullPlayerdead;
    private Image[] logos;

    private Player player;
    private BuffManager buffManager;

    private TileMap tileMap;

    private GunsManager gunsManager;

    private float mouseX;
    private float mouseY;

    // ingame huds
    private HealthBar healthBar;
    private ArmorBar armorBar;
    private MiniMap miniMap;
    private CombatIndicator damageIndicator;
    private cz.Empatix.Render.Hud.Image coin;
    private Console console;
    private AlertManager alertManager;

    private EnemyManager enemyManager;

    // postprocessing
    private Framebuffer objectsFramebuffer;
    private Framebuffer pauseBlurFramebuffer;
    private Framebuffer fadeFramebuffer;
    private Fade fade;
    private boolean transitionContinue;
    private GaussianBlur gaussianBlur;
    private LightManager lightManager;


    private ItemManager itemManager;
    private ArtefactManager artefactManager;
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

    // pause time deltas
    private static long pauseTimeEnded;
    private static long pauseTimeStarted;

    private TextRender[] textRender;


    public InGame(GameStateManager gsm){
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
                        player.cleanUp();
                        gsm.setState(GameStateManager.MENU);
                    } else if (type == PAUSERESUME){
                        resume();
                        gunsManager.stopShooting();
                        setCursor(Game.CROSSHAIR);
                    } else{
                        // TODO: stats menu
                        //DataManager.saveGame(this);
                        //gameTimeSave = System.currentTimeMillis();
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
            gunsManager.startShooting();
        }
    }
    @Override
    public void mouseScroll(double x, double y){
        gunsManager.changeGunScroll();
    }
    @Override
    protected void keyReleased(int k) {
        if(player.isDead()){
            float time = (System.currentTimeMillis()-player.getDeathTime());
            if(time > 5500){
                transitionContinue = true;

            }
            return;
        }
        if(console.isEnabled()){
            console.keyReleased(k);
        }
        if (k == GLFW_KEY_ESCAPE){
            pause = !pause;
            if(pause){
                setCursor(ARROW);
                pauseTimeStarted = System.currentTimeMillis();
            } else {
                resume();
            }
            gunsManager.stopShooting();
        }
        player.keyReleased(k);
        miniMap.keyReleased(k);

        if(pause) return;

        if (k == ControlSettings.getValue(ControlSettings.RELOAD)){
            gunsManager.reload();
        }
    }

    @Override
    protected void keyPressed(int k) {
        if(player.isDead()) return;
        if(pause) return;


        if (k == GLFW.GLFW_KEY_F3){
            console.setEnabled(!console.isEnabled());
        }

        if(console.isEnabled()){
            console.keyPressed(k);
            return;
        }
        float px = player.getX();
        float py = player.getY();
        float mx = tileMap.getX();
        float my = tileMap.getY();
        gunsManager.keyPressed(k,(int)(mouseX-mx-px),(int)(mouseY-my-py));

        player.keyPressed(k);
        miniMap.keyPressed(k);

        // if player interracts with something like artefact/gun
        boolean interract = itemManager.keyPressed(k,(int)(mouseX-mx-px),(int)(mouseY-my-py));
        if(!interract){
            tileMap.keyPressed(k,player);
        }

        if(k == ControlSettings.getValue(ControlSettings.ARTEFACT_USE)){
            artefactManager.activate();
        }

    }

    @Override
    protected void init() {
        DiscordRP.getInstance().update("Singleplayer - In-Game","Floor I");

        textRender = new TextRender[17];
        for(int i = 0;i<17;i++) textRender[i] = new TextRender();

        AudioManager.cleanUpAllSources();

        gameStart = System.currentTimeMillis();
        pauseTimeEnded=0;
        pauseTimeStarted=0;
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
        player = new Player(tileMap);
        tileMap.setPlayer(player);
        buffManager = new BuffManager();

        // weapons
        // load gun manager with tilemap object
        gunsManager = new GunsManager(tileMap,player);
        GunsManager.init(gunsManager);

        artefactManager = new ArtefactManager(tileMap,player);
        ArtefactManager.init(artefactManager);

        // items drops
        // load item manager with instances of objects
        itemManager = new ItemManager(tileMap,gunsManager,artefactManager,player);
        ItemManager.init(itemManager);

        // generate map + create objects which needs item manager & gun manager created
        tileMap.loadMap();
        // move player to starter room
        player.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());

        // make camera move smoothly
        tileMap.setTween(5);

        //health bar
        healthBar = new HealthBar("Textures\\healthBar",new Vector3f(250,125,0),5,56,4);
        healthBar.enableHoverValuesShow();
        healthBar.setOffsetsBar(18,1);
        healthBar.initHealth(player.getHealth(),player.getMaxHealth());
        //armor bar
        armorBar = new ArmorBar("Textures\\armorbar",new Vector3f(275,175,0),3,55,4);
        armorBar.enableHoverValuesShow();
        armorBar.setOffsetsBar(13f,2.5f);
        armorBar.initArmor(player.getArmor(),player.getMaxArmor());
        //minimap
        tileMap.fillMiniMap();
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

        console = new Console(gunsManager,player,itemManager,enemyManager);
    }

    @Override
    protected void draw() {
        objectsFramebuffer.bindFBO();
        // clear framebuffer
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        tileMap.draw(Tile.NORMAL);

        player.drawShadow();
        enemyManager.drawShadow();
        tileMap.preDrawObjects(true);

        tileMap.draw(Tile.BLOCKED);

        tileMap.preDrawObjects(false);

        itemManager.draw();

        artefactManager.preDraw();

        player.draw();

        artefactManager.draw();

        tileMap.drawObjects();

        enemyManager.draw();
        
        gunsManager.draw();

        objectsFramebuffer.unbindFBO();

        if(player.isDead()){
            fadeFramebuffer.bindFBO();
            glClear(GL_COLOR_BUFFER_BIT);
        }
        if(pause){
            pauseBlurFramebuffer.bindFBO();
            glClear(GL_COLOR_BUFFER_BIT);
        }


        lightManager.draw(objectsFramebuffer);

        if (Game.displayCollisions){
            textRender[0].draw("X: "+(int)player.getX(),new Vector3f(200,550,0),3,new Vector3f(1.0f,1.0f,1.0f));
            textRender[1].draw("Y: "+(int)player.getY(),new Vector3f(200,600,0),3,new Vector3f(1.0f,1.0f,1.0f));
        }

        gunsManager.drawHud();
        artefactManager.drawHud();
        enemyManager.drawHud();

        player.drawVignette();
        buffManager.draw();

        tileMap.drawTitle();

        healthBar.draw();
        armorBar.draw();
        miniMap.draw();
        damageIndicator.draw();
        console.draw();

        coin.draw();
        alertManager.draw();
        textRender[2].draw(""+player.getCoins(),new Vector3f(145,1019,0),3,new Vector3f(1.0f,0.847f,0.0f));


        if(player.isDead()){
            int totalReward = 0;
            int rewardAccuracy = 0;
            int rewardKilledEnemies = (int)Math.sqrt(EnemyManager.enemiesKilled)*(int)((tileMap.getFloor()+1)/1.25);
            int rewardFloor = (int)Math.pow(tileMap.getFloor(),1.5);
            if(GunsManager.bulletShooted != 0){
                int accuracy = (int)((float)GunsManager.hitBullets/GunsManager.bulletShooted*100);
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
            fadeFramebuffer.unbindFBO();
            fade.draw(fadeFramebuffer);
            if(transitionContinue) return;
            skullPlayerdead.draw();
            float time = (System.currentTimeMillis()-player.getDeathTime());
            if(time > 3500){
                char[] gameOverTitle = "GAME OVER".toCharArray();
                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 0;time > 3500+i*95 && i < gameOverTitle.length;i++){
                    stringBuilder.append(gameOverTitle[i]);
                }
                textRender[3].draw(stringBuilder.toString(),new Vector3f( TextRender.getHorizontalCenter(0,1920,stringBuilder.toString(),5),340,0),5,new Vector3f(1f,0.25f,0f));
                glColor4f(1f,1f,1f,1f);
                glLineWidth(10f);
                glBegin(GL_LINES);
                float first = 960-(time-3500)/2.5f;
                float secondary = 960+(time-3500)/2.5f;
                if(first<480) first=480;
                if(secondary>1440) secondary=1440;
                glVertex2f(secondary, 380);
                glVertex2f(first, 380);
                glEnd();
                glLineWidth(1f);
            }
            if(time > 5000){
                for(Image img : logos){
                    img.draw();
                }
                textRender[4].draw("Floor: "+(tileMap.getFloor()+1),new Vector3f(600,500,0),3,new Vector3f(1f,1f,1f));
                textRender[5].draw("Enemies killed: "+EnemyManager.enemiesKilled,new Vector3f(600,600,0),3,new Vector3f(1f,1f,1f));
                if(GunsManager.bulletShooted == 0){
                    textRender[6].draw("Accuracy: 0%",new Vector3f(600,700,0),3,new Vector3f(1f,1f,1f));

                } else {
                    textRender[7].draw("Accuracy: "+(int)((float)GunsManager.hitBullets/GunsManager.bulletShooted*100)+"%",new Vector3f(600,700,0),3,new Vector3f(1f,1f,1f));
                }
                long sec = (player.getDeathTime()-gameStart)/1000%60;
                long min = ((player.getDeathTime()-gameStart)/1000/60)%60;
                long hours = (player.getDeathTime()-gameStart)/1000/3600;

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
            if(time > 5500){
                if(System.currentTimeMillis() / 500 % 2 == 0) {
                    textRender[12].draw("Press anything to continue...",new Vector3f(1500,1000,0),2,new Vector3f(1f,1f,1f));

                }
            }
            miniMap.forceHideBigMap();

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
        AudioManager.update();
        if(player.isDead()){
            enemyManager.updateOnlyAnimations();
            fade.update(transitionContinue);
            if(fade.isTransitionDone()){
                gsm.setState(GameStateManager.PROGRESSROOM);
                glfwSetInputMode(Game.window,GLFW_CURSOR,GLFW_CURSOR_NORMAL);
                return;
            }
            player.update();
            alertManager.update();
            healthBar.update(player.getHealth(), player.getMaxHealth());
            float time = (System.currentTimeMillis()-player.getDeathTime());
            if(time > 2000){
                Vector3f pos = skullPlayerdead.getPos();
                float shift = (time-2000)/1500;
                if(shift > 1) shift = 1;
                pos.y += ((540 - 400 * shift) - pos.y);
                Vector3f newpos = new Vector3f(pos.x(),(int)pos.y(),0);
                skullPlayerdead.setPosition(newpos);
            }
            skullPlayerdead.setAlpha(time/4500f);
            return;
        }
        // loc of mouse
        mouseX = gsm.getMouseX();
        mouseY = gsm.getMouseY();

        artefactManager.update(pause);

        if(pause){
            for(MenuBar hud:pauseBars){
                hud.setClick(false);
                if(hud.intersects(mouseX,mouseY)){
                    hud.setClick(true);
                }
            }
        } else {
            itemManager.update();

            ArrayList<Enemy> enemies = EnemyManager.getInstance().getEnemies();

            buffManager.update();
            player.update();
            // set tilemap/camera with trying succeeding to center player in camera, this position is also affected my mouse
            tileMap.setPosition(
                    Camera.getWIDTH() / 2f - player.getX() -(mouseX-960)/30,
                    Camera.getHEIGHT() / 2f - player.getY() -(mouseY- 540)/30,
                    false
            );


            tileMap.updateObjects();

            // updating if player entered some another room
            tileMap.updateCurrentRoom(
                    (int) player.getX(),
                    (int) player.getY()
            );

            // update player icon location by new room
            miniMap.update(tileMap);
            miniMap.hover(mouseX,mouseY);

            // updating bullets(ammo)
            float px = player.getX();
            float py = player.getY();
            float mx = tileMap.getX();
            float my = tileMap.getY();
            gunsManager.shoot(mouseX-mx-px,
                    mouseY-my-py,
                    px,
                    py);
            gunsManager.update();
            // updating if player shoots any enemies
            enemyManager.update();
            damageIndicator.update();
            console.update();

            // check bullet collision with enemies
            gunsManager.checkCollisions(enemies);
            player.checkCollision(enemies);

            healthBar.update(player.getHealth(), player.getMaxHealth());
            healthBar.showDisplayValues(false);
            if(healthBar.intersects(mouseX,mouseY)) healthBar.showDisplayValues(true);

            armorBar.update(player.getArmor(),player.getMaxArmor());
            armorBar.showDisplayValues(false);
            if(armorBar.intersects(mouseX,mouseY)) armorBar.showDisplayValues(true);

            if(miniMap.isDisplayBigMap()){
                armorBar.showDisplayValues(true);
                healthBar.showDisplayValues(true);
            }
            alertManager.update();
        }

        gaussianBlur.update(pause);
        lightManager.update();
    }

    /**
     *
     * @return returns time delta in ms
     */
    public static long deltaPauseTime(){
        return pauseTimeEnded;
    }

    public void pause(){
        if(!pause && !player.isDead()) {
            setCursor(ARROW);
            pauseTimeStarted = System.currentTimeMillis();
            pause = true;

        }
    }
    public void resume(){
        setCursor(Game.CROSSHAIR);
        pauseTimeEnded += System.currentTimeMillis() - pauseTimeStarted;
        pause=false;

    }

    public Player getPlayer(){return player;}


}

package cz.Empatix.Gamestates;


import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Database.Database;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.Artefacts.ArtefactManager;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Java.Loader;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Damageindicator.DamageIndicator;
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
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static cz.Empatix.Main.Game.ARROW;
import static cz.Empatix.Main.Game.setCursor;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;


public class InGame extends GameState {
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

    private Player player;

    private TileMap tileMap;

    private GunsManager gunsManager;

    private float mouseX;
    private float mouseY;

    // ingame huds
    private HealthBar healthBar;
    private ArmorBar armorBar;
    private MiniMap miniMap;
    private DamageIndicator damageIndicator;
    private cz.Empatix.Render.Hud.Image coin;
    private Console console;

    private EnemyManager enemyManager;

    // post processing
    private Framebuffer objectsFramebuffer;
    private Framebuffer pauseBlurFramebuffer;
    private Framebuffer fadeFramebuffer;
    private Fade fade;
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
    private static long pauseTimeStarted;
    private static long pauseTimeEnded;

    private TextRender[] textRender;


    InGame(GameStateManager gsm){
        this.gsm = gsm;
        textRender = new TextRender[17];
        for(int i = 0;i<17;i++) textRender[i] = new TextRender();
    }

    @Override
    void mouseReleased(int button) {
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
                        // TODO: save menu
                    }
                }
            }
        } else{
            gunsManager.stopShooting();
        }
    }

    @Override
    void mousePressed(int button) {
        if(button == GLFW_MOUSE_BUTTON_LEFT){
            gunsManager.startShooting();
        }
    }
    @Override
    public void mouseScroll(double x, double y){
        gunsManager.changeGunScroll();
    }
    @Override
    void keyReleased(int k) {
        if(player.isDead()){
            float time = (System.currentTimeMillis()-player.getDeathTime());
            if(time > 5500){
                gsm.setState(GameStateManager.PROGRESSROOM);
                glfwSetInputMode(Game.window,GLFW_CURSOR,GLFW_CURSOR_NORMAL);

            }
            return;
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

        if (k == 'R'){
            gunsManager.reload();
        }
    }

    @Override
    void keyPressed(int k) {
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
        itemManager.keyPressed(k,(int)(mouseX-mx-px),(int)(mouseY-my-py));
        tileMap.keyPressed(k,player);
        miniMap.keyPressed(k);

        if(k == GLFW.GLFW_KEY_F){
            artefactManager.activate();
        }

    }

    @Override
    void init() {
        AudioManager.cleanUpAllSources();

        gameStart = System.currentTimeMillis();
        pauseTimeEnded=0;
        pauseTimeStarted=0;
        pause = false;

        endRewardEarned = false;

        objectsFramebuffer = new Framebuffer();
        pauseBlurFramebuffer = new Framebuffer();
        fadeFramebuffer = new Framebuffer();
        lightManager = new LightManager();
        fade = new Fade("shaders\\fade");
        gaussianBlur = new GaussianBlur("shaders\\blur");


        setCursor(Game.CROSSHAIR);

        miniMap = new MiniMap();

        // Tile map
        tileMap = new TileMap(64,miniMap);
        tileMap.loadTiles("Textures\\tileset64.tga");

        // player
        // create player object
        player = new Player(tileMap);
        tileMap.setPlayer(player);

        // weapons
        // load gun manager with tilemap object
        gunsManager = new GunsManager(tileMap);

        artefactManager = new ArtefactManager(tileMap,player);
        // items drops
        // load item manager with instances of objects
        itemManager = new ItemManager(tileMap,gunsManager,artefactManager,player);

        // generate map + create objects which needs item manager & gun manager created
        tileMap.loadMap();
        // move player to starter room
        player.setPosition(tileMap.getPlayerStartX(), tileMap.getPlayerStartY());

        // make camera move smoothly
        tileMap.setTween(0.10);

        //health bar
        healthBar = new HealthBar("Textures\\healthBar",new Vector3f(250,125,0),5,45,3);
        healthBar.initHealth(player.getHealth(),player.getMaxHealth());
        //armor bar
        armorBar = new ArmorBar("Textures\\armorbar",new Vector3f(275,175,0),3);
        armorBar.initArmor(player.getArmor(),player.getMaxArmor());
        //minimap
        tileMap.fillMiniMap();
        damageIndicator = new DamageIndicator();
        // coin
        coin = new Image("Textures\\coin.tga",new Vector3f(75,1000,0),1.5f);

        //audio
        AudioManager.playSoundtrack(Soundtrack.IDLE);

        enemyManager = new EnemyManager(player,tileMap);



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
    void draw() {
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

        artefactManager.draw();

        player.draw();

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

        tileMap.drawTitle();

        healthBar.draw();
        armorBar.draw();
        miniMap.draw();
        damageIndicator.draw();

        console.draw();

        coin.draw();
        textRender[2].draw(""+player.getCoins(),new Vector3f(170,1019,0),3,new Vector3f(1.0f,0.847f,0.0f));


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
            if(player.isDead()){
                skullPlayerdead.draw();
            }
            float time = (System.currentTimeMillis()-player.getDeathTime());
            if(time > 3500){
                char[] gameOverTitle = "GAME OVER".toCharArray();
                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 0;time > 3500+i*65 && i < gameOverTitle.length;i++){
                    stringBuilder.append(gameOverTitle[i]);
                }
                textRender[3].draw(stringBuilder.toString(),new Vector3f( 800,340,0),5,new Vector3f(1f,0.25f,0f));
                glColor4f(1f,1f,1f,1f);
                glLineWidth(3f);
                glBegin(GL_LINES);
                float first = 960-(time-3500)/2.5f;
                float secondary = 960+(time-3500)/2.5f;
                if(first<480) first=480;
                if(secondary>1440) secondary=1440;
                glVertex2f(secondary, 380);
                glVertex2f(first, 380);
                glEnd();
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

        }
        if(pause){
            pauseBlurFramebuffer.unbindFBO();

            gaussianBlur.draw(pauseBlurFramebuffer);

            pauseBackground.draw();
            for(MenuBar bar: pauseBars){
                bar.draw();
            }

            textRender[13].draw("Pause",new Vector3f(925,300,0),7,new Vector3f(0.874f,0.443f,0.149f));
            textRender[14].draw("Resume",new Vector3f(905,465,0),4,new Vector3f(0.874f,0.443f,0.149f));
            textRender[15].draw("Save",new Vector3f(955,655,0),4,new Vector3f(0.874f,0.443f,0.149f));
            textRender[16].draw("Exit",new Vector3f(975,845,0),4,new Vector3f(0.874f,0.443f,0.149f));


        }

    }



    @Override
    void update() {
        AudioManager.update();
        if(player.isDead()){
            enemyManager.updateOnlyAnimations();
            fade.update();
            player.update();
            healthBar.update(player.getHealth(), player.getMaxHealth());
            float time = (System.currentTimeMillis()-player.getDeathTime());
            if(time > 2000){
                Vector3f pos = skullPlayerdead.getPos();
                float y = pos.y() + (140-pos.y()) * time/40000;
                Vector3f newpos = new Vector3f(pos.x(),y,0);
                skullPlayerdead.setPosition(newpos);
            }
            skullPlayerdead.setAlpha(time/4500f);
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
                Camera.getWIDTH() / 2f - player.getX(),
                Camera.getHEIGHT() / 2f - player.getY()
        );

        artefactManager.update();

        if(pause){
            for(MenuBar hud:pauseBars){
                hud.setClick(false);
                if(hud.intersects(mouseX,mouseY)){
                    hud.setClick(true);
                }
            }
        } else {
            itemManager.update();

            ArrayList<Enemy> enemies = EnemyManager.getEnemies();

            player.update();

            tileMap.updateObjects();

            // updating if player entered some another room
            tileMap.updateCurrentRoom(
                    (int) player.getX(),
                    (int) player.getY()
            );

            // update player icon location by new room
            miniMap.update(tileMap);


            // updating bullets(ammo)
            float px = player.getX();
            float py = player.getY();
            float mx = tileMap.getX();
            float my = tileMap.getY();
            gunsManager.shot(mouseX-mx-px,
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
            armorBar.update(player.getArmor(),player.getMaxArmor());
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

package cz.Empatix.Render.Hud.MultiplayerNPC;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Multiplayer.MultiplayerManager;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Alerts.AlertManager;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.Hud.InputBar;
import cz.Empatix.Render.Hud.MenuBar;
import cz.Empatix.Render.Hud.SliderBar;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Utility.Loader;
import org.joml.Vector3f;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.*;


public class MultiplayerMenu {
    public static void load(){
        Loader.loadImage("Textures\\ProgressRoom\\mpmenu-add.tga");
    }
    private Background background;
    private Background addBackground;

    private InputBar nameInput;
    private MenuBar[] menuBars;

    private static Source source;
    private static int soundMenuClick;

    private static final int CREATE = 0;
    private static final int REFRESH = 1;
    private static final int ADD = 2;
    private static final int ADDCONFIRM = 3;

    private TextRender[] mainTextRenders;
    private ArrayList<ServerTab> serverTabs;
    private ReentrantLock lock;
    private boolean refreshing;
    private int dots;
    private long lastTime;
    private List<InetAddress> addresses;

    private SliderBar sliderBar;
    private float scrollY;

    private InputBar addInput;
    private MenuBar confirmAdd;
    private boolean adding;

    private static long errorCooldown;
    private final static int NO_NAME = 0;
    private final static int FULL_SERVER = 1;
    private final static int TIMEOUT = 2;
    private static int error_type;

    private static GameStateManager gsm;

    public MultiplayerMenu(GameStateManager gsm){
        MultiplayerMenu.gsm = gsm;

        background = new Background("Textures\\ProgressRoom\\upgrademenu-guns.tga");
        background.setFadeEffect(false);
        background.setDimensions(900,750);
        background.setOffset(new Vector3f(200f,0,0));

        addBackground = new Background("Textures\\ProgressRoom\\mpmenu-add.tga");
        addBackground.setFadeEffect(false);
        addBackground.setDimensions(900,250);

        nameInput = new InputBar("Textures\\Menu\\input_bar.tga",new Vector3f(450,250,0),1.5f,300,100,"Your name:");
        nameInput.setType(0);

        addInput = new InputBar("Textures\\Menu\\input_bar.tga",new Vector3f(800,550,0),1.5f,300,100,"IP address:");
        addInput.setType(1);
        confirmAdd = new MenuBar("Textures\\Menu\\menu_bar.tga",new Vector3f(1200,550,0),1.5f,200,100,true);
        confirmAdd.setType(ADDCONFIRM);

        menuBars = new MenuBar[3];

        menuBars[0] = new MenuBar("Textures\\Menu\\menu_bar.tga",new Vector3f(450,450,0),1.5f,200,100,true);
        menuBars[0].setType(CREATE);
        menuBars[1] = new MenuBar("Textures\\Menu\\menu_bar.tga",new Vector3f(450,650,0),1.5f,200,100,true);
        menuBars[1].setType(REFRESH);
        menuBars[2] = new MenuBar("Textures\\Menu\\menu_bar.tga",new Vector3f(450,850,0),1.5f,200,100,true);
        menuBars[2].setType(ADD);

        sliderBar = new SliderBar(new Vector3f(1591,540,0),3);
        sliderBar.setLength(730);
        sliderBar.setVertical();
        sliderBar.setValue(0f);


        source = AudioManager.createSource(Source.EFFECTS,1f);
        soundMenuClick = AudioManager.loadSound("menuclick.ogg");

        serverTabs = new ArrayList<>();

        mainTextRenders = new TextRender[4];
        for(int i = 0;i<mainTextRenders.length;i++) mainTextRenders[i] = new TextRender();

        MultiplayerManager mpmanager = new MultiplayerManager();
        refreshServers();
        lock = new ReentrantLock();
        dots = 0;

    }
    public void draw(){
        if(adding){
            addBackground.draw();
            addInput.draw();
            confirmAdd.draw();
            mainTextRenders[2].draw("Add", new Vector3f(TextRender.getHorizontalCenter(1050, 1350, "add", 4), 550, 0), 4, new Vector3f(0.874f, 0.443f, 0.149f));
        } else {
            background.draw();
            nameInput.draw();
            for(MenuBar bar : menuBars){
                bar.draw();
            }

            mainTextRenders[0].draw("Create", new Vector3f(TextRender.getHorizontalCenter(300, 600, "Create", 4), 450, 0), 4, new Vector3f(0.874f, 0.443f, 0.149f));
            mainTextRenders[1].draw("Refresh", new Vector3f(TextRender.getHorizontalCenter(300, 600, "Refresh", 4), 650, 0), 4, new Vector3f(0.874f, 0.443f, 0.149f));
            mainTextRenders[2].draw("Add", new Vector3f(TextRender.getHorizontalCenter(300, 600, "add", 4), 850, 0), 4, new Vector3f(0.874f, 0.443f, 0.149f));
            if(refreshing){
                StringBuilder refreshText = new StringBuilder("Searching servers");
                for(int i = 0;i<dots;i++) refreshText.append(".");
                mainTextRenders[2].draw(refreshText.toString(), new Vector3f(TextRender.getHorizontalCenter(720, 1600, refreshText.toString(), 3), 350, 0), 3, new Vector3f(1, 0.1f, 0.149f));
            } else {
                float sliderY = sliderBar.getValue() * -(((serverTabs.size()-3) * 250)+50);
                glEnable(GL_SCISSOR_TEST);
                glScissor(720,177,880,726);
                for(ServerTab tab : serverTabs) {
                    tab.draw((int)sliderY);
                }
                glDisable(GL_SCISSOR_TEST);

                if(serverTabs.size() == 0){
                    mainTextRenders[3].draw("No servers found..", new Vector3f(TextRender.getHorizontalCenter(720, 1600, "No servers found..", 3), 350, 0), 3, new Vector3f(1, 0.1f, 0.149f));
                }
            }
            sliderBar.draw();
        }

    }
    public void update(float x, float y){
        if(adding){
            addInput.update();
            confirmAdd.setClick(false);
            if(confirmAdd.intersects(x,y)) confirmAdd.setClick(true);
        } else {
            // check if there was any refresh
            lock.lock();
            try{
                if(addresses != null){ // there was refresh
                    for(ServerTab tab: serverTabs){
                        tab.unload();
                    }
                    serverTabs = new ArrayList<>();
                    for(InetAddress address : addresses){
                        byte[] octets = address.getAddress();
                        if(octets[0] != 127){ // is not localhost
                            serverTabs.add(new ServerTab(address,serverTabs.size()));
                        }
                    }
                    addresses = null; // set null so we know we updated server tabs by this refresh
                }
            } finally {
                lock.unlock();
            }
            sliderBar.disableSlideDraw(serverTabs.size() < 3);

            nameInput.update();
            for (MenuBar bar : menuBars) {
                bar.setClick(false);
                if (bar.intersects(x, y)) {
                    bar.setClick(true);
                }
            }
            if(refreshing){
                if((System.nanoTime() - lastTime)/1_000_000 > 100){
                    dots++;
                    lastTime = System.nanoTime();
                }
                if(dots >= 4) dots = 1;
            } else {
                if(sliderBar.isLocked()){
                    sliderBar.update(x,y);
                    scrollY = sliderBar.getValue();
                }
                float value = sliderBar.getValue();
                value += (scrollY - value) * Game.deltaTime * 10;
                if(value > 1) value = 1;
                else if (value < 0) value = 0;
                sliderBar.setValue(value);
                float sliderY = sliderBar.getValue() * -(((serverTabs.size()-3) * 250)+50);
                for(ServerTab tab : serverTabs){
                    tab.updatePosition((int)sliderY);
                    tab.update(x,y-sliderY);
                }
            }
        }

    }

    public void mousePressed(float x, float y, Player p){
        if(adding){
            if (addInput.intersects(x,y)) {
                addInput.setEnabled(true);
                addInput.setClick(true);
                source.play(soundMenuClick);
            } else{
                addInput.setEnabled(false);
                addInput.setClick(false);
            }
            if(confirmAdd.intersects(x,y)){
                if(addInput.getValue().isEmpty()){
                    if(System.currentTimeMillis() - errorCooldown > 2000){
                        AlertManager.add(AlertManager.WARNING,"Please fill IP address");
                        errorCooldown = System.currentTimeMillis();
                    }
                } else {
                    try {
                        if(!addInput.getValue().startsWith("127.")){
                            serverTabs.add(new ServerTab(InetAddress.getByName(addInput.getValue()),serverTabs.size()));
                        } else {
                            AlertManager.add(AlertManager.WARNING,"Localhost can't be added");
                        }
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        AlertManager.add(AlertManager.WARNING,"IP was not found");
                    }
                    adding = false;
                }
            }
        } else {
            if(nameInput.intersects(x,y)){
                nameInput.setEnabled(true);
                nameInput.setClick(true);
                source.play(soundMenuClick);
            } else {
                nameInput.setEnabled(false);
                nameInput.setClick(false);
            }
            float sliderY = sliderBar.getValue() * -(((serverTabs.size()-3) * 250)+50);
            for(ServerTab tab : serverTabs){
                tab.mousePressed(x,y-sliderY,nameInput);
            }
            for(MenuBar bar : menuBars){
                if(bar.intersects(x,y)){
                    source.play(soundMenuClick);
                    if(bar.getType() == CREATE){
                        if(!nameInput.isEmpty()){
                            gsm.setStateInitMP(GameStateManager.PROGRESSROOMMP,true,nameInput.getValue(),"localhost");
                        } else if(System.currentTimeMillis() - errorCooldown > 2000){
                            AlertManager.add(AlertManager.WARNING,"Please fill your name");
                            errorCooldown = System.currentTimeMillis();
                            error_type = NO_NAME;
                        }
                    } else if (bar.getType() == REFRESH){
                        refreshServers();
                    } else if (bar.getType() == ADD){
                        adding = true;
                    }
                    source.play(soundMenuClick);
                    break;
                }
            }
            if(sliderBar.intersects(x,y) && serverTabs.size() >= 3){
                sliderBar.setLocked(true);
            }
        }
    }
    private void refreshServers(){
        if(refreshing) return;
        refreshing = true;
        scrollY = 0;
        sliderBar.setLocked(false);
        sliderBar.setValue(0);
        new Thread("Servers search"){
            @Override
            public void run() {
                List<InetAddress> newAddreses = MultiplayerManager.getInstance().client.getClient().discoverHosts(54777,1000);
                String[] hostnames = new String[newAddreses.size()];
                for(int i = 0;i<hostnames.length;i++){
                    hostnames[i] = newAddreses.get(i).getHostName();
                    System.out.println(hostnames[i]);
                }
                boolean[] duplicate = new boolean[hostnames.length];
                Arrays.fill(duplicate,false);
                for(int i = 0;i< hostnames.length;i++){
                    for(int j = 0;j< hostnames.length;j++){
                        if(hostnames[i].equalsIgnoreCase(hostnames[j]) && j != i && !duplicate[i]){
                            duplicate[j] = true;
                        }
                    }
                }
                int shift = 0;
                for(int i = 0;i< duplicate.length-shift;i++){
                    if(duplicate[i-shift]){
                        newAddreses.remove(i-shift);
                        shift++;
                    }
                }
                lock.lock();
                try{
                    addresses = newAddreses;
                } finally {
                    lock.unlock();
                }
                refreshing = false;
            }
        }.start();
    }
    public void mouseReleased(float x, float y){
        unlockSlider();
    }
    public void unlockSlider(){
        sliderBar.unlock();
    }

    public void mouseScroll(double x, double y) {
        if(!refreshing && serverTabs.size() >= 3){
            float value = sliderBar.getValue();
            scrollY = value-(float)y/(2*serverTabs.size());
        }
    }

    public void keyPress(int k) {
        nameInput.keyPressed(k);
        addInput.keyPressed(k);
    }

    public void keyReleased(int k) {
        nameInput.keyReleased(k);
        addInput.keyReleased(k);
    }
    public boolean isUsingInputBar(){
        return nameInput.isClick() || addInput.isClick();
    }
    private static class  ServerTab{
        private TextRender[] textRenders;
        private InetAddress inetAddress;
        private MenuBar selector;
        private boolean errorButton;

        private Image tab;

        private int row;

        // create new server tab if we dont have enough
        public ServerTab(InetAddress ip, int row){
            this.row = row;
            inetAddress = ip;
            textRenders = new TextRender[2];
            textRenders[0] = new TextRender();
            textRenders[1] = new TextRender();

            selector = new MenuBar("Textures\\Menu\\menu_bar.tga",new Vector3f(1450,307+row*250,0),1f,200,100,true);

            tab = new Image("Textures\\ProgressRoom\\serverfound.tga",new Vector3f(1153,307+row*250,0),4);
        }
        public void draw(int sliderY){
            tab.draw();
            selector.draw();
            textRenders[0].draw(inetAddress.getHostAddress(),new Vector3f(TextRender.getHorizontalCenter(750,1350,inetAddress.getHostAddress(),3),330+row*250+sliderY,0),3, new Vector3f(0.874f, 0.443f, 0.149f));
            if(errorButton) {
                textRenders[1].draw("Join",new Vector3f(TextRender.getHorizontalCenter(1350,1550,"Join",3),310+row*250+sliderY,0),3, new Vector3f(0.874f, 0.143f, 0.149f));
            } else {
                textRenders[1].draw("Join",new Vector3f(TextRender.getHorizontalCenter(1350,1550,"Join",3),310+row*250+sliderY,0),3, new Vector3f(0.874f, 0.443f, 0.149f));
            }
        }
        public void unload(){
            textRenders[0].clearVBOs();
            textRenders[1].clearVBOs();
        }
        public void mousePressed(float x, float y,InputBar inputName){
            if(selector.intersects(x,y)){
                source.play(soundMenuClick);
                if(!inputName.isEmpty()){
                    gsm.setStateInitMP(GameStateManager.PROGRESSROOMMP,false,inputName.getValue(),inetAddress.getHostAddress());
                } else {
                    if(System.currentTimeMillis() - errorCooldown > 2000){
                        AlertManager.add(AlertManager.WARNING,"Please fill your name");
                        errorCooldown = System.currentTimeMillis();
                        error_type = NO_NAME;
                        errorButton = true;
                    }
                }
            }
        }
        public void update(float x, float y) {
            selector.setClick(false);
            if (selector.intersects(x, y)) {
                selector.setClick(true);
            }
            if (System.currentTimeMillis() - errorCooldown > 2000 && errorButton) {
                errorButton = false;
            }
        }

        public void updatePosition(int sliderY) {
            Vector3f posTab = tab.getPos();
            posTab.y = 315+row*250+sliderY;
            Vector3f posSelector = selector.getPosition();
            posSelector.y = 307+row*250+sliderY;
        }
    }
    public void playerAbandonedInteraction(){
        adding = false;
        nameInput.setEnabled(false);
        nameInput.setClick(false);
        addInput.setEnabled(false);
        addInput.setClick(false);
    }
}


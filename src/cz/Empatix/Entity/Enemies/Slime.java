package cz.Empatix.Entity.Enemies;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemies.Projectiles.Slimebullet;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.RoomObjects.DestroyableObject;
import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Multiplayer.GameServer;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;

import java.util.ArrayList;


public class Slime extends Enemy {

    private static final int IDLE = 0;
    private static final int DEAD = 1;

    private boolean shootready;
    private boolean disableDraw;
    private long shootCooldown;


    private ArrayList<Slimebullet> bullets;

    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Enemies\\slime.tga");
    }
    public Slime(TileMap tm, Player player) {
        super(tm,player);
        initStats(tm.getFloor());

        width = 64;
        height = 48;
        cwidth = 64;
        cheight = 48;
        scale = 2;

        facingRight = true;

        spriteSheetCols = 6;
        spriteSheetRows = 2;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\slime.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\slime.tga");
            Sprite[] sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                Sprite sprite = new Sprite(5,i,0,32,24,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            sprites = new Sprite[6];
            for(int i = 0; i < sprites.length; i++) {
                Sprite sprite = new Sprite(5,i,1,32,24,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

        }
        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(IDLE));
        animation.setDelay(175);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 2x
        width *= 2;
        height *= 2;
        cwidth *= 2;
        cheight *= 2;

        bullets = new ArrayList<>(20);

        createShadow();

    }

    public Slime(TileMap tm, Player[] player) {
        super(tm,player);
        initStats(tm.getFloor());
        if(tm.isServerSide()){
            width = 64;
            height = 48;
            cwidth = 64;
            cheight = 48;
            scale = 2;

            animation = new Animation(4);
            animation.setDelay(175);

            facingRight = true;

            width *= 2;
            height *= 2;
            cwidth *= 2;
            cheight *= 2;

            bullets = new ArrayList<>(20);
        } else {
            width = 64;
            height = 48;
            cwidth = 64;
            cheight = 48;
            scale = 2;

            facingRight = true;

            spriteSheetCols = 6;
            spriteSheetRows = 2;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\slime.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\slime.tga");
                Sprite[] sprites = new Sprite[4];
                for(int i = 0; i < sprites.length; i++) {
                    Sprite sprite = new Sprite(5,i,0,32,24,spriteSheetRows,spriteSheetCols);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);

                sprites = new Sprite[6];
                for(int i = 0; i < sprites.length; i++) {
                    Sprite sprite = new Sprite(5,i,1,32,24,spriteSheetRows,spriteSheetCols);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);

            }
            vboVertices = ModelManager.getModel(width,height);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,height);
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(IDLE));
            animation.setDelay(175);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 2x
            width *= 2;
            height *= 2;
            cwidth *= 2;
            cheight *= 2;

            bullets = new ArrayList<>(20);

            createShadow();
        }

    }
    public void initStats(int floor){
        movementVelocity = 100;
        moveAcceleration = 4f;
        stopAcceleration = 3.8f;

        health = maxHealth = (int)(9*(1+(Math.pow(floor,1.25)*0.12)));
        tryBoostHealth();
        damage = 1;

        type = melee;
    }

    @Override
    public void update() {
        setMapPosition();
        if(isSpawning()) return;
        // update animation
        animation.update();
        if(isDead() && animation.hasPlayedOnce()){
            disableDraw = true;
        }
        for(int i = 0;i<bullets.size();i++){
            Slimebullet slimebullet = bullets.get(i);
            // check if bullet was hitted or not before updating it
            boolean preHit = slimebullet.isHit();
            slimebullet.update();
            // multiplayer - serverside
            if(tileMap.isServerSide() || !MultiplayerManager.multiplayer){
                // if bullet hitted wall
                if(!preHit && slimebullet.isHit() && tileMap.isServerSide()){
                    MultiplayerManager mpManager = MultiplayerManager.getInstance();
                    Server server = MultiplayerManager.getInstance().server.getServer();
                    Network.HitEnemyProjectile enemyProjectile = new Network.HitEnemyProjectile();
                    mpManager.server.requestACK(enemyProjectile,enemyProjectile.idPacket);
                    enemyProjectile.id = slimebullet.id;
                    enemyProjectile.idEnemy = getId();
                    server.sendToAllUDP(enemyProjectile);
                }
                if(slimebullet.isHit()) continue;
                if(tileMap.isServerSide()){
                    if(GameServer.tick % 2 == 0){
                        Server server = MultiplayerManager.getInstance().server.getServer();
                        Network.MoveEnemyProjectile moveEnemyProjectile = new Network.MoveEnemyProjectile();
                        moveEnemyProjectile.idEnemy = id;
                        moveEnemyProjectile.id = slimebullet.id;
                        moveEnemyProjectile.tick = GameServer.tick;
                        moveEnemyProjectile.x = slimebullet.getX();
                        moveEnemyProjectile.y = slimebullet.getY();
                        server.sendToAllUDP(moveEnemyProjectile);
                    }
                }
                for(Player p : player){
                    if(p != null){
                        if(slimebullet.intersects(p) && !p.isFlinching() && !p.isDead()){
                            slimebullet.setHit();
                            p.hit(1);

                            if(tileMap.isServerSide()){
                                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                                Server server = MultiplayerManager.getInstance().server.getServer();
                                Network.HitEnemyProjectile enemyProjectile = new Network.HitEnemyProjectile();
                                mpManager.server.requestACK(enemyProjectile,enemyProjectile.idPacket);
                                enemyProjectile.id = slimebullet.id;
                                enemyProjectile.idEnemy = getId();
                                server.sendToAllUDP(enemyProjectile);
                            }
                        }
                    }
                }
                ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
                for(ArrayList<RoomObject> objects : objectsArray) {
                    if (objects == null) continue;
                    for(RoomObject object : objects) {
                        if (object instanceof DestroyableObject) {
                            if (slimebullet.intersects(object) && !((DestroyableObject) object).isDestroyed()) {
                                slimebullet.setHit();
                                ((DestroyableObject) object).setHit(1);

                                if (tileMap.isServerSide()) {
                                    MultiplayerManager mpManager = MultiplayerManager.getInstance();
                                    Server server = MultiplayerManager.getInstance().server.getServer();
                                    Network.HitEnemyProjectile enemyProjectile = new Network.HitEnemyProjectile();
                                    mpManager.server.requestACK(enemyProjectile,enemyProjectile.idPacket);
                                    enemyProjectile.id = slimebullet.id;
                                    enemyProjectile.idEnemy = getId();
                                    enemyProjectile.idHit = object.getId();
                                    server.sendToAllUDP(enemyProjectile);
                                }
                            }
                        } else if (object.collision && slimebullet.intersects(object)) {
                            slimebullet.setHit();

                            if (tileMap.isServerSide()) {
                                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                                Server server = MultiplayerManager.getInstance().server.getServer();
                                Network.HitEnemyProjectile enemyProjectile = new Network.HitEnemyProjectile();
                                mpManager.server.requestACK(enemyProjectile,enemyProjectile.idPacket);
                                enemyProjectile.id = slimebullet.id;
                                enemyProjectile.idEnemy = getId();
                                server.sendToAllUDP(enemyProjectile);
                            }
                        }
                    }
                }
                // singleplayer
            }
            if(slimebullet.shouldRemove()) {
                bullets.remove(i);
                i--;
            }
        }

        if(dead) return;

        if(!shootready && animation.getIndexOfFrame() == 0 && System.currentTimeMillis()-shootCooldown- InGame.deltaPauseTime() > 2000){
            shootready = true;
            shootCooldown = System.currentTimeMillis()- InGame.deltaPauseTime();
        }
        else if(shootready && animation.getIndexOfFrame() == 2 && (!MultiplayerManager.multiplayer || tileMap.isServerSide())){
            shootready=false;
            int playerIndex = theClosestPlayerIndex();

            final int tileTargetX = px[playerIndex]/tileSize;
            final int tileTargetY = py[playerIndex]/tileSize;

            final int tileEnemyX = (int)position.x/tileSize;
            final int tileEnemyY = (int)position.y/tileSize;

            if (Math.abs(tileEnemyX - tileTargetX) <= 12 && Math.abs(tileEnemyY - tileTargetY) <= 12) {

                for (int i = 0; i < 5; i++) {
                    Slimebullet slimebullet = new Slimebullet(tileMap, px[playerIndex] - position.x, py[playerIndex] - position.y, 1.3 * i);
                    slimebullet.setPosition(position.x, position.y);
                    bullets.add(slimebullet);
                    if(tileMap.isServerSide()){
                        MultiplayerManager mpManager = MultiplayerManager.getInstance();
                        Network.AddEnemyProjectile addEnemyProjectile = new Network.AddEnemyProjectile();
                        mpManager.server.requestACK(addEnemyProjectile,addEnemyProjectile.idPacket);
                        addEnemyProjectile.idEnemy = id;
                        addEnemyProjectile.id = slimebullet.id;
                        addEnemyProjectile.x = px[playerIndex] - position.x;
                        addEnemyProjectile.y = py[playerIndex] - position.y;
                        addEnemyProjectile.inaccuracy = 1.3f*i;
                        Server server = mpManager.server.getServer();
                        server.sendToAllUDP(addEnemyProjectile);
                    }
                }
            }
        }
        super.update();
        movePacket();

    }

    public void draw() {
        for(Slimebullet bullet : bullets){
            bullet.draw();
        }
        if(!disableDraw){
            super.draw();
        }
    }
    @Override
    public void hit(int damage) {
        super.hit(damage);
        if(isDead()){
            if(tileMap.isServerSide()){
                animation = new Animation(6);
                animation.setDelay(65);
            } else {
                animation.setFrames(spritesheet.getSprites(DEAD));
                animation.setDelay(65);
            }
        }
    }
    @Override
    public boolean shouldRemove(){
        return animation.hasPlayedOnce() && isDead() && bullets.size()==0;
    }
    @Override
    public void drawShadow() {
        if(!disableDraw) drawShadow(5f);
    }

    @Override
    public void handleAddEnemyProjectile(Network.AddEnemyProjectile o) {
        Slimebullet slimebullet = new Slimebullet(tileMap,o.x,o.y,o.inaccuracy);
        slimebullet.setPosition(position.x,position.y);
        slimebullet.setId(o.id);
        bullets.add(slimebullet);
    }

    @Override
    public void handleMoveEnemyProjectile(Network.MoveEnemyProjectile o) {
        for(Slimebullet bullet : bullets){
            if(bullet.getId() == o.id){
                bullet.addInterpolationPosition(o);
            }

        }
    }

    @Override
    public void handleHitEnemyProjectile(Network.HitEnemyProjectile hitPacket) {
        for(Slimebullet bullet : bullets){
            if(bullet.getId() == hitPacket.id){
                bullet.setHit();
                // player was hitted on client side
                if (hitPacket.idHit != -1){
                    ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
                    for(ArrayList<RoomObject> objects : objectsArray) {
                        if (objects == null) continue;
                        for (RoomObject roomObject : objects) {
                            if (roomObject.getId() == hitPacket.idHit) {
                                ((DestroyableObject) roomObject).setHit(1);
                            }
                        }
                    }
                }
            }

        }
    }
    public void forceRemove(){
        for(Slimebullet b : bullets){
            b.forceRemove();
        }
    }
    @Override
    public void applyHitEffects(Player hitPlayer) {

    }
}


package cz.Empatix.Entity.Enemies;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemies.Projectiles.KingSlimebullet;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Hud.HealthBar;
import cz.Empatix.Render.RoomObjects.Chest;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;

public class KingSlime extends Enemy {

    private static final int IDLE = 0;
    private static final int DEAD = 1;

    private boolean shootready;
    private boolean disableDraw;
    private long shootCooldown;

    private long shootCooldownCircle;


    private int angle;
    private boolean chestCreated;

    private final ArrayList<KingSlimebullet> bullets;
    private transient HealthBar healthBar;

    private long directionChangeCooldown;
    private boolean invertDirection;

    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Enemies\\slimeking.tga");
    }
    public KingSlime(TileMap tm, Player player) {
        super(tm,player);
        initStats(tm.getFloor());

        width = 64;
        height = 48;
        cwidth = 64;
        cheight = 48;
        scale = 5;

        type = melee;
        facingRight = true;
        bullets=new ArrayList<>(100);

        spriteSheetCols = 6;
        spriteSheetRows = 2;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\slimeking.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\slimeking.tga");
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
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        angle=0;
        chestCreated=false;

        healthBar = new HealthBar("Textures\\bosshealthbar",new Vector3f(960,1000,0),7,56,4);
        healthBar.setOffsetsBar(14,1);
        healthBar.initHealth(health,maxHealth);

        createShadow();

        directionChangeCooldown = System.currentTimeMillis()- InGame.deltaPauseTime();
        invertDirection = false;

    }
    // multiplayer
    public KingSlime(TileMap tm, Player[] player) {
        super(tm,player);
        initStats(tm.getFloor());
        if(tm.isServerSide()){
            width = 64;
            height = 48;
            cwidth = 64;
            cheight = 48;
            scale = 5;

            animation = new Animation(4);
            animation.setDelay(175);

            facingRight = true;
            bullets=new ArrayList<>(100);

            // because of scaling image by 2x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            angle=0;
            chestCreated=false;

            directionChangeCooldown = System.currentTimeMillis()- InGame.deltaPauseTime();
            invertDirection = false;
        } else  {
            width = 64;
            height = 48;
            cwidth = 64;
            cheight = 48;
            scale = 5;

            facingRight = true;
            bullets=new ArrayList<>(100);

            spriteSheetCols = 6;
            spriteSheetRows = 2;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\slimeking.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\slimeking.tga");
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
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            angle=0;
            chestCreated=false;

            healthBar = new HealthBar("Textures\\bosshealthbar",new Vector3f(960,1000,0),7,56,4);
            healthBar.setOffsetsBar(14,1);
            healthBar.initHealth(health,maxHealth);

            createShadow();

            directionChangeCooldown = System.currentTimeMillis()- InGame.deltaPauseTime();
            invertDirection = false;
        }

    }
    public void initStats(int floor){
        moveSpeed = 0.6f;
        maxSpeed = 1.6f;
        stopSpeed = 0.5f;

        movementVelocity = 100;
        moveAcceleration = 4f;
        stopAcceleration = 3.8f;

        health = maxHealth = (int)(90*(1+(Math.pow(floor,1.25)*0.12)));
        damage = 1;

        type = melee;
    }

    @Override
    public void update() {
        if(MultiplayerManager.multiplayer){
            if(tileMap.isServerSide()) updateMPServer();
            else updateMPClient();
        } else {
            updateSP();
        }

    }
    public void updateSP(){
        setMapPosition();
        healthBar.update(health,maxHealth);
        if(isSpawning()) return;
        // update animation
        animation.update();

        // creating chest+ladder after death
        // checking !itemDropped if boss wasn't killed by setDead();
        // this function is used when player dies so we want to not create ladder
        if(isDead() && animation.hasPlayedOnce() && !itemDropped){
            disableDraw = true;
            if(!chestCreated){
                chestCreated=true;

                Chest chest = new Chest(tileMap);
                chest.setPosition(position.x,position.y);
                chest.enableDropArtefact();
                tileMap.addObject(chest,tileMap.getRoomByCoords(position.x,position.y).getId());

                tileMap.addLadder();
            }
        }
        // checking collisions of slime bullets
        for(int i = 0;i<bullets.size();i++){
            KingSlimebullet slimebullet = bullets.get(i);
            slimebullet.update();
            // if bullet hitted wall
            if(slimebullet.isHit()) continue;
            for(Player p : player){
                if(p != null){
                    if(slimebullet.intersects(p) && !p.isFlinching() && !p.isDead()){
                        slimebullet.setHit();
                        p.hit(1);

                    }
                }
            }
            ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
            for(ArrayList<RoomObject> objects : objectsArray) {
                if (objects == null) continue;
                for (RoomObject object : objects) {
                    if (object instanceof DestroyableObject) {
                        if (slimebullet.intersects(object) && !slimebullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                            slimebullet.setHit();
                            ((DestroyableObject) object).setHit(1);
                        }
                    } else if (object.collision && slimebullet.intersects(object)) {
                        slimebullet.setHit();
                    }
                }
            }
            if(slimebullet.shouldRemove()) {
                bullets.remove(i);
                i--;
            }
        }

        if(dead) return;

        if((float)health/maxHealth <= 0.5 && System.currentTimeMillis()-shootCooldownCircle- InGame.deltaPauseTime() > 2500){
            shootCooldownCircle = System.currentTimeMillis()- InGame.deltaPauseTime();
            for (int i = 0; i < 5; ) {
                double inaccuracy = 0.055 * i;

                int index = theClosestPlayerIndex();

                KingSlimebullet slimebullet = new KingSlimebullet(
                        tileMap,
                        px[index]-position.x,
                        py[index]-position.y,
                        inaccuracy
                );
                slimebullet.setPosition(position.x, position.y);
                bullets.add(slimebullet);
                if (i >= 0) i++;
                else i--;
                i = -i;
            }
        }

        if(!shootready && System.currentTimeMillis()-shootCooldown- InGame.deltaPauseTime() > 50){
            shootready = true;
            shootCooldown = System.currentTimeMillis()- InGame.deltaPauseTime();
        }
        else if(shootready) {
            shootready = false;


            float offset = Random.nextInt(3) * 0.1f;


            // FLOOR 3 change between time change direction
            if(tileMap.getFloor() >= 2 && System.currentTimeMillis() - directionChangeCooldown - InGame.deltaPauseTime() > 7500){
                directionChangeCooldown = System.currentTimeMillis()- InGame.deltaPauseTime();
                invertDirection = !invertDirection;
            }
            if(invertDirection){
                angle -= 7;
            } else {
                angle += 7;
            }
            if(angle >= 360){
                angle-=360;
            }
            if(angle < 0){
                angle+=360;
            }
            // spin bullets
            KingSlimebullet slimebullet = new KingSlimebullet(
                    tileMap,
                    Math.sin(Math.toRadians(angle)),
                    Math.sin(Math.toRadians(angle-90)),
                    offset+0.05 *(1-Random.nextInt(2)*2)
            );
            slimebullet.setPosition(position.x, position.y);
            bullets.add(slimebullet);

            slimebullet = new KingSlimebullet(
                    tileMap,
                    Math.sin(Math.toRadians(angle-180)),
                    Math.sin(Math.toRadians(angle-270)),
                    offset+0.05 *(1-Random.nextInt(2)*2)
            );
            slimebullet.setPosition(position.x, position.y);
            bullets.add(slimebullet);

        }
        super.update();
        movePacket();
    }
    public void updateMPClient(){
        setMapPosition();
        healthBar.update(health,maxHealth);
        if(isSpawning()) return;
        // update animation
        animation.update();

        // creating chest+ladder after death
        // checking !itemDropped if boss wasn't killed by setDead();
        // this function is used when player dies so we want to not create ladder
        if(isDead() && animation.hasPlayedOnce() && !itemDropped){
            disableDraw = true;
        }
        // checking collisions of slime bullets
        for(int i = 0;i<bullets.size();i++){
            KingSlimebullet slimebullet = bullets.get(i);
            slimebullet.update();
            if(slimebullet.shouldRemove()) {
                bullets.remove(i);
                i--;
            }
        }

        if(dead) return;

        super.update();
    }
    public void updateMPServer(){
        setMapPosition();
        if(isSpawning()) return;
        // update animation
        animation.update();

        // creating chest+ladder after death
        // checking !itemDropped if boss wasn't killed by setDead();
        // this function is used when player dies so we want to not create ladder
        if(isDead() && animation.hasPlayedOnce() && !itemDropped){
            disableDraw = true;
            if(!chestCreated){
                chestCreated=true;

                Chest chest = new Chest(tileMap);
                chest.setPosition(position.x,position.y);
                chest.enableDropArtefact();
                tileMap.addObject(chest,tileMap.getRoomByCoords(position.x,position.y).getId());

                tileMap.addLadder();
            }
        }
        // checking collisions of slime bullets
        boolean[] hitBullet = new boolean[bullets.size()];
        int[] hitIds = new int[bullets.size()];
        int totalHit = 0;
        int totalToMove = 0;

        Arrays.fill(hitBullet,false);
        for(int i = 0;i<bullets.size();i++){
            KingSlimebullet slimebullet = bullets.get(i);
            boolean preHit = slimebullet.isHit();
            slimebullet.update();
            // if bullet hitted wall
            if(!preHit && slimebullet.isHit() && tileMap.isServerSide()){
                hitBullet[i] = true;
                hitIds[i] = -1;
                totalHit++;
            }
            if(slimebullet.isHit()) continue;

            for(Player p : player){
                if(p != null){
                    if(slimebullet.intersects(p) && !p.isFlinching() && !p.isDead()){
                        slimebullet.setHit();
                        p.hit(1);

                        hitBullet[i] = true;
                        hitIds[i] = -1;
                        totalHit++;
                        break;
                    }
                }
            }
            ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
            A: for(ArrayList<RoomObject> objects : objectsArray) {
                if (objects == null) continue;
                for (RoomObject object : objects) {
                    if (object instanceof DestroyableObject) {
                        if (slimebullet.intersects(object) && !slimebullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                            slimebullet.setHit();
                            ((DestroyableObject) object).setHit(1);

                            hitBullet[i] = true;
                            hitIds[i] = object.id;
                            totalHit++;
                            break A;
                        }
                    } else if (object.collision && slimebullet.intersects(object)) {
                        slimebullet.setHit();

                        hitBullet[i] = true;
                        hitIds[i] = object.id;
                        totalHit++;
                        break A;
                    }
                }
            }
            if(!slimebullet.isHit()) totalToMove++; // if slimebullet is after logics still valid for moving
        }
        // hitbullets packet
        MultiplayerManager mpManager = MultiplayerManager.getInstance();
        Server server = MultiplayerManager.getInstance().server.getServer();
        Network.HitEnemyProjectileInstanced hepi = new Network.HitEnemyProjectileInstanced(totalHit);
        hepi.idEnemy = id;
        int totalHitFound = 0;
        for(int i = 0;i<bullets.size() && totalHitFound != totalHit;i++){
            KingSlimebullet slimebullet = bullets.get(i);
            if(hitBullet[i]){
                hepi.id[totalHitFound] = slimebullet.id;
                hepi.idHit[totalHitFound] = hitIds[i];
                totalHitFound++;
            }
        }
        mpManager.server.requestACK(hepi,hepi.idPacket);
        server.sendToAllUDP(hepi);

        // total of bullets we are moving, that has not been marked as hitted
        Network.MoveEnemyProjectileInstanced mpei = new Network.MoveEnemyProjectileInstanced(totalToMove);
        int totalMoveFound = 0;
        mpei.idEnemy = id;
        for(int i = 0;i<bullets.size() && totalMoveFound != totalToMove;i++){
            KingSlimebullet slimebullet = bullets.get(i);
            if(slimebullet.shouldRemove()) {
                bullets.remove(i);
                i--;
            } else if (!slimebullet.isHit()) {
                mpei.id[totalMoveFound] = slimebullet.getId();
                mpei.x[totalMoveFound] = slimebullet.getX();
                mpei.y[totalMoveFound] = slimebullet.getY();
                totalMoveFound++;
            }
        }
        server.sendToAllUDP(mpei);

        if(dead) return;

        if((float)health/maxHealth <= 0.5 && System.currentTimeMillis()-shootCooldownCircle- InGame.deltaPauseTime() > 2500
               ){
            shootCooldownCircle = System.currentTimeMillis()- InGame.deltaPauseTime();
            for (int i = 0; i < 5; ) {
                double inaccuracy = 0.055 * i;

                int index = theClosestPlayerIndex();

                KingSlimebullet slimebullet = new KingSlimebullet(
                        tileMap,
                        px[index]-position.x,
                        py[index]-position.y,
                        inaccuracy
                );
                slimebullet.setPosition(position.x, position.y);
                bullets.add(slimebullet);

                Network.AddEnemyProjectile addEnemyProjectile = new Network.AddEnemyProjectile();
                mpManager.server.requestACK(addEnemyProjectile,addEnemyProjectile.idPacket);
                addEnemyProjectile.idEnemy = id;
                addEnemyProjectile.id = slimebullet.id;
                addEnemyProjectile.x = px[index] - position.x;
                addEnemyProjectile.y = py[index] - position.y;
                addEnemyProjectile.inaccuracy = (float)inaccuracy;
                server.sendToAllUDP(addEnemyProjectile);

                if (i >= 0) i++;
                else i--;
                i = -i;
            }
        }

        if(!shootready && System.currentTimeMillis()-shootCooldown- InGame.deltaPauseTime() > 50){
            shootready = true;
            shootCooldown = System.currentTimeMillis()- InGame.deltaPauseTime();
        }
        else if(shootready) {
            shootready = false;

            float offset = Random.nextInt(3) * 0.1f;

            // FLOOR 3 change between time change direction
            if(tileMap.getFloor() >= 2 && System.currentTimeMillis() - directionChangeCooldown - InGame.deltaPauseTime() > 7500){
                directionChangeCooldown = System.currentTimeMillis()- InGame.deltaPauseTime();
                invertDirection = !invertDirection;
            }
            if(invertDirection){
                angle -= 7;
            } else {
                angle += 7;
            }
            if(angle >= 360){
                angle-=360;
            }
            if(angle < 0){
                angle+=360;
            }
            // spin bullets
            KingSlimebullet slimebullet = new KingSlimebullet(
                    tileMap,
                    Math.sin(Math.toRadians(angle)),
                    Math.sin(Math.toRadians(angle-90)),
                    offset+0.05 *(1-Random.nextInt(2)*2)
            );
            slimebullet.setPosition(position.x, position.y);
            bullets.add(slimebullet);

            Network.AddEnemyProjectile addEnemyProjectile = new Network.AddEnemyProjectile();
            mpManager.server.requestACK(addEnemyProjectile,addEnemyProjectile.idPacket);
            addEnemyProjectile.idEnemy = id;
            addEnemyProjectile.id = slimebullet.id;
            addEnemyProjectile.x = (float)Math.sin(Math.toRadians(angle));
            addEnemyProjectile.y = (float)Math.sin(Math.toRadians(angle-90));
            addEnemyProjectile.inaccuracy = (float)(offset+0.05 *(1-Random.nextInt(2)*2));
            server.sendToAllUDP(addEnemyProjectile);

            slimebullet = new KingSlimebullet(
                    tileMap,
                    Math.sin(Math.toRadians(angle-180)),
                    Math.sin(Math.toRadians(angle-270)),
                    offset+0.05 *(1-Random.nextInt(2)*2)
            );
            slimebullet.setPosition(position.x, position.y);
            bullets.add(slimebullet);
            addEnemyProjectile = new Network.AddEnemyProjectile();
            mpManager.server.requestACK(addEnemyProjectile,addEnemyProjectile.idPacket);
            addEnemyProjectile.idEnemy = id;
            addEnemyProjectile.id = slimebullet.id;
            addEnemyProjectile.x = (float)Math.sin(Math.toRadians(angle-180));
            addEnemyProjectile.y = (float)Math.sin(Math.toRadians(angle-270));
            addEnemyProjectile.inaccuracy = (float)(offset+0.05 *(1-Random.nextInt(2)*2));
            server.sendToAllUDP(addEnemyProjectile);

        }
        super.update();
        movePacket();
    }

    public void draw() {

        for(KingSlimebullet bullet : bullets){
            bullet.draw();
        }
        if(!disableDraw){
            super.draw();
        }


    }
    public void drawHud(){
        if(isDead()) return;
        healthBar.draw();
    }
    @Override
    public void hit(int damage) {
        super.hit(damage);
        if(isDead()){
            if(tileMap.isServerSide()){
                animation = new Animation(6);
                animation.setDelay(85);
            } else {
                animation.setFrames(spritesheet.getSprites(DEAD));
                animation.setDelay(85);
                AudioManager.playSoundtrack(Soundtrack.IDLE);
            }
            // deleting all projectiles after death
            for(KingSlimebullet slimebullet : bullets){
                slimebullet.setHit();
            }
        }
    }
    @Override
    public boolean shouldRemove(){
        return animation.hasPlayedOnce() && isDead() && bullets.size()==0;
    }

    @Override
    public void drawShadow() {
        if(!disableDraw) drawShadow(11f);
    }

    @Override
    public void handleAddEnemyProjectile(Network.AddEnemyProjectile o) {
        if(isDead()) return;
        KingSlimebullet slimebullet = new KingSlimebullet(tileMap,o.x,o.y,o.inaccuracy);
        slimebullet.setPosition(position.x,position.y);
        slimebullet.setId(o.id);
        bullets.add(slimebullet);
    }

    @Override
    public void handleMoveEnemyProjectile(Network.MoveEnemyProjectile o) {
        for(KingSlimebullet bullet : bullets){
            if(bullet.getId() == o.id){
                bullet.setPosition(o.x,o.y);
                break;
            }

        }
    }
    @Override
    public void handleMoveEnemyProjectile(Network.MoveEnemyProjectileInstanced o) {
        for(int i = 0;i<o.id.length;i++){
            for(KingSlimebullet bullet : bullets){
                if(bullet.getId() == o.id[i]){
                    bullet.setPosition(o.x[i],o.y[i]);
                    break;
                }
            }
        }
    }
    @Override
    public void handleHitEnemyProjectile(Network.HitEnemyProjectile hitPacket) {
        for(KingSlimebullet bullet : bullets){
            if(bullet.getId() == hitPacket.id){
                bullet.setHit();
                if (hitPacket.idHit != -1) {
                    ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
                    A: for (ArrayList<RoomObject> objects : objectsArray) {
                        if (objects == null) continue;
                        for (RoomObject roomObject : objects) {
                            if (roomObject.getId() == hitPacket.idHit) {
                                ((DestroyableObject) roomObject).setHit(1);
                                break A;
                            }
                        }
                    }
                }
                break;
            }
        }
    }
    @Override
    public void handleHitEnemyProjectile(Network.HitEnemyProjectileInstanced hitPacket) {
        for(int i = 0;i<hitPacket.id.length;i++){
            for(KingSlimebullet bullet : bullets){
                if(bullet.getId() == hitPacket.id[i]){
                    bullet.setHit();
                    if (hitPacket.idHit[i] != -1) {
                        ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
                        A: for (ArrayList<RoomObject> objects : objectsArray) {
                            if (objects == null) continue;
                            for (RoomObject roomObject : objects) {
                                if (roomObject.getId() == hitPacket.idHit[i]) {
                                    ((DestroyableObject) roomObject).setHit(1);
                                    break A;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
    public void forceRemove(){
        for(KingSlimebullet b : bullets){
            b.forceRemove();
        }
    }
}

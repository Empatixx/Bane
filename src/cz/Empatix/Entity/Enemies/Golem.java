package cz.Empatix.Entity.Enemies;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PacketHolder;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Hud.HealthBar;
import cz.Empatix.Render.RoomObjects.Chest;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;

public class Golem extends Enemy {

    private static final int IDLE = 0;
    private static final int ENERGY = 1;
    private static final int ARM_PROJECTILE = 2;
    private static final int ARMORING = 3;
    private static final int ATTACK = 4;
    private static final int EYE_BEAM = 5;
    private static final int BARRIER = 6;
    private static final int DEAD = 7;

    private boolean chestCreated;

    private LaserBeam laserBeam;

    private HealthBar healthBar;

    private long shieldCooldown;
    private boolean speedBuff;
    private long speedCooldown;
    private long beamCooldown;
    private long armoringTime;
    private long lastRegen;

    private int shieldStacks;

    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Enemies\\golem.png");
        Loader.loadImage("Textures\\Sprites\\Enemies\\laserbeam.tga");
    }
    public Golem(TileMap tm, Player player) {
        super(tm,player);

        moveSpeed = 0.7f;
        maxSpeed = 5.2f;
        stopSpeed = 0.75f;

        width = 100;
        height = 100;
        cwidth = 50;
        cheight = 40;
        scale = 6;

        health = maxHealth = (int)(130*(1+(Math.pow(tm.getFloor(),1.25)*0.12)));
        damage = 2;

        type = melee;
        facingRight = true;

        spriteSheetCols = 14;
        spriteSheetRows = 8;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\golem.png");

        // creating a new spritesheet

        int[] numSprites = new int[]{4,8,9,8,7,7,10,14};
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\golem.png");
            for(int j = 0;j<numSprites.length;j++) {
                Sprite[] sprites = new Sprite[numSprites[j]];
                for (int i = 0; i < numSprites[j]; i++) {
                    Sprite sprite = new Sprite(new float[]
                            {(float) i / spriteSheetCols, (float)j/spriteSheetRows,

                                    (float) i / spriteSheetCols, (1.0f+j) / spriteSheetRows,

                                    (1.0f + i) / spriteSheetCols, (1.0f+j) / spriteSheetRows,

                                    (1.0f + i) / spriteSheetCols, (float)j/spriteSheetRows}
                    );
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);
            }
        }
        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(IDLE));
        animation.setDelay(145);
        currentAction = IDLE;

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 2x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        chestCreated=false;

        healthBar = new HealthBar("Textures\\bosshealthbar",new Vector3f(960,1000,0),7,56,4);
        healthBar.setOffsetsBar(14,1);
        healthBar.initHealth(health,maxHealth);

        createShadow();

        speedBuff = false;
        speedCooldown = System.currentTimeMillis() - InGame.deltaPauseTime() - 3000;
        shieldCooldown = System.currentTimeMillis() - InGame.deltaPauseTime() - 3000;
        beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
        lastRegen = System.currentTimeMillis() - InGame.deltaPauseTime();
        laserBeam = new LaserBeam(tm,this.player);

        shieldStacks = 0;

    }

    // multiplayer
    public Golem(TileMap tm, Player[] player) {
        super(tm,player);
        if(tm.isServerSide()){
            moveSpeed = 0.7f;
            maxSpeed = 5.2f;
            stopSpeed = 0.75f;

            width = 100;
            height = 100;
            cwidth = 50;
            cheight = 40;
            scale = 6;

            health = maxHealth = (int)(130*(1+(Math.pow(tm.getFloor(),1.25)*0.12)));
            damage = 2;

            type = melee;
            facingRight = true;

            currentAction = IDLE;

            animation = new Animation(4);
            animation.setDelay(145);

            // because of scaling image by 2x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            chestCreated=false;

            speedBuff = false;
            speedCooldown = System.currentTimeMillis() - InGame.deltaPauseTime() - 3000;
            shieldCooldown = System.currentTimeMillis() - InGame.deltaPauseTime() - 3000;
            beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
            lastRegen = System.currentTimeMillis() - InGame.deltaPauseTime();
            // MULTIPLAYER SUPPORT
            laserBeam = new LaserBeam(tm,player);
            laserBeam.setId(id); // setting id so id will be same as enemy

            shieldStacks = 0;
        } else {
            moveSpeed = 0.7f;
            maxSpeed = 5.2f;
            stopSpeed = 0.75f;

            width = 100;
            height = 100;
            cwidth = 50;
            cheight = 40;
            scale = 6;

            health = maxHealth = (int)(130*(1+(Math.pow(tm.getFloor(),1.25)*0.12)));
            damage = 2;

            type = melee;
            facingRight = true;

            spriteSheetCols = 14;
            spriteSheetRows = 8;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\golem.png");

            // creating a new spritesheet

            int[] numSprites = new int[]{4,8,9,8,7,7,10,14};
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\golem.png");
                for(int j = 0;j<numSprites.length;j++) {
                    Sprite[] sprites = new Sprite[numSprites[j]];
                    for (int i = 0; i < numSprites[j]; i++) {
                        Sprite sprite = new Sprite(new float[]
                                {(float) i / spriteSheetCols, (float)j/spriteSheetRows,

                                        (float) i / spriteSheetCols, (1.0f+j) / spriteSheetRows,

                                        (1.0f + i) / spriteSheetCols, (1.0f+j) / spriteSheetRows,

                                        (1.0f + i) / spriteSheetCols, (float)j/spriteSheetRows}
                        );
                        sprites[i] = sprite;

                    }
                    spritesheet.addSprites(sprites);
                }
            }
            vboVertices = ModelManager.getModel(width,height);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,height);
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(IDLE));
            animation.setDelay(145);
            currentAction = IDLE;

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 2x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            chestCreated=false;

            healthBar = new HealthBar("Textures\\bosshealthbar",new Vector3f(960,1000,0),7,56,4);
            healthBar.setOffsetsBar(14,1);
            healthBar.initHealth(health,maxHealth);

            createShadow();

            speedBuff = false;
            speedCooldown = System.currentTimeMillis() - InGame.deltaPauseTime() - 3000;
            shieldCooldown = System.currentTimeMillis() - InGame.deltaPauseTime() - 3000;
            beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
            lastRegen = System.currentTimeMillis() - InGame.deltaPauseTime();
            laserBeam = new LaserBeam(tm,this.player);

            shieldStacks = 0;
        }

    }

    @Override
    public void setId(int id) {
        super.setId(id);
        laserBeam.setId(id);
    }

    @Override
    public void update() {
        setMapPosition();
        if(!tileMap.isServerSide())healthBar.update(health,maxHealth);
        if(isSpawning()) return;
        // update animation
        animation.update();
        if(isDead() && animation.isPlayingLastFrame() && !itemDropped){
            if(!chestCreated && (tileMap.isServerSide() || !MultiplayerManager.multiplayer)){
                chestCreated=true;

                Chest chest = new Chest(tileMap);
                chest.setPosition(position.x,position.y);
                chest.enableDropArtefact();
                tileMap.addObject(chest,tileMap.getRoomByCoords(position.x,position.y).getId());

                tileMap.addLadder();
            }
        }
        if(dead) return;

        // ENEMY AI
        EnemyAI();
        // preventing changing facingRight because of the closest player if boss if doing beams

        int meleePlayer = theClosestPlayerIndex();
        int beamPlayer = theFarthestPlayerIndex();

        if(!MultiplayerManager.multiplayer || tileMap.isServerSide()){
            if(     Math.abs(position.x - player[meleePlayer].getX()) < 255
                    && position.y - player[meleePlayer].getY() < 10 && position.y - player[meleePlayer].getY() > -230
                    && currentAction == IDLE && !speedBuff && !player[meleePlayer].isDead()){
                currentAction = ATTACK;
                if(!MultiplayerManager.multiplayer){
                    animation.setFrames(spritesheet.getSprites(ATTACK));
                    animation.setDelay(130);
                } else if (tileMap.isServerSide()){
                    animation = new Animation(7);
                    animation.setDelay(130);
                }
            } else if(beamPlayer != -1 && System.currentTimeMillis() - beamCooldown - InGame.deltaPauseTime() > 6500 && position.distance(player[beamPlayer].getPosition()) > 550 && currentAction == IDLE){
                currentAction = EYE_BEAM;
                beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
                if(!MultiplayerManager.multiplayer){
                    animation.setFrames(spritesheet.getSprites(EYE_BEAM));
                    animation.setDelay(185);
                } else if (tileMap.isServerSide()){
                    animation = new Animation(7);
                    animation.setDelay(185);
                }
                laserBeam.setLastPlayerTargetIndex(beamPlayer);
                laserBeam.setPosition(position.x,position.y-height/scale-5);
            } else if (System.currentTimeMillis() - speedCooldown - InGame.deltaPauseTime() > 8000 && currentAction == IDLE){
                speedCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
                speedBuff = true;
                maxSpeed = 15f;
                currentAction = ENERGY;
                if(!MultiplayerManager.multiplayer){
                    animation.setFrames(spritesheet.getSprites(ENERGY));
                    animation.setDelay(85);
                } else if (tileMap.isServerSide()){
                    animation = new Animation(8);
                    animation.setDelay(85);
                }
            } else if (System.currentTimeMillis() - shieldCooldown - InGame.deltaPauseTime() > 7000 && currentAction == IDLE){
                shieldCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
                currentAction = BARRIER;
                if(!MultiplayerManager.multiplayer){
                    animation.setFrames(spritesheet.getSprites(BARRIER));
                    animation.setDelay(125);
                } else if (tileMap.isServerSide()){
                    animation = new Animation(10);
                    animation.setDelay(125);
                }
                shieldStacks++;
            } else if (shieldStacks >= 3 && currentAction == IDLE){
                shieldStacks = 0;
                armoringTime = System.currentTimeMillis() - InGame.deltaPauseTime();
                currentAction = ARMORING;
                reflectBullets = true;
                if(!MultiplayerManager.multiplayer){
                    animation.setFrames(spritesheet.getSprites(ARMORING));
                    animation.setDelay(125);
                } else if (tileMap.isServerSide()){
                    animation = new Animation(8);
                    animation.setDelay(85);
                }
            } else if(currentAction != IDLE && animation.hasPlayedOnce()){
                if(currentAction == ENERGY){
                    currentAction = ATTACK;
                    if(!MultiplayerManager.multiplayer){
                        animation.setFrames(spritesheet.getSprites(ATTACK));
                        animation.setDelay(90);
                    } else if (tileMap.isServerSide()){
                        animation = new Animation(7);
                        animation.setDelay(85);
                    }
                } else {
                    currentAction = IDLE;
                    if(!MultiplayerManager.multiplayer){
                        animation.setFrames(spritesheet.getSprites(IDLE));
                        animation.setDelay(225);
                    } else if (tileMap.isServerSide()){
                        animation = new Animation(4);
                        animation.setDelay(225);
                    }
                }
            }
            if(currentAction == EYE_BEAM){
                if (player[laserBeam.getLastPlayerTargetIndex()].getX() > position.x) facingRight = true;
                else if (player[laserBeam.getLastPlayerTargetIndex()].getX() < position.x) facingRight = false;
            }
            if(facingRight != laserBeam.isFacingRight()){
                laserBeam.setPosition(position.x,position.y-height/scale-5);
                laserBeam.setFacingRight(facingRight);
            }
            if(tileMap.isServerSide()){
                Network.EnemySync golemAnimSync = new Network.EnemySync();
                golemAnimSync.id = id;
                golemAnimSync.currAction = (byte)currentAction;
                golemAnimSync.sprite = (byte)animation.getIndexOfFrame();
                golemAnimSync.time = animation.getTime();

                Server server = MultiplayerManager.getInstance().server.getServer();
                server.sendToAllUDP(golemAnimSync);
            }
            if(currentAction == ATTACK){
                right = false;
                left = false;
                up = false;
                down = false;
                for(Player p : player){
                    if(p != null){
                        if (Math.abs(position.x - p.getX()) < 275
                                && position.y - p.getY() < 20 && position.y - p.getY() > -250
                                && animation.isPlayingLastFrame()){
                            p.hit(4);
                        }
                    }
                }
            } else if(currentAction == EYE_BEAM){
                right = false;
                left = false;
                up = false;
                down = false;
                laserBeam.update();
            } else if(currentAction == ARMORING){
                if(System.currentTimeMillis() - lastRegen - InGame.deltaPauseTime() > 600){
                    lastRegen = System.currentTimeMillis()- InGame.deltaPauseTime();
                    int regen = (int)Math.ceil(maxHealth * 0.01 *(1-(float)health/maxHealth));
                    heal(regen);

                }
                right = false;
                left = false;
                up = false;
                down = false;
                if(animation.isPlayingLastFrame()){
                    animation.setDelay(-1);
                    if(System.currentTimeMillis() - armoringTime - InGame.deltaPauseTime() > 8000){
                        animation.setDelay(225);
                        reflectBullets = false;
                    }
                }
            } else if(speedBuff && System.currentTimeMillis() - speedCooldown - InGame.deltaPauseTime() > 1000){
                speedBuff = false;
                maxSpeed = 5.2f;
            }
            getNextPosition();
            checkTileMapCollision();
            setPosition(temp.x, temp.y);
        } else {
            if(currentAction == EYE_BEAM) {
                right = false;
                left = false;
                up = false;
                down = false;
                if (player[laserBeam.getLastPlayerTargetIndex()].getX() > position.x) facingRight = true;
                else if (player[laserBeam.getLastPlayerTargetIndex()].getX() < position.x) facingRight = false;
                laserBeam.update();
            }
        }
        movePacket();
    }

    public void draw() {
        super.draw();
        if(currentAction == EYE_BEAM && !isDead()){
            laserBeam.draw();
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
            currentAction = DEAD;
            if(tileMap.isServerSide()){
                animation = new Animation(14);
                animation.setDelay(65);
            } else {
                animation.setFrames(spritesheet.getSprites(DEAD));
                animation.setDelay(65);
                AudioManager.playSoundtrack(Soundtrack.IDLE);
            }
        }
    }
    @Override
    public boolean shouldRemove(){
        return animation.hasPlayedOnce() && isDead();
    }

    @Override
    public void drawShadow() {
        drawShadow(11f,65);
    }


    private static class LaserBeam extends MapObject {
        private Vector3f originalPos;
        double angle;
        private Player[] player;

        private long lastTimeBeamSync = -1;
        private int lastPlayerTargetIndex;

        LaserBeam(TileMap tm, Player[] p){
            super(tm);
            if(tm.isServerSide()){
                this.player = p;
                width = 810;
                height = 45;
                cwidth = 810;
                cheight = 20;
                scale = 4;
                facingRight = true;

                spriteSheetCols = 1;
                spriteSheetRows = 14;
                // creating a new spritesheet
                animation = new Animation(14);
                animation.setDelay(95);

                // because of scaling image by 2x
                width *= scale;
                height *= scale;
                cwidth *= scale;
                cheight *= scale;
            } else {
                this.player = p;
                width = 810;
                height = 45;
                cwidth = 810;
                cheight = 20;
                scale = 4;
                facingRight = true;

                spriteSheetCols = 1;
                spriteSheetRows = 14;

                // try to find spritesheet if it was created once
                spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\laserbeam.tga");

                // creating a new spritesheet
                if (spritesheet == null){
                    spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\laserbeam.tga");
                    Sprite[] sprites = new Sprite[spriteSheetRows];
                    for (int j = 0; j < spriteSheetRows; j++) {
                        Sprite sprite = new Sprite(new float[]
                                {
                                        0f, (float)j/spriteSheetRows,

                                        0f, (1.0f+j) / spriteSheetRows,

                                        1f, (1.0f+j) / spriteSheetRows,

                                        1f, (float)j/spriteSheetRows
                                }
                        );
                        sprites[j] = sprite;

                    }
                    spritesheet.addSprites(sprites);
                }
                vboVertices = ModelManager.getModel(width,height);
                if (vboVertices == -1){
                    vboVertices = ModelManager.createModel(width,height);
                }

                animation = new Animation();
                animation.setFrames(spritesheet.getSprites(0));
                animation.setDelay(95);

                shader = ShaderManager.getShader("shaders\\rotation");
                if (shader == null){
                    shader = ShaderManager.createShader("shaders\\rotation");
                }
                // because of scaling image by 2x
                width *= scale;
                height *= scale;
                cwidth *= scale;
                cheight *= scale;
            }
        }

        public int getLastPlayerTargetIndex() {
            return lastPlayerTargetIndex;
        }

        public void setFacingRight(boolean b){
            facingRight = b;
        }
        public boolean isFacingRight(){return facingRight;}
        public void update(){
            animation.update();
            setMapPosition();
            if(tileMap.isServerSide() || !MultiplayerManager.multiplayer){
                float y = player[lastPlayerTargetIndex].getY() - originalPos.y;
                float x = player[lastPlayerTargetIndex].getX() - originalPos.x;
                float angle = (float)Math.atan(y/x);
                if(!facingRight){
                    angle+=Math.PI;
                }
                boolean reverseDir = false;
                if(Math.PI*2-Math.abs(this.angle - angle) < Math.abs(this.angle - angle)){
                    reverseDir = true;
                }
                if(!reverseDir){
                    this.angle += (angle - this.angle) * .035;
                } else {
                    if(this.angle >= 0){
                        this.angle += ((Math.PI*3/2. - this.angle)+(Math.PI/2.+angle)) * .035;
                        if(this.angle >= Math.PI*3/2.){
                            this.angle-=Math.PI*3/2.;
                            this.angle=-Math.PI/2. - this.angle;
                        }
                    } else {
                        this.angle -= ((Math.PI*3/2. - angle)+(Math.PI/2.+this.angle)) * .035;
                        if(this.angle <= -Math.PI/2.){
                            this.angle+=Math.PI/2;
                            this.angle=Math.PI*3/2.-this.angle;
                        }
                    }

                }

                position.x = originalPos.x + (width/2-65) * (float)Math.cos(this.angle);
                position.y = originalPos.y + (width/2-65) * (float)Math.sin(this.angle);
                for(Player p : player){
                    if(p != null){
                        if(intersects(p) && canHit()){
                            p.hit(1);
                        }
                    }
                }
                ArrayList<RoomObject>[] objectsArray = tileMap.getRoomMapObjects();
                for(ArrayList<RoomObject> objects : objectsArray) {
                    if (objects == null) continue;
                    for (RoomObject object : objects) {
                        if (object instanceof DestroyableObject) {
                            if (intersects(object) && !((DestroyableObject) object).isDestroyed()) {
                                ((DestroyableObject) object).setHit(1);
                                if(tileMap.isServerSide()){
                                    MultiplayerManager mpManager = MultiplayerManager.getInstance();
                                    Network.LaserBeamHit laserBeamHit = new Network.LaserBeamHit();
                                    mpManager.server.requestACK(laserBeamHit,laserBeamHit.idPacket);
                                    laserBeamHit.idHit = object.id;
                                    Server server = mpManager.server.getServer();
                                    server.sendToAllUDP(laserBeamHit);
                                }
                            }
                        }
                    }
                }
                if(tileMap.isServerSide()){
                    Network.LaserBeamSync LBSync = new Network.LaserBeamSync();
                    LBSync.angle = this.angle;
                    LBSync.id = id;
                    LBSync.sprite = (byte)animation.getIndexOfFrame();
                    LBSync.x = position.x;
                    LBSync.y = position.y;
                    LBSync.time = animation.getTime();
                    LBSync.lastTarget = ((PlayerMP)player[lastPlayerTargetIndex]).getIdConnection();
                    Server server = MultiplayerManager.getInstance().server.getServer();
                    server.sendToAllUDP(LBSync);
                }
            } else {
                Object[] objects = MultiplayerManager.getInstance().packetHolder.getWithoutClear(PacketHolder.LASERBEAMSYNC);
                Network.LaserBeamSync theRecent = null;
                for(Object o : objects){
                    Network.LaserBeamSync sync = (Network.LaserBeamSync) o;
                    if(sync.id == id){
                        if(theRecent == null) theRecent = sync;
                        else if (theRecent.idPacket < sync.idPacket){
                            theRecent = sync;
                        }
                    }
                }
                if(theRecent != null)handleSync(theRecent);
            }
        }
        public void handleSync(Network.LaserBeamSync sync){
            MultiplayerManager.getInstance().packetHolder.remove(PacketHolder.LASERBEAMSYNC,sync);
            if(lastTimeBeamSync < sync.idPacket) {
                angle = sync.angle;
                animation.setFrame(sync.sprite);
                animation.setTime(sync.time);
                position.x = sync.x;
                position.y = sync.y;
                lastTimeBeamSync = sync.idPacket;
                int index = 0;
                for(Player p : player){
                    if(p != null){
                        if(((PlayerMP)p).getIdConnection() == sync.lastTarget){
                            lastPlayerTargetIndex = index;
                        }
                    }
                    index++;
                }
            }
        }
        @Override
        public void draw() {
            Matrix4f target;
            target = new Matrix4f().translate(position)
                        .scale(scale)
                        .rotate((float)angle,new Vector3f(0,0,1));

            Camera.getInstance().projection().mul(target,target);

            shader.bind();
            shader.setUniformi("sampler",0);
            glActiveTexture(GL_TEXTURE0);
            shader.setUniformm4f("projection",target);

            spritesheet.bindTexture();

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);


            glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
            glVertexAttribPointer(0,2,GL_INT,false,0,0);


            glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());
            glVertexAttribPointer(1,2,GL_FLOAT,false,0,0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER,0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            shader.unbind();
            glBindTexture(GL_TEXTURE_2D,0);
            glActiveTexture(GL_TEXTURE0);

            if (Game.displayCollisions){
                glColor3i(255,255,255);
                glBegin(GL_LINE_LOOP);
                Vector2f point = new Vector2f();
                // BOTTOM LEFT
                point.set(position.x+xmap-cwidth/2,position.y+ymap-cheight/2);
                rotatePoint(point,angle);
                glVertex2f(point.x,point.y);
                // TOP LEFT
                point.set(position.x+xmap-cwidth/2,position.y+ymap+cheight/2);
                rotatePoint(point,angle);
                glVertex2f(point.x, point.y);
                // BOTTOM RIGHT
                point.set(position.x+xmap+cwidth/2,position.y+ymap+cheight/2);
                rotatePoint(point,angle);
                glVertex2f(point.x, point.y);
                // TOP RIGHT
                point.set(position.x+xmap+cwidth/2,position.y+ymap-cheight/2);
                rotatePoint(point,angle);
                glVertex2f(point.x, point.y);
                glEnd();

                glPointSize(10);
                glColor3i(255,0,0);
                glBegin(GL_POINTS);
                glVertex2f(position.x+xmap,position.y+ymap);
                glEnd();
            }
        }

        /**
         *
         * @param point - coords of a corner point of the square
         * @param angle - angle of rotation
         */
        public void rotatePoint(Vector2f point, double angle){

            // translate point to origin
            float tempX = point.x - (position.x+xmap);
            float tempY = point.y - (position.y+ymap);

            // now apply angle
            float rotatedX = tempX*(float)Math.cos(angle) - tempY*(float)Math.sin(angle);
            float rotatedY = tempX*(float)Math.sin(angle) + tempY*(float)Math.cos(angle);

            // translate back
            point.x = rotatedX + (position.x+xmap);
            point.y = rotatedY + (position.y+ymap);
        }

        @Override
        public void setPosition(float x, float y) {
            super.setPosition(x, y);
            originalPos = new Vector3f(x,y,0);
        }

        public void setLastPlayerTargetIndex(int lastPlayerTargetIndex) {
            this.lastPlayerTargetIndex = lastPlayerTargetIndex;
        }

        public boolean canHit(){
            return animation.getIndexOfFrame() >= 8;
        }
        @Override
        public boolean intersects(MapObject o) {
            Vector2f[] pointsBox1 = new Vector2f[4];
            Vector2f[] pointsBox2 = new Vector2f[4];

            Vector2f[] axis1 = new Vector2f[2];
            Vector2f[] axis2 = new Vector2f[2];

            for (int i = 0; i < 4; i++) {
                if(i<2){
                    axis1[i] = new Vector2f();
                    axis2[i] = new Vector2f();
                }
                pointsBox1[i] = new Vector2f();
                pointsBox2[i] = new Vector2f();
            }

            pointsBox1[0].set(position.x+xmap - cwidth / 2,position.y+ymap + cheight / 2);
            pointsBox1[1].set(position.x+xmap - cwidth / 2,position.y+ymap - cheight / 2);
            pointsBox1[2].set(position.x+xmap + cwidth / 2,position.y+ymap - cheight / 2);
            pointsBox1[3].set(position.x+xmap + cwidth / 2,position.y+ymap + cheight / 2);

            for (int i = 0; i < 4; i++) {
                rotatePoint(pointsBox1[i], angle);
            }

            int cwidth = o.getCwidth();
            int cheight = o.getCheight();
            Vector3f position = o.getPosition();
            pointsBox2[0].set(position.x+xmap - cwidth / 2,position.y+ymap + cheight / 2);
            pointsBox2[1].set(position.x+xmap- cwidth / 2,position.y+ymap - cheight / 2);
            pointsBox2[2].set(position.x+xmap + cwidth / 2,position.y+ymap - cheight / 2);
            pointsBox2[3].set(position.x+xmap + cwidth / 2,position.y+ymap + cheight / 2);

            axis1[0] = pointsBox1[2].sub(pointsBox1[3],axis1[0]);
            axis1[0].perpendicular();
            axis1[1] = pointsBox1[3].sub(pointsBox1[0],axis1[1]);
            axis1[1].perpendicular();

            axis2[0] = pointsBox2[2].sub(pointsBox2[3],axis2[0]);
            axis2[0].perpendicular();
            axis2[1] = pointsBox2[3].sub(pointsBox2[0],axis2[1]);
            axis2[1].perpendicular();

            float[] min1 = new float[2], min2 = new float[2];
            float[] max1 = new float[2], max2 = new float[2];
            for(int i = 0;i<2;i++){
                min1[i] = axis2[i].dot(pointsBox1[0]);
                max1[i] = axis2[i].dot(pointsBox1[1]);
                for(int j = 0;j<4;j++){
                    float dot = axis2[i].dot(pointsBox1[j]);
                    if(dot < min1[i]){
                        min1[i] = dot;
                    }
                    if(dot > max1[i]){
                        max1[i] = dot;
                    }
                }
            }
            for(int i = 0;i<2;i++){
                min2[i] = axis2[i].dot(pointsBox2[0]);
                max2[i] = axis2[i].dot(pointsBox2[1]);
                for(int j = 0;j<4;j++){
                    float dot = axis2[i].dot(pointsBox2[j]);
                    if(dot < min2[i]){
                        min2[i] = dot;
                    }
                    if(dot > max2[i]){
                        max2[i] = dot;
                    }
                }
            }
            for(int i = 0;i<2;i++){
                if (!((min1[i] < max2[i] && min1[i] > min2[i]) || (min2[i] < max1[i] && min2[i] > min1[i]))){
                    return false;
                }
            }

            for(int i = 0;i<2;i++){
                min1[i] = axis1[i].dot(pointsBox1[0]);
                max1[i] = axis1[i].dot(pointsBox1[1]);
                for(int j = 0;j<4;j++){
                    float dot = axis1[i].dot(pointsBox1[j]);
                    if(dot < min1[i]){
                        min1[i] = dot;
                    }
                    if(dot > max1[i]){
                        max1[i] = dot;
                    }
                }
            }
            for(int i = 0;i<2;i++){
                min2[i] = axis1[i].dot(pointsBox2[0]);
                max2[i] = axis1[i].dot(pointsBox2[1]);
                for(int j = 0;j<4;j++){
                    float dot = axis1[i].dot(pointsBox2[j]);
                    if(dot < min2[i]){
                        min2[i] = dot;
                    }
                    if(dot > max2[i]){
                        max2[i] = dot;
                    }
                }
            }
            for(int i = 0;i<2;i++){
                if (!((min1[i] < max2[i] && min1[i] > min2[i]) || (min2[i] < max1[i] && min2[i] > min1[i]))){
                    return false;
                }
            }

            return true;
        }
        public int theFarthestPlayerIndex(){
            int theClosest = 0;
            float dist = originalPos.distance(player[0].getPosition());
            for(int i = 1;i<player.length;i++){
                if(player[i] == null) continue;
                if(player[i].isDead()) continue;
                float newDist = originalPos.distance(player[i].getPosition());
                if(dist < newDist){
                    theClosest = i;
                    dist = newDist;
                }
            }
            return theClosest;
        }
    }
    @Override
    public void handleAddEnemyProjectile(Network.AddEnemyProjectile o) {
    }

    @Override
    public void handleMoveEnemyProjectile(Network.MoveEnemyProjectile o) {
    }
    @Override
    public void handleMoveEnemyProjectile(Network.MoveEnemyProjectileInstanced o) {
    }
    @Override
    public void handleHitEnemyProjectile(Network.HitEnemyProjectile hitPacket) {

    }
    @Override
    public void handleHitEnemyProjectile(Network.HitEnemyProjectileInstanced hitPacket) {

    }
    public void forceRemove(){
    }
}

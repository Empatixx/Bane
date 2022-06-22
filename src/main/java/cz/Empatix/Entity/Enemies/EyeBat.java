package cz.Empatix.Entity.Enemies;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.RoomObjects.DestroyableObject;
import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Main.Game;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PacketHolder;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Room;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class EyeBat extends Enemy {
    private static final int IDLE = 0;
    private static final int BEAM = 2;
    private static final int DEAD = 1;


    private long beamCooldown;

    private LaserBeam laserBeam;

    public EyeBat(TileMap tm, Player player) {
        super(tm,player);
        initStats(tm.getFloor());

        width = 80;
        height = 80;
        cwidth = 80;
        cheight = 80;
        scale = 2;

        animation = new Animation(5);
        animation.setDelay(125);

        facingRight = true;

        spriteSheetCols = 5;
        spriteSheetRows = 1;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\eyebat.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\eyebat.tga");
            Sprite[] sprites = new Sprite[5];
            for(int i = 0; i < sprites.length; i++) {
                //Sprite sprite = new Sprite(texCoords);
                Sprite sprite = new Sprite(5,i,0,width,height,spriteSheetRows,spriteSheetCols);
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
        animation.setDelay(125);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 2x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        createShadow();

        currentAction = IDLE;

        laserBeam = new LaserBeam(tm,this.player);
        beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
    }

    public EyeBat(TileMap tm, Player[] player) {
        super(tm,player);
        initStats(tm.getFloor());
        if(tm.isServerSide()){
            width = 80;
            height = 80;
            cwidth = 80;
            cheight = 80;
            scale = 2;

            facingRight = true;

            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            createShadow();

            currentAction = IDLE;

            animation = new Animation(5);
            animation.setDelay(125);

            laserBeam = new LaserBeam(tm,this.player);
            laserBeam.setId(id); // setting id so id will be same as enemy
            beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
        } else {
            width = 80;
            height = 80;
            cwidth = 80;
            cheight = 80;
            scale = 2;

            facingRight = true;

            spriteSheetCols = 5;
            spriteSheetRows = 1;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\eyebat.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\eyebat.tga");
                Sprite[] sprites = new Sprite[5];
                for(int i = 0; i < sprites.length; i++) {
                    //Sprite sprite = new Sprite(texCoords);
                    Sprite sprite = new Sprite(5,i,0,width,height,spriteSheetRows,spriteSheetCols);
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
            animation.setDelay(125);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image by 2x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            createShadow();

            currentAction = IDLE;

            laserBeam = new LaserBeam(tm,this.player);
            beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
        }
    }
    public void initStats(int floor){
        movementVelocity =  510;
        moveAcceleration = 6f;
        stopAcceleration = 5f;

        health = maxHealth = (int)(11*(1+(Math.pow(floor,1.25)*0.12)));
        tryBoostHealth();
        damage = 1;

        type = melee;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        laserBeam.setId(id);
    }
    @Override
    public void update() {
        setMapPosition();
        if(isSpawning()) return;
        // update animation
        animation.update();

        if(dead) return;

        tryEnrage();
        tryRegen();
        // ENEMY AI
        EnemyAI();

        int indexPlayer = theFarthestPlayerIndex();
        if(!MultiplayerManager.multiplayer || tileMap.isServerSide()){
            if(currentAction == IDLE && indexPlayer != -1 && System.currentTimeMillis() - InGame.deltaPauseTime() - beamCooldown > 5000 && position.distance(player[indexPlayer].getPosition()) > 550
                    && position.distance(player[indexPlayer].getPosition()) < 1500){
                laserBeam.setLastPlayerTargetIndex(indexPlayer);
                if(canShot()){
                    currentAction = BEAM;
                    if(facingRight){
                        laserBeam.setPosition(position.x,position.y);
                    } else {
                        laserBeam.setPosition(position.x,position.y+20);
                    }
                    laserBeam.resetAnimation();
                    beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
                }
            } else if (currentAction == BEAM && (laserBeam.hasEnded() || !canShot())){
                currentAction = IDLE;
            }
            if(currentAction == BEAM){
                if (player[laserBeam.getLastPlayerTargetIndex()].getX() > position.x) facingRight = true;
                else if (player[laserBeam.getLastPlayerTargetIndex()].getX() < position.x) facingRight = false;
            }
            if(facingRight != laserBeam.isFacingRight()){
                laserBeam.setPosition(position.x,position.y);
                laserBeam.setFacingRight(facingRight);
            }
            if(tileMap.isServerSide()){
                Network.EnemySync golemAnimSync = new Network.EnemySync();
                golemAnimSync.id = id;
                golemAnimSync.currAction = (byte)currentAction;
                golemAnimSync.sprite =  (byte)animation.getIndexOfFrame();
                golemAnimSync.time = animation.getTime();

                Server server = MultiplayerManager.getInstance().server.getServer();
                server.sendToAllUDP(golemAnimSync);
            }
            if(currentAction == BEAM){
                right = false;
                left = false;
                up = false;
                down = false;
                laserBeam.update();
            }

            // update position
            getMovementSpeed();
            checkTileMapCollision();
            setPosition(temp.x, temp.y);
        } else if (MultiplayerManager.multiplayer){
            interpolator.update(position.x,position.y);
            if(currentAction == BEAM){
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

    @Override
    public void hit(int damage) {
        super.hit(damage);
    }

    @Override
    public void draw() {
        super.draw();
        if(currentAction == BEAM){
            laserBeam.draw();
        }
    }
    @Override
    public void drawShadow() {
        drawShadow(5.5f);
    }

    private static class LaserBeam extends MapObject {
        private Vector3f originalPos;
        double angle;
        private Player[] player;

        private long lastTimeBeamSync = -1;
        private int lastPlayerTargetIndex;

        LaserBeam(TileMap tm, Player[] p) {
            super(tm);
            if(tm.isServerSide()){
                this.player = p;
                width = 810;
                height = 45;
                cwidth = 810;
                cheight = 20;
                scale = 3;
                facingRight = true;

                spriteSheetCols = 1;
                spriteSheetRows = 14;

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
                scale = 3;
                facingRight = true;

                spriteSheetCols = 1;
                spriteSheetRows = 14;

                // try to find spritesheet if it was created once
                spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\laserbeam-eyebat.tga");

                // creating a new spritesheet
                if (spritesheet == null){
                    spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\laserbeam-eyebat.tga");
                    Sprite[] sprites = new Sprite[spriteSheetRows];
                    for (int j = 0; j < spriteSheetRows; j++) {
                        Sprite sprite = new Sprite(new float[]
                                {
                                        0f, (float) j / spriteSheetRows,

                                        0f, (1.0f + j) / spriteSheetRows,

                                        1f, (1.0f + j) / spriteSheetRows,

                                        1f, (float) j / spriteSheetRows
                                }
                        );
                        sprites[j] = sprite;

                    }
                    spritesheet.addSprites(sprites);
                }
                vboVertices = ModelManager.getModel(width, height);
                if (vboVertices == -1){
                    vboVertices = ModelManager.createModel(width, height);
                }

                animation = new Animation();
                animation.setFrames(spritesheet.getSprites(0));
                animation.setDelay(95);

                shader = ShaderManager.getShader("shaders\\shader");
                if (shader == null){
                    shader = ShaderManager.createShader("shaders\\shader");
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
        public void setLastPlayerTargetIndex(int lastPlayerTargetIndex) {
            this.lastPlayerTargetIndex = lastPlayerTargetIndex;
        }

        boolean hasEnded(){return animation.hasPlayedOnce();}

        void resetAnimation(){
            if(!tileMap.isServerSide()) {
                animation.setFrames(spritesheet.getSprites(0));
            } else {
                animation = new Animation(14);
                animation.setDelay(125);
            }
        }

        boolean isFacingRight(){return facingRight;}

        public void update() {
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
                    this.angle += (angle - this.angle) * Game.deltaTime * 1.5;
                } else {
                    if(this.angle >= 0){
                        this.angle += ((Math.PI*3/2. - this.angle)+(Math.PI/2.+angle)) * Game.deltaTime * 1.5;
                        if(this.angle >= Math.PI*3/2.){
                            this.angle-=Math.PI*3/2.;
                            this.angle=-Math.PI/2. - this.angle;
                        }
                    } else {
                        this.angle -= ((Math.PI*3/2. - angle)+(Math.PI/2.+this.angle)) * Game.deltaTime * 1.5;
                        if(this.angle <= -Math.PI/2.){
                            this.angle+=Math.PI/2;
                            this.angle=Math.PI*3/2.-this.angle;
                        }
                    }

                }

                position.x = originalPos.x + (width/2-45) * (float)Math.cos(this.angle);
                position.y = originalPos.y + (width/2-45) * (float)Math.sin(this.angle);
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
                    LBSync.sprite =  (byte)animation.getIndexOfFrame();
                    LBSync.x = position.x;
                    LBSync.y = position.y;
                    LBSync.time = animation.getTime();
                    LBSync.lastTarget = lastPlayerTargetIndex;
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
        public void handleSync(Network.LaserBeamSync sync) {
            MultiplayerManager.getInstance().packetHolder.remove(PacketHolder.LASERBEAMSYNC,sync);
            if (lastTimeBeamSync < sync.idPacket){
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
                    .rotate((float) angle, new Vector3f(0, 0, 1));

            Camera.getInstance().projection().mul(target, target);

            shader.bind();
            shader.setUniformi("sampler", 0);
            glActiveTexture(GL_TEXTURE0);
            shader.setUniformm4f("projection", target);

            spritesheet.bindTexture();

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);


            glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);


            glBindBuffer(GL_ARRAY_BUFFER, animation.getFrame().getVbo());
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);

            shader.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(GL_TEXTURE0);

            if (Game.displayCollisions){
                glColor3i(255, 255, 255);
                glBegin(GL_LINE_LOOP);
                Vector2f point = new Vector2f();
                // BOTTOM LEFT
                point.set(position.x + xmap - cwidth / 2, position.y + ymap - cheight / 2);
                rotatePoint(point, angle);
                glVertex2f(point.x, point.y);
                // TOP LEFT
                point.set(position.x + xmap - cwidth / 2, position.y + ymap + cheight / 2);
                rotatePoint(point, angle);
                glVertex2f(point.x, point.y);
                // BOTTOM RIGHT
                point.set(position.x + xmap + cwidth / 2, position.y + ymap + cheight / 2);
                rotatePoint(point, angle);
                glVertex2f(point.x, point.y);
                // TOP RIGHT
                point.set(position.x + xmap + cwidth / 2, position.y + ymap - cheight / 2);
                rotatePoint(point, angle);
                glVertex2f(point.x, point.y);
                glEnd();

                glPointSize(10);
                glColor3i(255, 0, 0);
                glBegin(GL_POINTS);
                glVertex2f(position.x + xmap, position.y + ymap);
                glEnd();
            }
        }

        /**
         * @param point - coords of a corner point of the square
         * @param angle - angle of rotation
         */
        public void rotatePoint(Vector2f point, double angle) {

            // translate point to origin
            float tempX = point.x - (position.x + xmap);
            float tempY = point.y - (position.y + ymap);

            // now apply angle
            float rotatedX = tempX * (float) Math.cos(angle) - tempY * (float) Math.sin(angle);
            float rotatedY = tempX * (float) Math.sin(angle) + tempY * (float) Math.cos(angle);

            // translate back
            point.x = rotatedX + (position.x + xmap);
            point.y = rotatedY + (position.y + ymap);
        }

        @Override
        public void setPosition(float x, float y) {
            super.setPosition(x, y);
            originalPos = new Vector3f(x, y, 0);
        }

        public boolean canHit() {
            return animation.getIndexOfFrame() >= 8;
        }

        @Override
        public boolean intersects(MapObject o) {
            Vector2f[] pointsBox1 = new Vector2f[4];
            Vector2f[] pointsBox2 = new Vector2f[4];

            Vector2f[] axis1 = new Vector2f[2];
            Vector2f[] axis2 = new Vector2f[2];

            for (int i = 0; i < 4; i++) {
                if (i < 2){
                    axis1[i] = new Vector2f();
                    axis2[i] = new Vector2f();
                }
                pointsBox1[i] = new Vector2f();
                pointsBox2[i] = new Vector2f();
            }

            pointsBox1[0].set(position.x + xmap - cwidth / 2, position.y + ymap + cheight / 2);
            pointsBox1[1].set(position.x + xmap - cwidth / 2, position.y + ymap - cheight / 2);
            pointsBox1[2].set(position.x + xmap + cwidth / 2, position.y + ymap - cheight / 2);
            pointsBox1[3].set(position.x + xmap + cwidth / 2, position.y + ymap + cheight / 2);

            for (int i = 0; i < 4; i++) {
                rotatePoint(pointsBox1[i], angle);
            }

            int cwidth = o.getCwidth();
            int cheight = o.getCheight();
            Vector3f position = o.getPosition();
            pointsBox2[0].set(position.x + xmap - cwidth / 2, position.y + ymap + cheight / 2);
            pointsBox2[1].set(position.x + xmap - cwidth / 2, position.y + ymap - cheight / 2);
            pointsBox2[2].set(position.x + xmap + cwidth / 2, position.y + ymap - cheight / 2);
            pointsBox2[3].set(position.x + xmap + cwidth / 2, position.y + ymap + cheight / 2);

            axis1[0] = pointsBox1[2].sub(pointsBox1[3], axis1[0]);
            axis1[0].perpendicular();
            axis1[1] = pointsBox1[3].sub(pointsBox1[0], axis1[1]);
            axis1[1].perpendicular();

            axis2[0] = pointsBox2[2].sub(pointsBox2[3], axis2[0]);
            axis2[0].perpendicular();
            axis2[1] = pointsBox2[3].sub(pointsBox2[0], axis2[1]);
            axis2[1].perpendicular();

            float[] min1 = new float[2], min2 = new float[2];
            float[] max1 = new float[2], max2 = new float[2];
            for (int i = 0; i < 2; i++) {
                min1[i] = axis2[i].dot(pointsBox1[0]);
                max1[i] = axis2[i].dot(pointsBox1[1]);
                for (int j = 0; j < 4; j++) {
                    float dot = axis2[i].dot(pointsBox1[j]);
                    if (dot < min1[i]){
                        min1[i] = dot;
                    }
                    if (dot > max1[i]){
                        max1[i] = dot;
                    }
                }
            }
            for (int i = 0; i < 2; i++) {
                min2[i] = axis2[i].dot(pointsBox2[0]);
                max2[i] = axis2[i].dot(pointsBox2[1]);
                for (int j = 0; j < 4; j++) {
                    float dot = axis2[i].dot(pointsBox2[j]);
                    if (dot < min2[i]){
                        min2[i] = dot;
                    }
                    if (dot > max2[i]){
                        max2[i] = dot;
                    }
                }
            }
            for (int i = 0; i < 2; i++) {
                if (!((min1[i] < max2[i] && min1[i] > min2[i]) || (min2[i] < max1[i] && min2[i] > min1[i]))){
                    return false;
                }
            }

            for (int i = 0; i < 2; i++) {
                min1[i] = axis1[i].dot(pointsBox1[0]);
                max1[i] = axis1[i].dot(pointsBox1[1]);
                for (int j = 0; j < 4; j++) {
                    float dot = axis1[i].dot(pointsBox1[j]);
                    if (dot < min1[i]){
                        min1[i] = dot;
                    }
                    if (dot > max1[i]){
                        max1[i] = dot;
                    }
                }
            }
            for (int i = 0; i < 2; i++) {
                min2[i] = axis1[i].dot(pointsBox2[0]);
                max2[i] = axis1[i].dot(pointsBox2[1]);
                for (int j = 0; j < 4; j++) {
                    float dot = axis1[i].dot(pointsBox2[j]);
                    if (dot < min2[i]){
                        min2[i] = dot;
                    }
                    if (dot > max2[i]){
                        max2[i] = dot;
                    }
                }
            }
            for (int i = 0; i < 2; i++) {
                if (!((min1[i] < max2[i] && min1[i] > min2[i]) || (min2[i] < max1[i] && min2[i] > min1[i]))){
                    return false;
                }
            }

            return true;
        }
    }
    private boolean canShot(){
        Vector2f[] tilePoints = new Vector2f[4];
        for(int i = 0;i<4;i++){
            tilePoints[i] = new Vector2f();
        }
        Room currentRoom;
        if(MultiplayerManager.multiplayer){
            int playerrIndex = laserBeam.lastPlayerTargetIndex;
            currentRoom = tileMap.getRoomByCoords(player[playerrIndex].getX(),player[playerrIndex].getY());
        } else {
            currentRoom = tileMap.getCurrentRoom();
        }

        int xMin = currentRoom.getxMin();
        int yMin = currentRoom.getyMin();

        int tileSize = tileMap.getTileSize();

        int shiftX = xMin/tileSize;
        int shiftY = yMin/tileSize;

        for(int i = 0;i < currentRoom.getNumRows();i++){
            for(int j = 0;j < currentRoom.getNumCols();j++){
                if(tileMap.getType(i+shiftY,j+shiftX) == Tile.BLOCKED){
                    tilePoints[0].set(xMin+j*tileSize,yMin+i*tileSize);
                    tilePoints[1].set(xMin+tileSize+j*tileSize,yMin+i*tileSize);
                    tilePoints[2].set(xMin+tileSize+j*tileSize,yMin+tileSize+i*tileSize);
                    tilePoints[3].set(xMin+j*tileSize,yMin+tileSize+i*tileSize);
                    if (rayCast(tilePoints[0], tilePoints[1]) ||
                            rayCast(tilePoints[1], tilePoints[2]) ||
                            rayCast(tilePoints[2], tilePoints[3]) ||
                            rayCast(tilePoints[3], tilePoints[0])){
                        return false;
                    }
                }
            }
        }
        return true;
    }
    private boolean rayCast(Vector2f point11, Vector2f point12){
        // init
        Vector2f r = new Vector2f();
        Vector2f s = new Vector2f();

        float d,u,t;
        // get it in our additive vector form
        point12.sub(point11,r);
        Vector2f point21 = new Vector2f(position.x,position.y);
        int indexPlayer = theClosestPlayerIndex();
        Vector2f point22 = new Vector2f(player[indexPlayer].getX(),player[indexPlayer].getY());
        point22.sub(point21,s);

        // we use this again so only calc once
        d = r.x * s.y - r.y * s.x;
        u = ((point21.x - point11.x) * r.y - (point21.y - point11.y) * r.x) / d;
        t = ((point21.x - point11.x) * s.y - (point21.y - point11.y) * s.x) / d;

        // if they do intersect return
        return (0 <= u && u <= 1 && 0 <= t && t <= 1);
    }
    @Override
    public void handleAddEnemyProjectile(Network.AddEnemyProjectile o) {
    }

    @Override
    public void handleMoveEnemyProjectile(Network.MoveEnemyProjectile o) {
    }
    @Override
    public void handleHitEnemyProjectile(Network.HitEnemyProjectile hitPacket) {

    }
    @Override
    public void handleSync(Network.EnemySync sync){
        animation.setTime(sync.time);
        currentAction = sync.currAction;
        animation.setFrame(sync.sprite);
    }
    public void forceRemove(){

    }
    @Override
    public void applyHitEffects(Player hitPlayer) {

    }
}

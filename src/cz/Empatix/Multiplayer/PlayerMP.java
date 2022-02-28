package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.GameStateManager;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.Spritesheet;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Room;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class PlayerMP extends Player {
    private String username;
    private int idConnection;

    private boolean origin;
    private MultiplayerManager mpManager;

    private TextRender textRender;
    private boolean ghost;

    private Spritesheet ghostSpritesheet;
    private int vboGhostVertices;

    private long lastTimeMove;
    private Network.MovePlayer currentMove;
    private Network.MovePlayer previousMove;

    private Room deathRoom;
    private int totalMoves;

    public PlayerMP(TileMap tm, String username){
        super(tm);
        this.username = username;
        origin = false;
        mpManager = MultiplayerManager.getInstance();
        ghost = false;
        if(!tm.isServerSide()){
            vboGhostVertices = ModelManager.getModel(32,32);
            if (vboGhostVertices == -1){
                vboGhostVertices = ModelManager.createModel(32,32);
            }
            spriteSheetCols = 3;
            spriteSheetRows = 1;
            final int[] numFrames = {
                    3
            };
            ghostSpritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Player\\p_ghost.tga");

            // creating a new spritesheet
            if (ghostSpritesheet == null){
                ghostSpritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Player\\p_ghost.tga");
                for(int i = 0; i < spriteSheetRows; i++) {

                    Sprite[] images = new Sprite[numFrames[i]];

                    for (int j = 0; j < numFrames[i]; j++) {

                        float[] texCoords =
                                {
                                        (float) j / spriteSheetCols, (float) i / spriteSheetRows,

                                        (float) j / spriteSheetCols, (1.0f + i) / spriteSheetRows,

                                        (1.0f + j) / spriteSheetCols, (1.0f + i) / spriteSheetRows,

                                        (1.0f + j) / spriteSheetCols, (float) i / spriteSheetRows
                                };


                        Sprite sprite = new Sprite(texCoords);

                        images[j] = sprite;

                    }

                    ghostSpritesheet.addSprites(images);
                }
            }
        }
    }

    public void setOrigin(boolean origin) {
        this.origin = origin;
    }

    public boolean isOrigin() {
        return origin;
    }

    @Override
    public void draw() {
        if(ghost){
        // pokud neni object na obrazovce - zrusit
            if (isNotOnScrean()){
                return;
            }

            // blikání - po hitu - hráč

            if (flinching){
                long elapsed = (System.nanoTime() - flinchingTimer) / 1000000;
                if (elapsed / 100 % 2 == 0){
                    shader.unbind();
                    glBindTexture(GL_TEXTURE_2D,0);
                    glActiveTexture(GL_TEXTURE0);

                    return;
                }
            }

            Matrix4f target;
            if (facingRight) {
                target = new Matrix4f().translate(position)
                        .scale(scale);
            } else {
                target = new Matrix4f().translate(position)
                        .rotateY((float) Math.PI)
                        .scale(scale);

            }
            Camera.getInstance().projection().mul(target,target);

            shader.bind();
            shader.setUniformi("sampler",0);
            glActiveTexture(GL_TEXTURE0);
            shader.setUniformm4f("projection",target);

            ghostSpritesheet.bindTexture();

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);


            glBindBuffer(GL_ARRAY_BUFFER, vboGhostVertices);
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
                glBegin(GL_LINE_STRIP);
                // BOTTOM LEFT
                glVertex2f(position.x+xmap-cwidth/2,position.y+ymap-cheight/2);
                // TOP LEFT
                glVertex2f(position.x+xmap-cwidth/2, position.y+ymap+cheight/2);
                // TOP RIGHT
                glVertex2f(position.x+xmap+cwidth/2, position.y+ymap+cheight/2);
                // BOTTOM RIGHT
                glVertex2f(position.x+xmap+cwidth/2, position.y+ymap-cheight/2);
                glEnd();

                glPointSize(10);
                glColor3i(255,0,0);
                glBegin(GL_POINTS);
                glVertex2f(position.x+xmap,position.y+ymap);
                glEnd();


            }
        } else {
            super.draw();
        }
        if(textRender == null) textRender = new TextRender();
        float centerX = TextRender.getHorizontalCenter((int)(position.x+xmap),(int)(position.x+xmap),username,2);
        Vector3f color = new Vector3f();
        if(isGhost()){
            color.set(0.760f);
        } else {
            color.set(0.874f,0.443f,0.149f);
        }
        textRender.draw(username,new Vector3f(centerX,position.y+ymap-cheight/2-10,0),2,color);
    }

    public String getUsername() {
        return username;
    }

    public void remove(){
        light.remove();
    }

    public void update() {
        setMapPosition();
        // check if player should be still rolling
        if(System.currentTimeMillis() - rollCooldown - InGame.deltaPauseTime() >= 400 && rolling){
            rolling = false;
            right=false;
            up=false;
            down=false;
            left=false;
        }
        if(!ghost){
            // check if player is not dead
            if (health <= 0) {
                speed.x = 0;
                speed.y = 0;
                right=false;
                up=false;
                down=false;
                left=false;
                flinching = false;
                setDead();
            } else if (health < 3 && !lowHealth && !tileMap.isServerSide() && isOrigin()){
                lowHealth = true;
                sourcehealth.play(soundLowHealth);
            }
            else if (health >= 3 && lowHealth && !tileMap.isServerSide() && isOrigin()){
                lowHealth = false;
                sourcehealth.stop();
            }
        } else {
            // player was revived
            if(health > 0){
                reset(); // restat
            }
        }

        if (lowHealth && (float)(System.currentTimeMillis()-heartBeat-InGame.deltaPauseTime())/1000 > 0.85f){
            heartBeat = System.currentTimeMillis()-InGame.deltaPauseTime();
            if(lastDamage == DamageAbsorbedBy.ARMOR){
                hitVignette[1].updateFadeTime();
            } else if(lastDamage == DamageAbsorbedBy.HEALTH){
                hitVignette[0].updateFadeTime();
            }
        }
        if(!tileMap.isServerSide()){
            if(((Math.abs(speed.x) >= maxSpeed || Math.abs(speed.y) >= maxSpeed)) && !ghost){
                float value = Math.abs(speed.x);
                if(value < Math.abs(speed.y)) value = Math.abs(speed.y);
                if(System.currentTimeMillis() - InGame.deltaPauseTime() - lastTimeSprintParticle > 400-value*20){
                    lastTimeSprintParticle = System.currentTimeMillis()- InGame.deltaPauseTime();
                    SprintParticle sprintParticle = new SprintParticle(tileMap);
                    if((up || down) && !left && !right){
                        sprintParticle.setPosition(
                                position.x+16*(float)Math.sin(2*Math.PI*((System.currentTimeMillis()%1000)/1000d)),
                                position.y+height/2);
                    } else if((right || left) && !up && !down){
                        sprintParticle.setPosition(
                                position.x,
                                position.y+height/2+16*(float)Math.sin(Math.PI*(1+((System.currentTimeMillis()%1000)/1000d))));
                    } else {
                        sprintParticle.setPosition(position.x,position.y+height/2);

                    }
                    sprintParticles.add(sprintParticle);
                }
            }
            for(int i = 0;i<sprintParticles.size();i++){
                SprintParticle sprintParticle = sprintParticles.get(i);

                sprintParticle.update();
                if(sprintParticle.shouldRemove()){
                    sprintParticles.remove(i);
                    i--;
                }
            }
        }
        getMovementSpeed();
        if(!ghost) checkRoomObjectsCollision();
        else checkGhostRestrictions();
        checkTileMapCollision();
        if(tileMap.isServerSide())setPosition(temp.x, temp.y);
        else if(currentMove != null){
            float timeSinceLastInput = (System.currentTimeMillis() - mpManager.client.getClient().getReturnTripTime() - lastTimeMove)/1000f;
            //System.out.println("SEC: "+timeSinceLastInput);
            final double ns = 1 / 60.0;
            float t = (float) (timeSinceLastInput/totalMoves / (ns));
            /*System.out.println("=======");
            System.out.println("position X: "+position.x+" position y: "+position.y);
            System.out.println("T: "+t);*/
            if(t < 0) t = 0;
            Vector3f finalPos = new Vector3f();
            if(previousMove != null){
                finalPos.x = previousMove.x;
                finalPos.y = previousMove.y;
                finalPos.lerp(new Vector3f(currentMove.x,currentMove.y,0),t);
            }
            else {
                finalPos.x = currentMove.x;
                finalPos.y = currentMove.y;
            }
            //System.out.println("dif X: "+(finalPos.x-position.x)+" dif y: "+(finalPos.y-position.y));
            setPosition(finalPos.x,finalPos.y);
            //System.out.println("=======");

        }
        if(!tileMap.isServerSide()){
            if(!ghost){
                if (right || left) {
                    if (currentAction != SIDE) {
                        currentAction = SIDE;
                        animation.setFrames(spritesheet.getSprites(SIDE));
                        animation.setDelay(75);
                    }
                } else if (up) {
                    if (currentAction != UP) {
                        currentAction = UP;
                        animation.setFrames(spritesheet.getSprites(UP));
                        animation.setDelay(50);
                    }
                } else if (down) {
                    if (currentAction != DOWN) {
                        currentAction = DOWN;
                        animation.setFrames(spritesheet.getSprites(DOWN));
                        animation.setDelay(75);
                    }
                } else {
                    if (currentAction != IDLE) {
                        currentAction = IDLE;
                        animation.setFrames(spritesheet.getSprites(IDLE));
                        animation.setDelay(100);
                    }
                }
            }

            // direction of player
            if (left) facingRight = false;
            if (right) facingRight = true;

            if(lastDamage == DamageAbsorbedBy.ARMOR){
                hitVignette[1].update();
            } else if(lastDamage == DamageAbsorbedBy.HEALTH){
                hitVignette[0].update();
            }
        }
        //  IMMORTALITY AFTER GETTING HIT
        if (flinching){
            if ((float)(System.currentTimeMillis() - flinchingTimer - InGame.deltaPauseTime())/ 1000 > 1.5) {
                flinching = false;
            }
        }

        // next sprite of player
        if(!tileMap.isServerSide())animation.update();
        if(isOrigin()){
            Client client = mpManager.client.getClient();
            Network.MovePlayerInput movePlayer = new Network.MovePlayerInput();
            movePlayer.username = username;
            movePlayer.down = down;
            movePlayer.up = up;
            movePlayer.left = left;
            movePlayer.right = right;
            client.sendUDP(movePlayer);
        }
    }
    public void updateLostPacket(){
        getMovementSpeed();
        if(!ghost) checkRoomObjectsCollision();
        else checkGhostRestrictions();
        checkTileMapCollision();
        setPosition(temp.x, temp.y);
    }

    public void fakeHit(Network.PlayerHit playerHit){
        lastDamage = playerHit.type;
        flinching = true;
        flinchingTimer = System.currentTimeMillis();
        if(lastDamage == DamageAbsorbedBy.ARMOR){
            hitVignette[1].updateFadeTime();
        } else if(lastDamage == DamageAbsorbedBy.HEALTH){
            hitVignette[0].updateFadeTime();
        }
        source.play(soundPlayerhurt[Random.nextInt(2)]);
    }

    @Override
    public void setDead(){
        deathTime = System.currentTimeMillis();
        dead = true;
        speed.x = 0;
        speed.y = 0;
        right=false;
        up=false;
        down=false;
        left=false;
        flinching = false;
        if(!tileMap.isServerSide()){
            if(sourcehealth.isPlaying()) sourcehealth.stop();
            lowHealth = false;
            source.play(soundPlayerdeath);
            animation.setFrames(ghostSpritesheet.getSprites(0));
            animation.setDelay(100);

            light.setIntensity(2f);
        } else {
            GunsManagerMP gunsManagerMP = GunsManagerMP.getInstance();
            ArtefactManagerMP artefactManagerMP = ArtefactManagerMP.getInstance();

            MPStatistics mpStatistics = MultiplayerManager.getInstance().server.getMpStatistics();
            mpStatistics.setTimeDeath(username,deathTime);

            // random direction of drop
            int x = -100 + Random.nextInt(201);
            int y = -100 + Random.nextInt(201);
            gunsManagerMP.dropPlayerWeapon(username,x,y,0);
            x = -100 + Random.nextInt(201);
            y = -100 + Random.nextInt(201);
            gunsManagerMP.dropPlayerWeapon(username,x,y,1);
            x = -100 + Random.nextInt(201);
            y = -100 + Random.nextInt(201);
            artefactManagerMP.setCurrentArtefact(null,x,y,username);
        }
        deathRoom = tileMap.getRoomByCoords(position.x,position.y);
        cwidth = width = 32;
        cheight = height = 32;
        scale = 3;
        cheight *= scale;
        cwidth *= scale;
        height *= scale;
        width *= scale;
        ghost = true;
    }

    public boolean isGhost() {
        return ghost;
    }
    @Override
    public void checkCollision(ArrayList<Enemy> enemies){
        for (Enemy currentEnemy:enemies){
            // check player X enemy collision
            if (intersects(currentEnemy) && !currentEnemy.isDead() && !currentEnemy.isSpawning()){
                hit(currentEnemy.getDamage());
                if(isDead()){
                    tileMap.clearEnemiesInPlayersRoom(this);
                }
            }
        }
    }

    /**
     * check if player as ghost can enter this location if it is not some new room, that other or him didn't discover
     */
    public void checkGhostRestrictions(){
        dest.x = position.x + speed.x;
        dest.y = position.y + speed.y;

        Room room = tileMap.getRoomByCoords(dest.x,dest.y);
        if(room != null){
            if(!room.hasBeenEntered() && tileMap.isNotDeathRoomOfPlayers(room)){
                int xMin = room.getxMin();
                int xMax = room.getxMax();
                int yMin = room.getyMin();
                int yMax = room.getyMax();
                if(position.y < yMax && position.y > yMin){
                    if(dest.x > xMin && dest.x < xMax){
                        speed.x = 0;
                    }
                }
                if(position.x < xMax && position.x > xMin){
                    if(dest.y > yMin && dest.y < yMax){
                        speed.y = 0;
                    }
                }
            }
        }
    }

    public Room getDeathRoom() {
        return deathRoom;
    }

    public void reset(){
        width = cwidth= 32;
        height = cheight = 72;

        // COLLISION WIDTH/HEIGHT
        scale = 2;

        moveSpeed = 0.8f;
        maxSpeed = 11.84f;
        stopSpeed = 3.25f;

        health = maxHealth = 7;
        coins = 0;

        armor = maxArmor = 3;

        dead = false;
        flinching = false;
        facingRight = true;

        currentAction = IDLE;

        // because of scaling image by 5x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        rolling = false;

        ghost = false;
        if(!tileMap.isServerSide()){
            animation.setFrames(spritesheet.getSprites(IDLE));
            animation.setDelay(100);
            light.setIntensity(4f);
        }
    }

    public void setPosition(Network.MovePlayer move)
    {
        RefreshToPosition(move, currentMove);
    }

    private void RefreshToPosition(Network.MovePlayer move, Network.MovePlayer prevData)
    {
        previousMove = prevData;
        this.currentMove = move;
        lastTimeMove = GameStateManager.timeUpdate;
        //if(prevData != null) System.out.println("CHANGE X: "+(move.x-prevData.x));
    }
    private Vector3f lerp(Vector3f pos, Vector3f dest, float t){
        Vector3f finalPos = new Vector3f();
        finalPos.x=pos.x+(dest.x-pos.x)*t;
        finalPos.y=pos.y+(dest.y-pos.y)*t;
        return finalPos;
    }

    public void setTotalMoves(int totalMoves) {
        this.totalMoves = totalMoves;
    }

    public void setIdConnection(int idConnection) {
        this.idConnection = idConnection;
    }

    public int getIdConnection() {
        return idConnection;
    }
}

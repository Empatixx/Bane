package cz.Empatix.Multiplayer;

import com.esotericsoftware.kryonet.Client;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Multiplayer.MultiplayerManager;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Text.TextRender;
import cz.Empatix.Render.TileMap;
import org.joml.Vector3f;

public class PlayerMP extends Player {
    private String username;

    private boolean origin;
    private MultiplayerManager mpManager;

    private TextRender textRender;

    public PlayerMP(TileMap tm, String username){
        super(tm);
        this.username = username;
        origin = false;
        mpManager = MultiplayerManager.getInstance();
        setHealth(200);
    }

    public void setOrigin(boolean origin) {
        this.origin = origin;
    }

    public boolean isOrigin() {
        return origin;
    }

    @Override
    public void draw() {
        super.draw();
        if(textRender == null) textRender = new TextRender();
        float centerX = TextRender.getHorizontalCenter((int)(position.x+xmap),(int)(position.x+xmap),username,2);
        textRender.draw(username,new Vector3f(centerX,position.y+ymap-cheight/2-10,0),2,new Vector3f(0.874f,0.443f,0.149f));
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
        // check if player is not dead
        if (health <= 0) {
            speed.x = 0;
            speed.y = 0;
            right=false;
            up=false;
            down=false;
            left=false;
            flinching = false;
        } else if (health < 3 && !lowHealth && !tileMap.isServerSide()){
            lowHealth = true;
            sourcehealth.play(soundLowHealth);
        }
        else if (health >= 3 && lowHealth && !tileMap.isServerSide()){
            lowHealth = false;
            sourcehealth.stop();
        }

        if (lowHealth && (float)(System.currentTimeMillis()-heartBeat-InGame.deltaPauseTime())/1000 > 0.85f){
            heartBeat = System.currentTimeMillis()-InGame.deltaPauseTime();
            if(lastDamage == DamageAbsorbedBy.ARMOR){
                hitVignette[1].updateFadeTime();
            } else {
                hitVignette[0].updateFadeTime();
            }
        }
        if(!tileMap.isServerSide()){
            if((Math.abs(speed.x) >= maxSpeed || Math.abs(speed.y) >= maxSpeed)){
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
        checkRoomObjectsCollision();
        checkTileMapCollision();
        if(tileMap.isServerSide() || !MultiplayerManager.multiplayer){
            setPosition(temp.x, temp.y);
        }
        if(!tileMap.isServerSide()){
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

            // direction of player
            if (left) facingRight = false;
            if (right) facingRight = true;

            if(lastDamage == DamageAbsorbedBy.ARMOR){
                hitVignette[1].update();
            } else {
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
    public void fakeHit(Network.PlayerHit playerHit){
        lastDamage = playerHit.type;

        if(lastDamage == DamageAbsorbedBy.ARMOR){
            hitVignette[1].updateFadeTime();
        } else {
            hitVignette[0].updateFadeTime();
        }
        source.play(soundPlayerhurt[Random.nextInt(2)]);

    }
}

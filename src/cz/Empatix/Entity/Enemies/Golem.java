package cz.Empatix.Entity.Enemies;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Soundtrack;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemies.Projectiles.KingSlimebullet;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Main.Game;
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

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
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

    private boolean disableDraw;

    private boolean chestCreated;

    private final ArrayList<KingSlimebullet> bullets;
    private final LaserBeam laserBeam;


    private HealthBar healthBar;

    private long shieldCooldown;
    private boolean speedBuff;
    private long speedCooldown;

    private int armor;
    private int maxArmor;

    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Enemies\\golem.png");
        Loader.loadImage("Textures\\Sprites\\Enemies\\laserbeam.tga");
    }
    public Golem(TileMap tm, Player player) {
        super(tm,player);

        moveSpeed = 0.7f;
        maxSpeed = 2.6f;
        stopSpeed = 0.75f;

        width = 100;
        height = 100;
        cwidth = 50;
        cheight = 40;
        scale = 6;

        armor = maxArmor = (int)(10*(1+(tm.getFloor()-1)*0.12));
        health = maxHealth = (int)(90*(1+(tm.getFloor()-1)*0.12));
        damage = 2;

        type = melee;
        facingRight = true;
        bullets=new ArrayList<>(100);

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

        healthBar = new HealthBar("Textures\\bosshealthbar",new Vector3f(960,1000,0),7,49,3);
        healthBar.initHealth(health,maxHealth);

        createShadow();

        speedBuff = false;
        speedCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
        shieldCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
        laserBeam = new LaserBeam(tm,player);

    }

    private void getNextPosition() {

        // movement
        if(left) {
            speed.x -= moveSpeed;
            if(speed.x < -maxSpeed) {
                speed.x = -maxSpeed;
            }
        }
        else if(right) {
            speed.x += moveSpeed;
            if(speed.x > maxSpeed) {
                speed.x = maxSpeed;
            }
        }
        else {
            if (speed.x < 0){
                speed.x += stopSpeed;
                if (speed.x > 0) speed.x = 0;
            } else if (speed.x > 0){
                speed.x -= stopSpeed;
                if (speed.x < 0) speed.x = 0;
            }
        }
        if(down) {
            speed.y += moveSpeed;
            if (speed.y > maxSpeed){
                speed.y = maxSpeed;
            }
        } else if (up){
            speed.y -= moveSpeed;
            if (speed.y < -maxSpeed){
                speed.y = -maxSpeed;
            }
        } else {
            if (speed.y < 0){
                speed.y += stopSpeed;
                if (speed.y > 0) speed.y = 0;
            } else if (speed.y > 0){
                speed.y -= stopSpeed;
                if (speed.y < 0) speed.y = 0;
            }
        }
    }

    public void update() {
        setMapPosition();
        healthBar.update(health,maxHealth);
        if(isSpawning()) return;
        // update animation
        if(!isDead() || (isDead() && !animation.isPlayingLastFrame())){
            animation.update();
        }
        if(isDead() && animation.isPlayingLastFrame()){
            if(!chestCreated){
                chestCreated=true;

                Chest chest = new Chest(tileMap);
                chest.setPosition(position.x,position.y);
                chest.enableDropArtefact();
                tileMap.addObject(chest);

                tileMap.addLadder();
            }
        }
        for(int i = 0;i<bullets.size();i++){
            KingSlimebullet slimebullet = bullets.get(i);
            slimebullet.update();
            if(slimebullet.intersects(player) && !player.isFlinching() && !player.isDead()){
                slimebullet.setHit();
                player.hit(1);
            }
            if(slimebullet.shouldRemove()) {
                bullets.remove(i);
                i--;
            }
            for(RoomObject object: tileMap.getRoomMapObjects()){
                if(object instanceof DestroyableObject) {
                    if (slimebullet.intersects(object) && !slimebullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                        slimebullet.setHit();
                        ((DestroyableObject) object).setHit(1);
                    }
                }
            }
        }
        if(dead) return;

        // ENEMY AI
        EnemyAI();

        if(     Math.abs(position.x - player.getX()) < 255
                && position.y - player.getY() < 10 && position.y - player.getY() > -230
                && currentAction == IDLE && !speedBuff && !player.isDead()){
            currentAction = ATTACK;
            animation.setFrames(spritesheet.getSprites(ATTACK));
            animation.setDelay(130);
        } else if(     /*Math.abs(position.x - player.getX()) > 50
                && position.y - player.getY() < 150 && position.y - player.getY() > 0*/
                 currentAction == IDLE){
            currentAction = EYE_BEAM;
            animation.setFrames(spritesheet.getSprites(EYE_BEAM));
            animation.setDelay(-1);
            laserBeam.setPosition(position.x,position.y-height/scale-5);
        } else if (System.currentTimeMillis() - speedCooldown - InGame.deltaPauseTime() > 10000 && currentAction == IDLE){
            speedCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
            speedBuff = true;
            maxSpeed = 12f;
            currentAction = ENERGY;
            animation.setFrames(spritesheet.getSprites(ENERGY));
            animation.setDelay(85);
        } else if (System.currentTimeMillis() - shieldCooldown - InGame.deltaPauseTime() > 15000 && currentAction == IDLE){
            shieldCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
            currentAction = BARRIER;
            animation.setFrames(spritesheet.getSprites(BARRIER));
            animation.setDelay(125);
        } else if(currentAction != IDLE && animation.hasPlayedOnce()){
            currentAction = IDLE;
            animation.setFrames(spritesheet.getSprites(IDLE));
            animation.setDelay(145);
        }
        if(facingRight != laserBeam.isFacingRight()){
            laserBeam.setPosition(position.x,position.y-height/scale-5);
            laserBeam.setFacingRight(facingRight);
        }

        if(currentAction == ATTACK){
            right = false;
            left = false;
            up = false;
            down = false;
            if (Math.abs(position.x - player.getX()) < 275
                    && position.y - player.getY() < 20 && position.y - player.getY() > -250
                    && animation.isPlayingLastFrame()){
                player.hit(4);
            }
        } else if(currentAction == EYE_BEAM){
            right = false;
            left = false;
            up = false;
            down = false;
            laserBeam.update();
        } else if(speedBuff && System.currentTimeMillis() - speedCooldown - InGame.deltaPauseTime() > 1000){
            speedBuff = false;
            maxSpeed = 2.6f;
        }
        // update position
        getNextPosition();
        checkTileMapCollision();
        setPosition(temp.x, temp.y);
    }

    public void draw() {

        for(KingSlimebullet bullet : bullets){
            bullet.draw();
        }
        if(!disableDraw){
            super.draw();
            if(currentAction == EYE_BEAM){
                laserBeam.draw();
            }
        }


    }
    public void drawHud(){
        if(isDead()) return;
        healthBar.draw();
    }
    @Override
    public void hit(int damage) {
        if(dead || isSpawning()) return;
        lastTimeDamaged=System.currentTimeMillis()- InGame.deltaPauseTime();

        armor-=damage;
        if(armor < 0){
            armor = 0;
        }
        if (damage < 0) damage = 0;
        health -= damage;
        if (health < 0) health = 0;

        if(health == 0){
            animation.setDelay(65);
            animation.setFrames(spritesheet.getSprites(DEAD));
            currentAction = DEAD;
            speed.x = 0;
            speed.y = 0;
            dead = true;

            AudioManager.playSoundtrack(Soundtrack.IDLE);

            // deleting all projectiles after death
            for(KingSlimebullet slimebullet : bullets){
                slimebullet.setHit();
            }
        }
    }
    @Override
    public boolean shouldRemove(){
        return false;
    }

    @Override
    public void drawShadow() {
        if(!disableDraw) drawShadow(11f,65);
    }

    private static class LaserBeam extends MapObject {
        private Vector3f originalPos;
        double angle;
        private Player p;

        private static Vector2f[] vector2fs = new Vector2f[4];
        LaserBeam(TileMap tm, Player p){
            super(tm);
            this.p = p;
            width = 810;
            height = 45;
            cwidth = 810;
            cheight = 45;
            scale = 3;
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

                sprites = new Sprite[spriteSheetRows];
                for (int j = 0; j < spriteSheetRows; j++) {
                    Sprite sprite = new Sprite(new float[]
                            {
                                    1f, (1.0f+j) / spriteSheetRows,

                                    1f, (float)j/spriteSheetRows,

                                    0f, (float)j/spriteSheetRows,

                                    0f, (1.0f+j) / spriteSheetRows

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
            animation.setFrame(8);
            animation.setDelay(-1);

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
        public void setFacingRight(boolean b){
            facingRight = b;
        }
        public boolean isFacingRight(){return facingRight;}
        public void update(){
            animation.update();
            setMapPosition();
            float y = originalPos.y-p.getY();
            float x = originalPos.x-p.getX();
            float angle = (float)Math.atan(y/x);
            if(!facingRight){
                angle+=Math.PI;
            }
            boolean reverseDir = false;
            if(Math.PI*2-Math.abs(this.angle - angle) < Math.abs(this.angle - angle)){
                reverseDir = true;
            }
            if(!reverseDir){
                this.angle += (angle - this.angle) * .07;
            } else {
                if(this.angle >= 0){
                    this.angle += ((Math.PI*3/2. - this.angle)+(Math.PI/2.+angle)) * .07;
                    if(this.angle >= Math.PI*3/2.){
                        this.angle-=Math.PI*3/2.;
                        this.angle=-Math.PI/2. - this.angle;
                    }
                } else {
                    this.angle -= ((Math.PI*3/2. - angle)+(Math.PI/2.+this.angle)) * .07;
                    if(this.angle <= -Math.PI/2.){
                        this.angle+=Math.PI/2;
                        this.angle=Math.PI*3/2.-this.angle;
                    }
                }

            }

            position.x = originalPos.x + (width/2-65) * (float)Math.cos(this.angle);
            position.y = originalPos.y + (width/2-65) * (float)Math.sin(this.angle);

            if(intersects(p)){
                p.hit(0);
                System.out.println("PICOOO");
            }

        }

        @Override
        public void draw() {
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

            pointsBox1[0].set(position.x - cwidth / 2,position.y + cheight / 2);
            pointsBox1[1].set(position.x - cwidth / 2,position.y - cheight / 2);
            pointsBox1[2].set(position.x + cwidth / 2,position.y - cheight / 2);
            pointsBox1[3].set(position.x + cwidth / 2,position.y + cheight / 2);

            for (int i = 0; i < 4; i++) {
                rotatePoint(pointsBox1[i], angle);
            }

            int cwidth = o.getCwidth();
            int cheight = o.getCheight();
            Vector3f position = o.getPosition();
            pointsBox2[0].set(position.x - cwidth / 2,position.y + cheight / 2);
            pointsBox2[1].set(position.x - cwidth / 2,position.y - cheight / 2);
            pointsBox2[2].set(position.x + cwidth / 2,position.y - cheight / 2);
            pointsBox2[3].set(position.x + cwidth / 2,position.y + cheight / 2);

            return true;
        }
    }

}

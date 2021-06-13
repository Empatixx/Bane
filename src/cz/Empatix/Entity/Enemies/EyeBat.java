package cz.Empatix.Entity.Enemies;

import cz.Empatix.Entity.Animation;
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
import cz.Empatix.Render.Room;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.Tile;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

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

    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Enemies\\eyebat.tga");
        Loader.loadImage("Textures\\Sprites\\Enemies\\laserbeam-eyebat.tga");
    }

    private long beamCooldown;

    private LaserBeam laserBeam;

    public EyeBat(TileMap tm, Player player) {

        super(tm,player);

        moveSpeed = 2f;
        maxSpeed = 8.5f;
        stopSpeed = 1.6f;

        width = 80;
        height = 80;
        cwidth = 80;
        cheight = 80;
        scale = 2;


        health = maxHealth = (int)(11*(1+(Math.pow(tm.getFloor(),1.5)*0.12)));
        damage = 1;

        type = melee;
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

        laserBeam = new LaserBeam(tm,player);
        beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
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
        if(isSpawning()) return;
        // update animation
        animation.update();

        if(dead) return;

        // ENEMY AI
        EnemyAI();

        if(currentAction == IDLE && System.currentTimeMillis() - InGame.deltaPauseTime() - beamCooldown > 5000 && position.distance(player.getPosition()) > 550
                && position.distance(player.getPosition()) < 1500
            && canShot()){
            currentAction = BEAM;
            if(facingRight){
                laserBeam.setPosition(position.x,position.y);
            } else {
                laserBeam.setPosition(position.x,position.y+20);
            }
            laserBeam.resetAnimation();
            beamCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
        } else if (currentAction == BEAM && (laserBeam.hasEnded() || !canShot())){
            currentAction = IDLE;
        }
        if(facingRight != laserBeam.isFacingRight()){
            laserBeam.setPosition(position.x,position.y);
            laserBeam.setFacingRight(facingRight);
        }

        if(currentAction == BEAM){
            right = false;
            left = false;
            up = false;
            down = false;
            laserBeam.update();
        }

        // update position
        getNextPosition();
        checkTileMapCollision();
        setPosition(temp.x, temp.y);
    }

    @Override
    public void hit(int damage) {
        if(dead || isSpawning()) return;
        lastTimeDamaged=System.currentTimeMillis()-InGame.deltaPauseTime();
        health -= damage;
        if(health < 0) health = 0;
        if(health == 0){
            //animation.setDelay(100);
            //animation.setFrames(spritesheet.getSprites(DEAD));
            speed.x = 0;
            speed.y = 0;
            dead = true;

        }
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

    @Override
    public void loadSave() {
        super.loadSave();
        width = 80;
        height = 80;

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

        createShadow();
    }
    private static class LaserBeam extends MapObject {
        private Vector3f originalPos;
        double angle;
        private Player p;

        LaserBeam(TileMap tm, Player p) {
            super(tm);
            this.p = p;
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

        boolean hasEnded(){return animation.hasPlayedOnce();}

        void resetAnimation(){
            animation.setFrames(spritesheet.getSprites(0));

            float y = originalPos.y - p.getY();
            float x = originalPos.x - p.getX();
            float angle = (float) Math.atan(y / x);
            this.angle += (angle - this.angle);
            position.x = originalPos.x + (width / 2 - 50) * (float) Math.cos(this.angle);
            position.y = originalPos.y + (width / 2 - 50) * (float) Math.sin(this.angle);
        }
        void setFacingRight(boolean f){facingRight = f;}

        boolean isFacingRight(){return facingRight;}

        public void update() {
            animation.update();
            setMapPosition();
            float y = originalPos.y - p.getY();
            float x = originalPos.x - p.getX();
            float angle = (float) Math.atan(y / x);
            if (!facingRight){
                angle += Math.PI;
            }
            boolean reverseDir = false;
            if (Math.PI * 2 - Math.abs(this.angle - angle) < Math.abs(this.angle - angle)){
                reverseDir = true;
            }
            if (!reverseDir){
                this.angle += (angle - this.angle) * .07;
            } else {
                if (this.angle >= 0){
                    this.angle += ((Math.PI * 3 / 2. - this.angle) + (Math.PI / 2. + angle)) * .035;
                    if (this.angle >= Math.PI * 3 / 2.){
                        this.angle -= Math.PI * 3 / 2.;
                        this.angle = -Math.PI / 2. - this.angle;
                    }
                } else {
                    this.angle -= ((Math.PI * 3 / 2. - angle) + (Math.PI / 2. + this.angle)) * .035;
                    if (this.angle <= -Math.PI / 2.){
                        this.angle += Math.PI / 2;
                        this.angle = Math.PI * 3 / 2. - this.angle;
                    }
                }

            }

            position.x = originalPos.x + (width / 2 - 50) * (float) Math.cos(this.angle);
            position.y = originalPos.y + (width / 2 - 50) * (float) Math.sin(this.angle);

            if (intersects(p) && canHit()){
                p.hit(0);
            }
            for (RoomObject object : tileMap.getRoomMapObjects()) {
                if (object instanceof DestroyableObject){
                    if (intersects(object) && !((DestroyableObject) object).isDestroyed()){
                        ((DestroyableObject) object).setHit(1);
                    }
                }
            }

        }

        @Override
        public void draw() {
            // blikání - po hitu - hráč
            if (flinching){
                long elapsed = (System.nanoTime() - flinchingTimer) / 1000000;
                if (elapsed / 100 % 2 == 0){
                    shader.unbind();
                    glBindTexture(GL_TEXTURE_2D, 0);
                    glActiveTexture(GL_TEXTURE0);

                    return;
                }
            }

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
        Room currentRoom = tileMap.getCurrentRoom();

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
        Vector2f point22 = new Vector2f(player.getX(),player.getY());
        point22.sub(point21,s);

        // we use this again so only calc once
        d = r.x * s.y - r.y * s.x;
        u = ((point21.x - point11.x) * r.y - (point21.y - point11.y) * r.x) / d;
        t = ((point21.x - point11.x) * s.y - (point21.y - point11.y) * s.x) / d;

        // if they do intersect return
        return (0 <= u && u <= 1 && 0 <= t && t <= 1);
    }
}

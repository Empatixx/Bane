package cz.Empatix.Entity.Enemies;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemies.Projectiles.Slimebullet;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;

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

        moveSpeed = 0.6f;
        maxSpeed = 1.6f;
        stopSpeed = 0.5f;

        width = 64;
        height = 48;
        cwidth = 64;
        cheight = 48;
        scale = 2;

        health = maxHealth = (int)(10*(1+(tm.getFloor()-1)*0.12));
        damage = 1;

        type = melee;
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
        if(isDead() && animation.hasPlayedOnce()){
            disableDraw = true;
        }
        for(int i = 0;i<bullets.size();i++){
            Slimebullet slimebullet = bullets.get(i);
            slimebullet.update();
            if(slimebullet.intersects(player) && !player.isFlinching() && !player.isDead()){
                slimebullet.setHit();
                player.hit(1);
            }
            for(RoomObject object: tileMap.getRoomMapObjects()){
                if(object instanceof DestroyableObject) {
                    if (slimebullet.intersects(object) && !slimebullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                        slimebullet.setHit();
                        ((DestroyableObject) object).setHit(1);
                    }
                }
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
        else if(shootready && animation.getIndexOfFrame() == 2){
            shootready=false;
            final int tileTargetX = px/tileSize;
            final int tileTargetY = py/tileSize;

            final int tileEnemyX = (int)position.x/tileSize;
            final int tileEnemyY = (int)position.y/tileSize;

            if (Math.abs(tileEnemyX - tileTargetX) <= 12 && Math.abs(tileEnemyY - tileTargetY) <= 12) {

                for (int i = 0; i < 5; i++) {
                    Slimebullet slimebullet = new Slimebullet(tileMap, px - position.x, py - position.y, 1.3 * i);
                    slimebullet.setPosition(position.x, position.y);
                    bullets.add(slimebullet);
                }
            }
        }
        // ENEMY AI
        EnemyAI();

        // update position
        getNextPosition();
        checkTileMapCollision();

        setPosition(temp.x, temp.y);
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
        if(dead || isSpawning()) return;
        lastTimeDamaged=System.currentTimeMillis()-InGame.deltaPauseTime();
        health -= damage;
        if(health < 0) health = 0;
        if(health == 0){
            animation.setDelay(65);
            animation.setFrames(spritesheet.getSprites(DEAD));
            speed.x = 0;
            speed.y = 0;
            dead = true;
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
    public void loadSave() {
        super.loadSave();
        width = 64;
        height = 48;

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

        for(Slimebullet slimebullet: bullets){
            slimebullet.loadSave();
        }

        createShadow();
    }
}


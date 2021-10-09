package cz.Empatix.Entity.Enemies;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemies.Projectiles.RedSlimebullet;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;


public class RedSlime extends Enemy {

    private static final int IDLE = 0;
    private static final int DEAD = 1;

    private boolean shootready;
    private boolean disableDraw;
    private long shootCooldown;

    private int projectiles;
    private boolean projectilesShooted;

    private ArrayList<RedSlimebullet> bullets;

    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Enemies\\redslime.tga");
    }
    public RedSlime(TileMap tm, Player player) {
        super(tm,player);

        moveSpeed = 0.6f;
        maxSpeed = 3.6f;
        stopSpeed = 0.5f;

        width = 64;
        height = 48;
        cwidth = 64;
        cheight = 48;
        scale = 2;

        health = maxHealth = (int)(9*(1+(Math.pow(tm.getFloor(),1.5)*0.12)));
        damage = 1;

        type = melee;
        facingRight = true;

        spriteSheetCols = 6;
        spriteSheetRows = 2;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\redslime.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\redslime.tga");
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
        animation.setDelay(145);

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

        projectiles = 0;
        projectilesShooted = false;

    }
    public RedSlime(TileMap tm, Player[] player) {
        super(tm,player);
        if(tm.isServerSide()){
            moveSpeed = 0.6f;
            maxSpeed = 3.6f;
            stopSpeed = 0.5f;

            width = 64;
            height = 48;
            cwidth = 64;
            cheight = 48;
            scale = 2;

            animation = new Animation(4);
            animation.setDelay(145);

            health = maxHealth = (int)(9*(1+(Math.pow(tm.getFloor(),1.5)*0.12)));
            damage = 1;

            type = melee;
            facingRight = true;

            // because of scaling image by 2x
            width *= 2;
            height *= 2;
            cwidth *= 2;
            cheight *= 2;

            bullets = new ArrayList<>(20);

            projectiles = 0;
            projectilesShooted = false;
        } else {
            moveSpeed = 0.6f;
            maxSpeed = 3.6f;
            stopSpeed = 0.5f;

            width = 64;
            height = 48;
            cwidth = 64;
            cheight = 48;
            scale = 2;

            health = maxHealth = (int)(9*(1+(Math.pow(tm.getFloor(),1.5)*0.12)));
            damage = 1;

            type = melee;
            facingRight = true;

            spriteSheetCols = 6;
            spriteSheetRows = 2;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\redslime.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\redslime.tga");
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
            animation.setDelay(145);

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

            projectiles = 0;
            projectilesShooted = false;
        }

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
            RedSlimebullet redslimebullet = bullets.get(i);
            redslimebullet.update();
            for(Player p : player){
                if(p != null){
                    if(redslimebullet.intersects(p) && !p.isFlinching() && !p.isDead()){
                        redslimebullet.setHit();
                        p.hit(1);
                    }
                }
            }
            for(RoomObject object: tileMap.getRoomMapObjects()){
                if(object instanceof DestroyableObject) {
                    if (redslimebullet.intersects(object) && !redslimebullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                        redslimebullet.setHit();
                        ((DestroyableObject) object).setHit(1);
                    }
                } else if(object.collision && redslimebullet.intersects(object)){
                    redslimebullet.setHit();
                }
            }

            if(redslimebullet.shouldRemove()) {
                bullets.remove(i);
                i--;
            }
        }
        if (!projectilesShooted && dead) {
            projectilesShooted = true;

            int index = theClosestPlayerIndex();
            for (int i = 0; i < projectiles; i++) {
                RedSlimebullet redSlimebullet = new RedSlimebullet(tileMap, px[index] - position.x, py[index] - position.y, 1.3 * i);
                redSlimebullet.setPosition(position.x, position.y);
                bullets.add(redSlimebullet);
            }
        }
        if(dead) return;

        super.update();
        movePacket();

    }

    public void draw() {
        for(RedSlimebullet bullet : bullets){
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
        projectiles++;
        if(health == 0){
            if(tileMap.isServerSide()){
                animation = new Animation(6);
                animation.setDelay(65);
            } else {
                animation.setFrames(spritesheet.getSprites(DEAD));
                animation.setDelay(65);
            }
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
        animation.setDelay(145);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 2x
        width *= 2;
        height *= 2;

        for(RedSlimebullet slimebullet: bullets){
            slimebullet.loadSave();
        }

        createShadow();
    }
}


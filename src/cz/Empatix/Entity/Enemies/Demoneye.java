package cz.Empatix.Entity.Enemies;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;

public class Demoneye extends Enemy {
    private static final int IDLE = 0;
    private static final int DEAD = 1;
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Enemies\\demoneye.tga");
    }
    public Demoneye(TileMap tm, Player player) {

        super(tm,player);

        moveSpeed = 2f;
        maxSpeed = 8.5f;
        stopSpeed = 1.6f;

        width = 76;
        height = 64;
        cwidth = 76;
        cheight = 64;
        scale = 2;


        health = maxHealth = (int)(11*(1+(Math.pow(tm.getFloor(),1.5)*0.12)));
        damage = 3;

        type = melee;
        facingRight = true;

        spriteSheetCols = 4;
        spriteSheetRows = 1;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\demoneye.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\demoneye.tga");
            Sprite[] sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                //Sprite sprite = new Sprite(texCoords);
                Sprite sprite = new Sprite(5,i,0,width,height,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            /*sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                Sprite sprite = new Sprite(5,i,1,width,height,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

             */
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
        width *= 2;
        height *= 2;
        cwidth *= 2;
        cheight *= 2;

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

        if(dead) return;

        // ENEMY AI
        EnemyAI();

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
    }
    @Override
    public void drawShadow() {
        drawShadow(5.5f);
    }

    @Override
    public void loadSave() {
        super.loadSave();
        width = 76;
        height = 64;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\demoneye.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\demoneye.tga");
            Sprite[] sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                //Sprite sprite = new Sprite(texCoords);
                Sprite sprite = new Sprite(5,i,0,width,height,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            /*sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                Sprite sprite = new Sprite(5,i,1,width,height,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

             */
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
        width *= 2;
        height *= 2;

        createShadow();
    }
}

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

public class Snake extends Enemy {
    private static final int IDLE = 0;
    private static final int DEAD = 1;

    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Enemies\\snake.tga");
    }
    public Snake(TileMap tm, Player player) {
        super(tm,player);

        moveSpeed = 1.4f;
        maxSpeed = 4.6f;
        stopSpeed = 0.35f;

        width = 64;
        height = 64;
        cwidth = 32;
        cheight = 30;
        scale = 3;

        health = maxHealth = (int)(9*(1+(Math.pow(tm.getFloor(),1.5)*0.12)));
        damage = 2;

        type = melee;
        facingRight = true;

        spriteSheetCols = 9;
        spriteSheetRows = 2;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\snake.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\snake.tga");
            Sprite[] sprites = new Sprite[9];
            for(int i = 0; i < sprites.length; i++) {
                //Sprite sprite = new Sprite(texCoords);
                Sprite sprite = new Sprite(5,i,0,width,height,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            sprites = new Sprite[5];
            for(int i = 0; i < sprites.length; i++) {
                Sprite sprite = new Sprite(5,i,1,width,height,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);
        }
                vboVertices = ModelManager.getModel(width,64);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,64);
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
    }

    public Snake(TileMap tm, Player[] player) {
        super(tm,player);
        if(tm.isServerSide()){
            moveSpeed = 1.4f;
            maxSpeed = 4.6f;
            stopSpeed = 0.35f;

            width = 64;
            height = 64;
            cwidth = 32;
            cheight = 30;
            scale = 3;

            animation = new Animation(9);
            animation.setDelay(125);

            health = maxHealth = (int)(9*(1+(Math.pow(tm.getFloor(),1.5)*0.12)));
            damage = 2;

            type = melee;
            facingRight = true;

            // because of scaling image by 2x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

        } else {
            moveSpeed = 1.4f;
            maxSpeed = 4.6f;
            stopSpeed = 0.35f;

            width = 64;
            height = 64;
            cwidth = 32;
            cheight = 30;
            scale = 3;

            health = maxHealth = (int)(9*(1+(Math.pow(tm.getFloor(),1.5)*0.12)));
            damage = 2;

            type = melee;
            facingRight = true;

            spriteSheetCols = 9;
            spriteSheetRows = 2;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\snake.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\snake.tga");
                Sprite[] sprites = new Sprite[9];
                for(int i = 0; i < sprites.length; i++) {
                    //Sprite sprite = new Sprite(texCoords);
                    Sprite sprite = new Sprite(5,i,0,width,height,spriteSheetRows,spriteSheetCols);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);

                sprites = new Sprite[5];
                for(int i = 0; i < sprites.length; i++) {
                    Sprite sprite = new Sprite(5,i,1,width,height,spriteSheetRows,spriteSheetCols);
                    sprites[i] = sprite;

                }
                spritesheet.addSprites(sprites);
            }
            vboVertices = ModelManager.getModel(width,64);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,64);
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
        }
    }


    @Override
    public void update() {
        setMapPosition();
        if(isSpawning()) return;
        // update animation
        animation.update();

        if(dead) return;

        float dist = position.distance(player[0].getPosition());
        if(dist < 600){
            maxSpeed = 4.6f + 8.5f * (1f - dist/600);
        } else {
            maxSpeed = 4.6f;
        }
        super.update();
        movePacket();

    }

    @Override
    public void hit(int damage) {
        if(dead || isSpawning()) return;
        lastTimeDamaged=System.currentTimeMillis()-InGame.deltaPauseTime();
        health -= damage;
        if(health < 0) health = 0;
        if(health == 0){
            if(tileMap.isServerSide()){
                animation = new Animation(5);
                animation.setDelay(100);
            } else {
                animation.setFrames(spritesheet.getSprites(DEAD));
                animation.setDelay(100);
            }
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
        drawShadow(5f,20);
    }

    @Override
    public void loadSave() {
        super.loadSave();
        width = 64;
        height = 40;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\snake.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\snake.tga");
            Sprite[] sprites = new Sprite[9];
            for(int i = 0; i < sprites.length; i++) {
                //Sprite sprite = new Sprite(texCoords);
                Sprite sprite = new Sprite(5,i,0,width,height,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            sprites = new Sprite[5];
            for(int i = 0; i < sprites.length; i++) {
                Sprite sprite = new Sprite(5,i,1,width,height,spriteSheetRows,spriteSheetCols);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);
        }
        vboVertices = ModelManager.getModel(width,32);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,32);
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
    }
}

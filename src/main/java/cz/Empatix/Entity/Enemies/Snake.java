package cz.Empatix.Entity.Enemies;

import cz.Empatix.Buffs.Blindness;
import cz.Empatix.Buffs.BuffManager;
import cz.Empatix.Buffs.BuffManagerMP;
import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Multiplayer.MultiplayerManager;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;

public class Snake extends Enemy {
    private static final int IDLE = 0;
    private static final int DEAD = 1;

    public Snake(TileMap tm, Player player) {
        super(tm,player);
        initStats(tm.getFloor());

        width = 64;
        height = 64;
        cwidth = 32;
        cheight = 30;
        scale = 3;

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
        initStats(tm.getFloor());
        if(tm.isServerSide()){
            width = 64;
            height = 64;
            cwidth = 32;
            cheight = 30;
            scale = 3;

            animation = new Animation(9);
            animation.setDelay(125);

            facingRight = true;

            // because of scaling image by 2x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

        } else {
            width = 64;
            height = 64;
            cwidth = 32;
            cheight = 30;
            scale = 3;
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
    public void initStats(int floor){
        movementVelocity = 400;
        moveAcceleration = 4f;
        stopAcceleration = 3f;

        health = maxHealth = (int)(8*(1+(Math.pow(floor,1.25)*0.12)));
        tryBoostHealth();
        damage = 2;

        type = melee;
    }

    @Override
    public void update() {
        setMapPosition();
        if(isSpawning()) return;
        // update animation
        animation.update();

        if(dead) return;

        super.update();
        movePacket();

    }

    @Override
    public void hit(int damage) {
        super.hit(damage);
        if(isDead()){
            if(tileMap.isServerSide()){
                animation = new Animation(5);
                animation.setDelay(100);
            } else {
                animation.setFrames(spritesheet.getSprites(DEAD));
                animation.setDelay(100);
            }
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
    public void handleAddEnemyProjectile(Network.AddEnemyProjectile o) {
    }

    @Override
    public void handleMoveEnemyProjectile(Network.MoveEnemyProjectile o) {
    }

    @Override
    public void handleHitEnemyProjectile(Network.HitEnemyProjectile hitPacket) {

    }
    public void forceRemove(){
    }

    @Override
    public void applyHitEffects(Player hitPlayer) {
        if(tileMap.isServerSide()){
            if(Math.random() > 0.5){
                Blindness blindness = new Blindness();
                BuffManagerMP buffManager = BuffManagerMP.getInstance();
                buffManager.addBuff(blindness,((PlayerMP)(hitPlayer)).getIdConnection());
            }
        } else if(!MultiplayerManager.multiplayer){
            if(Math.random() > 0.5){
                Blindness blindness = new Blindness();
                BuffManager buffManager = BuffManager.getInstance();
                buffManager.addBuff(blindness);
            }
        }
    }
}

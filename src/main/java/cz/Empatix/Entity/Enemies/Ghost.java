package cz.Empatix.Entity.Enemies;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.Singleplayer.InGame;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;

public class Ghost extends Enemy {
    private static final int IDLE = 0;
    private static final int DEAD = 1;

    private long cdRush;
    private boolean rush;

    private int defaultSpeed;

    public Ghost(TileMap tm, Player player) {
        super(tm,player);
        initStats(tm.getFloor());

        width = 22;
        height = 28;
        cwidth = 22;
        cheight = 28;
        scale = 5;

        facingRight = true;

        spriteSheetCols = 5;
        spriteSheetRows = 2;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\ghost.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\ghost.tga");
            Sprite[] sprites = new Sprite[4];
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
        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(IDLE));
        animation.setDelay(100);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        createShadow();

    }
    public Ghost(TileMap tm, Player[] player) {
        super(tm,player);
        initStats(tm.getFloor());
        if(tm.isServerSide()){
            width = 22;
            height = 28;
            cwidth = 22;
            cheight = 28;
            scale = 5;

            facingRight = true;

            animation = new Animation(4);
            animation.setDelay(100);

            // because of scaling image
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;
        } else {
            width = 22;
            height = 28;
            cwidth = 22;
            cheight = 28;
            scale = 5;

            facingRight = true;

            spriteSheetCols = 5;
            spriteSheetRows = 2;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Enemies\\ghost.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Enemies\\ghost.tga");
                Sprite[] sprites = new Sprite[4];
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
            vboVertices = ModelManager.getModel(width,height);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,height);
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(IDLE));
            animation.setDelay(100);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }
            // because of scaling image
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            createShadow();
        }

    }
    public void initStats(int floor){
        movementVelocity = 430;
        moveAcceleration = 6f;
        stopAcceleration = 2f;

        defaultSpeed = movementVelocity;

        health = maxHealth = (int)(11*(1+(Math.pow(floor,1.25)*0.12)));
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

        if(System.currentTimeMillis()-cdRush-InGame.deltaPauseTime() > 3000){
            cdRush = System.currentTimeMillis()-InGame.deltaPauseTime();
            defaultSpeed = movementVelocity;
            movementVelocity *=2;
            rush = true;
        } else if (rush && System.currentTimeMillis()-cdRush-InGame.deltaPauseTime() > 700){
            movementVelocity = defaultSpeed;
            rush = false;
        }
        super.update();
        movePacket();

    }

    @Override
    public void hit(int damage) {
        super.hit(damage);
        if(isDead()){
            if(tileMap.isServerSide()){
                animation = new Animation(5);
                animation.setDelay(85);
            } else {
                animation.setFrames(spritesheet.getSprites(DEAD));
                animation.setDelay(85);
            }
        }
    }

    @Override
    public void draw() {
        super.draw();
    }

    @Override
    public void drawShadow() {
        drawShadow(3.5f);
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

    }
}

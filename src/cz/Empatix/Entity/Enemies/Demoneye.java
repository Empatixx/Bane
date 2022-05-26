package cz.Empatix.Entity.Enemies;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.Player;
import cz.Empatix.Java.Loader;
import cz.Empatix.Multiplayer.Network;
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
        initStats(tm.getFloor());

        width = 76;
        height = 64;
        cwidth = 76;
        cheight = 64;
        scale = 2;

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
    public Demoneye(TileMap tm, Player[] player) {
        super(tm,player);
        initStats(tm.getFloor());
        if(tm.isServerSide()){
            width = 76;
            height = 64;
            cwidth = 76;
            cheight = 64;
            scale = 2;

            animation = new Animation(4);
            animation.setDelay(125);

            facingRight = true;
            width *= 2;
            height *= 2;
            cwidth *= 2;
            cheight *= 2;
        } else {
            width = 76;
            height = 64;
            cwidth = 76;
            cheight = 64;
            scale = 2;

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

    }
    public void initStats(int floor){
        movementVelocity =  390;
        moveAcceleration = 6f;
        stopAcceleration = 5f;

        health = maxHealth = (int)(11*(1+(Math.pow(floor,1.25)*0.12)));
        tryBoostHealth();
        damage = 3;

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
    public boolean shouldRemove(){
        return isDead();
    }
}

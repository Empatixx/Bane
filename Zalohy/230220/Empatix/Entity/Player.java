package cz.Empatix.Entity;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Graphics.Model.ModelManager;
import cz.Empatix.Graphics.Shaders.ShaderManager;
import cz.Empatix.Graphics.Sprites.Sprite;
import cz.Empatix.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.TileMap;

import java.util.ArrayList;


public class Player extends MapObject {

    private boolean shooting;
    private boolean dead;

    // vignette ( player hurt - effect )
    private Background hitVignette;
    private long heartBeat;
    private boolean lowHealth;

    // STUFF

    private int health;
    private int maxHealth;
    private int energy;
    private int maxEnergy;


    // animations
    private static final int IDLE = 0;
    private static final int SIDE = 1;
    private static final int DOWN = 2;
    private static final int UP = 3;

    // audio
    private final int[] soundPlayerhurt;

    private final Source sourcehealth;
    private final int soundLowHealth;



    public Player(TileMap tm, Camera camera) {
        super(tm);
        width = 32;
        height = 76;
        // spritesheet
        spriteSheetCols = 6;
        spriteSheetRows = 4;

        // COLLISION WIDTH/HEIGHT
        cwidth = 32;
        cheight = 76;

        moveSpeed = 0.68f;
        maxSpeed = 11.84f;
        stopSpeed = 1.6f;

        health = maxHealth = 5;
        energy = maxEnergy = 100;

        shooting = false;
        dead = false;
        flinching = false;
        facingRight = true;

        final int[] numFrames = {
                6, 6, 6, 6
        };

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Player\\player64.tga");


        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Player\\player64.tga");
            for(int i = 0; i < spriteSheetRows; i++) {

                Sprite[] images = new Sprite[numFrames[i]];

                for (int j = 0; j < numFrames[i]; j++) {

                    double[] texCoords =
                            {
                                    (double) j / spriteSheetCols, (double) i / spriteSheetRows,

                                    (double) j / spriteSheetCols, (1.0 + i) / spriteSheetRows,

                                    (1.0 + j) / spriteSheetCols, (1.0 + i) / spriteSheetRows,

                                    (1.0 + j) / spriteSheetCols, (double) i / spriteSheetRows
                            };


                    Sprite sprite = new Sprite(texCoords);

                    images[j] = sprite;

                }

                spritesheet.addSprites(images);
            }
        }



        currentAction = IDLE;

        animation = new Animation();
        animation.setFrames(spritesheet.getSprites(IDLE));
        animation.setDelay(100);

        vboVertexes = ModelManager.getModel(width,height);
        if (vboVertexes == -1){
            vboVertexes = ModelManager.createModel(width,height);
        }

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        // because of scaling image by 2x
        width *= 2;
        height *= 2;
        cwidth *= 2;
        cheight *= 2;


        //hit vignette
        hitVignette = new Background("Textures\\vignette.tga", camera);
        hitVignette.setFadeEffect(true);

        // audio
        soundPlayerhurt = new int[2];
        soundPlayerhurt[0] = AudioManager.loadSound("playerhurt_1.ogg");
        soundPlayerhurt[1] = AudioManager.loadSound("playerhurt_2.ogg");


        sourcehealth = new Source();
        soundLowHealth = AudioManager.loadSound("lowhealth.ogg");
        sourcehealth.setVolume(1f);
        sourcehealth.setLooping(true);
        //sourcehealth.setLooping(true);

        source.setVolume(0.45f);







    }

    public void update() {
        // check if player is not dead
        // TODO: gamestate for dying
        if (health < 0) {
            dead = true;
        } else if (health < 3 && !lowHealth){
            lowHealth = true;
            sourcehealth.play(soundLowHealth);
        }
        else if (health >= 3 && lowHealth){
            lowHealth = false;
            sourcehealth.stop();
        }

        if (lowHealth && (float)(System.currentTimeMillis()-heartBeat)/1000 > 0.85f){
            heartBeat = System.currentTimeMillis();
            hitVignette.updateFadeTime();
        }
            /*
        } else if (health < 3 && !sourcehealth.isPlaying()){
            sourcehealth.play(soundLowHealth);
        } else if (health > 3 && sourcehealth.isPlaying()){
            sourcehealth.stop();
        }
        System.out.print((float)(System.currentTimeMillis()-heartBeat)/1000+"\n");
        heartBeat = System.currentTimeMillis();
        hitVignette.updateFadeTime();
*/

        getMovementSpeed();
        checkTileMapCollision();
        setPosition(temp.x, temp.y);

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

        // next sprite of player
        animation.update();

        // direction of player
        if (left) facingRight = false;
        if (right) facingRight = true;

        //  NESMRTELNOST PO DOSTANI HITU
        if (flinching){
            if ((float)(System.nanoTime()-flinchingTimer)/ 1000000000 > 1.5) {
                flinching = false;
            }
        }
        hitVignette.update();
    }
    public void checkCollision(ArrayList<Enemy> enemies){
        for (Enemy currentEnemy:enemies){
            // check player X enemy collision
            if (intersects(currentEnemy)){
                hit(currentEnemy.getDamage());
            }
        }
    }

    private void getMovementSpeed() {
        // MAKING CHARACTER MOVE
        if (right){
            speed.x += moveSpeed;
            if (speed.x > maxSpeed){
                speed.x = maxSpeed;
            }
        }
        else if (left){
            speed.x -= moveSpeed;
            if (speed.x < -maxSpeed){
                speed.x = -maxSpeed;
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

        if (up){
            speed.y -= moveSpeed;
            if (speed.y < -maxSpeed){
                speed.y = -maxSpeed;
            }
        }
        else if (down){
            speed.y += moveSpeed;
            if (speed.y > maxSpeed){
                speed.y = maxSpeed;
            }
        }
        else {
            if (speed.y < 0){
                speed.y += stopSpeed;
                if (speed.y > 0) speed.y = 0;
            } else if (speed.y > 0){
                speed.y -= stopSpeed;
                if (speed.y < 0) speed.y = 0;
            }
        }

    }

    public void draw(Camera camera) {
        setMapPosition();
        super.draw(camera);
        if(flinching || lowHealth){
            hitVignette.draw();
        }
    }
    public void keyPressed(int key) {
        if (key == 'W'){
            setUp(true);
        }
        if (key == 'D'){
            setRight(true);
        }
        if (key == 'A'){
            setLeft(true);
        }
        if (key == 'S'){
            setDown(true);
        }
    }
    public void keyReleased(int key) {
        if (key == 'W'){
            setUp(false);
        }
        if (key == 'D'){
            setRight(false);
        }
        if (key == 'A'){
            setLeft(false);
        }
        if (key == 'S'){
            setDown(false);
        }
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }
    public void hit(int damage){
        if (flinching) return;
        health -= damage;
        if (health < 0) health = 0;
        if (health == 0) dead = true;
        flinching = true;
        flinchingTimer = System.nanoTime();
        hitVignette.updateFadeTime();
        source.play(soundPlayerhurt[Random.nextInt(2)]);

    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }
}

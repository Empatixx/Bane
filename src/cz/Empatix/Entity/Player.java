package cz.Empatix.Entity;

import cz.Empatix.AudioManager.AudioManager;
import cz.Empatix.AudioManager.Source;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.ControlSettings;
import cz.Empatix.Render.Background;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.Serializable;
import java.util.ArrayList;

import static cz.Empatix.Main.Game.window;
import static org.lwjgl.glfw.GLFW.*;
public class Player extends MapObject implements Serializable {
    public static void load(){
        Loader.loadImage("Textures\\Sprites\\Player\\player64.tga");
        Loader.loadImage("Textures\\vignette.tga");
        Loader.loadImage("Textures\\armorvignette.tga");
        Loader.loadImage("Textures\\Sprites\\Player\\sprint_particle.tga");
        Loader.loadImage("Textures\\shadow.tga");
    }
    // roll
    private long rollCooldown;
    private boolean rolling;


    private boolean dead;
    private long deathTime;

    // vignette ( player hurt - effect )
    transient private Background[] hitVignette;
    private long heartBeat;
    private boolean lowHealth;
    private DamageAbsorbedBy lastDamage;
    // if damage was absorbed by health or armor
    private enum DamageAbsorbedBy {
        ARMOR,
        HEALTH
    }

    // STUFF

    private int health;
    private int maxHealth;
    private int energy;
    private int maxEnergy;

    private int maxArmor;
    private int armor;

    private int coins;


    // animations
    private static final int IDLE = 0;
    private static final int SIDE = 1;
    private static final int DOWN = 2;
    private static final int UP = 3;

    transient ArrayList<SprintParticle> sprintParticles;
    private long lastTimeSprintParticle;

    // audio
    private int[] soundPlayerhurt;
    private int soundPlayerdeath;

    transient private Source sourcehealth;
    private int soundLowHealth;

    // visual


    public Player(TileMap tm) {
        super(tm);
        //width = cwidth= 17;
        //height = cheight = 24;
        width = cwidth= 32;
        height = cheight = 72;
        // spritesheet
        spriteSheetCols = 6;
        spriteSheetRows = 4;

        // COLLISION WIDTH/HEIGHT
        scale = 2;

        moveSpeed = 0.8f;
        maxSpeed = 11.84f;
        stopSpeed = 3.25f;

        health = maxHealth = 7;
        energy = maxEnergy = 100;

        coins = 0;

        armor = maxArmor = 3;

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

                    float[] texCoords =
                            {
                                    (float) j / spriteSheetCols, (float) i / spriteSheetRows,

                                    (float) j / spriteSheetCols, (1.0f + i) / spriteSheetRows,

                                    (1.0f + j) / spriteSheetCols, (1.0f + i) / spriteSheetRows,

                                    (1.0f + j) / spriteSheetCols, (float) i / spriteSheetRows
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

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }

        // because of scaling image by 5x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        //hit vignette
        hitVignette = new Background[2];

        hitVignette[0] = new Background("Textures\\vignette.tga");
        hitVignette[0].setFadeEffect(true);

        // hit armor vignette
        hitVignette[1] = new Background("Textures\\armorvignette.tga");
        hitVignette[1].setFadeEffect(true);


        // audio
        soundPlayerhurt = new int[2];
        soundPlayerhurt[0] = AudioManager.loadSound("playerhurt_1.ogg");
        soundPlayerhurt[1] = AudioManager.loadSound("playerhurt_2.ogg");
        soundPlayerdeath = AudioManager.loadSound("playerdeath.ogg");


        sourcehealth = AudioManager.createSource(Source.EFFECTS,1f);
        source = AudioManager.createSource(Source.EFFECTS,0.35f);
        soundLowHealth = AudioManager.loadSound("lowhealth.ogg");
        sourcehealth.setLooping(true);

        light = LightManager.createLight(new Vector3f(0.905f, 0.788f, 0.450f),new Vector2f(0,0),4f,this);

        rolling = false;

        sprintParticles = new ArrayList<>(5);

        createShadow();
    }
    public void loadSave(){
        width = cwidth= 32;
        height = cheight = 72;
        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Player\\player64.tga");
        final int[] numFrames = {
                6, 6, 6, 6
        };
        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Player\\player64.tga");
            for(int i = 0; i < spriteSheetRows; i++) {

                Sprite[] images = new Sprite[numFrames[i]];

                for (int j = 0; j < numFrames[i]; j++) {

                    float[] texCoords =
                            {
                                    (float) j / spriteSheetCols, (float) i / spriteSheetRows,

                                    (float) j / spriteSheetCols, (1.0f + i) / spriteSheetRows,

                                    (1.0f + j) / spriteSheetCols, (1.0f + i) / spriteSheetRows,

                                    (1.0f + j) / spriteSheetCols, (float) i / spriteSheetRows
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

        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1){
            vboVertices = ModelManager.createModel(width,height);
        }

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 5x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;
        //hit vignette
        hitVignette = new Background[2];

        hitVignette[0] = new Background("Textures\\vignette.tga");
        hitVignette[0].setFadeEffect(true);

        // hit armor vignette
        hitVignette[1] = new Background("Textures\\armorvignette.tga");
        hitVignette[1].setFadeEffect(true);


        // audio
        soundPlayerhurt = new int[2];
        soundPlayerhurt[0] = AudioManager.loadSound("playerhurt_1.ogg");
        soundPlayerhurt[1] = AudioManager.loadSound("playerhurt_2.ogg");
        soundPlayerdeath = AudioManager.loadSound("playerdeath.ogg");


        sourcehealth = AudioManager.createSource(Source.EFFECTS,1f);
        source = AudioManager.createSource(Source.EFFECTS,0.35f);
        soundLowHealth = AudioManager.loadSound("lowhealth.ogg");
        sourcehealth.setLooping(true);

        light = LightManager.createLight(new Vector3f(1.0f,1.0f,1.0f),new Vector2f(position.x+xmap,position.y+ymap),5f,this);

        sprintParticles = new ArrayList<>(5);

        createShadow();
    }

    public void update() {
        setMapPosition();
        // check if player should be still rolling
        if(System.currentTimeMillis() - rollCooldown - InGame.deltaPauseTime() >= 400 && rolling){
            rolling = false;
            right=false;
            up=false;
            down=false;
            left=false;
        }
        // check if player is not dead
        if (health <= 0) {
            speed.x = 0;
            speed.y = 0;
            right=false;
            up=false;
            down=false;
            left=false;
            flinching = false;
        } else if (health < 3 && !lowHealth){
            lowHealth = true;
            sourcehealth.play(soundLowHealth);
        }
        else if (health >= 3 && lowHealth){
            lowHealth = false;
            sourcehealth.stop();
        }

        if (lowHealth && (float)(System.currentTimeMillis()-heartBeat-InGame.deltaPauseTime())/1000 > 0.85f){
            heartBeat = System.currentTimeMillis()-InGame.deltaPauseTime();
            if(lastDamage == DamageAbsorbedBy.ARMOR){
                hitVignette[1].updateFadeTime();
            } else {
                hitVignette[0].updateFadeTime();
            }
        }
        if(Math.abs(speed.x) >= maxSpeed || Math.abs(speed.y) >= maxSpeed){
            float value = Math.abs(speed.x);
            if(value < Math.abs(speed.y)) value = Math.abs(speed.y);
            if(System.currentTimeMillis() - InGame.deltaPauseTime() - lastTimeSprintParticle > 400-value*20){
                lastTimeSprintParticle = System.currentTimeMillis()- InGame.deltaPauseTime();
                SprintParticle sprintParticle = new SprintParticle(tileMap);
                if((up || down) && !left && !right){
                    sprintParticle.setPosition(
                            position.x+16*(float)Math.sin(2*Math.PI*((System.currentTimeMillis()%1000)/1000d)),
                            position.y+height/2);
                } else if((right || left) && !up && !down){
                    sprintParticle.setPosition(
                            position.x,
                            position.y+height/2+16*(float)Math.sin(Math.PI*(1+((System.currentTimeMillis()%1000)/1000d))));
                } else {
                    sprintParticle.setPosition(position.x,position.y+height/2);

                }
                sprintParticles.add(sprintParticle);
            }
        }
        for(int i = 0;i<sprintParticles.size();i++){
            SprintParticle sprintParticle = sprintParticles.get(i);

            sprintParticle.update();
            if(sprintParticle.shouldRemove()){
                sprintParticles.remove(i);
                i--;
            }
        }
        getMovementSpeed();
        checkTileMapCollision();
        checkRoomObjectsCollision();

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
            if ((float)(System.currentTimeMillis() - flinchingTimer - InGame.deltaPauseTime())/ 1000 > 1.5) {
                flinching = false;
            }
        }
        if(lastDamage == DamageAbsorbedBy.ARMOR){
            hitVignette[1].update();
        } else {
            hitVignette[0].update();
        }
    }
    public void checkCollision(ArrayList<Enemy> enemies){
        for (Enemy currentEnemy:enemies){
            // check player X enemy collision
            if (intersects(currentEnemy) && !currentEnemy.isDead() && !currentEnemy.isSpawning()){
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

    public void draw() {
        super.draw();
    }

    public void drawVignette(){
        if(lastDamage == DamageAbsorbedBy.ARMOR){
            hitVignette[1].draw();
        } else {
            hitVignette[0].draw();
        }
    }

    public void keyPressed(int key) {
        if(key == GLFW_KEY_SPACE && false){
            if(System.currentTimeMillis() - rollCooldown - InGame.deltaPauseTime() >= 1000 && currentAction != IDLE){
                rollCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
                rolling = true;
            }
        }
        if(rolling) return;
        if (key == ControlSettings.getValue(ControlSettings.MOVE_UP)){
            setUp(true);
        }
        if (key == ControlSettings.getValue(ControlSettings.MOVE_RIGHT)){
            setRight(true);
        }
        if (key == ControlSettings.getValue(ControlSettings.MOVE_LEFT)){
            setLeft(true);
        }
        if (key == ControlSettings.getValue(ControlSettings.MOVE_DOWN)){
            setDown(true);
        }
    }
    public void keyReleased(int key) {
        if(rolling) return;
        if (key == ControlSettings.getValue(ControlSettings.MOVE_UP)){
            setUp(false);
        }
        if (key == ControlSettings.getValue(ControlSettings.MOVE_RIGHT)){
            setRight(false);
        }
        if (key == ControlSettings.getValue(ControlSettings.MOVE_LEFT)){
            setLeft(false);
        }
        if (key == ControlSettings.getValue(ControlSettings.MOVE_DOWN)){
            setDown(false);
        }
    }
    public void hit(int damage){
        if (flinching ||dead || rolling) return;

        int previousArmor = armor;
        int previousHealth = health;

        armor-=damage;
        if(armor < 0){
            armor = 0;
        }
        damage-=previousArmor;
        if (damage < 0) damage = 0;

        health -= damage;
        if (health < 0) health = 0;
        if (health == 0) dead = true;

        flinching = true;
        flinchingTimer = System.currentTimeMillis()-InGame.deltaPauseTime();

        if(previousHealth != health) lastDamage = DamageAbsorbedBy.HEALTH;
        else if (previousArmor != armor) lastDamage = DamageAbsorbedBy.ARMOR;
        if(lastDamage == DamageAbsorbedBy.ARMOR){
            hitVignette[1].updateFadeTime();
        } else {
            hitVignette[0].updateFadeTime();
        }
        source.play(soundPlayerhurt[Random.nextInt(2)]);

        if(health <=0 )setDead();

    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void cleanUp(){
        sourcehealth.delete();
        source.delete();
    }

    public int getArmor() {
        return armor;
    }

    public int getMaxArmor() {
        return maxArmor;
    }
    public void addHealth(int amount){
        health+=amount;
        if(health > maxHealth) health = maxHealth;
    }
    public void addArmor(int amount){
        armor+=amount;
        if(armor > maxArmor) armor = maxArmor;
    }
    public void addCoins(int amount){ coins+=amount;}

    public int getCoins() {
        return coins;
    }

    public boolean isDead() {
        return dead;
    }

    public long getDeathTime() {
        return deathTime;
    }

    public void setDead(){
        deathTime = System.currentTimeMillis();
        dead = true;
        speed.x = 0;
        speed.y = 0;
        right=false;
        up=false;
        down=false;
        left=false;
        flinching = false;
        if(sourcehealth.isPlaying()) sourcehealth.stop();
        lowHealth = false;
        glfwSetInputMode(window,GLFW_CURSOR,GLFW_CURSOR_DISABLED);
        source.play(soundPlayerdeath);
    }
    public void removeCoins(int amount){
        coins-=amount;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setHealth(int health) {
        this.health = health;
        this.maxHealth = health;
    }

    @Override
    public void drawShadow() {
        for(SprintParticle sprintParticle : sprintParticles){
            sprintParticle.draw();
        }
        drawShadow(3f);
    }
    private static class SprintParticle extends MapObject{
        // sprint particles
        SprintParticle(TileMap tm){
            super(tm);
            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\Player\\sprint_particle.tga");


            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\Player\\sprint_particle.tga");
                for(int i = 0; i < 3; i++) {

                    Sprite[] images = new Sprite[3];

                    for (int j = 0; j < 3; j++) {

                        float[] texCoords =
                                {
                                        (float) j / 3, 0,

                                        (float) j / 3, 1,

                                        (1.0f + j) / 3, 1,

                                        (1.0f + j) / 3, 0
                                };


                        Sprite sprite = new Sprite(texCoords);

                        images[j] = sprite;

                    }

                    spritesheet.addSprites(images);
                }
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(0));
            animation.setDelay(85);

            vboVertices = ModelManager.getModel(16,16);
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(16,16);
            }
            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }

            facingRight = true;
            scale = 4;
        }
        public boolean shouldRemove(){
            return animation.hasPlayedOnce();
        }
        public void update(){
            setMapPosition();
            animation.update();
        }
    }
    public void setMaxSpeed(float maxSpeed){ this.maxSpeed = maxSpeed;}

    public float getMaxSpeed(){return maxSpeed;}
}

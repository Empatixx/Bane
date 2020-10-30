package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.ItemDrops.ItemManager;
import cz.Empatix.Java.Random;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class Chest extends RoomObject {
    private static final int IDLE = 0;
    private static final int OPEN = 1;

    private boolean opened;
    private boolean dropGun;
    private boolean dropArtefact;


    public Chest(TileMap tm){
        super(tm);
        width = 16;
        height = 16;
        cwidth = 16;
        cheight = 16;
        scale = 8;

        facingRight = true;
        flinching=false;

        spriteSheetCols = 4;
        spriteSheetRows = 1;

        opened = false;
        collision = true;
        moveable=true;
        preDraw = false;
        speedMoveBoost = 1.75f;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\chest.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\chest.tga");
            Sprite[] sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                double[] texCoords =
                        {
                                (double) i/spriteSheetCols,0,

                                (double)i/spriteSheetCols,0.5,

                                (1.0+i)/spriteSheetCols,0.5,

                                (1.0+i)/spriteSheetCols,0
                        };
                Sprite sprite = new Sprite(texCoords);
                sprites[i] = sprite;

            }
            spritesheet.addSprites(sprites);

            sprites = new Sprite[4];
            for(int i = 0; i < sprites.length; i++) {
                double[] texCoords =
                        {
                                (double) i/spriteSheetCols,0.5,

                                (double)i/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,0.5
                        };
                Sprite sprite = new Sprite(texCoords);
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
        // because of scaling image by 8x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        stopSpeed = 0.55f;
        dropGun= false;
        dropArtefact = false;
    }
    public void enableDropWeapon(){
        dropGun = true;
    }
    public void enableDropArtefact(){
        dropArtefact = true;
    }
    public void update(){
        setMapPosition();
        checkTileMapCollision();
        setPosition(temp.x, temp.y);
        if(opened && animation.hasPlayedOnce()){
            remove=true;

            Vector2f speed = new Vector2f();

            float x = (float) Random.nextDouble()*(-1+Random.nextInt(2)*2);
            float y = (float)Random.nextDouble()*(-1+Random.nextInt(2)*2);

            if(dropGun) ItemManager.dropWeapon((int)position.x,(int)position.y,speed);
            if(dropArtefact) ItemManager.dropArtefact((int)position.x,(int)position.y);

            for(int i = 0;i<5;i++){
                double atan = Math.atan2(x,
                        y) + 1.3 * i;
                speed.x = (float)(Math.cos(atan) * 10);
                speed.y = (float)(Math.sin(atan) * 10);
                ItemManager.createDrop(position.x,position.y,speed);
            }
        }

        animation.update();

        if (speed.x < 0){
            speed.x += stopSpeed;
            if (speed.x > 0) speed.x = 0;
        } else if (speed.x > 0){
            speed.x -= stopSpeed;
            if (speed.x < 0) speed.x = 0;
        }

        if (speed.y < 0){
            speed.y += stopSpeed;
            if (speed.y > 0) speed.y = 0;
        } else if (speed.y > 0){
            speed.y -= stopSpeed;
            if (speed.y < 0) speed.y = 0;
        }
    }

    @Override
    public void touchEvent() {
        open();
    }

    public void open(){
        if(opened) return;
        opened = true;
        collision = false;
        animation.setFrames(spritesheet.getSprites(OPEN));

    }

    @Override
    public void draw() {
        // pokud neni object na obrazovce - zrusit
        if (isNotOnScrean()){
            return;
        }

        Matrix4f target;
        if (facingRight) {
            target = new Matrix4f().translate(position)
                    .scale(scale);
        } else {
            target = new Matrix4f().translate(position)
                    .scale(scale)
                    .rotateY(3.14f);

        }
        Camera.getInstance().projection().mul(target,target);

        shader.bind();
        shader.setUniformi("sampler",0);
        shader.setUniformm4f("projection",target);
        glActiveTexture(GL_TEXTURE0);
        spritesheet.bindTexture();

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);


        glBindBuffer(GL_ARRAY_BUFFER,animation.getFrame().getVbo());
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        shader.unbind();
        glBindTexture(GL_TEXTURE_2D,0);
        glActiveTexture(0);
        if (Game.displayCollisions){
            glColor3i(255,255,255);
            glBegin(GL_LINE_LOOP);
            // BOTTOM LEFT
            glVertex2f(position.x+xmap-cwidth/2,position.y+ymap-cheight/2);
            // TOP LEFT
            glVertex2f(position.x+xmap-cwidth/2, position.y+ymap+cheight/2);
            // TOP RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap+cheight/2);
            // BOTTOM RIGHT
            glVertex2f(position.x+xmap+cwidth/2, position.y+ymap-cheight/2);
            glEnd();

            glPointSize(10);
            glColor3i(255,0,0);
            glBegin(GL_POINTS);
            glVertex2f(position.x+xmap,position.y+ymap);
            glEnd();


        }
    }
    public boolean shouldRemove(){return remove;}
    @Override
    public void keyPress() {

    }
}

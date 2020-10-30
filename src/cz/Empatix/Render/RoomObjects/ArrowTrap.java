package cz.Empatix.Render.RoomObjects;

import cz.Empatix.Entity.Animation;
import cz.Empatix.Entity.MapObject;
import cz.Empatix.Entity.Player;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.Sprite;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class ArrowTrap extends RoomObject {
    public boolean remove;
    private ArrayList<Arrow> arrows;
    public long arrowShootCooldown;
    private Player player;
    public ArrowTrap(TileMap tm, Player p){
        super(tm);
        this.player = p;
        arrows = new ArrayList<>();
        width = 16;
        height = 17;
        cwidth = 8;
        cheight = 8;
        scale = 8;

        facingRight = true;
        flinching=false;

        spriteSheetCols = 2;
        spriteSheetRows = 1;

        collision = false;
        moveable=false;
        preDraw = true;
        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\arrowtrap.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\arrowtrap.tga");
            Sprite[] sprites = new Sprite[2];
            for(int i = 0; i < sprites.length; i++) {
                double[] texCoords =
                        {
                                (double) i/spriteSheetCols,0,

                                (double)i/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,1,

                                (1.0+i)/spriteSheetCols,0
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
        animation.setFrames(spritesheet.getSprites(0));
        animation.setDelay(2000);

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        // because of scaling image by 8x
        width *= scale;
        height *= scale;
        cwidth *= scale;
        cheight *= scale;

        remove = false;
    }

    public void update(){
        setMapPosition();
        animation.update();

        ArrayList<RoomObject> objects = tileMap.getRoomMapObjects();

        for(int i = 0;i<arrows.size();i++){
            Arrow arrow = arrows.get(i);
            arrow.update();
            if(arrow.shouldRemove()){
                arrows.remove(arrow);
                i--;
            }
            if(arrow.intersects(player) && !player.isFlinching() && !player.isDead()){
                player.hit(1);
                arrow.setHit();
            }
           for(RoomObject roomObject:objects){
               if(!roomObject.moveable && roomObject.collision)
               if(roomObject.intersects(arrow)){
                   arrow.setHit();
               }
           }
        }
        if(System.currentTimeMillis() - InGame.deltaPauseTime() - arrowShootCooldown > 4000 && animation.getIndexOfFrame() == 0){
            arrowShootCooldown = System.currentTimeMillis() - InGame.deltaPauseTime();
            Arrow arrow = new Arrow(tileMap);
            arrow.setPosition(position.x + 3,position.y+10);
            arrows.add(arrow);
        }
    }

    @Override
    public void touchEvent() {
        //if(damageAnimation)player.hit(1);
    }

    @Override
    public void draw() {
        for(Arrow arrow:arrows){
            arrow.draw();
        }
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
    public boolean shouldRemove(){
        return remove;
    }
    @Override
    public void keyPress() {

    }
    public static class Arrow extends MapObject {
        // SPRITE VARS
        private final static int sprites = 0;
        private final static int hitSprites = 1;

        // BASIC VARS
        private boolean hit;
        private boolean remove;

        private long collisionBypass;

        public Arrow(TileMap tm) {

            super(tm);
            facingRight = true;

            width = 32;
            height = 32;

            cwidth = 14;
            cheight = 24;

            scale = 4;

            this.speed.y = 10f;


            // load sprites
            spriteSheetCols = 4;
            spriteSheetRows = 2;

            // try to find spritesheet if it was created once
            spritesheet = SpritesheetManager.getSpritesheet("Textures\\Sprites\\arrow.tga");

            // creating a new spritesheet
            if (spritesheet == null){
                spritesheet = SpritesheetManager.createSpritesheet("Textures\\Sprites\\arrow.tga");

                Sprite[] images = new Sprite[4];

                for(int i = 0; i < images.length; i++) {
                    double[] texCoords =
                            {
                                    (double)i/spriteSheetCols,0,

                                    (double)i/spriteSheetCols,1.0/spriteSheetRows,

                                    (i+1.0)/spriteSheetCols,1.0/spriteSheetRows,

                                    (i+1.0f)/spriteSheetCols,0
                            };
                    Sprite sprite = new Sprite(texCoords);

                    images[i] = sprite;

                }
                spritesheet.addSprites(images);

                images = new Sprite[3];

                for(int i = 0; i < images.length; i++) {
                    double[] texCoords =
                            {
                                    (double)i/spriteSheetCols,0.5,

                                    (double)i/spriteSheetCols,1.0,

                                    (i+1.0)/spriteSheetCols,1.0,

                                    (i+1.0f)/spriteSheetCols,0.5
                            };
                    Sprite sprite = new Sprite(texCoords);

                    images[i] = sprite;

                }
                spritesheet.addSprites(images);
            }

            vboVertices = ModelManager.getModel(width,height   );
            if (vboVertices == -1){
                vboVertices = ModelManager.createModel(width,height);
            }

            animation = new Animation();
            animation.setFrames(spritesheet.getSprites(sprites));
            animation.setDelay(140);

            shader = ShaderManager.getShader("shaders\\shader");
            if (shader == null){
                shader = ShaderManager.createShader("shaders\\shader");
            }

            // because of scaling image by 2x
            width *= scale;
            height *= scale;
            cwidth *= scale;
            cheight *= scale;

            collisionBypass = System.currentTimeMillis() - InGame.deltaPauseTime();

        }

        public void setHit() {
            if(hit) return;
            hit = true;
            animation.setFrames(spritesheet.getSprites(hitSprites));
            animation.setDelay(90);
            speed.x = 0;
            speed.y = 0;
        }

        public boolean shouldRemove() { return remove; }

        public void update() {
            setMapPosition();
            if(System.currentTimeMillis() - collisionBypass - InGame.deltaPauseTime() < 200){
                temp.x = position.x + speed.x;
                temp.y = position.y + speed.y;
                setPosition(temp.x, temp.y);
            } else {
                checkTileMapCollision();
            }
            setPosition(temp.x, temp.y);

            if(speed.y == 0 && !hit) {
                setHit();
            }

            animation.update();
            if(hit) {
                if (animation.hasPlayedOnce()){
                    remove = true;
                }
            }

        }

        public void draw() {
            super.draw();
        }
        public boolean isHit() {return hit;}
    }
}

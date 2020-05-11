package cz.Empatix.Entity.ItemDrops;

import cz.Empatix.Entity.MapObject;
import cz.Empatix.Gamestates.InGame;
import cz.Empatix.Guns.Weapon;
import cz.Empatix.Main.Game;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Graphics.Model.ModelManager;
import cz.Empatix.Render.Graphics.Shaders.ShaderManager;
import cz.Empatix.Render.Graphics.Sprites.SpritesheetManager;
import cz.Empatix.Render.Postprocessing.Lightning.LightManager;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.DoubleBuffer;

import static org.lwjgl.opengl.GL20.*;

public class WeaponDrop extends ItemDrop {
    private int vboVertices;
    private int vboTextures;
    private int vboVerticesHud;
    private int textureId;

    private boolean canPick;

    private Weapon weapon;

    public WeaponDrop(TileMap tm,Weapon weapon, int x,int y){
        super(tm);
        this.weapon = weapon;
        type = GUN;
        canDespawn = false;
        liveTime = System.currentTimeMillis();
        pickedUp = false;

        width=cwidth=weapon.getWeaponHud().getWidth();
        height=cheight=weapon.getWeaponHud().getHeight();
        scale = 1.5f;
        facingRight = true;

        shader = ShaderManager.getShader("shaders\\shader");
        if (shader == null){
            shader = ShaderManager.createShader("shaders\\shader");
        }
        vboVertices = ModelManager.getModel(width,height);
        if (vboVertices == -1) {
            vboVertices = ModelManager.createModel(width, height);
        }

        vboVerticesHud = ModelManager.getModel(width+10,height+10);
        if (vboVerticesHud == -1) {
            vboVerticesHud = ModelManager.createModel(width+10, height+10);
        }

        // clicking icon
        double[] texCoords =
                {
                        0,0,
                        0,1,
                        1,1,
                        1,0
                };

        DoubleBuffer buffer = BufferUtils.createDoubleBuffer(texCoords.length);
        buffer.put(texCoords);
        buffer.flip();
        vboTextures = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glBufferData(GL_ARRAY_BUFFER,buffer,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER,0);

        textureId = weapon.getWeaponHud().getIdTexture();

        light = LightManager.createLight(new Vector3f(1.0f,0.8274f,0.0f),new Vector2f(0,0),1.25f,this);

        double atan = Math.atan2(y,x);
        speed.x = (float)(Math.cos(atan) * 10);
        speed.y = (float)(Math.sin(atan) * 10);
        maxSpeed = 10;
        moveSpeed = 1;
        stopSpeed = 0.35f;

        cwidth*=scale;
        cheight*=scale;

        // try to find spritesheet if it was created once
        spritesheet = SpritesheetManager.getSpritesheet("Textures\\weapon_drop.tga");

        // creating a new spritesheet
        if (spritesheet == null){
            spritesheet = SpritesheetManager.createSpritesheet("Textures\\weapon_drop.tga");
        }

    }
    public void draw(){

        long timeNow = System.currentTimeMillis();
        if((float)(timeNow - liveTime - InGame.deltaPauseTime())/1000 > 25  && canDespawn){
            if((timeNow - liveTime-InGame.deltaPauseTime()) / 10 % 2 == 0) return;
        }

        setMapPosition();

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
        glBindTexture(GL_TEXTURE_2D,textureId);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexAttribPointer(0,2,GL_INT,false,0,0);


        glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
        glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

        glDrawArrays(GL_QUADS, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER,0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);



        if(canPick){
            glActiveTexture(GL_TEXTURE0);
            spritesheet.bindTexture();

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);


            glBindBuffer(GL_ARRAY_BUFFER, vboVerticesHud);
            glVertexAttribPointer(0,2,GL_INT,false,0,0);


            glBindBuffer(GL_ARRAY_BUFFER,vboTextures);
            glVertexAttribPointer(1,2,GL_DOUBLE,false,0,0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER,0);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
        }

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

    @Override
    public boolean intersects(MapObject o) {
        Rectangle o1 = getRectangleGun();
        Rectangle o2 = o.getRectangle();
        return o2.intersects(o1);
    }
    private Rectangle getRectangleGun() {
        return new Rectangle(
                (int)position.x-cwidth/2-25,
                (int)position.y-cheight/2-25,
                cwidth+50,
                cheight+50
        );
    }

    @Override
    public void update() {
        getMovementSpeed();
        checkTileMapCollision();
        setPosition(temp.x, temp.y);
    }
    private void getMovementSpeed() {
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

    public void setCanPick(boolean canPick) {
        this.canPick = canPick;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public boolean isCanPick() {
        return canPick;
    }

    public double distance(float px, float py){
        return Math.sqrt(Math.pow(position.x-px,2)+Math.pow(position.y-py,2));
    }
}

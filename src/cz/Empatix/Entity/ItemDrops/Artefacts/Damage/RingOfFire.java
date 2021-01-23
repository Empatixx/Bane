package cz.Empatix.Entity.ItemDrops.Artefacts.Damage;

import cz.Empatix.Entity.Enemies.KingSlime;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.Bullet;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Damageindicator.DamageIndicator;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.RoomObjects.DestroyableObject;
import cz.Empatix.Render.RoomObjects.RoomObject;
import cz.Empatix.Render.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

public class RingOfFire extends Artefact {
    public static void load(){
        Loader.loadImage("Textures\\artefacts\\rof.tga");
        Loader.loadImage("Textures\\artefacts\\artifactcharge.tga");
    }
    private ArrayList<Bullet> bullets;
    public RingOfFire(TileMap tm, Player p){
        super(tm,p);
        maxCharge = 4;
        charge = 0;

        scale = 4;

        imageArtefact = new Image("Textures\\artefacts\\rof.tga",new Vector3f(1403,975,0),
                scale);
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        rarity = 1;

        bullets = new ArrayList<>(50);

    }

    @Override
    public void loadSave() {
        imageArtefact = new Image("Textures\\artefacts\\rof.tga",new Vector3f(1403,975,0),
                scale);
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        for(Bullet bullet : bullets){
            bullet.loadSave();
        }
    }

    @Override
    protected void update() {
        for(int i = 0;i<bullets.size();i++){
            Bullet bullet = bullets.get(i);
            bullet.update();
            if(bullet.shouldRemove()){
                bullets.remove(i);
                i--;
            }
        }
        ArrayList<RoomObject> objects = tm.getRoomMapObjects();
        A: for(Bullet bullet:bullets){
            EnemyManager enemyManager = EnemyManager.getInstance();
            for(Enemy enemy: enemyManager.getEnemies()){
                if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
                    if(enemy instanceof KingSlime) bullet.setDamage(1);
                    enemy.hit(bullet.getDamage());
                    int cwidth = enemy.getCwidth();
                    int cheight = enemy.getCheight();
                    int x = -cwidth/4+ Random.nextInt(cwidth/2);
                    if(bullet.isCritical()){
                        DamageIndicator.addCriticalDamageShow(bullet.getDamage(),(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                                ,new Vector2f(-x/25f,-1f));
                    } else {
                        DamageIndicator.addDamageShow(bullet.getDamage(),(int)enemy.getX()-x,(int)enemy.getY()-cheight/3
                                ,new Vector2f(-x/25f,-1f));
                    }
                    bullet.playEnemyHit();
                    bullet.setHit();
                    continue A;
                }
            }
            for(RoomObject object: objects){
                if(object instanceof DestroyableObject) {
                    if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                        bullet.playEnemyHit();
                        bullet.setHit();
                        ((DestroyableObject) object).setHit(bullet.getDamage());
                        continue A;
                    }
                }
            }
        }
    }

    @Override
    protected void draw() {
        for(Bullet bullet: bullets){
            bullet.draw();
        }
    }

    @Override
    protected void drawHud() {
        imageArtefact.draw();
        Matrix4f matrixPos;

        geometryShader.bind();

        for(int i = 0;i<charge;i++){
            if(chargeAnimation == i && charge == maxCharge){
                geometryShader.setUniform3f("color", new Vector3f(0.141f, 0.980f, 0));
            } else {
                geometryShader.setUniform3f("color", new Vector3f(0.035f, 0.784f, 0.117f));
            }

            matrixPos = new Matrix4f()
                    .translate(new Vector3f( 1376+16*i,1055,0));
            Camera.getInstance().hardProjection().mul(matrixPos, matrixPos);
            geometryShader.setUniformm4f("projection", matrixPos);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
            glVertexAttribPointer(0, 2, GL_INT, false, 0, 0);

            glDrawArrays(GL_QUADS, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glDisableVertexAttribArray(0);
        }
        geometryShader.unbind();


        chargeBar.draw();
    }

    @Override
    protected void activate() {
        charge = 0;
        for (int i = 1; i <= 50; ) {
            double inaccuracy = 0.155 * i;

            Bullet bullet = new Bullet(tm, 0, 0, inaccuracy,30);
            bullet.setPosition(p.getX(), p.getY());
            bullets.add(bullet);
            bullet.setDamage(4);
            if (i >= 0) i++;
            else i--;
            i = -i;

            }

    }

    @Override
    protected void charge() {
        charge++;
        if(charge > maxCharge) charge = maxCharge;
    }

}

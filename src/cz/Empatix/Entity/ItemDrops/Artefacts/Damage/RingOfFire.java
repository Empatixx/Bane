package cz.Empatix.Entity.ItemDrops.Artefacts.Damage;

import cz.Empatix.Entity.Enemies.KingSlime;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.Player;
import cz.Empatix.Guns.Bullet;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Java.Loader;
import cz.Empatix.Java.Random;
import cz.Empatix.Multiplayer.EnemyManagerMP;
import cz.Empatix.Multiplayer.PlayerMP;
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
        charge = maxCharge;

        scale = 4;

        imageArtefact = new Image("Textures\\artefacts\\rof.tga",new Vector3f(1403,975,0),
                scale);
        chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                2.6f);
        rarity = 1;

        bullets = new ArrayList<>(50);

    }
    public RingOfFire(TileMap tm, Player[] p){
        super(tm,p);
        maxCharge = 4;
        charge = maxCharge;

        rarity = 1;
        bullets = new ArrayList<>(50);

        scale = 4f;

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
    public void update(boolean pause) {
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
                if(bullet.intersects(enemy) && enemy.canReflect()){
                    Vector3f speed = bullet.getSpeed();
                    speed.x = -speed.x;
                    speed.y = -speed.y;
                    return;
                }
                if(bullet.isFriendlyFire()){
                    if(bullet.intersects(p[0]) && !bullet.isHit() && !p[0].isDead() && !p[0].isFlinching()){
                        p[0].hit(bullet.getDamage());
                        bullet.setHit(Bullet.TypeHit.PLAYER);
                        GunsManager.hitBullets++;
                    }
                }
                else if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
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
                    bullet.setHit(Bullet.TypeHit.ENEMY);
                    continue A;
                }
            }
            for(RoomObject object: objects){
                if(object instanceof DestroyableObject) {
                    if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                        bullet.setHit(Bullet.TypeHit.ROOMOBJECT);
                        ((DestroyableObject) object).setHit(bullet.getDamage());
                        continue A;
                    }
                }
            }
        }
    }
    @Override
    public void update(String username) {
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
            EnemyManagerMP enemyManager = EnemyManagerMP.getInstance();
            for(Enemy enemy: enemyManager.getEnemies()){
                if(bullet.intersects(enemy) && enemy.canReflect()){
                    Vector3f speed = bullet.getSpeed();
                    speed.x = -speed.x;
                    speed.y = -speed.y;
                    return;
                }
                for(Player p : p) {
                    if (p == null) continue;
                    if(bullet.isFriendlyFire()){
                        if(bullet.intersects(p) && !bullet.isHit() && !p.isDead() && !p.isFlinching()){
                            p.hit(bullet.getDamage());
                            bullet.setHit(Bullet.TypeHit.PLAYER);
                            GunsManager.hitBullets++;
                        }
                    }
                }
                if (bullet.intersects(enemy) && !bullet.isHit() && !enemy.isDead() && !enemy.isSpawning()) {
                    if(enemy instanceof KingSlime) bullet.setDamage(1);
                    enemy.hit(bullet.getDamage());
                    bullet.setHit(Bullet.TypeHit.ENEMY);
                    continue A;
                }
            }
            for(RoomObject object: objects){
                if(object instanceof DestroyableObject) {
                    if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                        bullet.setHit(Bullet.TypeHit.ROOMOBJECT);
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
                geometryShader.setUniform3f("color", new Vector3f(0.109f, 0.552f, 0.203f));
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
    public void activate() {
        charge = 0;
        for (int i = 1; i <= 50; ) {
            double inaccuracy = 0.155 * i;
            Bullet bullet = new Bullet(tm, 0, 0, inaccuracy,30);
            bullet.setPosition(p[0].getX(), p[0].getY());
            bullets.add(bullet);
            bullet.setDamage(4);
            if (i >= 0) i++;
            else i--;
            i = -i;
        }

    }
    @Override
    public void activate(String username) {
        charge = 0;
        for(Player p : p){
            if(p == null) continue;
            if(((PlayerMP)p).getUsername().equalsIgnoreCase(username)){
                for (int i = 1; i <= 50; ) {
                    double inaccuracy = 0.155 * i;
                    Bullet bullet = new Bullet(tm, 0, 0, inaccuracy, 30);
                    bullet.setPosition(p.getX(), p.getY());
                    bullets.add(bullet);
                    bullet.setDamage(4);
                    if (i >= 0) i++;
                    else i--;
                    i = -i;
                }
                break;
            }
        }

    }
    @Override
    public void charge() {
        charge++;
        if(charge > maxCharge) charge = maxCharge;
    }

}

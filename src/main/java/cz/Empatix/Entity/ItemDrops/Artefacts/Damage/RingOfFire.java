package cz.Empatix.Entity.ItemDrops.Artefacts.Damage;

import com.esotericsoftware.kryonet.Server;
import cz.Empatix.Entity.Enemies.KingSlime;
import cz.Empatix.Entity.Enemy;
import cz.Empatix.Entity.EnemyManager;
import cz.Empatix.Entity.ItemDrops.Artefacts.Artefact;
import cz.Empatix.Entity.Player;
import cz.Empatix.Entity.RoomObjects.DestroyableObject;
import cz.Empatix.Entity.RoomObjects.RoomObject;
import cz.Empatix.Multiplayer.MultiplayerManager;
import cz.Empatix.Guns.Bullet;
import cz.Empatix.Guns.GunsManager;
import cz.Empatix.Multiplayer.ArtefactManagerMP;
import cz.Empatix.Multiplayer.EnemyManagerMP;
import cz.Empatix.Multiplayer.Network;
import cz.Empatix.Multiplayer.PlayerMP;
import cz.Empatix.Render.Camera;
import cz.Empatix.Render.Hud.Image;
import cz.Empatix.Render.TileMap;
import cz.Empatix.Utility.Loader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;

public class RingOfFire extends Artefact {
    public static void load(){
        Loader.loadImage("Textures\\artefacts\\rof.tga");
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

        if(!tm.isServerSide()){
            imageArtefact = new Image("Textures\\artefacts\\rof.tga",new Vector3f(1403,975,0),
                    scale);
            chargeBar = new Image("Textures\\artefacts\\artifactcharge.tga",new Vector3f(1400,1055,0),
                    2.6f);
        }

    }

    @Override
    public void updateSP(boolean pause) {
        for(int i = 0;i<bullets.size();i++){
            Bullet bullet = bullets.get(i);
            bullet.update();
            if(bullet.shouldRemove()){
                bullets.remove(i);
                i--;
            }
        }
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
                    showDamageIndicator(bullet.getDamage(),bullet.isCritical(),enemy);
                    bullet.setHit(Bullet.TypeHit.ENEMY);
                    continue A;
                }
            }
            ArrayList<RoomObject>[] objectsArray = tm.getRoomMapObjects();
            for(ArrayList<RoomObject> objects : objectsArray) {
                if (objects == null) continue;
                for (RoomObject object : objects) {
                    if (object instanceof DestroyableObject) {
                        if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                            bullet.setHit(Bullet.TypeHit.ROOMOBJECT);
                            ((DestroyableObject) object).setHit(bullet.getDamage());
                            continue A;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateMPClient() {
        for(int i = 0;i<bullets.size();i++){
            Bullet bullet = bullets.get(i);
            bullet.update();
            if(bullet.shouldRemove()){
                bullets.remove(i);
                i--;
            }
        }
    }

    @Override
    public void updateMPServer(String username) {
        for(int i = 0;i<bullets.size();i++){
            Bullet bullet = bullets.get(i);
            bullet.update();
            if(bullet.shouldRemove()){
                bullets.remove(i);
                i--;
            }
        }
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
                    bullet.setHit(Bullet.TypeHit.ENEMY, enemy.getId());
                    continue A;
                }
            }
            ArrayList<RoomObject>[] objectsArray = tm.getRoomMapObjects();
            for(ArrayList<RoomObject> objects : objectsArray) {
                if (objects == null) continue;
                for (RoomObject object : objects) {
                    if (object instanceof DestroyableObject) {
                        if (bullet.intersects(object) && !bullet.isHit() && !((DestroyableObject) object).isDestroyed()) {
                            bullet.setHit(Bullet.TypeHit.ROOMOBJECT, object.getId());
                            ((DestroyableObject) object).setHit(bullet.getDamage());
                            continue A;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleAddBulletPacket(Network.ArtefactAddBullet addBullet) {
        Bullet bullet = new Bullet(tm, addBullet.id);
        bullet.setPosition(addBullet.px, addBullet.py);
        bullets.add(bullet);
        bullet.setDamage(4);
    }
    @Override
    protected void preDraw() {
        for(Bullet bullet: bullets){
            bullet.draw();
        }
    }

    @Override
    protected void draw() {

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
    public void handleHitBulletPacket(Network.HitBullet hitBullet) {
        for(Bullet b : bullets){
            if(b.getId() == hitBullet.id){
                b.setHit(hitBullet.type);
                if(hitBullet.type == Bullet.TypeHit.ENEMY){
                    EnemyManager em = EnemyManager.getInstance();
                    Enemy e = em.handleHitEnemyPacket(hitBullet.idHit,b.getDamage());
                    showDamageIndicator(b.getDamage(),b.isCritical(),e);
                } else if (hitBullet.type == Bullet.TypeHit.ROOMOBJECT){
                    ArrayList<RoomObject>[] objectsArray = tm.getRoomMapObjects();
                    for(ArrayList<RoomObject> objects : objectsArray) {
                        if (objects == null) continue;
                        for(RoomObject obj : objects) {
                            if (obj.getId() == hitBullet.idHit) {
                                if (obj instanceof DestroyableObject) {
                                    ((DestroyableObject) obj).setHit(b.getDamage());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleMoveBulletPacket(Network.MoveBullet moveBullet) {
        for(Bullet b : bullets){
            if(b.id== moveBullet.id){
                b.setPosition(moveBullet.x, moveBullet.y);
            }
        }
    }

    @Override
    public boolean playerHitEvent() {
        return false;
    }

    @Override
    public void playerDropEvent() {

    }

    @Override
    public void activate() {
        charge = 0;
        for (int i = 1; i <= 50; ) {
            double inaccuracy = 0.155 * i;
            Bullet bullet = new Bullet(tm, 0, 0, inaccuracy,1800);
            bullet.setPosition(p[0].getX(), p[0].getY());
            bullets.add(bullet);
            bullet.setDamage(4);
            if (i >= 0) i++;
            else i--;
            i = -i;
        }

    }
    @Override
    public void activate(int idUser) {
        charge = 0;
        for(Player p : p){
            if(p == null) continue;
            if(((PlayerMP)p).getIdConnection() == idUser){
                MultiplayerManager mpManager = MultiplayerManager.getInstance();
                Server server = mpManager.server.getServer();
                ArtefactManagerMP artefactManager = ArtefactManagerMP.getInstance();
                for (int i = 1; i <= 50; ) {
                    double inaccuracy = 0.155 * i;
                    Bullet bullet = new Bullet(tm, 0, 0, inaccuracy, 1800);
                    bullet.setPosition(p.getX(), p.getY());
                    bullets.add(bullet);
                    bullet.setDamage(4);
                    if (i >= 0) i++;
                    else i--;
                    i = -i;
                    Network.ArtefactAddBullet addBullet = new Network.ArtefactAddBullet();
                    mpManager.server.requestACK(addBullet,addBullet.idPacket);
                    addBullet.slot = (byte)artefactManager.getArtefactSlot(this);
                    addBullet.px = p.getX();
                    addBullet.py = p.getY();
                    addBullet.inaccuracy = (float)inaccuracy;
                    addBullet.id = bullet.getId();
                    server.sendToAllUDP(addBullet);
                }
                break;
            }
        }

    }
    @Override
    public void activateClientSide(int idUser) {
        super.activateClientSide(idUser);
        charge = 0;
    }
    @Override
    public void charge() {
        charge++;
        if(charge > maxCharge) charge = maxCharge;
    }

    public void clear() {
        for(Bullet b : bullets){
            b.delete();
        }
        bullets.clear();
    }
    public void clearMP() {
        bullets.clear();
    }
}

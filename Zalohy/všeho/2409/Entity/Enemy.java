package Entity;

import Render.TileMap;

public class Enemy extends MapObject {

    protected int health;
    protected int maxHealth;
    protected boolean dead;
    protected int damage;

    // AI vars
    protected int type;
    public static final  int melee = 0;
    public static final  int shooter = 1;
    public static final  int hybrid = 2;

    private Player player;
    private int px;
    private int py;

    //protected boolean flinching;
    //protected long flinchTimer;

    public Enemy(TileMap tm, Player player) {
        super(tm);
        this.player = player;
    }

    public boolean isDead() { return dead; }

    public int getDamage() { return damage; }

    public void hit(int damage) {
        if(dead) return;
        health -= damage;
        if(health < 0) health = 0;
        if(health == 0) dead = true;

        //flinching = true;
        //flinchTimer = System.nanoTime();
    }

    public void update() { }
    // ENEMY AI
    public void EnemyAI(){

        updatePlayerCords();

        if (type == melee){
            for(int x = getx() - 500;x < getx()+500;x++){
                for(int y = gety() - 500;y < gety() + 500;y++){
                    if (px == x && py == y){
                        if (px < getx()){
                            setLeft(true);
                            setRight(false);
                        } else if (px > getx()) {
                            setRight(true);
                            setLeft(false);
                        }
                        if (py < gety()){
                            setUp(true);
                            setDown(false);
                        } else if (py > gety()) {
                            setDown(true);
                            setUp(false);
                        }
                    }
                }
            }
        }
    }
    private void updatePlayerCords(){
        px = player.getx();
        py = player.gety();
    }
}
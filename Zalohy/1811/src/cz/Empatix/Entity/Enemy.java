package cz.Empatix.Entity;

import cz.Empatix.Render.TileMap;

public abstract class Enemy extends MapObject {

    protected int health;
    protected int maxHealth;
    boolean dead;
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

    int getDamage() { return damage; }

    void hit(int damage) {
        if(dead) return;
        health -= damage;
        if(health < 0) health = 0;
        if(health == 0) dead = true;

        //flinching = true;
        //flinchTimer = System.nanoTime();
    }

    public void update() { }
    // ENEMY AI
    protected void EnemyAI(){

        updatePlayerCords();

        if (type == melee){
            if (
                    x - px < 250 || x - px > - 250
                    &&
                    y - py < 250 || y - py > - 250
            ){


                if (px == x){
                    setLeft(false);
                    setRight(false);
                }
                else if (px < x){
                    setLeft(true);
                    setRight(false);
                } else if (px > x){
                    setRight(true);
                    setLeft(false);
                }
                if (px == y){
                    setDown(false);
                    setUp(false);
                }
                else if (py < y){
                    setUp(true);
                    setDown(false);
                } else if (py > y){
                    setDown(true);
                    setUp(false);
                }
            }
        }
    }
    private void updatePlayerCords(){
        px = player.getx();
        py = player.gety();
    }
    @Override
    // finalize method is called on object once
    // before garbage collecting it
    protected void finalize()
    {
        System.out.println("Object garbage collected : " + this);
    }
}
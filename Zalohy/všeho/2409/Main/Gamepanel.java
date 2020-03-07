package Main;

import Gamestates.GameStateManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class Gamepanel extends JPanel implements KeyListener,Runnable, MouseListener {
    public static final boolean devMode = false;
    public static boolean displayCollisions = false;

    public static int WIDTH = 960;
    public static int HEIGHT = 540;
    private static int SCALE = 2;

    public static int FPS = 0;
    private boolean running;
    private Thread thread;

    // image
    private BufferedImage image;
    private Graphics2D g;

    private GameStateManager gsm;


    // mouse location x/y tilemap
    public static double mouseX;
    public static double mouseY;


    Gamepanel() {
        super();
        setPreferredSize(
                new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setFocusable(true);
        requestFocus();
    }
    @Override
    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            addKeyListener(this);
            addMouseListener(this);
            thread.start();
        }
    }
    private void init() {


        image = new BufferedImage(
                WIDTH, HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );
        g = (Graphics2D) image.getGraphics();

        running = true;

        gsm = new GameStateManager();
    }



    /**
     * FPS SYSTEM
     */
    @Override
    public void run() {

        init();

        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        final double ns = 1000000000.0 / 60.0;

        double delta = 0;
        int frames = 0;
        int updates = 0;

        while(running){

            long now = System.nanoTime();
            delta += (now-lastTime) / ns;
            lastTime = now;

            while (delta >= 1){
                update();
                updates++;
                delta--;

            }
            frames++;
            draw();
            drawToScreen();

            if (System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                System.out.print("UPS: "+updates+"   "+"FPS: "+frames+"\n");
                FPS = frames;
                frames = 0;
                updates = 0;
            }
        }
    }
    private void update() {
        // loc of mouse
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        mouseX = mouseLoc.x;
        mouseY = mouseLoc.y;

        // gamestate update
        gsm.update();
    }

    private void draw() {
        // gamestate draw
        gsm.draw(g);
    }

        private void drawToScreen() {
            Graphics g2 = getGraphics();
            g2.drawImage(image, 0, 0,
                    WIDTH * SCALE, HEIGHT * SCALE,
                    null);
            g2.dispose();
        }


    public void keyTyped(KeyEvent key) {}
    public void keyPressed(KeyEvent key) { gsm.keyPressed(key.getKeyCode()); }
    public void keyReleased(KeyEvent key) {
        gsm.keyReleased(key.getKeyCode());
    }

    public void mousePressed(MouseEvent e) {
        gsm.mousePressed(e.getButton());
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        gsm.mouseReleased(e.getButton());
    }

    public void mouseClicked(MouseEvent e) {
    }
}

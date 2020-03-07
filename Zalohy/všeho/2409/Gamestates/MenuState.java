package Gamestates;

import Main.Gamepanel;
import Render.Background;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class MenuState extends GameState{

    private Background bg;

    private Color titleColor;
    private Font titleFont;

    private Font font;

    private BufferedImage[] buttons;


    MenuState(GameStateManager gsm){
        this.gsm = gsm;
        init();
    }


    @Override
    public void draw(Graphics2D g) {

        bg.draw(g);


        g.drawImage(
                buttons[0],
                1,
                1,
                67,
                71,
                null
        );
    }

    @Override
    public void init() {
        try {
            bg = new Background("/Backgrounds/menu.gif");
            bg.setVector(-0.25, 0);

            titleColor = new Color(128, 0, 0);
            titleFont = new Font("Century Gothic", Font.PLAIN, 36);

            font = new Font("Arial", Font.PLAIN, 21);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedImage spritesheet;
            if (!Gamepanel.devMode){
                spritesheet = ImageIO.read(new File("tlacitka2.png"));
            } else {
                spritesheet = ImageIO.read(
                        getClass().getResourceAsStream(
                                "tlacitka2.png"
                        )
                );
            }
            buttons = new BufferedImage[3];
            for(int i = 0; i < buttons.length; i++) {
                buttons[i] = spritesheet.getSubimage(
                        0,
                        i*71,
                        67,
                        71
                );
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyPressed(int k) {

    }
    @Override
    public void keyReleased(int k) {

    }
    @Override
    public void mouseReleased(int button) {

    }

    @Override
    public void mousePressed(int button) {

    }

    @Override
    public void update() {
        bg.update();
    }
}

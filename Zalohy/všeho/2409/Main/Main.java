package Main;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("2DGAME");
        window.setContentPane(new Gamepanel());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setUndecorated(true);
        window.setIconImage(new ImageIcon(Main.class.getResource("/test.png")).getImage());

        window.pack();
        window.setVisible(true);

    }
}

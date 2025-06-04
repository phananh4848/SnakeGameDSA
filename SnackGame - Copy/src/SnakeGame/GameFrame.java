package SnakeGame;

import javax.swing.*;

public class GameFrame extends JFrame {
    public GameFrame(int level, String mode) {
        this.add(new GamePanel(level, mode));
        this.setTitle("Snake Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}

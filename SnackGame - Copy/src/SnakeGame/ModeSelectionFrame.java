package SnakeGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ModeSelectionFrame extends JFrame {
    public ModeSelectionFrame() {
        setTitle("Select Mode");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        JLabel label = new JLabel("Choose game mode:");
        add(label);

        JButton playerButton = new JButton("Player");
        JButton botButton = new JButton("Bot");

        playerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LevelSelectionFrame("player");
                dispose();
            }
        });

        botButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LevelSelectionFrame("bot");
                dispose();
            }
        });

        add(playerButton);
        add(botButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new ModeSelectionFrame();
    }
}

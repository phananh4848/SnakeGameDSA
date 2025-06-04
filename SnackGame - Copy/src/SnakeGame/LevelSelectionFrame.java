package SnakeGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LevelSelectionFrame extends JFrame {
    public LevelSelectionFrame(final String mode) {
        setTitle("Select Level");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        JLabel label = new JLabel("Choose a level:");
        add(label);

        for (int i = 1; i <= 10; i++) {  // How many level
            final int level = i;
            JButton button = new JButton("Level " + i);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new GameFrame(level, mode);
                    dispose();
                }
            });
            add(button);
        }

        setLocationRelativeTo(null);
        setVisible(true);
    }
}

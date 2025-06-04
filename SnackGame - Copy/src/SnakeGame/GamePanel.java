package SnakeGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 800;
    static final int SCREEN_HEIGHT = 800;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    int DELAY;
    int originalDelay;

    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];

    int bodyParts = 6;
    int applesEaten;
    int appleX, appleY;
    int greenAppleX = -1, greenAppleY = -1;
    int blueAppleX = -1, blueAppleY = -1;
    boolean greenAppleVisible = false;
    boolean blueAppleVisible = false;
    boolean goldenApplesActive = false;
    boolean speedBoostActive = false;
    boolean running = false;
    boolean gameOverDialogShown = false;

    int score = 0;
    int level;
    long blueAppleSpawnTime = 0;
    long goldenAppleStartTime;

    final int BLUE_APPLE_DURATION = 10000;
    final int BLUE_APPLE_COOLDOWN = 30000;
    final int GOLDEN_APPLE_DURATION = 10000;

    Timer timer;
    Random random;

    ArrayList<Point> goldenApples = new ArrayList<Point>();
    ArrayList<Point> obstacles = new ArrayList<Point>();

    public enum GameMode { PLAYER, BOT }
    GameMode gameMode;

    char direction = 'R';

    public GamePanel(int level, String mode) {
        this.level = level;
        this.gameMode = "bot".equalsIgnoreCase(mode) ? GameMode.BOT : GameMode.PLAYER;
        this.random = new Random();

        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        this.DELAY = 150 - level * 10;
        this.originalDelay = this.DELAY;

        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        gameOverDialogShown = false;
        blueAppleVisible = false;
        blueAppleSpawnTime = System.currentTimeMillis();
        generateObstacles(level);
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void newApple() {
        boolean valid;
        do {
            valid = true;
            appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;

            if ((greenAppleVisible && appleX == greenAppleX && appleY == greenAppleY) ||
                (blueAppleVisible && appleX == blueAppleX && appleY == blueAppleY)) {
                valid = false;
            }

            for (Point p : obstacles) {
                if (p.x == appleX && p.y == appleY) {
                    valid = false;
                    break;
                }
            }
        } while (!valid);

        if (!greenAppleVisible && random.nextInt(10) < 2) {
            spawnGreenApple();
        }
    }

    public void spawnGreenApple() {
        boolean valid;
        do {
            valid = true;
            greenAppleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            greenAppleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;

            if ((greenAppleX == appleX && greenAppleY == appleY) ||
                (blueAppleVisible && greenAppleX == blueAppleX && greenAppleY == blueAppleY)) {
                valid = false;
            }

            for (Point p : obstacles) {
                if (p.x == greenAppleX && p.y == greenAppleY) {
                    valid = false;
                    break;
                }
            }
        } while (!valid);

        greenAppleVisible = true;
    }

    public void spawnBlueApple() {
        boolean valid;
        do {
            valid = true;
            blueAppleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            blueAppleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;

            if ((blueAppleX == appleX && blueAppleY == appleY) ||
                (greenAppleVisible && blueAppleX == greenAppleX && blueAppleY == greenAppleY)) {
                valid = false;
            }

            for (Point p : obstacles) {
                if (p.x == blueAppleX && p.y == blueAppleY) {
                    valid = false;
                    break;
                }
            }
        } while (!valid);

        blueAppleVisible = true;
        blueAppleSpawnTime = System.currentTimeMillis();
    }

    public void spawnGoldenApples() {
        goldenApples.clear();
        int spawned = 0;
        while (spawned < 10) {
            int gx = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int gy = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Point p = new Point(gx, gy);

            boolean overlap = false;
            if ((appleX == p.x && appleY == p.y) ||
                (greenAppleVisible && greenAppleX == p.x && greenAppleY == p.y) ||
                (blueAppleVisible && blueAppleX == p.x && blueAppleY == p.y)) {
                overlap = true;
            }

            for (Point obs : obstacles) {
                if (obs.equals(p)) {
                    overlap = true;
                    break;
                }
            }

            if (!overlap) {
                goldenApples.add(p);
                spawned++;
            }
        }
        goldenApplesActive = true;
        goldenAppleStartTime = System.currentTimeMillis();
        activateSpeedBoost();
    }

    public void activateSpeedBoost() {
        if (!speedBoostActive) {
            speedBoostActive = true;
            timer.setDelay(originalDelay * 2 / 3);
        }
    }

    public void deactivateSpeedBoost() {
        speedBoostActive = false;
        timer.setDelay(originalDelay);
    }

    public void generateObstacles(int level) {
        obstacles.clear();
        int numObstacles = level * 5;
        for (int i = 0; i < numObstacles; i++) {
            Point p;
            do {
                int ox = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
                int oy = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
                p = new Point(ox, oy);
            } while (obstacles.contains(p));
            obstacles.add(p);
        }
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= UNIT_SIZE; break;
            case 'D': y[0] += UNIT_SIZE; break;
            case 'L': x[0] -= UNIT_SIZE; break;
            case 'R': x[0] += UNIT_SIZE; break;
        }
    }

    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            score += 1;
            applesEaten++;
            newApple();
        }

        if (greenAppleVisible && x[0] == greenAppleX && y[0] == greenAppleY) {
            greenAppleVisible = false;
            greenAppleX = greenAppleY = -1;
            score += 5;
            spawnGoldenApples();
        }

        if (goldenApplesActive) {
            ArrayList<Point> toRemove = new ArrayList<>();
            for (Point p : goldenApples) {
                if (x[0] == p.x && y[0] == p.y) {
                    score += 30;
                    toRemove.add(p);
                }
            }
            goldenApples.removeAll(toRemove);

            if (System.currentTimeMillis() - goldenAppleStartTime > GOLDEN_APPLE_DURATION) {
                goldenApplesActive = false;
                goldenApples.clear();
                deactivateSpeedBoost();
            }
        }

        if (blueAppleVisible && x[0] == blueAppleX && y[0] == blueAppleY) {
            score += 50;
            blueAppleVisible = false;
            blueAppleX = blueAppleY = -1;
            blueAppleSpawnTime = System.currentTimeMillis();  // reset cooldown timer
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) running = false;
        }

        for (Point p : obstacles) {
            if (x[0] == p.x && y[0] == p.y) running = false;
        }

        if (x[0] < 0) x[0] = SCREEN_WIDTH - UNIT_SIZE;
        else if (x[0] >= SCREEN_WIDTH) x[0] = 0;
        if (y[0] < 0) y[0] = SCREEN_HEIGHT - UNIT_SIZE;
        else if (y[0] >= SCREEN_HEIGHT) y[0] = 0;

        if (!running) {
            timer.stop();
            if (!gameOverDialogShown) {
                gameOverDialogShown = true;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        showGameOverOptions();
                    }
                });
            }
        }
    }

    public void autoMove() {
    Point start = new Point(x[0], y[0]);
    Point goal = null;

    Set<Point> obstaclesSet = new HashSet<>(this.obstacles);
    for (int i = 1; i < bodyParts; i++) {
        obstaclesSet.add(new Point(x[i], y[i]));
    }

    if (blueAppleVisible) {
        goal = new Point(blueAppleX, blueAppleY);
    } else if (greenAppleVisible) {
        goal = new Point(greenAppleX, greenAppleY);
    } else if (goldenApplesActive && !goldenApples.isEmpty()) {
        goal = getNearestGoldenApple(start);
    } else {
        goal = new Point(appleX, appleY);
    }

    List<Point> path = AStar.findPath(start, goal, obstaclesSet, SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE);

    if (path != null && !path.isEmpty()) {
        Point next = path.get(0);
        if (next.x < x[0]) direction = 'L';
        else if (next.x > x[0]) direction = 'R';
        else if (next.y < y[0]) direction = 'U';
        else if (next.y > y[0]) direction = 'D';
    }
}

private Point getNearestGoldenApple(Point start) {
    Point nearest = null;
    int minDist = Integer.MAX_VALUE;
    for (Point p : goldenApples) {
        int dist = Math.abs(start.x - p.x) + Math.abs(start.y - p.y);
        if (dist < minDist) {
            minDist = dist;
            nearest = p;
        }
    }
    return nearest;
}


    public void showGameOverOptions() {
        int option = JOptionPane.showOptionDialog(
                this,
                "Game Over! What would you like to do?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Try Again", "Close"},
                "Try Again");

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    public void resetGame() {
        bodyParts = 6;
        applesEaten = 0;
        score = 0;
        direction = 'R';
        running = true;
        greenAppleVisible = false;
        goldenApples.clear();
        goldenApplesActive = false;
        deactivateSpeedBoost();
        blueAppleVisible = false;
        blueAppleX = blueAppleY = -1;
        blueAppleSpawnTime = System.currentTimeMillis();
        obstacles.clear();
        generateObstacles(level);
        startGame();
    }

    public void draw(Graphics g) {
        if (running) {
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            if (greenAppleVisible) {
                g.setColor(Color.GREEN);
                g.fillOval(greenAppleX, greenAppleY, UNIT_SIZE, UNIT_SIZE);
            }

            if (blueAppleVisible) {
                g.setColor(Color.CYAN);
                g.fillOval(blueAppleX, blueAppleY, UNIT_SIZE, UNIT_SIZE);
            }

            g.setColor(Color.YELLOW);
            for (Point p : goldenApples) {
                g.fillOval(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
            }

            for (Point p : obstacles) {
                g.setColor(Color.GRAY);
                g.fillRect(p.x, p.y, UNIT_SIZE, UNIT_SIZE);
            }

            for (int i = 0; i < bodyParts; i++) {
                g.setColor(i == 0 ? Color.green : new Color(45, 180, 0));
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }

            drawScore(g);
        } else {
            gameOver(g);
        }
    }

    public void drawScore(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Ink Free", Font.BOLD, 24));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Score: " + score, (SCREEN_WIDTH - metrics.stringWidth("Score: " + score)) / 2, g.getFont().getSize());
    }

    public void gameOver(Graphics g) {
        drawScore(g);
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            if (gameMode == GameMode.BOT) autoMove();
            move();
            checkApple();
            checkCollisions();

            long now = System.currentTimeMillis();
            if (!blueAppleVisible && now - blueAppleSpawnTime >= BLUE_APPLE_COOLDOWN) {
                spawnBlueApple();
            }
            if (blueAppleVisible && now - blueAppleSpawnTime >= BLUE_APPLE_DURATION) {
                blueAppleVisible = false;
                blueAppleSpawnTime = System.currentTimeMillis();
            }
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: if (direction != 'R') direction = 'L'; break;
                case KeyEvent.VK_RIGHT: if (direction != 'L') direction = 'R'; break;
                case KeyEvent.VK_UP: if (direction != 'D') direction = 'U'; break;
                case KeyEvent.VK_DOWN: if (direction != 'U') direction = 'D'; break;
                case KeyEvent.VK_P:
                    if (running) {
                        timer.stop();
                        running = false;
                    } else {
                        running = true;
                        timer.start();
                    }
                    break;
            }
        }
    }
}

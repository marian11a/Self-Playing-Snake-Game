import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 640;
    static final int UNIT_SIZE = 20;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static final int DELAY = 100;
    int[] x = new int[GAME_UNITS];
    int[] y = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        x[0] = UNIT_SIZE * 3;
        y[0] = UNIT_SIZE * 5;
        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Grid!
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            // Apple!
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            // Border!
            g.setColor(new Color(102, 51, 0));
            g.fillRect(0, 0, SCREEN_WIDTH, UNIT_SIZE * 3);
            g.fillRect(0, 0, UNIT_SIZE, SCREEN_HEIGHT);
            g.fillRect(SCREEN_WIDTH - UNIT_SIZE, 0, UNIT_SIZE, SCREEN_HEIGHT);
            g.fillRect(0, SCREEN_HEIGHT - UNIT_SIZE, SCREEN_WIDTH, UNIT_SIZE);

            g.setColor(Color.white);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten
                    , (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2
                    , g.getFont().getSize());
            int[] gameState = {x[0], y[0], appleX, appleY};
        } else {
            gameOver(g);
        }
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] = y[0] - UNIT_SIZE;
            case 'D' -> y[0] = y[0] + UNIT_SIZE;
            case 'L' -> x[0] = x[0] - UNIT_SIZE;
            case 'R' -> x[0] = x[0] + UNIT_SIZE;
        }
    }

    public void newApple() {
        boolean validApplePosition = false;
        while (!validApplePosition) {
            appleX = random.nextInt((int) ((SCREEN_WIDTH - 2 * UNIT_SIZE) / UNIT_SIZE)) * UNIT_SIZE;
            appleY = random.nextInt((int) ((SCREEN_HEIGHT - 2 * UNIT_SIZE) / UNIT_SIZE)) * UNIT_SIZE;

            if (appleX >= UNIT_SIZE && appleX < SCREEN_WIDTH - UNIT_SIZE &&
                    appleY >= UNIT_SIZE * 3 && appleY < SCREEN_HEIGHT - UNIT_SIZE) {
                validApplePosition = true;

                // Check if the apple spawns inside the snake
                for (int i = bodyParts; i > 0; i--) {
                    if ((x[i] == appleX) && (y[i] == appleY)) {
                        validApplePosition = false;
                        break;
                    }
                }
            }
        }
    }


    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        // Checks if head collides with body!
        for (int i = bodyParts; i > 0; i--) {
            if ((x[i] == x[0]) && (y[i] == y[0])) {
                running = false;
                break;
            }
        }

        if (x[0] < UNIT_SIZE) {
            x[0] = SCREEN_WIDTH - UNIT_SIZE * 2;
            running = false;
        } else if (x[0] >= SCREEN_WIDTH - UNIT_SIZE) {
            x[0] = UNIT_SIZE;
            running = false;
        } else if (y[0] < UNIT_SIZE * 3) {
            y[0] = SCREEN_HEIGHT - UNIT_SIZE * 2;
            running = false;
        } else if (y[0] >= SCREEN_HEIGHT - UNIT_SIZE) {
            y[0] = UNIT_SIZE * 3;
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {
        // Game Over text!
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over!"
                , (SCREEN_WIDTH - metrics1.stringWidth("Game Over!")) / 2
                , SCREEN_HEIGHT / 2);

        // Score text!
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten
                , (SCREEN_WIDTH - metrics2.stringWidth("Score: " + applesEaten)) / 2
                , SCREEN_HEIGHT - SCREEN_HEIGHT / 3);

        // Restart automatically after 1500ms
        Timer delayTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
                ((Timer) e.getSource()).stop(); // Stop the timer after the delay
            }
        });
        delayTimer.setRepeats(false); // Ensure the timer only runs once
        delayTimer.start();
    }

    public void restartGame() {
        bodyParts = 6;
        applesEaten = 0;
        this.setBackground(Color.black);
        direction = 'R';
        running = true;
        x = new int[GAME_UNITS];
        y = new int[GAME_UNITS];
        x[0] = UNIT_SIZE * 3;
        y[0] = UNIT_SIZE * 5;
        startGame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            direction = decideNextMove(); // <-- "ctrl" + "/" for a Player-Mode!
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }


    // Player-mode: activate in actionPerformed method
    public class MyKeyAdapter extends KeyAdapter {
        private long lastKeyPressTime = 0;

        @Override
        public void keyPressed(KeyEvent e) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastKeyPressTime < DELAY) {
                // Ignore the key press if the delay hasn't passed
                return;
            }

            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
            }

            // Update the last key press time
            lastKeyPressTime = currentTime;
        }
    }

    public char decideNextMove() {
        int headX = x[0];
        int headY = y[0];

        int distX = appleX - headX;
        int distY = appleY - headY;

        char nextMove;
        if (Math.abs(distX) > Math.abs(distY)) {
            nextMove = distX > 0 ? 'R' : 'L';
        } else {
            nextMove = distY > 0 ? 'D' : 'U';
        }

        // Simulate the next move
        int nextHeadX = headX;
        int nextHeadY = headY;
        switch (nextMove) {
            case 'U' -> nextHeadY -= UNIT_SIZE;
            case 'D' -> nextHeadY += UNIT_SIZE;
            case 'L' -> nextHeadX -= UNIT_SIZE;
            case 'R' -> nextHeadX += UNIT_SIZE;
        }

        // Check if the next move leads to a collision with the body
        for (int i = 1; i < bodyParts; i++) {
            if (nextHeadX == x[i] && nextHeadY == y[i]) {
                return getRandomValidMove(headX, headY);
            }
        }

        // Check if the next move leads to a collision with the walls
        if (nextHeadX < UNIT_SIZE || nextHeadX >= SCREEN_WIDTH - UNIT_SIZE ||
                nextHeadY < UNIT_SIZE * 3 || nextHeadY >= SCREEN_HEIGHT - UNIT_SIZE) {
            return getRandomValidMove(headX, headY);
        }

        return nextMove;
    }

    private char getRandomValidMove(int headX, int headY) {
        char[] possibleMoves = {'U', 'D', 'L', 'R'};
        ArrayList<Character> validMoves = new ArrayList<>();
        for (char move : possibleMoves) {
            int testHeadX = headX;
            int testHeadY = headY;
            switch (move) {
                case 'U' -> testHeadY -= UNIT_SIZE;
                case 'D' -> testHeadY += UNIT_SIZE;
                case 'L' -> testHeadX -= UNIT_SIZE;
                case 'R' -> testHeadX += UNIT_SIZE;
            }

            // Check if this move is valid (not leading to a collision)
            boolean valid = true;
            for (int j = 1; j < bodyParts; j++) {
                if (testHeadX == x[j] && testHeadY == y[j]) {
                    valid = false;
                    break;
                }
            }
            if (valid && testHeadX >= UNIT_SIZE && testHeadX < SCREEN_WIDTH - UNIT_SIZE &&
                    testHeadY >= UNIT_SIZE * 3 && testHeadY < SCREEN_HEIGHT - UNIT_SIZE) {
                validMoves.add(move);
            }
        }

        return increaseChancesOfAValidMove(validMoves);

    }

    private char increaseChancesOfAValidMove(ArrayList<Character> validMoves) {
        int maxCount = Integer.MAX_VALUE;
        char bestMove = direction;
        int headX = x[0];
        int headY = y[0];
        String str = "";

        if (validMoves.size() > 1) {
            removeValidMoveIfItGoesToADeadEndCorner(validMoves, headX, headY);
        }

        for (char move : validMoves) {
            int count = 0;

            switch (move) {
                case 'U':
                    for (int i = 1; i < bodyParts; i++) {
                        if (y[i] < headY) {
                            count++;
                        }
                    }
                    break;
                case 'D':
                    for (int i = 1; i < bodyParts; i++) {
                        if (y[i] > headY) {
                            count++;
                        }
                    }
                    break;
                case 'L':
                    for (int i = 1; i < bodyParts; i++) {
                        if (x[i] < headX) {
                            count++;
                        }
                    }
                    break;
                case 'R':
                    for (int i = 1; i < bodyParts; i++) {
                        if (x[i] > headX) {
                            count++;
                        }
                    }
                    break;
            }

            if (count <= maxCount) {
                maxCount = count;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private void removeValidMoveIfItGoesToADeadEndCorner(ArrayList<Character> validMoves, int headX, int headY) {
        if (headX == UNIT_SIZE || headX == SCREEN_WIDTH - UNIT_SIZE * 2||
                headY == UNIT_SIZE * 3 || headY == SCREEN_HEIGHT - UNIT_SIZE * 2) {

            boolean top = false;
            boolean bottom = false;
            boolean left = false;
            boolean right = false;

            if (headX == UNIT_SIZE) {

                for (Character move : validMoves) {
                    switch (move) {
                        case 'U':
                            for (int i = 1; i < bodyParts; i++) {
                                if (y[i] == UNIT_SIZE * 3) {
                                    top = true;
                                    break;
                                }
                            }
                            break;
                        case 'D':
                            for (int i = 1; i < bodyParts; i++) {
                                if (y[i] == SCREEN_HEIGHT - UNIT_SIZE * 2) {
                                    bottom = true;
                                    break;
                                }
                            }
                            break;
                        default:
                            System.out.println("error left border");
                            break;
                    }
                }
            } else if (headX == SCREEN_WIDTH - UNIT_SIZE * 2) {

                for (Character move : validMoves) {
                    switch (move) {
                        case 'U':
                            for (int i = 1; i < bodyParts; i++) {
                                if (y[i] == UNIT_SIZE * 3) {
                                    top = true;
                                    break;
                                }
                            }
                            break;
                        case 'D':
                            for (int i = 1; i < bodyParts; i++) {
                                if (y[i] == SCREEN_HEIGHT - UNIT_SIZE * 2) {
                                    bottom = true;
                                    break;
                                }
                            }
                            break;
                        default:
                            System.out.println("error right border");
                            break;
                    }
                }
            } else if (headY == UNIT_SIZE * 3) {

                for (Character move : validMoves) {
                    switch (move) {
                        case 'L':
                            for (int i = 1; i < bodyParts; i++) {
                                if (x[i] == UNIT_SIZE) {
                                    left = true;
                                    break;
                                }
                            }
                            break;
                        case 'R':
                            for (int i = 1; i < bodyParts; i++) {
                                if (x[i] == SCREEN_WIDTH - UNIT_SIZE * 2) {
                                    right = true;
                                    break;
                                }
                            }
                            break;
                        default:
                            System.out.println("error top border");
                            break;
                    }
                }
            } else {

                for (Character move : validMoves) {
                    switch (move) {
                        case 'L':
                            for (int i = 1; i < bodyParts; i++) {
                                if (x[i] == UNIT_SIZE) {
                                    left = true;
                                    break;
                                }
                            }
                            break;
                        case 'R':
                            for (int i = 1; i < bodyParts; i++) {
                                if (x[i] == SCREEN_WIDTH - UNIT_SIZE * 2) {
                                    right = true;
                                    break;
                                }
                            }
                            break;
                        default:
                            System.out.println("error bottom border");
                            break;
                    }
                }
            }

            if (top || bottom || left || right) {
                if (top) {
                    validMoves.remove(Character.valueOf('U'));
                } else if (bottom) {
                    validMoves.remove(Character.valueOf('D'));
                } else if (left) {
                    validMoves.remove(Character.valueOf('L'));
                } else {
                    validMoves.remove(Character.valueOf('R'));
                }
            }

        }
    }
}
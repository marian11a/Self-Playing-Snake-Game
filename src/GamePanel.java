import
        javax.swing.*;
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
    static final int DELAY = 10;
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
    private int counterV;

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

        int nextHeadX = headX;
        int nextHeadY = headY;
        ArrayList<Character> validMoves = getValidMoves(headX, headY);

        int freeNodesCounter = countAllFreeNodes(validMoves);
        if (freeNodesCounter != validMoves.size()) {
            return findAllFreeNodes(validMoves);
        }

        boolean touchesTwoBorders = checkIfSnakeTouchesTwoBorders();
        boolean headReachesApple = canHeadReachApple(validMoves);
        char nextMove;
        boolean horizontalPriority  = Math.abs(distX) > Math.abs(distY);

        // Don't go towards the apple
        if (touchesTwoBorders && !headReachesApple) {
            if (horizontalPriority ) {
                nextMove = distX > 0 ? 'L' : 'R';
            } else {
                nextMove = distY > 0 ? 'U' : 'D';
            }
        // Go towards the apple
        } else {
            if (horizontalPriority ) {
                nextMove = distX > 0 ? 'R' : 'L';
            } else {
                nextMove = distY > 0 ? 'D' : 'U';
            }
        }

        // Simulate the next move
        switch (nextMove) {
            case 'U' -> nextHeadY -= UNIT_SIZE;
            case 'D' -> nextHeadY += UNIT_SIZE;
            case 'L' -> nextHeadX -= UNIT_SIZE;
            case 'R' -> nextHeadX += UNIT_SIZE;
        }

        // Check if the next move leads to a collision with the body
        for (int i = 1; i < bodyParts; i++) {
            if (nextHeadX == x[i] && nextHeadY == y[i]) {
                return findAllFreeNodes(validMoves);
            }
        }

        // Check if the next move leads to a collision with the walls
        if (nextHeadX < UNIT_SIZE || nextHeadX >= SCREEN_WIDTH - UNIT_SIZE ||
                nextHeadY < UNIT_SIZE * 3 || nextHeadY >= SCREEN_HEIGHT - UNIT_SIZE) {
            return findAllFreeNodes(validMoves);
        }

        return nextMove;
    }

    private char findAllFreeNodes(ArrayList<Character> validMoves) {
        char bestMove = direction;
        int maxCount = 0;
        for (char move : validMoves) {
            char[][] grid = convertToGrid();
            int[] headPosition = findHeadPosition(grid);

            int headRow = headPosition[0];
            int headCol = headPosition[1];
            grid[headRow][headCol] = 'B';

            switch (move) {
                case 'U' -> grid[--headRow][headCol] = 'H';
                case 'D' -> grid[++headRow][headCol] = 'H';
                case 'L' -> grid[headRow][--headCol] = 'H';
                case 'R' -> grid[headRow][++headCol] = 'H';
            }

            boolean isTrue = true;
            makeVForEachSideWithFreeNode(grid, headRow, headCol);
            fillEachDirectionWithV(grid, isTrue);
            int currentCount = countV(grid);

            if (currentCount >= maxCount && currentCount != 0) {
                maxCount = currentCount;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int countAllFreeNodes(ArrayList<Character> validMoves) {
        int equalCounter = 1;
        int maxCount = Integer.MIN_VALUE;
        for (char move : validMoves) {
            char[][] grid = convertToGrid();
            int[] headPosition = findHeadPosition(grid);

            int headRow = headPosition[0];
            int headCol = headPosition[1];

            grid[headRow][headCol] = 'B';
            switch (move) {
                case 'U' -> grid[--headRow][headCol] = 'H';
                case 'D' -> grid[++headRow][headCol] = 'H';
                case 'L' -> grid[headRow][--headCol] = 'H';
                case 'R' -> grid[headRow][++headCol] = 'H';
            }

            boolean isTrue = true;
            makeVForEachSideWithFreeNode(grid, headRow, headCol);
            fillEachDirectionWithV(grid, isTrue);
            int currentCount = countV(grid);

            if (currentCount >= maxCount) {
                if (currentCount == maxCount) {
                    equalCounter++;
                }
                maxCount = currentCount;
            }
        }

        return equalCounter;
    }

    private boolean canHeadReachApple(ArrayList<Character> validMoves) {
        for (char move : validMoves) {
            char[][] grid = convertToGrid();
            int[] headPosition = findHeadPosition(grid);

            int headRow = headPosition[0];
            int headCol = headPosition[1];

            grid[headRow][headCol] = 'B';
            switch (move) {
                case 'U' -> grid[--headRow][headCol] = 'H';
                case 'D' -> grid[++headRow][headCol] = 'H';
                case 'L' -> grid[headRow][--headCol] = 'H';
                case 'R' -> grid[headRow][++headCol] = 'H';
            }

            boolean isTrue = true;
            makeVForEachSideWithFreeNode(grid, headRow, headCol);
            fillEachDirectionWithV(grid, isTrue);

            int[] applePosition = findApplePosition(grid);
            int appleRow = applePosition[0];
            int appleCol = applePosition[1];

            if (grid[appleRow - 1][appleCol] == 'V') {
                return true;
            }
            if (grid[appleRow + 1][appleCol] == 'V') {
                return true;
            }
            if (grid[appleRow][appleCol - 1] == 'V') {
                return true;
            }
            if (grid[appleRow][appleCol + 1] == 'V') {
                return true;
            }
        }

        return false;
    }

    private int countV(char[][] grid) {
        int counter = 0;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == 'V') {
                    counter++;
                }
            }
        }
        return counter;
    }

    private void makeVForEachSideWithFreeNode(char[][] grid, int headRow, int headCol) {
        if (grid[headRow - 1][headCol] == '*') {
            grid[headRow - 1][headCol] = 'V';
            counterV++;
        }
        if (grid[headRow + 1][headCol] == '*') {
            grid[headRow + 1][headCol] = 'V';
            counterV++;
        }
        if (grid[headRow][headCol - 1] == '*') {
            grid[headRow][headCol - 1] = 'V';
            counterV++;
        }
        if (grid[headRow][headCol + 1] == '*') {
            grid[headRow][headCol + 1] = 'V';
            counterV++;
        }
    }

    private void fillEachDirectionWithV(char[][] grid, boolean isTrue) {
        while (isTrue) {
            counterV = 0;
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[0].length; col++) {
                    if (grid[row][col] == 'V') {
                        makeVForEachSideWithFreeNode(grid, row, col);
                    }
                }
            }

            if (counterV == 0) {
                isTrue = false;
            }
        }
    }

    private char[][] convertToGrid() {
        char[][] grid = new char[SCREEN_HEIGHT / UNIT_SIZE][SCREEN_WIDTH / UNIT_SIZE];

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                grid[i][j] = '*';
            }
        }

        for (int i = 0; i < bodyParts - 1; i++) {
            if (i == 0) {
                grid[y[i] / UNIT_SIZE][x[i] / UNIT_SIZE] = 'H';
            } else {
                grid[y[i] / UNIT_SIZE][x[i] / UNIT_SIZE] = 'B';
            }
        }

        grid[appleY / UNIT_SIZE][appleX / UNIT_SIZE] = 'A';

        for (int i = 2; i < grid.length; i++) {
            grid[i][0] = 'B';
            grid[i][grid[0].length - 1] = 'B';
        }
        for (int j = 0; j < grid[0].length; j++) {
            grid[2][j] = 'B';
            grid[grid.length - 1][j] = 'B';
        }

        return grid;
    }

    private ArrayList<Character> getValidMoves(int headX, int headY) {
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

        return validMoves;
    }

    public boolean checkIfSnakeTouchesTwoBorders() {
        boolean touchesLeftBorder = false;
        boolean touchesTopBorder = false;
        boolean touchesBottomBorder = false;
        boolean touchesRightBorder = false;

        for (int i = 0; i < bodyParts; i++) {
            if (x[i] <= UNIT_SIZE) {
                touchesLeftBorder = true;
            }
            if (x[i] >= SCREEN_WIDTH - UNIT_SIZE * 2) {
                touchesRightBorder = true;
            }
            if (y[i] <= UNIT_SIZE * 3) {
                touchesTopBorder = true;
            }
            if (y[i] >= SCREEN_HEIGHT - UNIT_SIZE * 2) {
                touchesBottomBorder = true;
            }
        }

        return touchesLeftBorder && touchesRightBorder || touchesBottomBorder && touchesTopBorder;
    }

    private int[] findHeadPosition(char[][] grid) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == 'H') {
                    return new int[]{row, col};
                }
            }
        }
        return new int[]{y[0] / UNIT_SIZE, x[0] / UNIT_SIZE};
    }

    private int[] findApplePosition(char[][] grid) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == 'A') {
                    return new int[]{row, col};
                }
            }
        }
        return new int[]{appleY / UNIT_SIZE, appleX / UNIT_SIZE};
    }
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Random;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author exstac
 */
public class AsteroidsWorld extends GameCanvas implements Runnable {

    /** The game states */
    private static final int SHOWING_LEGAL_NOTICE = 1;
    private static final int SHOWING_MENU = 2;
    private static final int SINGLE_PLAYER = 3;
    private static final int GAME_PAUSED = 4;
    private static final int HOSTING_MULTI = 5;
    private static final int JOINING_MULTI = 6;
    private static final int MULTIPLAYER = 7;
    private static final int SHOWING_WINNER = 8;
    private static final int NEW_HISHSCORE = 9;
    private static final int SHOWING_HISHSCORE = 10;
    private int state = SHOWING_LEGAL_NOTICE;
    private DeltaTimer t;
    private long deltaTime;
    private RockEntity[] rocks = null;
    private ParticleEntity[] particles = null;
    private PowerupEntity[] powerups = null;
    private UFOEntity ufo = null;
    private ShipEntity ship = null;
    private Random rand = null;
    private int score;
    private int nextExtraLife;
    private int rockCount;
    private int rockKillCount;
    private int level;
    private HighScore hs;
    private double grid;
    private boolean leftSoftPressed;
    private boolean rightSoftPressed;
    private boolean drawFPS = false;
    private Asteroids parent = null;
    private int levelTimer;
    private int ufoTimer;
    private int keyState;
    private int selectedMenuItem;
    private String playerName;
    private Font smallFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private Font smallBoldFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);
    private Font mediumFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private Font mediumBoldFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    private Font largeFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    private Font largeBoldFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_LARGE);
    private BluetoothConnection btc = null;
    private boolean gameWinner;

    public AsteroidsWorld(Asteroids parent) {
        super(false);
        this.parent = parent;
    }

    public void start() {
        Thread runner = new Thread(this);
        runner.start();
    }

    public void run() {
        // TODO: Remove the try-catch in run()
        try {
            Graphics g = getGraphics();
            init();
            while (true) {
                deltaTime = t.getDelta();
                if (!isShown()) {
                    // Do nothing
                } else if (state == SHOWING_LEGAL_NOTICE) {
                    // <editor-fold defaultstate="collapsed" desc="SHOWING_LEGAL_NOTICE state">
                    drawLegalNotice(g);
                    if (leftSoftPressed || (keyState & FIRE_PRESSED) != 0) {
                        state = SHOWING_MENU;
                        leftSoftPressed = false;
                        rightSoftPressed = false;
                        keyState &= ~FIRE_PRESSED;
                    }
                    // </editor-fold>
                    /**
                     * Continue to SHOWING_MENU
                     */
                } else if (state == SHOWING_MENU) {
                    // <editor-fold defaultstate="collapsed" desc="SHOWING_MENU state">
                    drawMenu(g);
                    if (leftSoftPressed || (keyState & FIRE_PRESSED) != 0) {
                        switch (selectedMenuItem) {
                            case 0: // Single player game
                                state = SINGLE_PLAYER;
                                reset();
                                break;
                            case 1: // Hosting multi
                                state = HOSTING_MULTI;
                                break;
                            case 2: // Joining multi
                                state = JOINING_MULTI;
                                break;
                            case 3:
                                state = SHOWING_HISHSCORE;
                                break;
                        }
                        leftSoftPressed = false;
                        keyState &= ~FIRE_PRESSED;
                    }
                    if (rightSoftPressed) {
                        parent.destroyApp(false);
                        parent.notifyDestroyed();
                    }
                    // </editor-fold>
                    /**
                     * Continue to SINGLE_PLAYER or
                     *             HOSTING_MULTI or
                     *             JOINING_MULTI or
                     *             SHOWING_HIGHSCORE or
                     *             Quit the app
                     */
                } else if (state == SINGLE_PLAYER) {
                    // <editor-fold defaultstate="collapsed" desc="SINGLE_PLAYER state">
                    if (rockCount == 0) {
                        advanceLevel();
                    }
                    if (score >= nextExtraLife) {
                        ship.addExtraLife();
                        nextExtraLife += 10000;
                    }
                    ship.handleInput(keyState, deltaTime);
                    updateScreen(g);
                    if (leftSoftPressed || !isShown()) {
                        state = GAME_PAUSED;
                        leftSoftPressed = false;
                    }
                    if (rightSoftPressed || !ship.isAlive()) {
                        state = hs.isRecord(score) ? NEW_HISHSCORE : SHOWING_MENU;
                        rightSoftPressed = false;
                    }
                    // </editor-fold>
                    /**
                     * Continue to SHOWING_MENU or
                     *             GAME_PAUSED or
                     *             NEW_HIGHSCORE
                     */
                } else if (state == GAME_PAUSED) {
                    // <editor-fold defaultstate="collapsed" desc="GAME_PAUSED state">
                    deltaTime = 0;
                    updateScreen(g);

                    if (leftSoftPressed && btc == null) {
                        state = SINGLE_PLAYER;
                        leftSoftPressed = false;
                        rightSoftPressed = false;
                    } else if (leftSoftPressed) {
                        btc.send(BluetoothConnection.RESUME_GAME);
                        leftSoftPressed = false;
                        rightSoftPressed = false;
                        state = MULTIPLAYER;
                    }
                    if (btc != null) {
                        if (btc.read() == BluetoothConnection.RESUME_GAME) {
                            leftSoftPressed = false;
                            rightSoftPressed = false;
                            state = MULTIPLAYER;
                        }
                        Thread.sleep(100);
                    }
                    // </editor-fold>
                    /**
                     * Continue to SINGLE_PLAYER or
                     *             MULTIPLAYER
                     */
                } else if (state == HOSTING_MULTI) {
                    // <editor-fold defaultstate="collapsed" desc="HOSTING_MULTI state">
                    if (btc == null) {
                        btc = new BluetoothConnection(true);
                    }
                    g.setColor(0x000000);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(0xFFFFFF);
                    if (btc.state == BluetoothConnection.WAITING) {
                        g.drawString("Waiting for", getWidth() / 2, getHeight() / 2 - mediumBoldFont.getHeight() / 2, Graphics.HCENTER | Graphics.BASELINE);
                        g.drawString("incoming connections", getWidth() / 2, getHeight() / 2 + mediumBoldFont.getHeight() / 2, Graphics.HCENTER | Graphics.BASELINE);
                        g.drawString("Cancel", getWidth() - 5, getHeight() - 5, Graphics.RIGHT | Graphics.BOTTOM);
                    } else if (btc.state == BluetoothConnection.CONNECTED) {
                        state = MULTIPLAYER;
                        rightSoftPressed = false;
                        leftSoftPressed = false;
                        reset();
                    }
                    if (rightSoftPressed || btc.state == BluetoothConnection.FAILED) {
                        state = SHOWING_MENU;
                        rightSoftPressed = false;
                        btc.destroy();
                        btc = null;
                    }
                    updateRocks(g);
                    flushGraphics();
                    // </editor-fold>
                    /**
                     * Continue to SHOWING_MENU or
                     *             MULTIPLAYER
                     */
                } else if (state == JOINING_MULTI) {
                    // <editor-fold defaultstate="collapsed" desc="JOINING_MULTI state">
                    if (btc == null) {
                        btc = new BluetoothConnection(false);
                    }
                    g.setColor(0x000000);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(0xFFFFFF);
                    if (btc.state == BluetoothConnection.WAITING) {
                        g.drawString("Searching...", getWidth() / 2, getHeight() / 2 - mediumBoldFont.getHeight() / 2, Graphics.HCENTER | Graphics.BASELINE);
                        g.drawString("Cancel", getWidth() - 5, getHeight() - 5, Graphics.RIGHT | Graphics.BOTTOM);
                    } else if (btc.state == BluetoothConnection.CONNECTING) {
                        g.drawString("Server found.", getWidth() / 2, getHeight() / 2 - mediumBoldFont.getHeight(), Graphics.HCENTER | Graphics.TOP);
                        g.drawString("Connecting...", getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.TOP);
                        g.drawString("Cancel", getWidth() - 5, getHeight() - 5, Graphics.RIGHT | Graphics.BOTTOM);
                    } else if (btc.state == BluetoothConnection.CONNECTED) {
                        state = MULTIPLAYER;
                        rightSoftPressed = false;
                        leftSoftPressed = false;
                        reset();
                    }
                    if (rightSoftPressed || btc.state == BluetoothConnection.FAILED) {
                        state = SHOWING_MENU;
                        rightSoftPressed = false;
                        btc.destroy();
                        btc = null;
                    }
                    updateRocks(g);
                    flushGraphics();
                    // </editor-fold>
                    /**
                     * Continue to SHOWING_MENU or
                     *             MULTIPLAYER
                     */
                } else if (state == MULTIPLAYER) {
                    // <editor-fold defaultstate="collapsed" desc="MULTIPLAYER state">
                    ship.handleInput(keyState, deltaTime);
                    updateScreen(g);
                    if (rockCount == 0) {
                        advanceLevel();
                    }
                    if (leftSoftPressed || !isShown()) {
                        state = GAME_PAUSED;
                        btc.send(BluetoothConnection.PAUSE_GAME);
                        leftSoftPressed = false;
                    }
                    switch (btc.read()) {
                        case BluetoothConnection.END_CONNECTION:
                            state = SHOWING_WINNER;
                            gameWinner = true;
                            btc.destroy();
                            btc = null;
                            break;
                        case BluetoothConnection.ADD_BIG_ROCK:
                            addRocks(1, RockEntity.BIG, 300);
                            break;
                        case BluetoothConnection.ADD_MEDIUM_ROCK:
                            addRocks(1, RockEntity.MEDIUM, 300);
                            break;
                        case BluetoothConnection.ADD_SMALL_ROCK:
                            addRocks(1, RockEntity.SMALL, 300);
                            break;
                        case BluetoothConnection.PAUSE_GAME:
                            state = GAME_PAUSED;
                            continue;
                        default:
                            // No data to read
                            break;
                    }

                    if (rightSoftPressed || !ship.isAlive()) {
                        gameWinner = false;
                        state = SHOWING_WINNER;
                        rightSoftPressed = false;
                        if (btc != null) {
                            btc.destroy();
                            btc = null;
                        }
                    }
                    //state = SHOWING_MENU;
                    // </editor-fold>
                    /**
                     * Continue to SHOWING_MENU
                     */
                } else if (state == SHOWING_WINNER) {
                    // <editor-fold defaultstate="collapsed" desc="SHOWING_WINNER state">
                    char[][] text = {{'Y', 'o', 'u', ' ', 'w', 'i', 'n'},
                        {'Y', 'o', 'u', ' ', 'l', 'o', 'o', 's', 'e'}};
                    g.setColor(0x000000);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    updateRocks(g);

                    g.setColor(0xFFFFFF);
                    g.setFont(mediumBoldFont);
                    if (gameWinner) {
                        g.drawChars(text[0], 0, text[0].length, getWidth() / 2, getHeight() / 2, Graphics.BASELINE | Graphics.HCENTER);
                    } else {
                        g.drawChars(text[1], 0, text[1].length, getWidth() / 2, getHeight() / 2, Graphics.BASELINE | Graphics.HCENTER);
                    }
                    g.drawString("Menu", 5, getHeight() - 5, Graphics.LEFT | Graphics.BOTTOM);
                    flushGraphics();
                    if (leftSoftPressed) {
                        state = SHOWING_MENU;
                        leftSoftPressed = false;
                        rightSoftPressed = false;
                    }
                    //</editor-fold>
                    /**
                     * Continue to SHOWING_HIGHSCORE
                     */
                } else if (state == NEW_HISHSCORE) {
                    // <editor-fold defaultstate="collapsed" desc="NEW_HIGHSCORE state">
                    getPlayerName(g);
                    if (leftSoftPressed) {
                        state = SHOWING_HISHSCORE;
                        leftSoftPressed = false;
                        rightSoftPressed = false;
                    }
                    //</editor-fold>
                    /**
                     * Continue to SHOWING_HIGHSCORE
                     */
                } else if (state == SHOWING_HISHSCORE) {
                    // <editor-fold defaultstate="collapsed" desc="SHOWING_HIGHSCORE state">
                    drawEndScreen(g);
                    if (leftSoftPressed || (keyState & FIRE_PRESSED) != 0) {
                        state = SHOWING_MENU;
                        leftSoftPressed = false;
                        rightSoftPressed = false;
                        keyState &= ~FIRE_PRESSED;
                    }
                    //</editor-fold>
                    /**
                     * Continue to SHOWING_MENU
                     */
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addParticles(int count, double x, double y) {
        for (int i = 0; count > 0 && i < particles.length; ++i) {
            if (!particles[i].isActive()) {
                particles[i].reset(
                        x + (3 * grid * rand.nextDouble() - 1.5 * grid),
                        y + (3 * grid * rand.nextDouble() - 1.5 * grid),
                        0.12 * Math.sin(2 * Math.PI * rand.nextDouble()),
                        -0.12 * Math.cos(2 * Math.PI * rand.nextDouble()),
                        rand.nextInt(600));
                count--;
            }
        }
    }

    private void addRocks(int count, int size, int spawnTimer) {
        addRocks(count, size, 0, 0, spawnTimer);
    }

    private void addRocks(int count, int size, double x, double y, int spawnTimer) {
        double tmpx, tmpy;
        for (int i = 0; i < rocks.length && count > 0; ++i) {
            if (rocks[i] == null) {
                if (x == 0 && y == 0) {
                    do {
                        tmpx = rand.nextInt(getWidth());
                        tmpy = rand.nextInt(getHeight());
                    } while (ship.getDist(tmpx, tmpy) < getWidth() / 3);
                } else {
                    tmpx = x + rand.nextInt(10) - 5;
                    tmpy = y + rand.nextInt(10) - 5;
                }
                rocks[i] = new RockEntity(tmpx, tmpy, (rand.nextDouble() - 0.5) / 10.0, (rand.nextDouble() - 0.5) / 10.0, getWidth(), getHeight(), size, rand.nextInt(4), grid, spawnTimer);
                count--;
            }
        }
    }

    private void sendRocks() {
        while (rockKillCount > 0) {
            if (rockKillCount >= 7) {
                btc.send(BluetoothConnection.ADD_BIG_ROCK);
                rockKillCount -= 7;
            } else if (rockKillCount >= 3) {
                btc.send(BluetoothConnection.ADD_MEDIUM_ROCK);
                rockKillCount -= 3;
            } else if (rockKillCount >= 1) {
                btc.send(BluetoothConnection.ADD_SMALL_ROCK);
                rockKillCount -= 1;
            }
        }
    }

    private void advanceLevel() {
        if (levelTimer < 0) {
            level++;
            rockCount = level + 2 > 12 ? 12 : level + 2;
            rockCount /= state == MULTIPLAYER ? 2 : 1;
            addRocks(rockCount, RockEntity.BIG, 300);
            levelTimer = 0;
        } else if (levelTimer == 0) {
            levelTimer = 500;
        } else {
            levelTimer -= deltaTime;
            if (levelTimer == 0) {
                levelTimer--;
            }
        }
    }

    private void drawEndScreen(Graphics g) {
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        updateRocks(g);
        g.setColor(0xFFFFFF);
        hs.draw(g, getWidth(), getHeight());
        g.setFont(mediumBoldFont);
        g.drawString("Menu", 5, getHeight() - 5, Graphics.LEFT | Graphics.BOTTOM);
        flushGraphics();
    }

    private void drawLegalNotice(Graphics g) {
        String[] s = {"This ", "game ", "is ", "an ", "unofficial ", "clone ", "of ", "the ", "original ", "Asteroids® ", "game ", "and ", "is ", "not ", "endorsed ", "by ", "the ", "registered ", "trademark ", "and ", "copyright ", "owners ", "Atari Interactive, Inc."};
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(smallFont);
        g.setColor(0xFFFFFF);
        g.drawString("Legal notice:", 5, 5, 0);
        int xOffset = 0;
        int line = 1;
        for (int i = 0; i < s.length; ++i) {
            if (xOffset + smallFont.stringWidth(s[i]) > 0.9 * getWidth()) {
                xOffset = 0;
                line++;
            }
            g.drawString(s[i], 5 + xOffset, 5 + line * smallFont.getHeight(), Graphics.LEFT | Graphics.TOP);
            xOffset += smallFont.stringWidth(s[i]);
        }
        g.drawString("Continue", 5, getHeight() - 5, Graphics.LEFT | Graphics.BOTTOM);
        flushGraphics();
    }

    private void drawLives(Graphics g) {
        double x = 10 + grid, y = 40;
        for (int i = 0; i < ship.lives; ++i) {
            g.drawLine((int) (x + 3 * i * grid), (int) (y - 2 * grid), (int) (x + 3 * i * grid + grid), (int) (y + grid));
            g.drawLine((int) (x + 3 * i * grid), (int) (y - 2 * grid), (int) (x + 3 * i * grid - grid), (int) (y + grid));
            g.drawLine((int) (x + 3 * i * grid - 5.0 / 6.0 * grid), (int) (y + grid / 2), (int) (x + 3 * i * grid + 5.0 / 6.0 * grid), (int) (y + grid / 2));
        }
    }

    private void drawMenu(Graphics g) {
        String[] menuItems = {"Start game", "Host game", "Join game", "Highscores"};
        final int yOffset = ((menuItems.length - 1) * smallFont.getHeight() +
                smallBoldFont.getHeight() + largeFont.getHeight()) / 2;
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < rocks.length; ++i) {
            if (rocks[i] != null) {
                rocks[i].update(g, deltaTime);
            }
        }
        g.setColor(0xFFFFFF);
        g.setFont(largeBoldFont);
        g.drawString("ASTEROIDS", getWidth() / 2, getHeight() / 2 - yOffset, Graphics.TOP | Graphics.HCENTER);
        for (int i = 0; i < menuItems.length; ++i) {
            if (selectedMenuItem == i) {
                g.setFont(smallBoldFont);
            } else {
                g.setFont(smallFont);
            }
            g.drawString(menuItems[i], getWidth() / 2, getHeight() / 2 - yOffset + i * smallFont.getHeight() + largeFont.getHeight(), Graphics.TOP | Graphics.HCENTER);
        }
        g.setFont(mediumBoldFont);
        g.drawString("Select", 5, getHeight() - 5, Graphics.LEFT | Graphics.BOTTOM);
        g.drawString("Exit", getWidth() - 5, getHeight() - 5, Graphics.RIGHT | Graphics.BOTTOM);
        flushGraphics();
        if ((keyState & DOWN_PRESSED) != 0) {
            selectedMenuItem = (selectedMenuItem + 1) % menuItems.length;
            keyState &= ~DOWN_PRESSED;
        } else if ((keyState & UP_PRESSED) != 0) {
            selectedMenuItem = (selectedMenuItem == 0) ? menuItems.length - 1 : selectedMenuItem - 1;
            keyState &= ~UP_PRESSED;
        }
    }
    private boolean NEW_HIGHSCORE_InitDone = false;
    char current = 0;
    char[] tmpName = new char[10];
    int caretPos, stringWidth;

    private void getPlayerName(Graphics g) {
        if (!NEW_HIGHSCORE_InitDone) {
            caretPos = 0;
            stringWidth = 0;
            if (playerName.length() > 0) {
                playerName.getChars(0, playerName.length(), tmpName, 0);
            } else {
                for (int i = 0; i < tmpName.length; ++i) {
                    tmpName[i] = ' ';
                }
            }
            current = tmpName[0];
            NEW_HIGHSCORE_InitDone = true;
        }
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(0xFFFFFF);
        g.setFont(mediumFont);
        g.drawString("NEW HIGHSCORE!", 5, getHeight() / 2 - mediumFont.getHeight(), Graphics.LEFT | Graphics.BASELINE);
        g.drawString("Enter name: ", 5, getHeight() / 2, Graphics.LEFT | Graphics.BASELINE);
        g.drawChars(tmpName, 0, tmpName.length, 5, getHeight() / 2 + mediumFont.getHeight(), Graphics.LEFT | Graphics.BASELINE);
        g.drawString("_", 5 + stringWidth, getHeight() / 2 + mediumFont.getHeight(), Graphics.LEFT | Graphics.BASELINE);
        g.drawString("Submit", 5, getHeight() - 5, Graphics.LEFT | Graphics.BOTTOM);
        updateRocks(g);
        flushGraphics();
        if ((keyState & DOWN_PRESSED) != 0) {
            if (current == ' ') {
                current = 'A';
            } else if (current == 'Z') {
                current = ' ';
            } else {
                current++;
            }
            keyState &= ~DOWN_PRESSED;
        } else if ((keyState & UP_PRESSED) != 0) {
            if (current == 'A') {
                current = ' ';
            } else if (current == ' ') {
                current = 'Z';
            } else {
                current--;
            }
            keyState &= ~UP_PRESSED;
        } else if ((keyState & RIGHT_PRESSED) != 0) {
            if (caretPos < tmpName.length - 1) {
                stringWidth += mediumFont.charWidth(current);
                caretPos++;
                current = tmpName[caretPos];
            }
            keyState &= ~RIGHT_PRESSED;
        } else if ((keyState & LEFT_PRESSED) != 0) {
            if (caretPos > 0) {
                caretPos--;
                current = tmpName[caretPos];
                stringWidth -= mediumFont.charWidth(current);
            }
            keyState &= ~LEFT_PRESSED;
        }
        tmpName[caretPos] = current;
        if (leftSoftPressed) {
            hs.addRecord(playerName = new String(tmpName), score);
        }
    }

    private void init() {
        t = new DeltaTimer();
        grid = (double) getWidth() / 60;
        rocks = new RockEntity[50];
        particles = new ParticleEntity[100];
        powerups = new PowerupEntity[10];
        rand = new Random();
        ship = new ShipEntity(getWidth(), getHeight(), grid, rand, false);
        ufo = new UFOEntity(getWidth(), getHeight(), grid, rand, ship);
        for (int i = 0; i < particles.length; ++i) {
            particles[i] = new ParticleEntity(getWidth(), getHeight());
        }
        for (int i = 0; i < powerups.length; ++i) {
            powerups[i] = new PowerupEntity(getWidth(), getHeight(), grid, rand);
        }
        hs = new HighScore("scores");
        playerName = hs.getDefaultName();
        for (int i = 0; i < 15; ++i) {
            addRocks(1, rand.nextInt(3) + 1, rand.nextInt(getWidth()), rand.nextInt(getHeight()), -1);
        }
    }

    private void updateParticles(Graphics g) {
        for (int i = 0; i < particles.length; ++i) {
            if (particles[i].isActive()) {
                particles[i].update(g, deltaTime);
            }
        }
    }

    private void updatePowerups(Graphics g) {
        for (int i = 0; i < powerups.length; ++i) {
            if (powerups[i].isActive()) {
                powerups[i].update(g, deltaTime);
                if (powerups[i].collides(ship)) {
                    switch (powerups[i].type) {
                        case PowerupEntity.SHIELD:
                            ship.setShield();
                            break;
                        case PowerupEntity.EXTRA_LIFE:
                            ship.lives++;
                            break;
                        case PowerupEntity.EXTRA_ROCKS:
                            rockKillCount += 5;
                            break;
                        case PowerupEntity.PIERCING_SHOTS:
                            ship.setPiercingShots();
                            break;
                        default:
                            break;
                    }
                    powerups[i].setInactive();
                }
            }
        }
    }

    private void explodeRock(int i) {
        rockCount--;
        if (rocks[i].getSize() != RockEntity.SMALL) {
            addRocks(2, rocks[i].getSize() - 1, rocks[i].getX(), rocks[i].getY(), -1);
            rockCount += 2;
        }
        addParticles((rocks[i].getSize() + 2) * 5, rocks[i].getX(), rocks[i].getY());
        rocks[i] = null;
    }

    private void updateRocks(Graphics g) {
        for (int i = 0; i < rocks.length; ++i) {
            if (rocks[i] != null) {
                rocks[i].update(g, deltaTime);
                if (ship == null || ufo == null) {
                    continue;
                } else if (ship.shotCollision(rocks[i])) {
                    switch (rocks[i].getSize()) {
                        case RockEntity.BIG:
                            score += 20;
                            break;
                        case RockEntity.MEDIUM:
                            score += 50;
                            break;
                        case RockEntity.SMALL:
                            score += 100;
                            break;
                    }
                    if (state == MULTIPLAYER && !ship.gotPiercingShots()) {
                        rockKillCount++;
                        for (int j = 0; j < powerups.length; ++j) {
                            if (!powerups[j].isActive()) {
                                powerups[j].reset(rocks[i].getX(), rocks[i].getY());
                                break;
                            }
                        }
                    }
                    explodeRock(i);
                } else if (rocks[i].collides(ship)) {
                    explodeRock(i);
                    ship.kill();
                } else if (ufo.isActive() && rocks[i].collides(ufo.getShot())) {
                    ufo.getShot().setInactive();
                    explodeRock(i);
                } else if (ufo.isActive() && rocks[i].collides(ufo)) {
                    explodeRock(i);
                    addParticles(30, ufo.getX(), ufo.getY());
                    ufo.setInactive();
                }
            }
        }
    }

    private void drawScore(Graphics g) {
        g.setFont(mediumBoldFont);
        g.setColor(0xFFFFFF);
        if (state == MULTIPLAYER) {
            g.drawString("Rocks: " + rockKillCount, 10, 5, Graphics.LEFT | Graphics.TOP);
        } else {
            g.drawString(score + "", 10, 5, Graphics.LEFT | Graphics.TOP);
            g.drawString("Level " + level, getWidth() - 10, 5, Graphics.RIGHT | Graphics.TOP);
        }
    }

    private void updateShip(Graphics g) {
        ship.update(g, deltaTime);
    }

    protected void keyRepeated(int keyCode) {
        if (state == NEW_HISHSCORE) {
            keyPressed(keyCode);
        }
    }

    protected void keyPressed(int keyCode) {
        // TODO: Create a custom input class
        final int LEFT_SOFT = -6;
        final int RIGHT_SOFT = -7;
        int gameAction = getGameAction(keyCode);
        //System.out.println(keyCode + ", char: " + (char) keyCode + ", game action: " + gameAction);
        switch (keyCode) {
            case LEFT_SOFT:
                leftSoftPressed = true;
                break;
            case RIGHT_SOFT:
                rightSoftPressed = true;
                break;
            case '*':
                ship.setAutoFire();
                break;
            case '#':
                if (state == SINGLE_PLAYER) {
                    ship.teleport();
                } else if (state == MULTIPLAYER) {
                    ship.activatePowerup();
                }
                break;
            case '0':
                drawFPS = !drawFPS;
                break;
            case '7':
                if (state != NEW_HISHSCORE && state != SHOWING_MENU) {
                    keyState |= LEFT_PRESSED;
                    break;
                }
            case '9':
                if (state != NEW_HISHSCORE && state != SHOWING_MENU) {
                    keyState |= RIGHT_PRESSED;
                    break;
                }
            default:
                keyState |= 1 << gameAction;
                break;
        }
    }

    protected void keyReleased(int keyCode) {
        if (keyCode == '7') {
            keyState &= ~LEFT_PRESSED;
        } else if (keyCode == '8') {
            keyState &= ~UP_PRESSED;
        } else if (keyCode == '9') {
            keyState &= ~RIGHT_PRESSED;
        }
        keyState &= ~(1 << getGameAction(keyCode));
    }

    public void updateScreen(Graphics g) {
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(mediumBoldFont);
        g.setColor(0xFFFFFF);
        if (state == GAME_PAUSED) {
            g.drawString("GAME PAUSED", getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.BASELINE);
            g.drawString("Resume", 5, getHeight() - 5, Graphics.LEFT | Graphics.BOTTOM);
        } else {
            if (drawFPS && deltaTime != 0) {
                g.drawString(1000 / deltaTime + "", getWidth() / 2, 5, Graphics.HCENTER | Graphics.TOP);
            }
            g.drawString("Pause", 5, getHeight() - 5, Graphics.LEFT | Graphics.BOTTOM);
            g.drawString("Quit", getWidth() - 5, getHeight() - 5, Graphics.RIGHT | Graphics.BOTTOM);
        }
        if (state == MULTIPLAYER) {
            switch (ship.powerup) {
                case PowerupEntity.PIERCING_SHOTS:
                    g.drawString("<P>", getWidth() / 2, getHeight() - 5, Graphics.HCENTER | Graphics.BOTTOM);
                    break;
                case PowerupEntity.SHIELD:
                    g.drawString("<S>", getWidth() / 2, getHeight() - 5, Graphics.HCENTER | Graphics.BOTTOM);
                    break;
                default:
                    g.drawString("< >", getWidth() / 2, getHeight() - 5, Graphics.HCENTER | Graphics.BOTTOM);
                    break;
            }
        }
        updateShip(g);
        updateRocks(g);
        updateParticles(g);
        updatePowerups(g);
        updateUfo(g);
        drawScore(g);
        drawLives(g);
        flushGraphics();
    }

    private void updateUfo(Graphics g) {
        if (state == SINGLE_PLAYER && ufo.isActive()) {
            ufo.update(g, deltaTime);
            if (ship.shotCollision(ufo)) {
                ufo.setInactive();
                addParticles(30, ufo.getX(), ufo.getY());
                score += ufo.getSize() == UFOEntity.BIG ? 200 : 1000;
            }
            if (ufo.shotCollision(ship)) {
                ship.kill();
                addParticles(30, ship.getX(), ship.getY());
            }
        } else if ((ufoTimer -= deltaTime) < 0) {
            ufoTimer = rand.nextInt(20000) + 25000 - 2000 * (level > 10 ? 10 : level);
            ufo.reset(rand.nextDouble() > 0.8 ? UFOEntity.SMALL : UFOEntity.BIG);
        }
    }

    private void reset() {
        for (int i = 0; i < rocks.length; ++i) {
            rocks[i] = null;
        }
        for (int i = 0; i < particles.length; ++i) {
            particles[i].setInactive();
        }
        ship.reset();
        score = 0;
        nextExtraLife = 10000;
        rockCount = 0;
        rockKillCount = 0;
        level = 0;
        levelTimer = 0;
        ufoTimer = rand.nextInt(10000) + 20000;
        selectedMenuItem = 0;
    }
}

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

    public AsteroidsWorld(boolean suppressKeyEvents) {
        super(suppressKeyEvents);
    }

    public void start() {
        Thread runner = new Thread(this);
        runner.start();
    }

    public void run() {
        t = new DeltaTimer();
        xPos = getWidth() / 2;
        yPos = getHeight() / 2;
        grid = Math.max(getWidth() / 60, 4);

        shots = new Shot[6];
        for (int i = 0; i < shots.length; ++i) {
            shots[i] = new Shot(getWidth(), getHeight(), grid);
        }
        rocks = new Rock[50];
        particles = new Particle[100];
        for (int i = 0; i < particles.length; ++i) {
            particles[i] = new Particle(getWidth(), getHeight());
        }
        rand = new Random();
        hs = new HighScore("scores");
        Graphics g = getGraphics();
        rockCount = 0;
        level = 0;
        lives = 3;
        for (int i = 0; i < 15; ++i) {
            addRocks(1, rand.nextInt(3) + 1);
        }
        while (true) {
            g.setColor(0x000000);
            g.fillRect(0, 0, getWidth(), getHeight());
            deltaTime = t.getDelta();
            for (int i = 0; i < 15; ++i) {
                rocks[i].draw(deltaTime, g);
            }
            g.setColor(0xFFFFFF);
            g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE));
            g.drawString("ASTEROIDS", getWidth() / 2, getHeight() / 2, Graphics.BASELINE | Graphics.HCENTER);
            g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL));
            g.drawString("Press any button to play", getWidth() / 2, getHeight() / 2 + 30, Graphics.BASELINE | Graphics.HCENTER);
            flushGraphics();
            if (getKeyStates() != 0) {
                for (int i = 0; i < 15; ++i) {
                    rocks[i] = null;
                }
                break;
            }
        }
        while (lives != 0) {
            if (rockCount == 0) {
                level++;
                rockCount = level + 2 > 12 ? 12 : level + 2;
                addRocks(rockCount, Rock.BIG);
            }
            handleInput();
            updateScreen(g);
            moveShip();
        }
        hs.addRecord("Oscar", score);
        while (true) {
            drawEndScreen(g);
        }
    }

    private void addParticles(int count, double x, double y) {
        addParticles(count, x, y, 0);
    }

    private void addParticles(int count, double x, double y, double angle) {
        for (int i = 0; count > 0 && i < particles.length; ++i) {
            if (!particles[i].isActive()) {
                particles[i].reset(
                        x + (3 * grid * rand.nextDouble() - 1.5 * grid),
                        y + (3 * grid * rand.nextDouble() - 1.5 * grid),
                        0.12 * Math.sin(angle == 0 ? 2 * Math.PI * rand.nextDouble() : angle),
                        -0.12 * Math.cos(angle == 0 ? 2 * Math.PI * rand.nextDouble() : angle),
                        rand.nextInt(600));
                count--;
            }
        }
    }

    private void addRocks(int count, int size) {
        addRocks(count, size, 0, 0);
    }

    private void addRocks(int count, int size, double x, double y) {
        for (int i = 0; i < rocks.length && count > 0; ++i) {
            if (rocks[i] == null) {
                double tmpx = xPos, tmpy = yPos;
                if (x == 0 && y == 0) {
                    while (Math.sqrt((tmpx - xPos) * (tmpx - xPos) + (tmpy - yPos) * (tmpy - yPos)) < getWidth() / 4) {
                        tmpx = rand.nextInt(getWidth());
                        tmpy = rand.nextInt(getHeight());
                    }
                } else {
                    tmpx = x + rand.nextInt(10) - 5;
                    tmpy = y + rand.nextInt(10) - 5;
                }
                rocks[i] = new Rock(tmpx, tmpy, (rand.nextDouble() - 0.5) / 10.0, (rand.nextDouble() - 0.5) / 10.0, getWidth(), getHeight(), size, rand.nextInt(4), grid);
                count--;
            }
        }
    }

    private void drawEndScreen(Graphics g) {
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        drawRocks(g);
        g.setFont(Font.getDefaultFont());
        g.setColor(0xFFFFFF);
        hs.draw(g, getWidth(), getHeight());
        flushGraphics();
    }

    private void drawLives(Graphics g) {
        int x = 10 + grid, y = 40;
        for (int i = 0; i < lives; ++i) {
            g.drawLine(x + 3 * i * grid, y - 2 * grid, x + 3 * i * grid + grid, y + grid);
            g.drawLine(x + 3 * i * grid, y - 2 * grid, x + 3 * i * grid - grid, y + grid);
            g.drawLine(x + 3 * i * grid, y, x + 3 * i * grid - grid, y + grid);
            g.drawLine(x + 3 * i * grid, y, x + 3 * i * grid + grid, y + grid);
        }
    }

    private void drawParticles(Graphics g) {
        for (int i = 0; i < particles.length; ++i) {
            if (particles[i].isActive()) {
                particles[i].draw(deltaTime, g);
            }
        }
    }

    private void drawRocks(Graphics g) {
        for (int i = 0; i < rocks.length; ++i) {
            if (rocks[i] != null) {
                rocks[i].draw(deltaTime, g);
            }
        }
    }

    private void drawScore(Graphics g) {
        g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.setColor(0xFFFFFF);
        g.drawString(score + " grid: " + grid, 10, 28, Graphics.LEFT | Graphics.BOTTOM);
        g.drawString("Level " + level, getWidth() - 10, 28, Graphics.RIGHT | Graphics.BOTTOM);
    }

    private void drawShip(Graphics g) {
        g.setColor(0xFFFFFF);
        if (invincibleTimer > 0 && (invincibleTimer % 500) > 250) {
            return;
        }
        g.drawLine(
                (int) (xPos + 2 * grid * Math.sin(shipAngle)),
                (int) (yPos - 2 * grid * Math.cos(shipAngle)),
                (int) (xPos + grid * Math.cos(shipAngle) - grid * Math.sin(shipAngle)),
                (int) (yPos + grid * Math.cos(shipAngle) + grid * Math.sin(shipAngle)));
        g.drawLine(
                (int) (xPos + 2 * grid * Math.sin(shipAngle)),
                (int) (yPos - 2 * grid * Math.cos(shipAngle)),
                (int) (xPos - grid * Math.cos(shipAngle) - grid * Math.sin(shipAngle)),
                (int) (yPos + grid * Math.cos(shipAngle) - grid * Math.sin(shipAngle)));
        g.drawLine(
                (int) (xPos),
                (int) (yPos),
                (int) (xPos - grid * Math.cos(shipAngle) - grid * Math.sin(shipAngle)),
                (int) (yPos + grid * Math.cos(shipAngle) - grid * Math.sin(shipAngle)));
        g.drawLine(
                (int) (xPos),
                (int) (yPos),
                (int) (xPos + grid * Math.cos(shipAngle) - grid * Math.sin(shipAngle)),
                (int) (yPos + grid * Math.cos(shipAngle) + grid * Math.sin(shipAngle)));
    }

    private void handleInput() {
        int keyState = getKeyStates();
        if ((keyState & UP_PRESSED) != 0) {
            xVel += 0.0003 * deltaTime * Math.sin(shipAngle);
            yVel -= 0.0003 * deltaTime * Math.cos(shipAngle);
//            addParticles(1, 4, xPos, yPos, Math.toRadians(rand.nextInt(90)+135)+shipAngle);
        }
        if ((keyState & LEFT_PRESSED) != 0) {
            shipAngle -= Math.toRadians(deltaTime / 4);
            if (shipAngle < 0) {
                shipAngle += 2 * Math.PI;
            }
        }
        if ((keyState & RIGHT_PRESSED) != 0) {
            shipAngle += Math.toRadians(deltaTime / 4);
            if (shipAngle > 2 * Math.PI) {
                shipAngle -= 2 * Math.PI;
            }
        }
        if ((keyState & FIRE_PRESSED) != 0 && shotDelay <= 0) {
            for (int i = 0; i < shots.length; ++i) {
                if (!shots[i].isActive()) {
                    shots[i].reset(xPos, yPos, xVel, yVel, shipAngle);
                    shotDelay = 200;
                    break;
                }
            }
        }


        if (shotDelay > 0) {
            shotDelay -= deltaTime;
        }
    }

    private void moveShip() {
        xPos += deltaTime * xVel;
        yPos += deltaTime * yVel;

        // Wrap screen edges
        if (xPos < 0) {
            xPos += getWidth();
        } else if (xPos > getWidth()) {
            xPos -= getWidth();
        } else if (yPos < 0) {
            yPos += getHeight();
        } else if (yPos > getHeight()) {
            yPos -= getHeight();
        }

        for (int i = 0; i < rocks.length && invincibleTimer <= 0; ++i) {
            if (rocks[i] != null && rocks[i].collidesWith(xPos, yPos)) {
                lives--;
                invincibleTimer = 3000;
                xPos = getWidth() / 2;
                yPos = getHeight() / 2;
                xVel = 0;
                yVel = 0;
                rockCount--;
                if (rocks[i].getSize() != Rock.SMALL) {
                    addRocks(2, rocks[i].getSize() - 1, rocks[i].getX(), rocks[i].getY());
                    rockCount += 2;
                }
                addParticles((rocks[i].getSize() + 2) * 5, rocks[i].getX(), rocks[i].getY());
                rocks[i] = null;
            }
        }
        invincibleTimer -= invincibleTimer > 0 ? deltaTime : 0;
    }

    private void drawShots(Graphics g) {
        for (int i = 0; i < shots.length; ++i) {
            if (shots[i].isActive()) {
                shots[i].draw(deltaTime, g);
                for (int j = 0; j < rocks.length && shots[i] != null; ++j) {
                    if (rocks[j] != null && rocks[j].collidesWith(shots[i].getX(), shots[i].getY())) {
                        shots[i].setInactive();
                        rockCount--;
                        if (rocks[j].getSize() != Rock.SMALL) {
                            addRocks(2, rocks[j].getSize() - 1, rocks[j].getX(), rocks[j].getY());
                            rockCount += 2;
                        }

                        addParticles((rocks[j].getSize() + 2) * 5, rocks[j].getX(), rocks[j].getY());
                        switch (rocks[j].getSize()) {
                            case Rock.BIG:
                                score += 20;
                                break;
                            case Rock.MEDIUM:
                                score += 50;
                                break;
                            case Rock.SMALL:
                                score += 100;
                                break;
                        }
                        rocks[j] = null;
                    }
                }
            }
        }
    }

    public void updateScreen(Graphics g) {
        deltaTime = t.getDelta();
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        drawShots(g);
        drawShip(g);
        drawRocks(g);
        drawScore(g);
        drawLives(g);
        drawParticles(g);
        flushGraphics();
    }
    // hmm
    private DeltaTimer t;
    private long deltaTime;
    private double xPos = getWidth() / 2.0;
    private double yPos = getHeight() / 2.0;
    private double xVel = 0.0;
    private double yVel = 0.0;
    private double shipAngle = 0.0;
    private int shotDelay = 0;
    private Shot[] shots = null;
    private Rock[] rocks = null;
    private Particle[] particles = null;
    private Random rand = null;
    private int lives;
    private long invincibleTimer = 0;
    private int score = 0;
    private int rockCount;
    private int level;
    private HighScore hs;
    private int grid;
}

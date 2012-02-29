
import java.util.Random;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author exstac
 */
public class ShipEntity extends Entity {

    private boolean autoFire;
    private int shotDelay;
    private ShotEntity[] shots = null;
    public int lives;
    private int killedTimer, teleportTimer, piercingShotTimer, shieldTimer;
    private boolean accelerating, killed;
    private DebrisEntity[] debris = null;
    private Random rand = null;
    public int powerup;

    public ShipEntity(int width, int height, double grid, Random rand, boolean remoteShip) {
        this.width = width;
        this.height = height;
        this.grid = grid;
        this.x = width / 2;
        this.y = height / 2;
        this.rand = rand;
        debris = new DebrisEntity[5];
        autoFire = false;
        killed = false;
        lives = 0;
        powerup = -1;
        piercingShotTimer = 0;
        shieldTimer = 0;
        teleportTimer = 0;
        bounds = new BoundingShape(BoundingShape.CIRCLE, grid);
        shots = new ShotEntity[6];
        for (int i = 0; i < shots.length; ++i) {
            shots[i] = new ShotEntity(width, height, grid);
        }
        // Initialize debris
        for (int i = 0; i < debris.length; ++i) {
            debris[i] = new DebrisEntity();
        }
    }

    public void addExtraLife() {
        lives++;
    }

    public double getDist(Entity e) {
        return Math.sqrt((x - e.x) * (x - e.x) + (y - e.y) * (y - e.y));
    }

    public double getDist(double x, double y) {
        return Math.sqrt((x - this.x) * (x - this.x) + (y - this.y) * (y - this.y));
    }

    public boolean gotPiercingShots() {
        return piercingShotTimer > 0;
    }

    public void handleInput(long deltaTime) {
        handleInput(0, deltaTime);
    }

    public void handleInput(int keyState, long deltaTime) {
        if (killedTimer > 0) {
            return;
        }
        accelerating = (keyState & GameCanvas.UP_PRESSED) != 0;
        if ((keyState & GameCanvas.UP_PRESSED) != 0 ||
                (keyState & GameCanvas.DOWN_PRESSED) != 0) {
            dx += 0.0003 * deltaTime * Math.sin(angle);
            dy -= 0.0003 * deltaTime * Math.cos(angle);
            accelerating = true;
        } else {
            accelerating = false;
        }
        if ((keyState & GameCanvas.LEFT_PRESSED) != 0) {
            angle -= Math.toRadians((double) deltaTime / 4);
            if (angle < 0) {
                angle += 2 * Math.PI;
            }
        }
        if ((keyState & GameCanvas.RIGHT_PRESSED) != 0) {
            angle += Math.toRadians((double) deltaTime / 4);
            if (angle > 2 * Math.PI) {
                angle -= 2 * Math.PI;
            }
        }
        if ((autoFire || (keyState & GameCanvas.FIRE_PRESSED) != 0) && shotDelay <= 0) {
            for (int i = 0; i < shots.length; ++i) {
                if (!shots[i].isActive()) {
                    shots[i].reset(x, y, dx, dy, angle, piercingShotTimer > 0);
                    shotDelay = 200;
                    break;
                }
            }
        }

        if (shotDelay > 0) {
            shotDelay -= deltaTime;
        }
    }

    public boolean isAlive() {
        return lives > 0;
    }

    public void kill() {
        if (shieldTimer <= 0) {
            // Reset debris
            for (int i = 0; i < debris.length; ++i) {
                debris[i].reset();
            }
            // Set invulnerable and reset position and speed
            killedTimer = 2000;
            shieldTimer = 3000;
            x = width / 2;
            y = height / 2;
            dx = 0;
            dy = 0;
            killed = true;
            bounds.setCollidable(false);
        }
    }

    public void reset() {
        x = width / 2;
        y = height / 2;
        dx = 0;
        dy = 0;
        angle = 0;
        autoFire = false;
        killed = false;
        lives = 3;
        killedTimer = 0;
        teleportTimer = 0;
        for (int i = 0; i < debris.length; ++i) {
            debris[i] = new DebrisEntity();
        }
    }

    public void setAutoFire() {
        autoFire = !autoFire;
    }

    public void activatePowerup() {
        if (powerup == PowerupEntity.PIERCING_SHOTS) {
            piercingShotTimer = 3000;
        } else if (powerup == PowerupEntity.SHIELD) {
            shieldTimer = 5000;
        }
        powerup = -1;
    }

    public void setPiercingShots() {
        powerup = PowerupEntity.PIERCING_SHOTS;
    }

    public void setShield() {
        powerup = PowerupEntity.SHIELD;
    }

    public boolean shotCollision(Entity e) {
        for (int i = 0; i < shots.length; ++i) {
            if (shots[i].isActive() && shots[i].collides(e)) {
                if (piercingShotTimer <= 0) {
                    shots[i].setInactive();
                }
                return true;
            }
        }
        return false;
    }

    public void teleport() {
        if (teleportTimer <= 0) {
            x = rand.nextInt(width);
            y = rand.nextInt(height);
            shieldTimer = 3000;
            teleportTimer = 10000;
        }
    }

    public void update(Graphics g, long deltaTime) {
        for (int i = 0; i < shots.length; ++i) {
            shots[i].update(g, deltaTime);
        }
        for (int i = 0; i < debris.length; ++i) {
            debris[i].update(g, deltaTime);
        }

        x += deltaTime * dx;
        y += deltaTime * dy;

        // Wrap screen edges
        if (x < 0) {
            x += width;
        } else if (x > width) {
            x -= width;
        } else if (y < 0) {
            y += height;
        } else if (y > height) {
            y -= height;
        }
        //bounds.draw(g);
        g.setColor(0xFFFFFF);
        if (killedTimer <= 0) {
            g.drawLine(
                    (int) (x + 2 * grid * Math.sin(angle)),
                    (int) (y - 2 * grid * Math.cos(angle)),
                    (int) (x + grid * Math.cos(angle) - grid * Math.sin(angle)),
                    (int) (y + grid * Math.cos(angle) + grid * Math.sin(angle)));
            g.drawLine(
                    (int) (x + 2 * grid * Math.sin(angle)),
                    (int) (y - 2 * grid * Math.cos(angle)),
                    (int) (x - grid * Math.cos(angle) - grid * Math.sin(angle)),
                    (int) (y + grid * Math.cos(angle) - grid * Math.sin(angle)));
            g.drawLine(
                    (int) (x - 5.0 / 6.0 * grid * Math.cos(angle) - 1.0 / 2.0 * grid * Math.sin(angle)),
                    (int) (y + 1.0 / 2.0 * grid * Math.cos(angle) - 5.0 / 6.0 * grid * Math.sin(angle)),
                    (int) (x + 5.0 / 6.0 * grid * Math.cos(angle) - 1.0 / 2.0 * grid * Math.sin(angle)),
                    (int) (y + 1.0 / 2.0 * grid * Math.cos(angle) + 5.0 / 6.0 * grid * Math.sin(angle)));
            if (accelerating) {
                g.drawLine(
                        (int) (x - 5.0 / 6.0 * grid * Math.cos(angle) - 1.0 / 2.0 * grid * Math.sin(angle)),
                        (int) (y + 1.0 / 2.0 * grid * Math.cos(angle) - 5.0 / 6.0 * grid * Math.sin(angle)),
                        (int) (x - 2 * grid * Math.sin(angle)),
                        (int) (y + 2 * grid * Math.cos(angle)));
                g.drawLine(
                        (int) (x - 2 * grid * Math.sin(angle)),
                        (int) (y + 2 * grid * Math.cos(angle)),
                        (int) (x + 5.0 / 6.0 * grid * Math.cos(angle) - 1.0 / 2.0 * grid * Math.sin(angle)),
                        (int) (y + 1.0 / 2.0 * grid * Math.cos(angle) + 5.0 / 6.0 * grid * Math.sin(angle)));
            }
            if (shieldTimer > 0) {
                g.drawArc((int) (x - 2 * grid), (int) (y - 2 * grid),
                        (int) (4 * grid), (int) (4 * grid), 0, 360);
            }
        }
        // Update timers
        if (killedTimer > 0) {
            killedTimer -= deltaTime;
        } else if (killed) {
            lives--;
            killed = false;
        } else if (shieldTimer > 0) {
            shieldTimer -= deltaTime;
            bounds.setCollidable(true);
        }
        if (teleportTimer > 0) {
            teleportTimer -= deltaTime;
        }
        if (piercingShotTimer > 0) {
            piercingShotTimer -= deltaTime;
        }
    }

    public void teleport(double x, double y) {
        this.x = x;
        this.y = y;
        dx = 0;
        dy = 0;
    }

    private class DebrisEntity extends Entity {

        double dAngle;
        double[] coords;
        long activeTimer;

        public DebrisEntity() {
        }

        public void update(Graphics g, long deltaTime) {
            if (activeTimer <= 0) {
                return;
            }
            activeTimer -= deltaTime;
            x += dx * deltaTime;
            y += dy * deltaTime;
            if (x < 0 || x > width || y < 0 || y > height) {
                active = false;
            }

            angle += dAngle * deltaTime;
            if (angle > 2 * Math.PI) {
                angle -= 2 * Math.PI;
            } else if (angle < 0) {
                angle += 2 * Math.PI;
            }

            g.setColor(0xFFFFFF);
            g.drawLine((int) (x + coords[0] * Math.cos(angle) - coords[1] * Math.sin(angle)),
                    (int) (y + coords[1] * Math.cos(angle) + coords[0] * Math.sin(angle)),
                    (int) (x - coords[2] * Math.cos(angle) + coords[3] * Math.sin(angle)),
                    (int) (y - coords[3] * Math.cos(angle) - coords[2] * Math.sin(angle)));
        }

        public void reset() {
            this.x = ShipEntity.this.x;
            this.y = ShipEntity.this.y;
            this.grid = ShipEntity.this.grid;
            this.dx = 0.8 * ShipEntity.this.dx + 0.5 * (ShipEntity.this.dx + 0.01) * (rand.nextDouble() - 0.5);
            this.dy = 0.8 * ShipEntity.this.dy + 0.5 * (ShipEntity.this.dy + 0.01) * (rand.nextDouble() - 0.5);
            this.width = ShipEntity.this.width;
            this.height = ShipEntity.this.height;
            dAngle = (2 * rand.nextInt(2) - 1) * (rand.nextDouble() + 3) / 500;
            coords = new double[4];
            for (int i = 0; i < coords.length; ++i) {
                coords[i] = (0.5 + rand.nextDouble()) * grid;
            }
            for (int i = 0; i < shots.length; ++i) {
                shots[i].setInactive();
            }
            activeTimer = 1500 + rand.nextInt(1000); // 1.5 - 2.5 s
        }
    }
}

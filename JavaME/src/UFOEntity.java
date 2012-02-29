
import java.util.Random;
import javax.microedition.lcdui.Graphics;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author exstac
 */
public class UFOEntity extends Entity {

    public static final double SMALL = 1, BIG = 1.5;
    private int moveTimer;
    private double size;
    private int shotTimer;
    private Random rand = null;
    private UFOShotEntity shot = null;
    private ShipEntity ship = null;

    public UFOEntity(int width, int height, double grid, Random rand, ShipEntity ship) {
        this.width = width;
        this.height = height;
        this.grid = grid;
        this.rand = rand;
        this.ship = ship;
        shotTimer = 1000;
        shot = new UFOShotEntity(width, height, grid);
        active = false;
    }

    public double getSize() {
        return size;
    }

    public void reset(double size) {
        this.size = size;
        bounds = new Entity.BoundingShape(BoundingShape.CIRCLE, 2 * size * grid);
        dx = (2 * rand.nextInt(2) - 1) * (width / grid) / 2500.0;
        dy = dx;
        x = (dx > 0) ? -3 * size * grid : 3 * size * grid + width;
        y = rand.nextInt(height);
        moveTimer = rand.nextInt(500) + 250;
        active = true;
    }

    public void shoot() {
        if (size == UFOEntity.BIG && !shot.isActive()) {
            shot.reset(x, y, rand.nextDouble() * 2 * Math.PI);
        } else if (size == UFOEntity.SMALL && !shot.isActive()) {
            double offset = Math.toRadians(30 * rand.nextDouble() - 15);
            shot.reset(x, y, mMath.atan2(ship.y - y, ship.x - x) + Math.PI / 2 + offset);
        }
    }

    public boolean shotCollision(Entity e) {
        return shot.collides(e);
    }

    public UFOShotEntity getShot() {
        return shot;
    }

    public void update(Graphics g, long deltaTime) {
        if (!isActive()) {
            return;
        }

        x += dx * deltaTime;
        y += dy * deltaTime;

        if ((shotTimer -= deltaTime) > 0) {
            shoot();
            shotTimer = 1000;
        }

        if ((moveTimer -= deltaTime) < 0) {
            dy = -dy;
            moveTimer = rand.nextInt(2000) + 500;
        }

        if ((dx > 0 && x - 3 * size * grid > width) ||
                (dx < 0 && x + 3 * size * grid < 0)) {
            if (rand.nextDouble() > 0.5) {
                reset(size);
            } else {
                setInactive();
            }
        }

        if (y - 3 * size * grid > height) {
            y = -size * grid;
        } else if (y + 2 * size * grid < 0) {
            y = 2 * size * grid + height;
        }

        g.setColor(0xFFFFFF);
        g.drawLine((int) (x - 2 * size * grid), (int) y,
                (int) (x + 2 * size * grid), (int) y);
        g.drawLine((int) (x - 2 * size * grid), (int) y,
                (int) (x - size * grid), (int) (y + size * grid));
        g.drawLine((int) (x - 2 * size * grid), (int) y,
                (int) (x - size * grid), (int) (y - size * grid));
        g.drawLine((int) (x + 2 * size * grid), (int) y,
                (int) (x + size * grid), (int) (y - size * grid));
        g.drawLine((int) (x + 2 * size * grid), (int) y,
                (int) (x + size * grid), (int) (y + size * grid));
        g.drawLine((int) (x + size * grid), (int) (y + size * grid),
                (int) (x - size * grid), (int) (y + size * grid));
        g.drawLine((int) (x + size * grid), (int) (y - size * grid),
                (int) (x - size * grid), (int) (y - size * grid));
        g.drawArc((int) (x - size * grid), (int) (y - 2 * size * grid),
                (int) (2 * size * grid), (int) (2 * size * grid), 0, 180);

        shot.update(g, deltaTime);
    }
}


import javax.microedition.lcdui.Graphics;

/**
 *
 * @author exstac
 */
public class RockEntity extends Entity {

    /** Available rock sizes */
    public static final int BIG = 3, MEDIUM = 2, SMALL = 1;
    /** Coordinates for all corners of the rock (relative to center) */
    private double[] xCoords, yCoords;
    /** */
    private double startTime, spawnTimer, radius;
    /** The size of this rock */
    private int size;
    /** Fake rocks for collision detection */
    private RockMockupEntity fakeRocks[] = null;

    public RockEntity(double x, double y, double dx, double dy, int width, int height, int size, int type, double gridSize, double spawnTimer) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.width = width;
        this.height = height;
        this.size = size;
        this.spawnTimer = spawnTimer;
        startTime = spawnTimer < 0 ? 1000 : spawnTimer;
        switch (size) {
            case RockEntity.BIG:
                this.grid = gridSize;
                break;
            case RockEntity.MEDIUM:
                this.grid = 3.0 * gridSize / 4;
                break;
            case RockEntity.SMALL:
                this.grid = gridSize / 2;
                break;
        }
        this.radius = 3 * grid;
        bounds = new BoundingShape(BoundingShape.CIRCLE, RockEntity.this.radius);
        fakeRocks = new RockMockupEntity[4];
        for (int i = 0; i < 4; ++i) {
            fakeRocks[i] = new RockMockupEntity(i);
        }

        switch (type % 3) {
            case 0: {
                double[] xs = {0 * grid, 1 * grid, 3 * grid, 2 * grid, 4 * grid, 3 * grid, 1 * grid, -1 * grid, -2 * grid, -3 * grid, -2 * grid};
                double[] ys = {-2 * grid, -3 * grid, -1 * grid, 0 * grid, 1 * grid, 3 * grid, 2 * grid, 3 * grid, 1 * grid, 0 * grid, -2 * grid};
                xCoords = xs;
                yCoords = ys;
                break;
            }
            case 1: {
                double[] xs = {-1 * grid, 2 * grid, 3 * grid, 3 * grid, 1 * grid, -0.5 * grid, -0.5 * grid, -2 * grid, -3 * grid, -2 * grid, -3 * grid};
                double[] ys = {-3 * grid, -3 * grid, -1 * grid, 0 * grid, 3 * grid, 3 * grid, 0 * grid, 3 * grid, 0.5 * grid, 0 * grid, -0.5 * grid};
                xCoords = xs;
                yCoords = ys;
                break;
            }
            case 2: {
                double[] xs = {-1.5 * grid, 1 * grid, 3 * grid, 0 * grid, 3 * grid, 1 * grid, 0 * grid, -2 * grid, -3 * grid, -3 * grid, -1 * grid};
                double[] ys = {-3 * grid, -3 * grid, -1.5 * grid, -0.5 * grid, 1 * grid, 3 * grid, 2 * grid, 3 * grid, 0.5 * grid, -1.5 * grid, -1.5 * grid};
                xCoords = xs;
                yCoords = ys;
                break;
            }
        }
    }

    public boolean collides(Entity e) {
        return super.collides(e) ||
                fakeRocks[0].collides(e) ||
                fakeRocks[1].collides(e) ||
                fakeRocks[2].collides(e) ||
                fakeRocks[3].collides(e);
    }

    public void update(Graphics g, long deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
        if (x > width) {
            x -= width;
        } else if (x < 0) {
            x += width;
        }
        if (y > height) {
            y -= height;
        } else if (y < 0) {
            y += height;
        }

        g.setColor(0xFFFFFF);
        if (spawnTimer > 0) {
            spawnTimer -= deltaTime;
        }
        double coeff = (startTime - spawnTimer) / startTime;
        for (int i = 0; i < xCoords.length; ++i) {
            g.drawLine(
                    (int) (x + coeff * xCoords[i]),
                    (int) (y + coeff * yCoords[i]),
                    (int) (x + coeff * xCoords[(i + 1) % xCoords.length]),
                    (int) (y + coeff * yCoords[(i + 1) % yCoords.length]));
            if (x - 4 * grid < 0) {
                g.drawLine(
                        (int) (x + coeff * xCoords[i] + width),
                        (int) (y + coeff * yCoords[i]),
                        (int) (x + coeff * xCoords[(i + 1) % xCoords.length] + width),
                        (int) (y + coeff * yCoords[(i + 1) % yCoords.length]));
            }
            if (y - 4 * grid < 0) {
                g.drawLine(
                        (int) (x + coeff * xCoords[i]),
                        (int) (y + coeff * yCoords[i] + height),
                        (int) (x + coeff * xCoords[(i + 1) % xCoords.length]),
                        (int) (y + coeff * yCoords[(i + 1) % yCoords.length]) + height);
            }
            if (x + 4 * grid > width) {
                g.drawLine(
                        (int) (x + coeff * xCoords[i] - width),
                        (int) (y + coeff * yCoords[i]),
                        (int) (x + coeff * xCoords[(i + 1) % xCoords.length] - width),
                        (int) (y + coeff * yCoords[(i + 1) % yCoords.length]));
            }
            if (y + 4 * grid > height) {
                g.drawLine(
                        (int) (x + coeff * xCoords[i]),
                        (int) (y + coeff * yCoords[i] - height),
                        (int) (x + coeff * xCoords[(i + 1) % xCoords.length]),
                        (int) (y + coeff * yCoords[(i + 1) % yCoords.length] - height));
            }
        }
    }

    public int getSize() {
        return size;
    }

    private class RockMockupEntity extends Entity {

        private int xOffset = 0;
        private int yOffset = 0;

        public RockMockupEntity(int dir) {
            bounds = new BoundingShape(BoundingShape.CIRCLE, RockEntity.this.radius);
            switch (dir) {
                case 0: // left
                    xOffset = -RockEntity.this.width;
                    break;
                case 1: // up
                    yOffset = -RockEntity.this.height;
                    break;
                case 2: // right
                    xOffset = RockEntity.this.width;
                    break;
                case 3: // down
                    yOffset = RockEntity.this.height;
                    break;
            }
        }

        public void update(Graphics g, long deltaTime) {
            // Stubbed out
        }

        public boolean collides(Entity e) {
            x = RockEntity.this.x + xOffset;
            y = RockEntity.this.y + yOffset;
            if (x + RockEntity.this.radius < 0 ||
                    y + RockEntity.this.radius < 0 ||
                    x - RockEntity.this.radius > RockEntity.this.width ||
                    y - RockEntity.this.radius > RockEntity.this.height) {
                return false;
            } else {
                return super.collides(e);
            }
        }
    }
}

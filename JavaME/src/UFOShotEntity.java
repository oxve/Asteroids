
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author exstac
 */
public class UFOShotEntity extends Entity {

    private long aliveTime;

    public UFOShotEntity(int width, int height, double grid) {
        this.width = width;
        this.height = height;
        this.grid = grid;
        bounds = new BoundingShape(BoundingShape.POINT);
        aliveTime = -1;
        active = false;
    }

    public void reset(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.dx = grid / 4 * 0.20 * Math.sin(angle);
        this.dy = grid / 4 * -0.20 * Math.cos(angle);
        aliveTime = 750;
        active = true;
    }

    public void update(Graphics g, long deltaTime) {
        if ((aliveTime -= deltaTime) <= 0) {
            aliveTime = 0;
            active = false;
        }
        x += dx * deltaTime;
        y += dy * deltaTime;
        if (x > width) {
            x = 0;
        } else if (x < 0) {
            x = width;
        }
        if (y > height) {
            y = 0;
        } else if (y < 0) {
            y = height;
        }
        g.setColor(0xFFFFFF);
        g.drawLine((int) (x - grid / 4), (int) (y - grid / 4), (int) (x - grid / 4), (int) (y + grid / 4));
        g.drawLine((int) (x - grid / 4), (int) (y + grid / 4), (int) (x + grid / 4), (int) (y + grid / 4));
        g.drawLine((int) (x + grid / 4), (int) (y + grid / 4), (int) (x + grid / 4), (int) (y - grid / 4));
        g.drawLine((int) (x + grid / 4), (int) (y - grid / 4), (int) (x - grid / 4), (int) (y - grid / 4));
    }
}

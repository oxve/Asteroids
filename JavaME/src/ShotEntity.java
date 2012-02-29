
import javax.microedition.lcdui.Graphics;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author exstac
 */
public class ShotEntity extends Entity {

    int aliveTime;
    boolean piercing = false;

    public ShotEntity(int width, int height, double grid) {
        this.width = width;
        this.height = height;
        this.grid = grid;
        bounds = new BoundingShape(BoundingShape.POINT);
        aliveTime = -1;
    }

    public void reset(double x, double y, double dx, double dy, double angle, boolean piercing) {
        this.x = x;
        this.y = y;
        this.dx = grid / 4 * 0.25 * Math.sin(angle) + dx;
        this.dy = grid / 4 * -0.25 * Math.cos(angle) + dy;
        this.piercing = piercing;
        aliveTime = 750;
    }

    public boolean isActive() {
        return aliveTime >= 0;
    }

    public void setInactive() {
        aliveTime = -1;
    }

    public void update(Graphics g, long deltaTime) {
        if ((aliveTime -= deltaTime) < 0) {
            return;
        }
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
        g.setColor(piercing ? 0xFF0000 : 0xFFFFFF);
        g.drawLine((int) (x - grid / 4), (int) (y - grid / 4), (int) (x - grid / 4), (int) (y + grid / 4));
        g.drawLine((int) (x - grid / 4), (int) (y + grid / 4), (int) (x + grid / 4), (int) (y + grid / 4));
        g.drawLine((int) (x + grid / 4), (int) (y + grid / 4), (int) (x + grid / 4), (int) (y - grid / 4));
        g.drawLine((int) (x + grid / 4), (int) (y - grid / 4), (int) (x - grid / 4), (int) (y - grid / 4));
    }
}

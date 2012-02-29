
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author exstac
 */
public class ParticleEntity extends Entity {

    public ParticleEntity(int width, int height) {
        this.width = width;
        this.height = height;
        aliveTime = -1;
    }

    public void reset(double x, double y, double dx, double dy, int aliveTime) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.aliveTime = aliveTime;
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
        g.setColor(0xFFFFFF);
        g.drawRect((int) x, (int) y, 1, 1);
    }
    private long aliveTime;
}

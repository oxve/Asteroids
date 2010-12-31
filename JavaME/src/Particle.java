
import javax.microedition.lcdui.Graphics;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author exstac
 */
public class Particle {

    public Particle(int width, int height) {
        this.width = width;
        this.height = height;
        aliveTime = -1;
    }

    public void reset(double x, double y, double xVel, double yVel, int aliveTime) {
        this.x = x;
        this.y = y;
        this.xVel = xVel;
        this.yVel = yVel;
        this.aliveTime = aliveTime;
    }

    public boolean isActive() {
        return aliveTime >= 0;
    }

    public void draw(long deltaTime, Graphics g) {
        if ((aliveTime -= deltaTime) < 0) {
            return;
        }
        x += xVel * deltaTime;
        y += yVel * deltaTime;
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
    private double x, y, xVel, yVel;
    private int width, height;
    private long aliveTime;
}

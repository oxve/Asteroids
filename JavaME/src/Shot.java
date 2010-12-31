
import javax.microedition.lcdui.Graphics;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author exstac
 */
public class Shot {

    public Shot(int width, int height, int grid) {
        this.width = width;
        this.height = height;
        this.grid = grid;
        aliveTime = -1;
    }

    public void reset(double x, double y, double xVel, double yVel, double angle) {
        this.x = x;
        this.y = y;
        this.xVel = grid / 4 * 0.25 * Math.sin(angle) + xVel;
        this.yVel = grid / 4 * -0.25 * Math.cos(angle) + yVel;
        aliveTime = 750;
    }

    public boolean isActive() {
        return aliveTime >= 0;
    }

    public void setInactive() {
        aliveTime = -1;
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
        int grid = 1;
        g.drawLine((int) x - grid, (int) y - grid, (int) x - grid, (int) y + grid);
        g.drawLine((int) x - grid, (int) y + grid, (int) x + grid, (int) y + grid);
        g.drawLine((int) x + grid, (int) y + grid, (int) x + grid, (int) y - grid);
        g.drawLine((int) x + grid, (int) y - grid, (int) x - grid, (int) y - grid);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    double x, y, xVel, yVel;
    int width, height, aliveTime, grid;
}

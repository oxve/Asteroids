
import javax.microedition.lcdui.Graphics;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author exstac
 */
public class Rock {

    private Rock() {
    }

    public Rock(double x, double y, double xVel, double yVel, int width, int height, int size, int type, int gridSize) {
        this.x = x;
        this.y = y;
        this.xVel = xVel;
        this.yVel = yVel;
        this.width = width;
        this.height = height;
        this.size = size;
        switch (size) {
            case Rock.BIG:
                this.grid = gridSize;
                break;
            case Rock.MEDIUM:
                this.grid = 3 * gridSize / 4;
                break;
            case Rock.SMALL:
                this.grid = gridSize / 2;
                break;
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

    public boolean collidesWith(double x, double y) {
        return 3 * grid > Math.sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y)) ||
                3 * grid > Math.sqrt((this.x + width - x) * (this.x + width - x) + (this.y - y) * (this.y - y)) ||
                3 * grid > Math.sqrt((this.x - width - x) * (this.x - width - x) + (this.y - y) * (this.y - y)) ||
                3 * grid > Math.sqrt((this.x - x) * (this.x - x) + (this.y + height - y) * (this.y + height - y)) ||
                3 * grid > Math.sqrt((this.x - x) * (this.x - x) + (this.y - height - y) * (this.y - height - y));
    }

    public void draw(long deltaTime, Graphics g) {
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
        for (int i = 0; i < xCoords.length; ++i) {
            g.drawLine(
                    (int) (x + xCoords[i]),
                    (int) (y + yCoords[i]),
                    (int) (x + xCoords[(i + 1) % xCoords.length]),
                    (int) (y + yCoords[(i + 1) % yCoords.length]));
            if (x - 4 * grid < 0) {
                g.drawLine(
                        (int) (x + xCoords[i] + width),
                        (int) (y + yCoords[i]),
                        (int) (x + xCoords[(i + 1) % xCoords.length] + width),
                        (int) (y + yCoords[(i + 1) % yCoords.length]));
            }
            if (y - 4 * grid < 0) {
                g.drawLine(
                        (int) (x + xCoords[i]),
                        (int) (y + yCoords[i] + height),
                        (int) (x + xCoords[(i + 1) % xCoords.length]),
                        (int) (y + yCoords[(i + 1) % yCoords.length]) + height);
            }
            if (x + 4 * grid > width) {
                g.drawLine(
                        (int) (x + xCoords[i] - width),
                        (int) (y + yCoords[i]),
                        (int) (x + xCoords[(i + 1) % xCoords.length] - width),
                        (int) (y + yCoords[(i + 1) % yCoords.length]));
            }
            if (y + 4 * grid > height) {
                g.drawLine(
                        (int) (x + xCoords[i]),
                        (int) (y + yCoords[i] - height),
                        (int) (x + xCoords[(i + 1) % xCoords.length]),
                        (int) (y + yCoords[(i + 1) % yCoords.length] - height));
            }
        }

    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getSize() {
        return size;
    }
    private double[] xCoords, yCoords;
    private double x, y, xVel, yVel;
    private int width, height, grid, size;
    public static final int BIG = 3, MEDIUM = 2, SMALL = 1;
}

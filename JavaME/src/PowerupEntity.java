
import java.util.Random;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author exstac
 */
public class PowerupEntity extends Entity {

    /** The different powerup types */
    public static final int SHIELD = 0;
    public static final int EXTRA_LIFE = 1;
    public static final int EXTRA_ROCKS = 2;
    public static final int PIERCING_SHOTS = 3;
    /** Other local variables */
    private final char[] typeSymbol = {'S', 'X', 'R', 'P'};
    public int type;
    private Random rand;
    private final Font mediumBoldFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_MEDIUM);

    public PowerupEntity(int width, int height, double grid, Random rand) {
        this.width = width;
        this.height = height;
        this.grid = grid;
        this.rand = rand;
        bounds = new BoundingShape(BoundingShape.CIRCLE, mediumBoldFont.getHeight() / 4);
        reset(0, 0);
        active = false;
    }

    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        active = true;
        int num = rand.nextInt(100);
        if (num < 5) { // 5% chance for schield
            type = SHIELD;
        } else if (num < 7) { // 2% chance for extra life
            type = EXTRA_LIFE;
        } else if (num < 12) {
            type = EXTRA_ROCKS; // 5% chance for extra rocks
        } else if (num < 17) {
            type = PIERCING_SHOTS; // 5% chance for piercing shots
        } else {
            active = false;
        }
    }

    public void update(Graphics g, long deltaTime) {
        if (!active) {
            return;
        }
        //bounds.draw(g);
        g.setColor(0x00FF00);
        g.setFont(mediumBoldFont);
        g.drawChar(typeSymbol[type], (int) x, (int) (y + mediumBoldFont.getHeight() / 4),
                Graphics.HCENTER | Graphics.BASELINE);
    }
}

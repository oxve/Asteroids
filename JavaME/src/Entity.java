
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author exstac
 */
public abstract class Entity {

    /** The current x location of this entity */
    protected double x;
    /** The current y location of this entity */
    protected double y;
    /** The current speed of this entity horizontally (pixels/ms) */
    protected double dx;
    /** The current speed of this entity vertically (pixels/ms) */
    protected double dy;
    /** The current angle of this entity's rotation */
    protected double angle;
    /** The width of the GameCanvas */
    protected int width;
    /** The height of the GameCanvas */
    protected int height;
    /** The size of a "grid" */
    protected double grid;
    /** Determines if this entity is active or not */
    protected boolean active;
    /** The bounds of this entity */
    protected BoundingShape bounds = null;

    public boolean collides(Entity e) {
        return bounds.collides(e.bounds);
    }

    public boolean isActive() {
        return active;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public abstract void update(Graphics g, long deltaTime);

    public void setInactive() {
        active = false;
    }

    protected class BoundingShape {

        /** The available bounding shapes */
        public static final int CIRCLE = 1, BOX = 2, POINT = 3;
        /** The type of this bounding shape */
        private int type;
        /** If the BoundingShape is a circle, this is it's radius */
        private double r;
        /** If the BoundingShape is a box, this is it's width and height */
        private int w, h;
        /** Determines wether the object is collidable or not */
        private boolean collidable = true;

        /**
         * The constructor for point shapes
         * @param type      must be BoundingShape.Point
         */
        public BoundingShape(int type) {
            this.type = type;
        }

        /**
         * The constructor for circle shapes
         * @param type      must be BoundingShape.CIRCLE
         * @param radius    the radius of the circle
         */
        public BoundingShape(int type, double radius) {
            this.type = type;
            this.r = radius;
        }

        /**
         * The constructor for box shapes
         * @param type      must be BoundingShape.BOX
         * @param width     the width of the box
         * @param height    the heigth of the box
         */
        public BoundingShape(int type, int width, int height) {
            this.type = type;
            this.w = width;
            this.h = height;
        }

        public void draw(Graphics g) {
            switch (type) {
                case BOX:
                    g.setColor(0xFF0000);
                    g.drawRect((int) (x - w / 2.0), (int) (y - h / 2.0), w, h);
                    break;
                case CIRCLE:
                    g.setColor(0xFF0000);
                    g.drawArc((int) (x - r), (int) (y - r), (int) (2*r), (int) (2*r), 0, 360);
                    break;
                default:
                    break;
            }
        }

        private double getX() {
            return x;
        }

        private double getY() {
            return y;
        }

        public void setCollidable(boolean collidable) {
            this.collidable = collidable;
        }

        /**
         * Controls if this overlaps that
         * @param   that    the bounds of the other object
         * @return  true    if object overlap
         *          false   otherwise
         */
        public boolean collides(BoundingShape that) {
            if (!this.collidable || !that.collidable) {
                return false;
            }
            if (this.type == BoundingShape.POINT && that.type == BoundingShape.CIRCLE ||
                    this.type == BoundingShape.CIRCLE && that.type == BoundingShape.POINT) {
                BoundingShape p = this.type == BoundingShape.POINT ? this : that;
                BoundingShape c = this.type == BoundingShape.CIRCLE ? this : that;
                return ((c.getX() - p.getX()) * (c.getX() - p.getX()) +
                        (c.getY() - p.getY()) * (c.getY() - p.getY())) <
                        (c.r * c.r);
            } else if (this.type == BoundingShape.CIRCLE && that.type == BoundingShape.CIRCLE) {
                return ((this.getX() - that.getX()) * (this.getX() - that.getX()) +
                        (this.getY() - that.getY()) * (this.getY() - that.getY())) <
                        (this.r + that.r) * (this.r + that.r);
            } else if (this.type == BoundingShape.BOX && that.type == BoundingShape.CIRCLE ||
                    this.type == BoundingShape.CIRCLE && that.type == BoundingShape.BOX) {
                BoundingShape b = this.type == BoundingShape.BOX ? this : that;
                BoundingShape c = this.type == BoundingShape.CIRCLE ? this : that;
                double testX = Math.min(Math.max(c.getX(), b.getX()), b.getX() + b.w);
                double testY = Math.min(Math.max(c.getY(), b.getY()), b.getY() + b.h);
                return ((c.getX() - testX) * (c.getX() - testX) +
                        (c.getY() - testY) * (c.getY() - testY)) < c.r * c.r;
            }
            return false;
        }
    }
}

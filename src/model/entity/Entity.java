package model.entity;

import java.awt.*;

public abstract class Entity {

    // Fields:

    protected static int width;          // Dimensions of the graphics area.
    protected static int height;

    protected Polygon basePolygon;             // Base sprite shape, centered at the origin (0,0).
    protected boolean active;            // Active flag.
    protected double  angle;             // Current angle of rotation.
    protected double  deltaAngle;        // Amount to change the rotation angle.
    protected double  x;
    protected double y;              // Current position on screen.
    protected double  deltaX;
    protected double deltaY;    // Amount to change the screen position.
    protected Polygon transformedPolygon;            // Final location and shape of sprite after
    // applying rotation and translation to get screen
    // position. Used for drawing on the screen and in
    // detecting collisions.

    // Constructors:


    public Entity() {
        this.basePolygon = new Polygon();
        this.active = false;
        this.angle = 0.0;
        this.deltaAngle = 0.0;
        this.x = 0.0;
        this.y = 0.0;
        this.deltaX = 0.0;
        this.deltaY = 0.0;
        this.transformedPolygon = new Polygon();

        initBasePolygon();
        transform();
    }

    // Methods:

    public abstract void initBasePolygon();

    public boolean transform() {
        boolean wrapped;

        // Update the rotation and position of the sprite based on the delta
        // values. If the sprite moves off the edge of the screen, it is wrapped
        // around to the other side and TRUE is returnd.

        this.angle += this.deltaAngle;
        if (this.angle < 0)
            this.angle += 2 * Math.PI;
        if (this.angle > 2 * Math.PI)
            this.angle -= 2 * Math.PI;
        wrapped = false;
        this.x += this.deltaX;
        if (this.x < -width / 2) {
            this.x += width;
            wrapped = true;
        }
        if (this.x > width / 2) {
            this.x -= width;
            wrapped = true;
        }
        this.y -= this.deltaY;
        if (this.y < -height / 2) {
            this.y += height;
            wrapped = true;
        }
        if (this.y > height / 2) {
            this.y -= height;
            wrapped = true;
        }

        this.transformedPolygon = new Polygon();
        for (int i = 0; i < this.basePolygon.npoints; i++)
            this.transformedPolygon.addPoint((int) Math.round(this.basePolygon.xpoints[i] * Math.cos(this.angle) + this.basePolygon.ypoints[i] * Math.sin(this.angle)) + (int) Math.round(this.x) + width / 2,
                    (int) Math.round(this.basePolygon.ypoints[i] * Math.cos(this.angle) - this.basePolygon.xpoints[i] * Math.sin(this.angle)) + (int) Math.round(this.y) + height / 2);

        return wrapped;
    }

//    public void render() {
//
//        int i;
//
//        // Render the sprite's shape and location by rotating it's base shape and
//        // moving it to it's proper screen position.
//        this.transformedPolygon = new Polygon();
//        for (i = 0; i < this.basePolygon.npoints; i++)
//            this.transformedPolygon.addPoint((int) Math.round(this.basePolygon.xpoints[i] * Math.cos(this.angle) + this.basePolygon.ypoints[i] * Math.sin(this.angle)) + (int) Math.round(this.x) + width / 2,
//                    (int) Math.round(this.basePolygon.ypoints[i] * Math.cos(this.angle) - this.basePolygon.xpoints[i] * Math.sin(this.angle)) + (int) Math.round(this.y) + height / 2);
//
//    }

    public boolean isColliding(Entity s) {

        int i;

        // Determine if one sprite overlaps with another, i.e., if any vertice
        // of one sprite lands inside the other.

        for (i = 0; i < s.transformedPolygon.npoints; i++)
            if (this.transformedPolygon.contains(s.transformedPolygon.xpoints[i], s.transformedPolygon.ypoints[i]))
                return true;
        for (i = 0; i < this.transformedPolygon.npoints; i++)
            if (s.transformedPolygon.contains(this.transformedPolygon.xpoints[i], this.transformedPolygon.ypoints[i]))
                return true;
        return false;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public static int getWidth() {
        return width;
    }

    public static void setWidth(int width) {
        Entity.width = width;
    }

    public static int getHeight() {
        return height;
    }

    public static void setHeight(int height) {
        Entity.height = height;
    }

    public Polygon getBasePolygon() {
        return basePolygon;
    }

    public void setBasePolygon(Polygon basePolygon) {
        this.basePolygon = basePolygon;
    }

    public boolean isActive() {
        return active;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }


    public void setDeltaAngle(double deltaAngle) {
        this.deltaAngle = deltaAngle;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(double deltaX) {
        this.deltaX = deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }

    public Polygon getTransformedPolygon() {
        return transformedPolygon;
    }
}

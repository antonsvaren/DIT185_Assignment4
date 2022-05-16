package model.entity;

public class Missile extends Entity {
    /**
     * The remaining duration of the missile.
     */
    private int missileCounter;

    @Override
    public void initBasePolygon() {
        basePolygon.addPoint(0, -4);
        basePolygon.addPoint(1, -3);
        basePolygon.addPoint(1, 3);
        basePolygon.addPoint(2, 4);
        basePolygon.addPoint(-2, 4);
        basePolygon.addPoint(-1, 3);
        basePolygon.addPoint(-1, -3);
    }

    /**
     * Resets the missile at the given coordinates.
     * @param startX The new x-coordinate of the missile.
     * @param startY The new y-coordinate of the missile.
     */
    public void init(double startX, double startY) {
        active = true;
        angle = 0.0;
        deltaAngle = 0.0;
        x = startX;
        y = startY;
        deltaX = 0.0;
        deltaY = 0.0;
        transform();
    }

    /**
     * Returns the remaining duration of the missile.
     */
    public int getMissileCounter() {
        return missileCounter;
    }

    /**
     * Updates the remaining duration of the missile.
     * @param missileCounter The updated duration.
     */
    public void setMissileCounter(int missileCounter) {
        this.missileCounter = missileCounter;
    }

    /**
     * Reduces the remaining duration of the missile by 1.
     * @return The updated value.
     */
    public int decrementCounter() {
        return this.missileCounter--;
    }

    /**
     * Makes missile inactive and sets remaining duration to 0.
     */
    public void stop() {
        active = false;
        setMissileCounter(0);
    }
}

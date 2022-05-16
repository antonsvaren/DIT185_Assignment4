package model.entity;

public class Missile extends Entity {
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

    public int getMissileCounter() {
        return missileCounter;
    }

    public void setMissileCounter(int missileCounter) {
        this.missileCounter = missileCounter;
    }

    public int decrementCounter() {
        return this.missileCounter--;
    }

    public void stop() {
        active = false;
        setMissileCounter(0);
    }
}

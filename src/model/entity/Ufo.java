package model.entity;

public class Ufo extends Entity {
    private int ufoPassesLeft;    // Counter for number of flying saucer passes.
    private int ufoCounter;       // Timer counter used to track each flying saucer pass.

    private double speedConstant;

    public Ufo(int ufoPassesLeft, int ufoCounter, double speedConstant) {
        super();
        this.ufoPassesLeft = ufoPassesLeft;
        this.ufoCounter = ufoCounter;
        this.speedConstant = speedConstant;
    }

    @Override
    public void initBasePolygon() {
        basePolygon.addPoint(-15, 0);
        basePolygon.addPoint(-10, -5);
        basePolygon.addPoint(-5, -5);
        basePolygon.addPoint(-5, -8);
        basePolygon.addPoint(5, -8);
        basePolygon.addPoint(5, -5);
        basePolygon.addPoint(10, -5);
        basePolygon.addPoint(15, 0);
        basePolygon.addPoint(10, 5);
        basePolygon.addPoint(-10, 5);
    }

    public void init() {

        double angle, speed;

        // Randomly set flying saucer at left or right edge of the screen.

        active = true;
        x = -width / 2;
        y = Math.random() * 2 * height - height;
        angle = Math.random() * Math.PI / 4 - Math.PI / 2;
        speed = speedConstant / 2 + Math.random() * (speedConstant / 2);
        deltaX = speed * -Math.sin(angle);
        deltaY = speed *  Math.cos(angle);
        if (Math.random() < 0.5) {
            x = width / 2;
            deltaX = -deltaX;
        }
        ufoCounter = (int) Math.abs(width / deltaX);
    }

    public int getUfoPassesLeft() {
        return ufoPassesLeft;
    }

    public void setUfoPassesLeft(int ufoPassesLeft) {
        this.ufoPassesLeft = ufoPassesLeft;
    }

    public int getUfoCounter() {
        return ufoCounter;
    }

    public void setUfoCounter(int ufoCounter) {
        this.ufoCounter = ufoCounter;
    }


    public double getSpeedConstant() {
        return speedConstant;
    }

    public void setSpeedConstant(double speedConstant) {
        this.speedConstant = speedConstant;
    }

    public void stop() {
        active = false;
        ufoCounter = 0;
        ufoPassesLeft = 0;
    }

    public int decrementCounter() {
        ufoCounter -= 1;
        return ufoCounter;
    }

    public int decrementPasses() {
        ufoPassesLeft -= 1;
        return ufoPassesLeft;
    }
}

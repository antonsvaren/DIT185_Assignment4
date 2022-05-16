package model.entity;

public class Ufo extends Entity {

    /**
     * Counter for number of flying saucer passes.
     */
    private int ufoPassesLeft;
    /**
     * Timer counter used to track each flying saucer pass.
     */
    private int ufoCounter;

    /**
     * Constant affecting the speed of the ufo. Will be multiplied
     * with a random value to get the true speed of the ship.
     */
    private final double speedConstant;

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

    /**
     * Resets the ufo.
     */
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

    public void setUfoPassesLeft(int ufoPassesLeft) {
        this.ufoPassesLeft = ufoPassesLeft;
    }

    /**
     * Makes ufo inactive.
     */
    public void stop() {
        active = false;
        ufoCounter = 0;
        ufoPassesLeft = 0;
    }

    /**
     * Reduces the remaining duration of the ufo by 1.
     * @return The updated duration.
     */
    public int decrementCounter() {
        ufoCounter -= 1;
        return ufoCounter;
    }

    public int decrementPasses() {
        ufoPassesLeft -= 1;
        return ufoPassesLeft;
    }
}

package model.entity;

import java.awt.*;

public class Asteroid extends Entity{
    private static final double SPEED_DELTA = 0.5;
    private static double speed;
    private final int minRockSides;
    private final int maxRockSides;
    private final int minRockSize;
    private final int maxRockSize;
    private final double maxRockSpin;
    private boolean isSmall;
    public static double getSpeed() {
        return speed;
    }

    public Asteroid(int minRockSides,
                    int maxRockSides,
                    int minRockSize,
                    int maxRockSize,
                    double maxRockSpin) {
        super();
        this.minRockSides = minRockSides;
        this.maxRockSides = maxRockSides;
        this.minRockSize = minRockSize;
        this.maxRockSize = maxRockSize;
        this.maxRockSpin = maxRockSpin;
    }

    public static void incrementSpeed() {
        setSpeed(speed + SPEED_DELTA);
    }

    public static void setSpeed(double speed) {
        Asteroid.speed = speed;
    }

    @Override
    public void initBasePolygon() {
        basePolygon = new Polygon();
        int s = getMinRockSides() + (int) (Math.random() * (getMaxRockSides() - getMinRockSides()));
        for (int i = 0; i < s; i ++) {
            double theta = 2 * Math.PI / s * i;
            double r = getMinRockSize() + (int) (Math.random() * (getMaxRockSize() - getMinRockSize()));
            int x = (int) -Math.round(r * Math.sin(theta));
            int y = (int)  Math.round(r * Math.cos(theta));
            basePolygon.addPoint(x, y);
        }
        transform();
    }

    public void initSmallAsteroid(double newX, double newY) {
        basePolygon = new Polygon();
        int s = getMinRockSides() + (int) (Math.random() * (getMaxRockSides() - getMinRockSides()));
        for (int i = 0; i < s; i++) {
            double theta = 2 * Math.PI / s * i;
            double r = (getMinRockSize() + (int) (Math.random() * (getMaxRockSize() - getMinRockSize()))) / 2;
            int x = (int) -Math.round(r * Math.sin(theta));
            int y = (int)  Math.round(r * Math.cos(theta));
            basePolygon.addPoint(x, y);
        }
        active = true;
        angle = 0.0;
        deltaAngle = Math.random() * 2 * getMaxRockSpin() - getMaxRockSpin();
        x = newX;
        y = newY;
        deltaX = Math.random() * 2 * speed - speed;
        deltaY = Math.random() * 2 * speed - speed;
        transform();
        setIsSmall(true);
    }

    public void init() {

        // Create a jagged shape for the asteroid and give it a random rotation.

        initBasePolygon();
        active = true;
        angle = 0.0;
        deltaAngle = Math.random() * 2 * getMaxRockSpin() - getMaxRockSpin();

        // Place the asteroid at one edge of the screen.

        if (Math.random() < 0.5) {
            x = -width / 2;
            if (Math.random() < 0.5)
                x = width / 2;
            y = Math.random() * height;
        }
        else {
            x = Math.random() * width;
            y = -height / 2;
            if (Math.random() < 0.5)
                y = height / 2;
        }

        // Set a random motion for the asteroid.

        deltaX = Math.random() * speed;
        if (Math.random() < 0.5)
            deltaX = -deltaX;
        deltaY = Math.random() * speed;
        if (Math.random() < 0.5)
            deltaY = -deltaY;

        setSmall(false);
        transform();
    }

    public void setIsSmall(boolean isSmall) {
        this.setSmall(isSmall);
    }

    public boolean getIsSmall() {
        return isSmall();
    }

    public int getMinRockSides() {
        return minRockSides;
    }

    public int getMaxRockSides() {
        return maxRockSides;
    }

    public int getMinRockSize() {
        return minRockSize;
    }

    public int getMaxRockSize() {
        return maxRockSize;
    }

    public double getMaxRockSpin() {
        return maxRockSpin;
    }

    public boolean isSmall() {
        return isSmall;
    }

    public void setSmall(boolean small) {
        isSmall = small;
    }
}

package model.entity;

import java.awt.*;

public class Explosion extends Entity{

    /**
     * The time remaining of the explosion.
     */
    private int explosionCounter;
    @Override
    public void initBasePolygon() {}

    /**
     * Sets the remaining duration of the explosion.
     */
    public void setCounter(int explosionCounter) {
        this.explosionCounter = explosionCounter;
    }

    /**
     * Reduce the remaining duration of the explosion by 1.
     * @return The duration after decrementing.
     */
    public int decrementCounter() {
        return this.explosionCounter--;
    }

    /**
     * Returns the remaining time of the duration.
     */
    public int getCounter() {
        return explosionCounter;
    }
}

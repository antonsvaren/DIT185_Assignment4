package model.entity;

import java.awt.*;

public class Explosion extends Entity{
    private int explosionCounter;

    @Override
    public void initBasePolygon() {

    }

    public void init() {
        basePolygon = new Polygon();
        active = false;
        explosionCounter = 0;
    }

    public void setCounter(int explosionCounter) {
        this.explosionCounter = explosionCounter;
    }

    public int decrementCounter() {
        return this.explosionCounter--;
    }

    public int getCounter() {
        return explosionCounter;
    }
}

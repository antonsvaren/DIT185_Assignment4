package model.entity;

public class Ship extends Entity{

    // Ship's rotation and acceleration rates and maximum speed.

    private final double angleStep;
    private final double speedStep;

    private final double maxSpeed;

    private final int shipExplosionMaxCount;

    /**
     * Timer counter for hyperspace.
     */
    private final int maxHyperCount;
    private int hyperCounter;

    private final Entity fwdThruster;

    private final Entity revThruster;

    private int shipExplosionCounter;

    private int maxShipsLeft;
    private int shipsLeft;



    public Ship(double angleStep,
                double speedStep,
                double maxSpeed,
                int maxHyperCount,
                int shipExplosionMaxCount,
                int maxShipsLeft,
                Entity fwdThruster,
                Entity revThruster) {
        super();
        this.angleStep = angleStep;
        this.speedStep = speedStep;
        this.maxSpeed = maxSpeed;
        this.fwdThruster = fwdThruster;
        this.revThruster = revThruster;
        this.shipExplosionMaxCount = shipExplosionMaxCount;
        this.maxShipsLeft = maxShipsLeft;
        this.shipsLeft = maxShipsLeft;
        this.maxHyperCount = maxHyperCount;

        shipExplosionCounter = 0;
        hyperCounter = 0;
    }

    @Override
    public void initBasePolygon() {
        basePolygon.addPoint(0, -10);
        basePolygon.addPoint(7, 10);
        basePolygon.addPoint(-7, 10);
    }

    /**
     * Reset ship position and angle.
     */
    public void init() {
        active = true;
        angle = 0.0;
        deltaAngle = 0.0;
        x = 0.0;
        y = 0.0;
        deltaX = 0.0;
        deltaY = 0.0;
        updateThruster(fwdThruster);
        updateThruster(revThruster);
    }

    public void updateShip(boolean left, boolean right, boolean up, boolean down) {

        double dx, dy, speed;

        // Rotate the ship if left or right cursor key is down.

        if (left) {
            angle += angleStep;
            if (angle > 2 * Math.PI)
                angle -= 2 * Math.PI;
        }
        if (right) {
            angle -= angleStep;
            if (angle < 0)
                angle += 2 * Math.PI;
        }

        // Fire thrusters if up or down cursor key is down.

        dx = speedStep * -Math.sin(angle);
        dy = speedStep *  Math.cos(angle);
        if (up) {
            deltaX += dx;
            deltaY += dy;
        }
        if (down) {
            deltaX -= dx;
            deltaY -= dy;
        }

        // Don't let ship go past the speed limit.

        if (up || down) {
            speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (speed > maxSpeed) {
                dx = maxSpeed * -Math.sin(angle);
                dy = maxSpeed *  Math.cos(angle);
                if (up)
                    deltaX = dx;
                else
                    deltaX = -dx;
                if (up)
                    deltaY = dy;
                else
                    deltaY = -dy;
            }
        }

//        transform();
    }

    @Override
    public boolean transform() {
        if (hyperCounter > 0) hyperCounter--;

        boolean wrapped = super.transform();
        updateThruster(fwdThruster);
        updateThruster(revThruster);
        return wrapped;
    }

    private void updateThruster(Entity thruster) {
        if (thruster == null) return;
        thruster.x = x;
        thruster.y = y;
        thruster.angle = angle;
        thruster.transform();
    }

    public void setShipExplosionCounter(int shipExplosionCounter) {
        this.shipExplosionCounter = shipExplosionCounter;
    }

    public void setHyperCounter(int hyperCounter) {
        this.hyperCounter = hyperCounter;
    }

    public void resetShipsLeft() {
        this.shipsLeft = maxShipsLeft;
    }
    public Entity getFwdThruster() {
        return fwdThruster;
    }

    public Entity getRevThruster() {
        return revThruster;
    }

    public int getHyperCounter() {
        return hyperCounter;
    }

    public int decrementExplosionCounter() {
        shipExplosionCounter -= 1;
        return shipExplosionCounter;
    }

    public int getShipsLeft() {
        return shipsLeft;
    }

    public void incrementShipsLeft() {
        shipsLeft++;
    }

    @Override
    public String toString() {
        return "Ship{" +
                "angleStep=" + angleStep +
                ", speedStep=" + speedStep +
                ", maxSpeed=" + maxSpeed +
                ", shipCounterMaxDuration=" + shipExplosionMaxCount +
                ", hyperCounter=" + hyperCounter +
                ", fwdThruster=" + fwdThruster +
                ", revThruster=" + revThruster +
                ", shipCounter=" + shipExplosionCounter +
                ", shipsLeft=" + shipsLeft +
                '}';
    }

    public void handleCollision() {
        shipsLeft--;
        shipExplosionCounter = shipExplosionMaxCount;
        hyperCounter = maxHyperCount;
        active = false;
    }
}

package model.entity;

public class Ship extends Entity{

    /**
     * The max amount the ship can rotate per update.
     */
    private final double angleStep;

    /**
     * The acceleration of the ship when thrusters are on.
     */
    private final double speedStep;

    /**
     * The max speed of the ship.
     */
    private final double maxSpeed;

    /**
     * The duration of the explosion when ship explodes.
     */
    private final int shipExplosionDuration;

    /**
     * The amount of time the ship spends in hyper space.
     */
    private final int hyperSpaceDuration;
    /**
     * The remaining amount of time the ship will spend in hyper space.
     */
    private int hyperCounter;

    /**
     * The forward thruster of the ship.
     */
    private final Entity fwdThruster;

    /**
     * The revers thruster of the ship.
     */
    private final Entity revThruster;

    /**
     * The remaining amount of time the ship explosion will remain.
     */
    private int shipExplosionCounter;

    /**
     * The amount of ships left when the game starts.
     */
    private final int initialShipsLeft;

    /**
     * The current number of ships remaining.
     */
    private int shipsLeft;



    public Ship(double angleStep,
                double speedStep,
                double maxSpeed,
                int hyperSpaceDuration,
                int shipExplosionDuration,
                int initialShipsLeft,
                Entity fwdThruster,
                Entity revThruster) {
        super();
        this.angleStep = angleStep;
        this.speedStep = speedStep;
        this.maxSpeed = maxSpeed;
        this.fwdThruster = fwdThruster;
        this.revThruster = revThruster;
        this.shipExplosionDuration = shipExplosionDuration;
        this.initialShipsLeft = initialShipsLeft;
        this.shipsLeft = initialShipsLeft;
        this.hyperSpaceDuration = hyperSpaceDuration;

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
     * Reset ship.
     */
    public void init() {
        active = true;
        angle = 0.0;
        deltaAngle = 0.0;
        x = 0.0;
        y = 0.0;
        deltaX = 0.0;
        deltaY = 0.0;
        updateThruster(getFwdThruster());
        updateThruster(getRevThruster());
    }

    /**
     * Update the ship state with the player input.
     * @param left Indicates if the player has pressed left.
     * @param right Indicates if the player has pressed right.
     * @param up Indicates if the player has pressed up.
     * @param down Indicates if the player has pressed down.
     */
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
            if (speed > getMaxSpeed()) {
                dx = getMaxSpeed() * -Math.sin(angle);
                dy = getMaxSpeed() *  Math.cos(angle);
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
    }

    @Override
    public boolean transform() {
        if (getHyperCounter() > 0) setHyperCounter(getHyperCounter() - 1);

        boolean wrapped = super.transform();
        updateThruster(getFwdThruster());
        updateThruster(getRevThruster());
        return wrapped;
    }

    /**
     * Updates the thruster to follow the ships position and angle.
     * @param thruster The thruster to update.
     */
    private void updateThruster(Entity thruster) {
        if (thruster == null) return;
        thruster.x = x;
        thruster.y = y;
        thruster.angle = angle;
        thruster.transform();
    }

    /**
     * Update the amount of time remaining in hyper space.
     * @param hyperCounter The remaining time.
     */
    public void setHyperCounter(int hyperCounter) {
        this.hyperCounter = hyperCounter;
    }

    /**
     * Sets the number of ships left to the initial value.
     */
    public void resetShipsLeft() {
        shipsLeft = initialShipsLeft;
    }
    public Entity getFwdThruster() {
        return fwdThruster;
    }

    public Entity getRevThruster() {
        return revThruster;
    }

    /**
     * Returns the amount of time remaining in hyper space.
     * @return The remaining amount of time in hyperspace.
     */
    public int getHyperCounter() {
        return hyperCounter;
    }

    /**
     * Reduces the duration of the ship explosion by 1.
     * @return THe update duration of the ship explosion.
     */
    public int decrementExplosionCounter() {
        return shipExplosionCounter--;
    }

    public int getShipsLeft() {
        return shipsLeft;
    }

    /**
     * Increments the number of ships left by 1.
     */
    public int incrementShipsLeft() {
        return shipsLeft++;
    }

    /**
     * Update the state of the ship in response to a collision.
     */
    public void handleCollision() {
        shipsLeft -= 1;
        shipExplosionCounter = shipExplosionDuration;
        hyperCounter = hyperSpaceDuration;
        active = false;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }
}

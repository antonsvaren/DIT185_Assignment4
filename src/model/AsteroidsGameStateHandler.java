package model;

import model.entity.*;

import java.awt.*;

public class AsteroidsGameStateHandler implements AsteroidsGameStateProvider {

    /**
     * Milliseconds between screen and the resulting frame rate.
     */
    static final int DELAY = 20;

    static final int FPS   = Math.round(1000 / DELAY);

    static final int MAX_NUMBER_OF_PHOTONS =  8;
    static final int MAX_NUMBER_OF_ASTEROIDS =  8;
    static final int MAX_AMOUNT_OF_SCRAP = 40;
    static final int SCRAP_COUNT  = 2 * FPS;
    static final int HYPER_COUNT  = 3 * FPS;  // calculated using number of
    static final int MISSILE_COUNT = 4 * FPS;  // seconds x frames per second.
    static final int STORM_PAUSE  = 3 * FPS; // Time between rounds

    static final int    MIN_ROCK_SIDES =   6; // Ranges for asteroid shape, size
    static final int    MAX_ROCK_SIDES =  16; // speed and rotation.
    static final int    MIN_ROCK_SIZE  =  20;
    static final int    MAX_ROCK_SIZE  =  40;
    static final double MIN_ROCK_SPEED =  40.0 / FPS;
    static final double MAX_ROCK_SPEED = 240.0 / FPS;
    static final double MAX_ROCK_SPIN  = Math.PI / FPS;

    static final int MAX_SHIPS = 3;     // Starting number of ships for
    static final int UFO_PASSES = 3;    // Number of passes for flying
    static final double SHIP_ANGLE_STEP = Math.PI / FPS;
    static final double SHIP_SPEED_STEP = 15.0 / FPS;
    static final double MAX_SHIP_SPEED  = 1.25 * MAX_ROCK_SPEED;
    static final int MAX_SHIP_COUNTER_DURATION = 2 * FPS;
    static private final double missile_PROBABILITY = 0.45 / FPS;
    static private final int BIG_POINTS    =  25;
    static private final int SMALL_POINTS  =  50;
    static final int UFO_POINTS    = 250;
    static final int MISSILE_POINTS = 500;

    // Number of points the must be scored to earn a new ship or to cause the
    // flying saucer to appear.
    static private final int NEW_SHIP_POINTS = 5000;
    static private final int NEW_UFO_POINTS  = 2750;

    private Ship ship;
    private Ufo ufo;
    private Missile missile;
    private final Photon[] photons = new Photon[MAX_NUMBER_OF_PHOTONS];
    private final Asteroid[] asteroids = new Asteroid[MAX_NUMBER_OF_ASTEROIDS];
    private final Explosion[] explosions = new Explosion[MAX_AMOUNT_OF_SCRAP];
    private int highScore;
    private int score;
    private int newShipScore;
    private int newUfoScore;
    private int photonIndex;
    private int breakDuration;  // Break-time counter.
    private int asteroidsLeft;  // Number of active asteroids.
    private int explosionIndex; // Next available explosion sprite.
    private boolean gameOver;
    private boolean thrustersOn;
    private boolean newExplosion;
    private boolean firing;
    private boolean collision;
    private boolean warping;
    private boolean missilePresent;
    private boolean detailedExplosions = true;

    /**
     * Resets the state of the game.
     */
    public void init() {
        FwdThruster fwdThruster = new FwdThruster();
        RevThruster revThruster = new RevThruster();

        ship = new Ship(
                SHIP_ANGLE_STEP,
                SHIP_SPEED_STEP,
                MAX_SHIP_SPEED,
                HYPER_COUNT,
                MAX_SHIP_COUNTER_DURATION,
                MAX_SHIPS,
                fwdThruster,
                revThruster);

        for (int i = 0; i < MAX_NUMBER_OF_PHOTONS; i++) {
            photons[i] = new Photon();
        }

        // Create shape for the flying saucer.
        ufo = new Ufo(0, 0, MAX_ROCK_SPEED);
        ufo.initBasePolygon();

        // Create shape for the guided missile.
        missile = new Missile();
        missile.initBasePolygon();

        // Create asteroid sprites.
        for (int i = 0; i < MAX_NUMBER_OF_ASTEROIDS; i++)
            asteroids[i] = new Asteroid(
                    MIN_ROCK_SIDES,
                    MAX_ROCK_SIDES,
                    MIN_ROCK_SIZE,
                    MAX_ROCK_SIZE,
                    MAX_ROCK_SPIN
            );

        Asteroid.setSpeed(MIN_ROCK_SPEED);

        // Create explosion sprites.
        for (int i = 0; i < MAX_AMOUNT_OF_SCRAP; i++)
            explosions[i] = new Explosion();

        // Initialize game data and put us in 'game over' mode.

        score = 0;
        newShipScore = NEW_SHIP_POINTS;
        newUfoScore = NEW_UFO_POINTS;
        ship.resetShipsLeft();

        initShip();
        initPhotons();
        ufo.stop();
        missile.stop();
        initAsteroids();

        gameOver = false;

        highScore = 0;
    }

    /**
     * Updates the ship with user input.
     * @param left Indicates if the user has pressed left.
     * @param right Indicates if the user has pressed right.
     * @param up Indicates if the user has pressed up.
     * @param down Indicates if the user has pressed down.
     */
    public void moveShip(boolean left, boolean right, boolean up, boolean down) {
        if (!ship.isActive()) return;

        if (up || down) thrustersOn = true;

        ship.updateShip(left, right, up, down);
    }

    /**
     * Fires a new photon from the ship.
     */
    public void firePhoton() {
        if (!ship.isActive()) return;

//        photonTime = System.currentTimeMillis();
        photonIndex++;
        if (photonIndex >= MAX_NUMBER_OF_PHOTONS)
            photonIndex = 0;
        photons[photonIndex].setActive(true);
        photons[photonIndex].setX(ship.getX());
        photons[photonIndex].setY(ship.getY());
        photons[photonIndex].setDeltaX(2 * MAX_ROCK_SPEED * -Math.sin(ship.getAngle()));
        photons[photonIndex].setDeltaY(2 * MAX_ROCK_SPEED * Math.cos(ship.getAngle()));

        firing = true;
    }

    /**
     * Causes ship to enter hyper space.
     */
    public void warpShip() {
        if (!ship.isActive() || ship.getHyperCounter() > 0) return;

        ship.setX(Math.random() * Entity.getWidth());
        ship.setY(Math.random() * Entity.getHeight());
        ship.setHyperCounter(HYPER_COUNT);

        warping = true;
    }

    /**
     * Reset the ship at the center of the screen.
     */
    public void initShip() {
        ship.init();
        ship.setHyperCounter(0);
    }

    /**
     * Resets the photons,
     */
    public void initPhotons() {
        for (int i = 0; i < MAX_NUMBER_OF_PHOTONS; i++)
            photons[i].setActive(false);
        photonIndex = 0;
    }

    /**
     * Resets asteroids.
     */
    public void initAsteroids() {
        for (int i = 0; i < MAX_NUMBER_OF_ASTEROIDS; i++) {
            asteroids[i].init();
        }

        breakDuration = STORM_PAUSE;
        asteroidsLeft = MAX_NUMBER_OF_ASTEROIDS;
        double asteroidsSpeed = Asteroid.getSpeed();
        if (asteroidsSpeed < MAX_ROCK_SPEED)
            Asteroid.incrementSpeed();
    }

    /**
     * Create one or two smaller asteroids from a larger one using inactive
     * asteroids. The new asteroids will be placed in the same position as the
     * old one but will have a new, smaller shape and new, randomly generated
     * movements.
     * @param n The index of the asteroid that will be turned into smaller asteroids.
     */
    public void initSmallAsteroids(int n) {
        int count = 0;
        int i = 0;
        double prevX = asteroids[n].getX();
        double prevY = asteroids[n].getY();
        do {
            if (!asteroids[i].isActive()) {
                asteroids[i].initSmallAsteroid(prevX, prevY);
                count++;
                asteroidsLeft++;
            }
            i++;
        } while (i < MAX_NUMBER_OF_ASTEROIDS && count < 2);
    }

    /**
     * Move and process all entities.
     */
    public void update() {
        updateShip();
        updatePhotons();
        updateUfo();
        updateMissile();
        updateAsteroids();
        updateExplosions();

        // If all asteroids have been destroyed create a new batch.
        if (asteroidsLeft <= 0)
            if (--breakDuration <= 0)
                initAsteroids();
    }

    /**
     * Reset flags describing the current state of the game.
     */
    public void resetFlags() {
        thrustersOn = false;
        newExplosion = false;
        firing = false;
        collision = false;
        warping = false;
    }

    /**
     * Move any active explosion debris. Stop explosion when its counter has
     * expired.
     */
    public void updateExplosions() {
        for (int i = 0; i < MAX_AMOUNT_OF_SCRAP; i++)
            if (explosions[i].isActive()) {
                explosions[i].transform();
                if (explosions[i].getCounter() - 1 < 0)
                    explosions[i].setActive(false);
                else
                    explosions[i].decrementCounter();
            }
    }

    /**
     * Move any active asteroids and check for collisions.
     */
    public void updateAsteroids() {
        for (int i = 0; i < MAX_NUMBER_OF_ASTEROIDS; i++)
            if (asteroids[i].isActive()) {
                asteroids[i].transform();

                // If hit by photon, kill asteroid and advance score. If asteroid is
                // large, make some smaller ones to replace it.

                for (int j = 0; j < MAX_NUMBER_OF_PHOTONS; j++)
                    if (photons[j].isActive() && asteroids[i].isActive() && asteroids[i].isColliding(photons[j])) {
                        asteroidsLeft--;
                        asteroids[i].setActive(false);
                        photons[j].setActive(false);
                        newExplosion = true;
                        explode(asteroids[i]);
                        if (!asteroids[i].getIsSmall()) {
                            score += BIG_POINTS;
                            initSmallAsteroids(i);
                        }
                        else
                            score += SMALL_POINTS;
                    }

                // If the ship is not in hyperspace, see if it is hit.
                if (ship.isActive() && ship.getHyperCounter() <= 0 &&
                        asteroids[i].isActive() && asteroids[i].isColliding(ship)) {

                    handleShipCollision();
                }
            }
    }

    /**
     * Move the guided missile and check for collision with ship or photon. Stop
     * it when its counter has expired.
     */
    public void updateMissile() {
        if (missile.isActive()) {
            if (missile.decrementCounter() <= 0) {
                missilePresent = false;
                missile.stop();
            }
            else {
                guideMissile();
                missile.transform();
                for (int i = 0; i < MAX_NUMBER_OF_PHOTONS; i++)
                    if (photons[i].isActive() && missile.isColliding(photons[i])) {
                        explode(missile);
                        missile.stop();
                        score += MISSILE_POINTS;
                    }
                if (missile.isActive() && ship.isActive() &&
                        ship.getHyperCounter() <= 0 && ship.isColliding(missile)) {
                    missile.stop();
                    handleShipCollision();
                }
            }
        }
    }

    private void handleShipCollision() {
        explode(ship);
        collision = true;
        ship.handleCollision();
        ufo.stop();
    }

    /**
     * Find the angle needed to hit the ship.
     */
    public void guideMissile() {
        if (!ship.isActive() || ship.getHyperCounter() > 0)
            return;

        double angle;
        double dx = ship.getX() - missile.getX();
        double dy = ship.getY() - missile.getY();
        if (dx == 0 && dy == 0)
            angle = Math.PI / 2;
        else {
            angle = Math.atan(Math.abs(dy / dx));
            if (dy > 0)
                angle = -angle;
            if (dx < 0)
                angle = Math.PI - angle;
        }

        // Adjust angle for screen coordinates.

        missile.setAngle(angle - Math.PI / 2);

        // Change the missile's angle so that it points toward the ship.

        missile.setDeltaX(0.75 * MAX_ROCK_SPEED * -Math.sin(missile.getAngle()));
        missile.setDeltaY(0.75 * MAX_ROCK_SPEED * Math.cos(missile.getAngle()));
    }

    /**
     * Move the flying saucer and check for collision with a photon. Stop it
     * when its counter has expired.
     */
    public void updateUfo() {
        if (ufo.isActive()) {
            if (ufo.decrementCounter() <= 0) {
                if (ufo.decrementPasses() > 0)
                    ufo.init();
                else
                    ufo.stop();
            }
            if (ufo.isActive()) {
                ufo.transform();

                for (int i = 0; i < MAX_NUMBER_OF_PHOTONS; i++) {
                    if (photons[i].isActive() && ufo.isColliding(photons[i])) {
                        explode(ufo);
                        ufo.stop();
                        score += UFO_POINTS;
                        newExplosion = true;
                    }
                }

                // On occasion, fire a missile at the ship if the saucer is not too
                // close to it.
                int d = (int) Math.max(Math.abs(ufo.getX() - ship.getX()), Math.abs(ufo.getY() - ship.getY()));
                if (ship.isActive() && ship.getHyperCounter() <= 0 &&
                        ufo.isActive() && !missile.isActive() &&
                        d > MAX_ROCK_SPEED * FPS / 2 &&
                        Math.random() < missile_PROBABILITY)
                    initMissile();

            }
        }
    }

    /**
     * Resets the missile.
     */
    public void initMissile() {
        missile.init(ufo.getX(), ufo.getY());
        missile.setMissileCounter(MISSILE_COUNT);
        missilePresent = true;
    }

    /**
     * Move any active photons. Stop it when its counter has expired.
     */
    public void updatePhotons() {
        for (int i = 0; i < MAX_NUMBER_OF_PHOTONS; i++)
            if (photons[i].isActive()) {
                boolean wrapped = photons[i].transform();
                if (wrapped) {
                    photons[i].setActive(false);
                }
            }
    }

    /**
     * Updates ship based on its current state.
     */
    public void updateShip() {
        ship.transform();
        if (!ship.isActive()) {

            // Ship is exploding, advance the countdown or create a new ship if it is
            // done exploding. The new ship is added as though it were in hyperspace.
            // (This gives the player time to move the ship if it is in imminent
            // danger.) If that was the last ship, end the game.

            if (ship.decrementExplosionCounter() <= 0)
                if (ship.getShipsLeft() > 0) {
                    initShip();
                    ship.setHyperCounter(HYPER_COUNT);
                }
                else
                    gameOver = true;
        }
        // Check the score and advance high score, add a new ship or start the
        // flying saucer as necessary.

        if (score > highScore)
            highScore = score;
        if (score > newShipScore) {
            newShipScore += NEW_SHIP_POINTS;
            ship.incrementShipsLeft();
        }
        if (score > newUfoScore && !ufo.isActive()) {
            newUfoScore += NEW_UFO_POINTS;
            ufo.setUfoPassesLeft(UFO_PASSES);
            ufo.init();
        }
    }

    /**
     * Create sprites for explosion animation. Each individual line segment
     * of the given sprite is used to create a new sprite that will move
     * outward  from the sprite's original position with a random rotation.
     * @param s The entity that will be exploded.
     */
    public void explode(Entity s) {


        s.transform();
        int c = 2;
        if (detailedExplosions || s.getTransformedPolygon().npoints < 6)
            c = 1;
        for (int i = 0; i < s.getTransformedPolygon().npoints; i += c) {
            explosionIndex++;
            if (explosionIndex >= MAX_AMOUNT_OF_SCRAP)
                explosionIndex = 0;
            explosions[explosionIndex].setActive(true);
            explosions[explosionIndex].setBasePolygon(new Polygon());
            int j = i + 1;
            if (j >= s.getTransformedPolygon().npoints)
                j -= s.getTransformedPolygon().npoints;
            int cx = (s.getBasePolygon().xpoints[i] + s.getBasePolygon().xpoints[j]) / 2;
            int cy = ((s.getBasePolygon().ypoints[i] + s.getBasePolygon().ypoints[j]) / 2);
            explosions[explosionIndex].getBasePolygon().addPoint(
                    s.getBasePolygon().xpoints[i] - cx,
                    s.getBasePolygon().ypoints[i] - cy);
            explosions[explosionIndex].getBasePolygon().addPoint(
                    s.getBasePolygon().xpoints[j] - cx,
                    s.getBasePolygon().ypoints[j] - cy);
            explosions[explosionIndex].setX(s.getX() + cx);
            explosions[explosionIndex].setY(s.getY() + cy);
            explosions[explosionIndex].setAngle(s.getAngle());
            explosions[explosionIndex].setDeltaAngle(4 * (Math.random() * 2 * MAX_ROCK_SPIN - MAX_ROCK_SPIN));
            explosions[explosionIndex].setDeltaX((Math.random() * 2 * MAX_ROCK_SPEED - MAX_ROCK_SPEED + s.getDeltaX()) / 2);
            explosions[explosionIndex].setDeltaY((Math.random() * 2 * MAX_ROCK_SPEED - MAX_ROCK_SPEED + s.getDeltaY()) / 2);
            explosions[explosionIndex].setCounter(SCRAP_COUNT);
        }
    }

    /**
     * Stops the ufo and missile.
     */
    public void stop() {
        ufo.stop();
        missile.stop();
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }


    @Override
    public boolean isThrustersOn() {
        return thrustersOn;
    }

    @Override
    public Photon[] getPhotons() {
        return  photons;
    }

    @Override
    public Missile getMissile() {
        return missile;
    }

    @Override
    public Asteroid[] getAsteroids() {
        return asteroids;
    }

    @Override
    public Ufo getUfo() {
        return ufo;
    }

    @Override
    public Ship getShip() {
        return ship;
    }

    @Override
    public int getHyperCount() {
        return HYPER_COUNT;
    }

    @Override
    public int getMaxScrap() {
        return MAX_AMOUNT_OF_SCRAP;
    }

    @Override
    public Explosion[] getExplosions() {
        return explosions;
    }

    @Override
    public int getScrapCount() {
        return SCRAP_COUNT;
    }
    @Override
    public int getScore() {
        return score;
    }
    @Override
    public int getHighScore() {
        return highScore;
    }
    @Override
    public boolean isNewExplosion() {
        return newExplosion;
    }
    @Override
    public boolean isFiring() {
        return firing;
    }
    @Override
    public boolean isCollision() {
        return collision;
    }

    @Override
    public boolean isWarping() {
        return warping;
    }

    @Override
    public boolean isUfoPresent() {
        return ufo.isActive();
    }

    @Override
    public boolean isMissilePresent() {
        return missilePresent;
    }

    public void setDetailedExplosions(boolean detailedExplosions) {
        this.detailedExplosions = detailedExplosions;
    }
}

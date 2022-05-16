package model;

import model.entity.*;

import java.awt.*;

public class AsteroidsGameStateHandler {

    static int fps = 60;

    static final int MAX_NUMBER_OF_SHOTS =  8;          // Maximum number of sprites
    static final int MAX_ROCKS =  8;          // for photons, asteroids and
    static final int MAX_SCRAP = 40;          // explosions.

    static final int SCRAP_COUNT  = 2 * fps;  // Timer counter starting values
    static final int HYPER_COUNT  = 3 * fps;  // calculated using number of
    static final int MISSILE_COUNT = 4 * fps;  // seconds x frames per second.
    static final int STORM_PAUSE  = 3 * fps; // Time between rounds

    static final int    MIN_ROCK_SIDES =   6; // Ranges for asteroid shape, size
    static final int    MAX_ROCK_SIDES =  16; // speed and rotation.
    static final int    MIN_ROCK_SIZE  =  20;
    static final int    MAX_ROCK_SIZE  =  40;
    static final double MIN_ROCK_SPEED =  40.0 / fps;
    static final double MAX_ROCK_SPEED = 240.0 / fps;
    static final double MAX_ROCK_SPIN  = Math.PI / fps;

    static final int MAX_SHIPS = 3;           // Starting number of ships for
    // each game.
    static final int UFO_PASSES = 3;          // Number of passes for flying
    // saucer per appearance.

    // Ship's rotation and acceleration rates and maximum speed.

    static final double SHIP_ANGLE_STEP = Math.PI / fps;
    static final double SHIP_SPEED_STEP = 15.0 / fps;
    static final double MAX_SHIP_SPEED  = 1.25 * MAX_ROCK_SPEED;

    static final int MAX_SHIP_COUNTER_DURATION = 2 * fps;

    static final int FIRE_DELAY = 50;         // Minimum number of milliseconds
    // required between photon shots.

    // Probablility of flying saucer firing a missile during any given frame
    // (other conditions must be met).

    static final double missile_PROBABILITY = 0.45 / fps;

    static final int BIG_POINTS    =  25;     // Points scored for shooting
    static final int SMALL_POINTS  =  50;     // various objects.
    static final int UFO_POINTS    = 250;
    static final int MISSILE_POINTS = 500;

    // Number of points the must be scored to earn a new ship or to cause the
    // flying saucer to appear.

    static final int NEW_SHIP_POINTS = 5000;
    static final int NEW_UFO_POINTS  = 2750;

    Ship ship;
    Ufo ufo;
    Missile missile;
    Photon[] photons = new Photon[MAX_NUMBER_OF_SHOTS];
    Asteroid[] asteroids = new Asteroid[MAX_ROCKS];
    Explosion[] explosions = new Explosion[MAX_SCRAP];
    private int highScore;
    private int score;
    private int newShipScore;
    private int newUfoScore;
    private int photonIndex;
    private long photonTime;     // Time value used to keep firing rate constant.
    private int breakDuration;                            // Break-time counter.
    private int asteroidsLeft;                               // Number of active asteroids.
    private int explosionIndex;                         // Next available explosion sprite.
    private boolean gameOver;
    private boolean thrustersOn;
    private boolean newExplosion;
    private boolean firing;
    private boolean collision;
    private boolean warping;
    private boolean missilePresent;

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

        for (int i = 0; i < MAX_NUMBER_OF_SHOTS; i++) {
            photons[i] = new Photon();
        }

        // Create shape for the flying saucer.
        ufo = new Ufo(0, 0, MAX_ROCK_SPEED);
        ufo.initBasePolygon();

        // Create shape for the guided missile.
        missile = new Missile();
        missile.initBasePolygon();

        // Create asteroid sprites.
        for (int i = 0; i < MAX_ROCKS; i++)
            asteroids[i] = new Asteroid(
                    MIN_ROCK_SIDES,
                    MAX_ROCK_SIDES,
                    MIN_ROCK_SIZE,
                    MAX_ROCK_SIZE,
                    MAX_ROCK_SPIN
            );

        Asteroid.setSpeed(MIN_ROCK_SPEED);

        // Create explosion sprites.
        for (int i = 0; i < MAX_SCRAP; i++)
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
        initExplosions();
        photonTime = System.currentTimeMillis();

        gameOver = false;

        highScore = 0;
    }

//    public void initGame() {
//
//        // Initialize game data and sprites.
//        score = 0;
//        newShipScore = NEW_SHIP_POINTS;
//        newUfoScore = NEW_UFO_POINTS;
//        ship.resetShipsLeft();
//
//        initShip();
//        initPhotons();
//        ufo.stop();
//        missile.stop();
//        initAsteroids();
//        initExplosions();
//        photonTime = System.currentTimeMillis();
//
//        gameOver = false;
//    }

    public void moveShip(boolean left, boolean right, boolean up, boolean down) {
        if (!ship.isActive()) return;

        if (up || down) thrustersOn = true;

        ship.updateShip(left, right, up, down);
    }

    public void firePhoton() {
        if (!ship.isActive()) return;

        photonTime = System.currentTimeMillis();
        photonIndex++;
        if (photonIndex >= MAX_NUMBER_OF_SHOTS)
            photonIndex = 0;
        photons[photonIndex].setActive(true);
        photons[photonIndex].setX(ship.getX());
        photons[photonIndex].setY(ship.getY());
        photons[photonIndex].setDeltaX(2 * MAX_ROCK_SPEED * -Math.sin(ship.getAngle()));
        photons[photonIndex].setDeltaY(2 * MAX_ROCK_SPEED * Math.cos(ship.getAngle()));

        firing = true;
    }

    public void warpShip() {
        if (!ship.isActive() || ship.getHyperCounter() > 0) return;

        ship.setX(Math.random() * Entity.getWidth());
        ship.setY(Math.random() * Entity.getHeight());
        ship.setHyperCounter(HYPER_COUNT);

        warping = true;
    }

    public void initShip() {
        // Reset the ship at the center of the screen.
        ship.init();
        ship.setHyperCounter(0);
    }

    public void initPhotons() {
        for (int i = 0; i < MAX_NUMBER_OF_SHOTS; i++)
            photons[i].setActive(false);
        photonIndex = 0;
    }

    public void initAsteroids() {
        // Create random shapes, positions and movements for each asteroid.

        for (int i = 0; i < MAX_ROCKS; i++) {
            asteroids[i].init();
        }

        breakDuration = STORM_PAUSE;
        asteroidsLeft = MAX_ROCKS;
        double asteroidsSpeed = Asteroid.getSpeed();
        if (asteroidsSpeed < MAX_ROCK_SPEED)
            Asteroid.incrementSpeed();
    }

    public void initSmallAsteroids(int n) {
        // Create one or two smaller asteroids from a larger one using inactive
        // asteroids. The new asteroids will be placed in the same position as the
        // old one but will have a new, smaller shape and new, randomly generated
        // movements.

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
        } while (i < MAX_ROCKS && count < 2);
    }

    public void initExplosions() {
        for (int i = 0; i < MAX_SCRAP; i++) {
            explosions[i].init();
        }
        explosionIndex = 0;
    }

    public void update() {
        // Move and process all sprites.
        updateShip();
        updatePhotons();
        updateUfo();
        updatemissile();
        updateAsteroids();
        updateExplosions();

        // If all asteroids have been destroyed create a new batch.
        if (asteroidsLeft <= 0)
            if (--breakDuration <= 0)
                initAsteroids();
    }

    public void resetFlags() {
        thrustersOn = false;
        newExplosion = false;
        firing = false;
        collision = false;
        warping = false;
    }

    public void updateExplosions() {
        // Move any active explosion debris. Stop explosion when its counter has
        // expired.

        for (int i = 0; i < MAX_SCRAP; i++)
            if (explosions[i].isActive()) {
                explosions[i].transform();
                if (explosions[i].getCounter() - 1 < 0)
                    explosions[i].setActive(false);
                else
                    explosions[i].decrementCounter();
            }
    }

    public void updateAsteroids() {
        // Move any active asteroids and check for collisions.
        for (int i = 0; i < MAX_ROCKS; i++)
            if (asteroids[i].isActive()) {
                asteroids[i].transform();

                // If hit by photon, kill asteroid and advance score. If asteroid is
                // large, make some smaller ones to replace it.

                for (int j = 0; j < MAX_NUMBER_OF_SHOTS; j++)
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

    public void updatemissile() {
        // Move the guided missile and check for collision with ship or photon. Stop
        // it when its counter has expired.

        if (missile.isActive()) {
            if (missile.decrementCounter() <= 0) {
                missilePresent = false;
                missile.stop();
            }
            else {
                guidemissile();
                missile.transform();
                for (int i = 0; i < MAX_NUMBER_OF_SHOTS; i++)
                    if (photons[i].isActive() && missile.isColliding(photons[i])) {
                        explode(missile);
                        missile.stop();
                        score += MISSILE_POINTS;
                    }
                if (missile.isActive() && ship.isActive() &&
                        ship.getHyperCounter() <= 0 && ship.isColliding(missile)) {
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

    public void guidemissile() {
        if (!ship.isActive() || ship.getHyperCounter() > 0)
            return;

        // Find the angle needed to hit the ship.

        double angle;
        double dx = ship.getX() - missile.getX();
        double dy = ship.getY() - missile.getY();
        if (dx == 0 && dy == 0)
            angle = 0;
        if (dx == 0) {
            if (dy < 0)
                angle = -Math.PI / 2;
            else
                angle = Math.PI / 2;
        }
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

    public void updateUfo() {
        // Move the flying saucer and check for collision with a photon. Stop it
        // when its counter has expired.
        if (ufo.isActive()) {
            if (ufo.decrementCounter() <= 0) {
                if (ufo.decrementPasses() > 0)
                    ufo.init();
                else
                    ufo.stop();
            }
            if (ufo.isActive()) {
                ufo.transform();

                for (int i = 0; i < MAX_NUMBER_OF_SHOTS; i++) {
                    if (photons[i].isActive() && ufo.isColliding(photons[i])) {
                        explode(ufo);
                        ufo.stop();
                        score += UFO_POINTS;
                        newExplosion = true;
                    }
                }

                // On occassion, fire a missile at the ship if the saucer is not too
                // close to it.
                int d = (int) Math.max(Math.abs(ufo.getX() - ship.getX()), Math.abs(ufo.getY() - ship.getY()));
                if (ship.isActive() && ship.getHyperCounter() <= 0 &&
                        ufo.isActive() && !missile.isActive() &&
                        d > MAX_ROCK_SPEED * fps / 2 &&
                        Math.random() < missile_PROBABILITY)
                    initMissile();

            }
        }
    }

    public void initMissile() {
        missile.init(ufo.getX(), ufo.getY());
        missile.setMissileCounter(MISSILE_COUNT);
        missilePresent = true;
    }

    public void updatePhotons() {

        int i;

        // Move any active photons. Stop it when its counter has expired.

        for (i = 0; i < MAX_NUMBER_OF_SHOTS; i++)
            if (photons[i].isActive()) {
                boolean wrapped = photons[i].transform();
                if (wrapped) {
                    photons[i].setActive(false);
                }
            }
    }

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

    public void explode(Entity s) {
        // Create sprites for explosion animation. Each individual line segment
        // of the given sprite is used to create a new sprite that will move
        // outward  from the sprite's original position with a random rotation.

        s.transform();
        int c = 2;
        if (s.getTransformedPolygon().npoints < 6)
            c = 1;
        for (int i = 0; i < s.getTransformedPolygon().npoints; i += c) {
            explosionIndex++;
            if (explosionIndex >= MAX_SCRAP)
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

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void stop() {
        ufo.stop();
        missile.stop();
    }

    public boolean isThrustersOn() {
        return thrustersOn;
    }

    public Photon[] getPhotons() {
        return  photons;
    }

    public Missile getMisile() {
        return missile;
    }

    public Asteroid[] getAsteroids() {
        return asteroids;
    }

    public Ufo getUfo() {
        return ufo;
    }

    public Ship getShip() {
        return ship;
    }

    public int getHyperCount() {
        return HYPER_COUNT;
    }

    public int getMaxScrap() {
        return MAX_SCRAP;
    }

    public Explosion[] getExplosions() {
        return explosions;
    }

    public int getScrapCount() {
        return SCRAP_COUNT;
    }
    public int getScore() {
        return score;
    }
    public int getHighScore() {
        return highScore;
    }
    public boolean isNewExplosion() {
        return newExplosion;
    }
    public boolean isFiring() {
        return firing;
    }
    public boolean isCollision() {
        return collision;
    }

    public boolean isWarping() {
        return warping;
    }

    public boolean isUfoPresent() {
        return ufo.isActive();
    }

    public boolean isMissilePresent() {
        return missilePresent;
    }
}

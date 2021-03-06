import model.GameStateProvider;
import model.entity.*;

import java.applet.AudioClip;
import java.awt.*;

public class AsteroidsView {

    // Sound clips.
    private final AudioClip crashSound;
    private final AudioClip explosionSound;
    private final AudioClip fireSound;
    private final AudioClip missileSound;
    private final AudioClip saucerSound;
    private final AudioClip thrustersSound;
    private final AudioClip warpSound;

    private boolean sound = true;
    private boolean missilePlaying;
    private boolean saucerPlaying;
    private boolean thrustersPlaying;
    private final GameStateProvider gameStateProvider;
    private boolean up = false;
    private boolean down = false;
    private final int numStars;
    private final Point[] stars;
    private boolean detailed = true;
    private String copyName;
    private String copyVers;
    private String copyInfo;
    private String copyLink;

    public AsteroidsView(AudioClip crashSound,
                         AudioClip explosionSound,
                         AudioClip fireSound,
                         AudioClip missileSound,
                         AudioClip saucerSound,
                         AudioClip thrustersSound,
                         AudioClip warpSound,
                         GameStateProvider gameStateProvider) {
        this.crashSound = crashSound;
        this.explosionSound = explosionSound;
        this.fireSound = fireSound;
        this.missileSound = missileSound;
        this.saucerSound = saucerSound;
        this.thrustersSound = thrustersSound;
        this.warpSound = warpSound;
        this.gameStateProvider = gameStateProvider;

        numStars = Entity.getWidth() * Entity.getHeight() / 5000;
        stars = new Point[numStars];
        for (int i = 0; i < numStars; i++)
            stars[i] = new Point((int) (Math.random() * Entity.getWidth()),
                            (int) (Math.random() * Entity.getHeight()));
    }

    /**
     * Plays sounds based on current state of the game.
     */
    public void playSounds() {

        if (gameStateProvider.isThrustersOn()) {
            playShipSound();
        } else {
            stopShipSound();
        }

        if (gameStateProvider.isUfoPresent()) {
            playSaucerSound();
        } else {
            stopSaucerSound();
        }

        if (gameStateProvider.isMissilePresent()) {
            playMissileSound();
        } else {
            stopMissileSound();
        }

        playExplosionSound();
        playFiringSound();
        playCollisionSound();
        playWarpingSound();
    }

    private void playWarpingSound() {
        if (gameStateProvider.isWarping() && sound) {
            warpSound.play();
        }
    }

    private void playCollisionSound() {
        if (gameStateProvider.isCollision() && sound) {
            crashSound.play();
        }
    }

    private void playFiringSound() {
        if (gameStateProvider.isFiring() && sound) {
            fireSound.play();
        }
    }

    private void playExplosionSound() {
        if (gameStateProvider.isNewExplosion() && sound) {
            explosionSound.play();
        }
    }

    private void playMissileSound() {
        if (missilePlaying || !sound) return;

        missilePlaying = true;
        missileSound.loop();
    }

    private void stopMissileSound() {
        missilePlaying = false;
        missileSound.stop();
    }

    public void playSaucerSound() {
        if (saucerPlaying || !sound) return;

        saucerPlaying = true;
        saucerSound.loop();
    }

    public void stopSaucerSound() {
        saucerPlaying = false;
        saucerSound.stop();
    }

    public void stopShipSound() {
        thrustersSound.stop();
        thrustersPlaying = false;
    }

    public void playShipSound() {
        if (!sound || thrustersPlaying) return;
        thrustersSound.loop();
        thrustersPlaying = true;
    }

    /**
     * Draws the next frame.
     */
    public void paint(Dimension d,
                      Graphics offGraphics,
                      Font font,
                      int fontWidth,
                      int fontHeight,
                      boolean paused,
                      FontMetrics fm,
                      boolean playing,
                      boolean loaded,
                      int clipTotal,
                      int clipsLoaded) {
        int i;
        int c;
        String s;
        int w, h;
        int x, y;

        // Create the off screen graphics context, if no good one exists.
        Entity[] photons = gameStateProvider.getPhotons();
        Entity[] asteroids = gameStateProvider.getAsteroids();
        Missile missile = gameStateProvider.getMissile();
        Ufo ufo = gameStateProvider.getUfo();
        Ship ship = gameStateProvider.getShip();
        Explosion[] explosions = gameStateProvider.getExplosions();

        int hyperCount = gameStateProvider.getHyperCount();
        int maxScrap = gameStateProvider.getMaxScrap();
        int scrapCount = gameStateProvider.getScrapCount();

        int score = gameStateProvider.getScore();
        int highScore = gameStateProvider.getHighScore();

        // Fill in background and stars.

        offGraphics.setColor(Color.black);
        offGraphics.fillRect(0, 0, d.width, d.height);
        if (detailed) {
            offGraphics.setColor(Color.white);
            for (i = 0; i < numStars; i++)
                offGraphics.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
        }

        // Draw photon bullets.

        offGraphics.setColor(Color.white);
        for (i = 0; i < photons.length; i++)
            if (photons[i].isActive())
                offGraphics.drawPolygon(photons[i].getTransformedPolygon());

        // Draw the guided missile, counter is used to quickly fade color to black
        // when near expiration.


        c = Math.min(missile.getMissileCounter() * 24, 255);
        offGraphics.setColor(new Color(c, c, c));
        if (missile.isActive()) {
            offGraphics.drawPolygon(missile.getTransformedPolygon());
            offGraphics.drawLine(missile.getTransformedPolygon().xpoints[missile.getTransformedPolygon().npoints - 1], missile.getTransformedPolygon().ypoints[missile.getTransformedPolygon().npoints - 1],
                    missile.getTransformedPolygon().xpoints[0], missile.getTransformedPolygon().ypoints[0]);
        }

        // Draw the asteroids.

        for (i = 0; i < asteroids.length; i++)
            if (asteroids[i].isActive()) {
                if (detailed) {
                    offGraphics.setColor(Color.black);
                    offGraphics.fillPolygon(asteroids[i].getTransformedPolygon());
                }
                offGraphics.setColor(Color.white);
                offGraphics.drawPolygon(asteroids[i].getTransformedPolygon());
                offGraphics.drawLine(asteroids[i].getTransformedPolygon().xpoints[asteroids[i].getTransformedPolygon().npoints - 1], asteroids[i].getTransformedPolygon().ypoints[asteroids[i].getTransformedPolygon().npoints - 1],
                        asteroids[i].getTransformedPolygon().xpoints[0], asteroids[i].getTransformedPolygon().ypoints[0]);
            }

        // Draw the flying saucer.
        if (ufo.isActive()) {
            if (detailed) {
                offGraphics.setColor(Color.black);
                offGraphics.fillPolygon(ufo.getTransformedPolygon());
            }
            offGraphics.setColor(Color.white);
            offGraphics.drawPolygon(ufo.getTransformedPolygon());
            offGraphics.drawLine(ufo.getTransformedPolygon().xpoints[ufo.
                            getTransformedPolygon().npoints - 1], ufo.getTransformedPolygon().ypoints[ufo.getTransformedPolygon().npoints - 1],
                    ufo.getTransformedPolygon().xpoints[0], ufo.getTransformedPolygon().ypoints[0]);
        }

        // Draw the ship, counter is used to fade color to white on hyperspace.


        c = 255 - (255 / hyperCount) * ship.getHyperCounter();
        if (ship.isActive()) {
            if (detailed && ship.getHyperCounter() == 0) {
                offGraphics.setColor(Color.black);
                offGraphics.fillPolygon(ship.getTransformedPolygon());
            }
            offGraphics.setColor(new Color(c, c, c));
            offGraphics.drawPolygon(ship.getTransformedPolygon());


            offGraphics.drawLine(ship.getTransformedPolygon().xpoints[ship.getTransformedPolygon().npoints - 1], ship.getTransformedPolygon().ypoints[ship.getTransformedPolygon().npoints - 1],
                    ship.getTransformedPolygon().xpoints[0], ship.getTransformedPolygon().ypoints[0]);

            // Draw thruster exhaust if thrusters are on. Do it randomly to get a
            // flicker effect.

            if (!paused && detailed && Math.random() < 0.5) {
                if (up) {
                    offGraphics.drawPolygon(ship.getFwdThruster().getTransformedPolygon());
                    offGraphics.drawLine(
                            ship.getFwdThruster().getTransformedPolygon().xpoints[ship.getFwdThruster().getTransformedPolygon().npoints - 1],
                            ship.getFwdThruster().getTransformedPolygon().ypoints[ship.getFwdThruster().getTransformedPolygon().npoints - 1],
                            ship.getFwdThruster().getTransformedPolygon().xpoints[0], ship.getFwdThruster().getTransformedPolygon().ypoints[0]
                    );
                }
                if (down) {
                    offGraphics.drawPolygon(ship.getRevThruster().getTransformedPolygon());
                    offGraphics.drawLine(
                            ship.getRevThruster().getTransformedPolygon().xpoints[ship.getRevThruster().getTransformedPolygon().npoints - 1],
                            ship.getRevThruster().getTransformedPolygon().ypoints[ship.getRevThruster().getTransformedPolygon().npoints - 1],
                            ship.getRevThruster().getTransformedPolygon().xpoints[0], ship.getRevThruster().getTransformedPolygon().ypoints[0]
                    );
                }
            }
        }

        // Draw any explosion debris, counters are used to fade color to black
        for (i = 0; i < maxScrap; i++)
            if (explosions[i].isActive()) {
                c = (255 / scrapCount) * explosions[i].getCounter();
                offGraphics.setColor(new Color(c, c, c));
                offGraphics.drawPolygon(explosions[i].getTransformedPolygon());
            }

        // Display status and messages.

        offGraphics.setFont(font);
        offGraphics.setColor(Color.white);

        offGraphics.drawString("Score: " + score, fontWidth, fontHeight);
        offGraphics.drawString("Ships: " + ship.getShipsLeft(), fontWidth, d.height - fontHeight);
        s = "High: " + highScore;
        offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), fontHeight);
        if (!sound) {
            s = "Mute";
            offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), d.height - fontHeight);
        }

        if (!playing) {
            s = copyName;

            offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 - 2 * fontHeight);
            s = copyVers;
            offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 - fontHeight);
            s = copyInfo;
            offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight);
            s = copyLink;
            offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + 2 * fontHeight);
            if (!loaded) {
                s = "Loading sounds...";
                w = 4 * fontWidth + fm.stringWidth(s);
                h = fontHeight;
                x = (d.width - w) / 2;
                y = 3 * d.height / 4 - fm.getMaxAscent();
                offGraphics.setColor(Color.black);
                offGraphics.fillRect(x, y, w, h);
                offGraphics.setColor(Color.gray);
                if (clipTotal > 0)
                    offGraphics.fillRect(x, y,  (w * clipsLoaded / clipTotal), h);
                offGraphics.setColor(Color.white);
                offGraphics.drawRect(x, y, w, h);
                offGraphics.drawString(s, x + 2 * fontWidth, y + fm.getMaxAscent());
            }
            else {
                s = "Game Over";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
                s = "'S' to Start";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + fontHeight);
            }
        }
        else if (paused) {
            s = "Game Paused";
            offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
        }
    }
    public void setCopyName(String copyName) {
        this.copyName = copyName;
    }

    public void setCopyVers(String copyVers) {
        this.copyVers = copyVers;
    }

    public void setCopyInfo(String copyInfo) {
        this.copyInfo = copyInfo;
    }

    public void setCopyLink(String copyLink) {
        this.copyLink = copyLink;
    }

    public void unPause() {
        if (sound && missilePlaying)
            missileSound.loop();
        if (sound && saucerPlaying)
            saucerSound.loop();
        if (sound && thrustersPlaying)
            thrustersSound.loop();
    }

    public void pause() {
        if (missilePlaying)
            missileSound.stop();
        if (saucerPlaying)
            saucerSound.stop();
        if (thrustersPlaying)
            thrustersSound.stop();
    }

    public void soundOff() {
        sound = false;
        crashSound.stop();
        explosionSound.stop();
        fireSound.stop();
        missileSound.stop();
        saucerSound.stop();
        thrustersSound.stop();
        warpSound.stop();
    }

    public void soundOn(boolean paused) {
        sound = true;
        if (missilePlaying && !paused)
            missileSound.loop();
        if (saucerPlaying && !paused)
            saucerSound.loop();
        if (thrustersPlaying && !paused)
            thrustersSound.loop();
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public void setDetailed(boolean detailed) {
        this.detailed = detailed;
    }
}

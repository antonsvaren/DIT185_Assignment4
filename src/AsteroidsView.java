import model.entity.*;

import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class AsteroidsView implements KeyListener {

    // Sound clips.
    private final AudioClip crashSound;
    private final AudioClip explosionSound;
    private final AudioClip fireSound;
    private final AudioClip missileSound;
    private final AudioClip saucerSound;
    private final AudioClip thrustersSound;
    private final AudioClip warpSound;

    private boolean sound;

    private boolean thrustersOn;
    private boolean ufoPresent;
    private boolean missilePresent;
    private boolean newExplosion;
    private boolean firing;
    private boolean collision;
    private boolean warping;
    private boolean missilePlaying;
    private boolean saucerPlaying;
    private boolean thrustersPlaying;

    private Ship ship;
    private Entity[] photons;
    private Missile missile;
    private Entity[] asteroids;
    private Entity ufo;
    private Explosion[] explosions;

    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;

    private final List<UserInputObserver> userInputObservers;
    private int numStars;
    private Point[] stars;
    private boolean detail;
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
                         AudioClip warpSound) {
        this.crashSound = crashSound;
        this.explosionSound = explosionSound;
        this.fireSound = fireSound;
        this.missileSound = missileSound;
        this.saucerSound = saucerSound;
        this.thrustersSound = thrustersSound;
        this.warpSound = warpSound;
        userInputObservers = new ArrayList<>();
    }

    public void init(int width, int height) {
        numStars = width * height / 5000;
        stars = new Point[numStars];
        for (int i = 0; i < numStars; i++)
            stars[i] = new Point((int) (Math.random() * Entity.getWidth()), (int) (Math.random() * Entity.getHeight()));

    }

    public void playSounds() {

        if (thrustersOn) {
            playShipSound();
        } else {
            stopShipSound();
        }

        if (ufoPresent) {
            playSaucerSound();
        } else {
            stopSaucerSound();
        }

        if (missilePresent) {
            playMissileSound();
        } else {
            stopMissileSound();
        }

        if (!sound) return;

        if (newExplosion) {
            explosionSound.play();
        }
        if (firing) {
            fireSound.play();
        }
        if (collision) {
            crashSound.play();
        }
        if (warping) {
            warpSound.play();
        }
    }


    private void playMissileSound() {
        if (missilePlaying) return;

        missilePlaying = true;
        missileSound.loop();
    }

    private void stopMissileSound() {
        missilePlaying = false;
        missileSound.stop();
    }

    public void playSaucerSound() {
        if (saucerPlaying) return;

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
        if (sound && !thrustersPlaying) {
            thrustersSound.loop();
            thrustersPlaying = true;
        }
    }

    public void keyPressed(KeyEvent e) {

        userInputObservers.forEach(observer -> {
            char c;

            // Check if any cursor keys have been pressed and set flags.
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                observer.left();
                up = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                observer.right();
                right = true;
            }

            if (e.getKeyCode() == KeyEvent.VK_UP) {
                observer.up();
                up = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                observer.down();
                down = true;
            }

            // Spacebar: fire a photon and start its counter.
            if (e.getKeyChar() == ' ') {
                observer.firePhoton();
            }

            // Allow upper or lower case characters for remaining keys.
            c = Character.toLowerCase(e.getKeyChar());

            // 'H' key: warp ship into hyperspace by moving to a random location and
            // starting counter.
            if (c == 'h' ) {
                observer.warpShip();
            }

            // 'P' key: toggle pause mode and start or stop any active looping sound
            // clips.

            if (c == 'p') {
//                if (paused) {
//                    if (sound && missilePlaying)
//                        missileSound.loop();
//                    if (sound && saucerPlaying)
//                        saucerSound.loop();
//                    if (sound && thrustersPlaying)
//                        thrustersSound.loop();
//                }
//                else {
//                    if (missilePlaying)
//                        missileSound.stop();
//                    if (saucerPlaying)
//                        saucerSound.stop();
//                    if (thrustersPlaying)
//                        thrustersSound.stop();
//                }
                observer.pause();
            }

            // 'M' key: toggle sound on or off and stop any looping sound clips.

            if (c == 'm') {
                if (sound) {
                    crashSound.stop();
                    explosionSound.stop();
                    fireSound.stop();
                    missileSound.stop();
                    saucerSound.stop();
                    thrustersSound.stop();
                    warpSound.stop();
                }
                else {
                    if (missilePlaying)
                        missileSound.loop();
                    if (saucerPlaying )
                        saucerSound.loop();
                    if (thrustersPlaying)
                        thrustersSound.loop();
                }
                sound = !sound;
            }

            // 'D' key: toggle graphics detail on or off.

            if (c == 'd') {
                detail = !detail;
                observer.toggleDetails();
            }

            // 'S' key: start the game, if not already in progress.

            if (c == 's')
                observer.toggleStart();

            // 'HOME' key: jump to web site (undocumented).

            if (e.getKeyCode() == KeyEvent.VK_HOME)
                observer.home();
        });

    }

    public void keyReleased(KeyEvent e) {

        // Check if any cursor keys where released and set flags.

        userInputObservers.forEach(observer -> {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                observer.releaseLeft();
                left = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                observer.releaseRight();
                right = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                observer.releaseUp();
                up = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                observer.releaseDown();
                down = false;
            }
        });

        if (thrustersPlaying) {
            thrustersSound.stop();
            thrustersPlaying = false;
        }
    }
    public void keyTyped(KeyEvent e) {}

    public void paint(Dimension d,
                      Dimension offDimension,
                      Graphics offGraphics,
                      Image offImage,
                      int hyperCount,
                      int maxScrap,
                      int scrapCount,
                      Font font,
                      int score,
                      int fontWidth,
                      int fontHeight,
                      boolean paused,
                      int highScore,
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



        // Fill in background and stars.

        offGraphics.setColor(Color.black);
        offGraphics.fillRect(0, 0, d.width, d.height);
        if (detail) {
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
                if (detail) {
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
            if (detail) {
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
            if (detail && ship.getHyperCounter() == 0) {
                offGraphics.setColor(Color.black);
                offGraphics.fillPolygon(ship.getTransformedPolygon());
            }
            offGraphics.setColor(new Color(c, c, c));
            offGraphics.drawPolygon(ship.getTransformedPolygon());


            offGraphics.drawLine(ship.getTransformedPolygon().xpoints[ship.getTransformedPolygon().npoints - 1], ship.getTransformedPolygon().ypoints[ship.getTransformedPolygon().npoints - 1],
                    ship.getTransformedPolygon().xpoints[0], ship.getTransformedPolygon().ypoints[0]);

            // Draw thruster exhaust if thrusters are on. Do it randomly to get a
            // flicker effect.

            if (!paused && detail && Math.random() < 0.5) {
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
//
//        // Copy the off screen buffer to the screen.
//        g.drawImage(offImage, 0, 0, this);
    }

    public AudioClip getCrashSound() {
        return crashSound;
    }

    public AudioClip getExplosionSound() {
        return explosionSound;
    }

    public AudioClip getFireSound() {
        return fireSound;
    }

    public AudioClip getMissileSound() {
        return missileSound;
    }

    public AudioClip getSaucerSound() {
        return saucerSound;
    }

    public AudioClip getThrustersSound() {
        return thrustersSound;
    }

    public AudioClip getWarpSound() {
        return warpSound;
    }

    public boolean isSound() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    public boolean isThrustersOn() {
        return thrustersOn;
    }

    public void setThrustersOn(boolean thrustersOn) {
        this.thrustersOn = thrustersOn;
    }

    public boolean isUfoPresent() {
        return ufoPresent;
    }

    public void setUfoPresent(boolean ufoPresent) {
        this.ufoPresent = ufoPresent;
    }

    public boolean isMissilePresent() {
        return missilePresent;
    }

    public void setMissilePresent(boolean missilePresent) {
        this.missilePresent = missilePresent;
    }

    public boolean isNewExplosion() {
        return newExplosion;
    }

    public void setNewExplosion(boolean newExplosion) {
        this.newExplosion = newExplosion;
    }

    public boolean isFiring() {
        return firing;
    }

    public void setFiring(boolean firing) {
        this.firing = firing;
    }

    public boolean isCollision() {
        return collision;
    }

    public void setCollision(boolean collision) {
        this.collision = collision;
    }

    public boolean isWarping() {
        return warping;
    }

    public void setWarping(boolean warping) {
        this.warping = warping;
    }

    public boolean isMissilePlaying() {
        return missilePlaying;
    }

    public void setMissilePlaying(boolean missilePlaying) {
        this.missilePlaying = missilePlaying;
    }

    public boolean isSaucerPlaying() {
        return saucerPlaying;
    }

    public void setSaucerPlaying(boolean saucerPlaying) {
        this.saucerPlaying = saucerPlaying;
    }

    public boolean isThrustersPlaying() {
        return thrustersPlaying;
    }

    public void setThrustersPlaying(boolean thrustersPlaying) {
        this.thrustersPlaying = thrustersPlaying;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public void setPhotons(Entity[] photons) {
        this.photons = photons;
    }

    public void setMissile(Missile missile) {
        this.missile = missile;
    }

    public void setAsteroids(Entity[] asteroids) {
        this.asteroids = asteroids;
    }

    public void setUfo(Entity ufo) {
        this.ufo = ufo;
    }

    public void setExplosions(Explosion[] explosions) {
        this.explosions = explosions;
    }
}

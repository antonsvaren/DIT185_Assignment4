/******************************************************************************
  controller.Asteroids, Version 1.3

  Copyright 1998-2001 by Mike Hall.
  Please see http://www.brainjar.com for terms of use.

  Revision History:

  1.01, 12/18/1999: Increased number of active photons allowed.
                    Improved explosions for more realism.
                    Added progress bar for loading of sound clips.
  1.2,  12/23/1999: Increased frame rate for smoother animation.
                    Modified code to calculate game object speeds and timer
                    counters based on the frame rate so they will remain
                    constant.
                    Improved speed limit checking for ship.
                    Removed wrapping of photons around screen and set a fixed
                    firing rate.
                    Added sprites for ship's thrusters.
  1.3,  01/25/2001: Updated to JDK 1.1.8.

  Usage:

  <applet code="AsteroidsController.class" width=700 height=400></applet>

  Keyboard Controls:

  S            - Start Game    P           - Pause Game
  Cursor Left  - Rotate Left   Cursor Up   - Fire Thrusters
  Cursor Right - Rotate Right  Cursor Down - Fire Retro Thrusters
  Spacebar     - Fire Cannon   H           - Hyperspace
  M            - Toggle Sound  D           - Toggle Graphics Detail

******************************************************************************/

import model.AsteroidsGameStateHandler;
import model.entity.*;

import java.awt.*;
import java.net.*;
import java.applet.Applet;
import java.applet.AudioClip;

/******************************************************************************
  The AsteroidsSprite class defines a game object, including it's shape,
  position, movement and rotation. It also can detemine if two objects collide.
******************************************************************************/


/******************************************************************************
  Main applet code.
******************************************************************************/

public class AsteroidsController extends Applet implements Runnable, UserInputObserver {

  static final int DELAY = 20;             // Milliseconds between screen and
  static final int FPS   =                 // the resulting frame rate.
          Math.round(1000 / DELAY);

  // Copyright information.
  String copyName = "controller.Asteroids";
  String copyVers = "Version 1.3";
  String copyInfo = "Copyright 1998-2001 by Mike Hall";
  String copyLink = "http://www.brainjar.com";
  String copyText = copyName + '\n' + copyVers + '\n'
                  + copyInfo + '\n' + copyLink;

  Thread loadThread;
  Thread loopThread;
  int numStars;
  Point[] stars;

  // Game data.

  // Flags for game state and options.

  boolean loaded = false;
  boolean paused;
  boolean playing;
  boolean sound;
  boolean detail;

  boolean up;


  // Key flags.

  boolean left  = false;
  boolean right = false;
  boolean down  = false;
//  int photonIndex;    // Index to next available photon sprite.
//
//  // Sound clips.
//  AudioClip crashSound;
//  AudioClip explosionSound;
//  AudioClip fireSound;
//  AudioClip missileSound;
//  AudioClip saucerSound;
//  AudioClip thrustersSound;
//  AudioClip warpSound;

  // Flags for looping sound clips.
  boolean thrustersPlaying;
  boolean saucerPlaying;
  boolean missilePlaying;

  // Counter and total used to track the loading of the sound clips.
  int clipTotal   = 0;
  int clipsLoaded = 0;

  // Off screen image.

  Dimension offDimension;
  Image     offImage;
  Graphics  offGraphics;

  // Data for the screen font.

  Font font = new Font("Helvetica", Font.BOLD, 12);
  FontMetrics fm = getFontMetrics(font);
  int fontWidth = fm.getMaxAdvance();
  int fontHeight = fm.getHeight();

  private AsteroidsGameStateHandler asteroidsGameStateHandler;
  private AsteroidsView asteroidsView;

  public String getAppletInfo() {

    // Return copyright information.
    return(copyText);
  }

  public void init() {
    Dimension d = getSize();

    // Display copyright information.
    System.out.println(copyText);


    // Set up key event handling and set focus to applet window.
    addKeyListener(asteroidsView);
    requestFocus();

    // Save the screen size.
    Entity.setWidth(d.width);
    Entity.setHeight(d.height);

    // TODO: to view
    // Generate the starry background.
    numStars = Entity.getWidth() * Entity.getHeight() / 5000;
    stars = new Point[numStars];
    for (int i = 0; i < numStars; i++)
      stars[i] = new Point((int) (Math.random() * Entity.getWidth()), (int) (Math.random() * Entity.getHeight()));

    sound = true;
    detail = true;
    initView();
    initGame();
    endGame();
  }

  public void initGame() {
    asteroidsGameStateHandler = new AsteroidsGameStateHandler();
    asteroidsGameStateHandler.init();
    playing = true;
    paused = false;
  }

  public void endGame() {

    // Stop ship, flying saucer, guided missile and associated sounds.
    playing = false;
    asteroidsView.stopShipSound();
    asteroidsGameStateHandler.stop();
  }

  @Override
  public void up() {
    up = true;
  }

  @Override
  public void down() {
    down = true;
  }

  @Override
  public void left() {
    left = true;
  }

  @Override
  public void right() {
    right = true;
  }

  @Override
  public void releaseUp() {
    up = false;
  }

  @Override
  public void releaseDown() {
    down = false;
  }

  @Override
  public void releaseLeft() {
    left = false;
  }

  @Override
  public void releaseRight() {
    right = false;
  }


  @Override
  public void firePhoton() {
    asteroidsGameStateHandler.firePhoton();
  }

  @Override
  public void warpShip() {
    asteroidsGameStateHandler.warpShip();
  }

  @Override
  public void pause() {
      paused = !paused;
  }

  @Override
  public void toggleDetails() {
    detail = !detail;
  }

  @Override
  public void toggleStart() {
    if (loaded && !playing)
      initGame();
  }


  public void initThreads() {
    if (loopThread == null) {
      loopThread = new Thread(this);
      loopThread.start();
    }
    if (!loaded && loadThread == null) {
      loadThread = new Thread(this);
      loadThread.start();
    }
  }

  @Override
  public void home() {

  }

  public void stop() {

    if (loopThread != null) {
      loopThread.stop();
      loopThread = null;
    }
    if (loadThread != null) {
      loadThread.stop();
      loadThread = null;
    }
  }

  public void run() {

    long startTime;

    // Lower this thread's priority and get the current time.

    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    startTime = System.currentTimeMillis();

    // Run thread for loading sounds.

    if (!loaded && Thread.currentThread() == loadThread) {
//      loadSounds();
      loaded = true;
      loadThread.stop();
    }

    // This is the main loop.

    while (Thread.currentThread() == loopThread) {



      if (asteroidsGameStateHandler.isGameOver()) {
          endGame();
      }

      if (!paused) {
        asteroidsGameStateHandler.moveShip(left, right, up, down);
        asteroidsGameStateHandler.update();
      }

      asteroidsView.playSounds();
      repaint();

      asteroidsGameStateHandler.resetFlags();
      try {
        startTime += DELAY;
        Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
      }
      catch (InterruptedException e) {
        break;
      }
    }
  }

  public void initView() {


    AudioClip crashSound;
    AudioClip explosionSound;
    AudioClip fireSound;
    AudioClip missileSound;
    AudioClip saucerSound;
    AudioClip thrustersSound;
    AudioClip warpSound;

    // Load all sound clips by playing and immediately stopping them. Update
    // counter and total for display.

    try {
      crashSound     = getAudioClip(new URL(getCodeBase(), "crash.au"));
      clipTotal++;
      explosionSound = getAudioClip(new URL(getCodeBase(), "explosion.au"));
      clipTotal++;
      fireSound      = getAudioClip(new URL(getCodeBase(), "fire.au"));
      clipTotal++;
      missileSound    = getAudioClip(new URL(getCodeBase(), "missile.au"));
      clipTotal++;
      saucerSound    = getAudioClip(new URL(getCodeBase(), "saucer.au"));
      clipTotal++;
      thrustersSound = getAudioClip(new URL(getCodeBase(), "thrusters.au"));
      clipTotal++;
      warpSound      = getAudioClip(new URL(getCodeBase(), "warp.au"));
      clipTotal++;
    }
    catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }

    try {
      crashSound.play();     crashSound.stop();     clipsLoaded++;
      repaint(); Thread.currentThread().sleep(DELAY);
      explosionSound.play(); explosionSound.stop(); clipsLoaded++;
      repaint(); Thread.currentThread().sleep(DELAY);
      fireSound.play();      fireSound.stop();      clipsLoaded++;
      repaint(); Thread.currentThread().sleep(DELAY);
      missileSound.play();    missileSound.stop();    clipsLoaded++;
      repaint(); Thread.currentThread().sleep(DELAY);
      saucerSound.play();    saucerSound.stop();    clipsLoaded++;
      repaint(); Thread.currentThread().sleep(DELAY);
      thrustersSound.play(); thrustersSound.stop(); clipsLoaded++;
      repaint(); Thread.currentThread().sleep(DELAY);
      warpSound.play();      warpSound.stop();      clipsLoaded++;
      repaint(); Thread.currentThread().sleep(DELAY);
    }
    catch (InterruptedException e) {}
    asteroidsView = new AsteroidsView(
            crashSound,
            explosionSound,
            fireSound,
            missileSound,
            saucerSound,
            thrustersSound,
            warpSound
    );

    asteroidsView.init(Entity.getWidth(), Entity.getHeight());
    asteroidsView.setAsteroids(asteroidsGameStateHandler.getAsteroids());
    asteroidsView.setPhotons(asteroidsGameStateHandler.getPhotons());
    asteroidsView.setMissile(asteroidsGameStateHandler.getMisile());
    asteroidsView.setUfo(asteroidsGameStateHandler.getUfo());
    asteroidsView.setShip(asteroidsGameStateHandler.getShip());
  }

//  public void playSounds() {
//
//    if (clipsLoaded < 6) return;
//
//    boolean thrustersOn = asteroidsGameStateHandler.isThrustersOn();
//    boolean newExplosion = asteroidsGameStateHandler.isNewExplosion();
//    boolean firing = asteroidsGameStateHandler.isFiring();
//    boolean collision = asteroidsGameStateHandler.isCollision();
//    boolean warping = asteroidsGameStateHandler.isWarping();
//    boolean ufoPresent = asteroidsGameStateHandler.isUfoPresent();
//    boolean missilePresent = asteroidsGameStateHandler.isMissilePresent();
//
//    if (thrustersOn) {
//      playShipSound();
//    } else {
//      stopShipSound();
//    }
//
//    if (ufoPresent) {
//      playSaucerSound();
//    } else {
//      stopSaucerSound();
//    }
//
//    if (missilePresent) {
//      playMissileSound();
//    } else {
//      stopMissileSound();
//    }
//
//    if (!sound) return;
//
//    if (newExplosion) {
//      explosionSound.play();
//    }
//    if (firing) {
//      fireSound.play();
//    }
//    if (collision) {
//      crashSound.play();
//    }
//    if (warping) {
//      warpSound.play();
//    }
//  }
//
//  private void playMissileSound() {
//    if (missilePlaying) return;
//
//    missilePlaying = true;
//    missileSound.loop();
//  }
//
//  private void stopMissileSound() {
//    missilePlaying = false;
//    missileSound.stop();
//  }
//
//  public void playSaucerSound() {
//    if (saucerPlaying) return;
//
//    saucerPlaying = true;
//    saucerSound.loop();
//  }
//
//  public void stopSaucerSound() {
//    saucerPlaying = false;
//    saucerSound.stop();
//  }
//
//  public void stopShipSound() {
//    if (loaded)
//      thrustersSound.stop();
//    thrustersPlaying = false;
//  }
//
//  public void playShipSound() {
//    if (sound && !paused && !thrustersPlaying) {
//      thrustersSound.loop();
//      thrustersPlaying = true;
//    }
//  }


//  public void keyPressed(KeyEvent e) {
//
//    char c;
//
//    // Check if any cursor keys have been pressed and set flags.
//    if (e.getKeyCode() == KeyEvent.VK_LEFT)
//      left = true;
//    if (e.getKeyCode() == KeyEvent.VK_RIGHT)
//      right = true;
//    if (e.getKeyCode() == KeyEvent.VK_UP)
//      up = true;
//    if (e.getKeyCode() == KeyEvent.VK_DOWN)
//      down = true;
//
//    // Spacebar: fire a photon and start its counter.
//    if (e.getKeyChar() == ' ') {
//        asteroidsGameStateHandler.firePhoton();
//    }
//
//    // Allow upper or lower case characters for remaining keys.
//    c = Character.toLowerCase(e.getKeyChar());
//
//    // 'H' key: warp ship into hyperspace by moving to a random location and
//    // starting counter.
//    if (c == 'h' ) {
//      asteroidsGameStateHandler.warpShip();
//    }
//
//    // 'P' key: toggle pause mode and start or stop any active looping sound
//    // clips.
//
//    if (c == 'p') {
//      if (paused) {
//        if (sound && missilePlaying)
//          missileSound.loop();
//        if (sound && saucerPlaying)
//          saucerSound.loop();
//        if (sound && thrustersPlaying)
//          thrustersSound.loop();
//      }
//      else {
//        if (missilePlaying)
//          missileSound.stop();
//        if (saucerPlaying)
//          saucerSound.stop();
//        if (thrustersPlaying)
//          thrustersSound.stop();
//      }
//      paused = !paused;
//    }
//
//    // 'M' key: toggle sound on or off and stop any looping sound clips.
//
//    if (c == 'm' && loaded) {
//      if (sound) {
//        crashSound.stop();
//        explosionSound.stop();
//        fireSound.stop();
//        missileSound.stop();
//        saucerSound.stop();
//        thrustersSound.stop();
//        warpSound.stop();
//      }
//      else {
//        if (missilePlaying && !paused)
//          missileSound.loop();
//        if (saucerPlaying && !paused)
//          saucerSound.loop();
//        if (thrustersPlaying && !paused)
//          thrustersSound.loop();
//      }
//      sound = !sound;
//    }
//
//    // 'D' key: toggle graphics detail on or off.
//
//    if (c == 'd')
//      detail = !detail;
//
//    // 'S' key: start the game, if not already in progress.
//
//    if (c == 's' && loaded && !playing)
//      initGame();
//
//    // 'HOME' key: jump to web site (undocumented).
//
//    if (e.getKeyCode() == KeyEvent.VK_HOME)
//      try {
//        getAppletContext().showDocument(new URL(copyLink));
//      }
//      catch (Exception excp) {}
//  }

//  public void keyReleased(KeyEvent e) {
//
//    // Check if any cursor keys where released and set flags.
//
//    if (e.getKeyCode() == KeyEvent.VK_LEFT)
//      left = false;
//    if (e.getKeyCode() == KeyEvent.VK_RIGHT)
//      right = false;
//    if (e.getKeyCode() == KeyEvent.VK_UP)
//      up = false;
//    if (e.getKeyCode() == KeyEvent.VK_DOWN)
//      down = false;
//
//    if (!up && !down && thrustersPlaying) {
//      thrustersSound.stop();
//      thrustersPlaying = false;
//    }
//  }

//  public void keyTyped(KeyEvent e) {}

  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics g) {

    Dimension d = getSize();

    if (offGraphics == null || d.width != offDimension.width || d.height != offDimension.height) {
      offDimension = d;
      offImage = createImage(d.width, d.height);
      offGraphics = offImage.getGraphics();
    }

//    Dimension d = getSize();
//    int i;
//    int c;
//    String s;
//    int w, h;
//    int x, y;
//
//    // Create the off screen graphics context, if no good one exists.
//
//    if (offGraphics == null || d.width != offDimension.width || d.height != offDimension.height) {
//      offDimension = d;
//      offImage = createImage(d.width, d.height);
//      offGraphics = offImage.getGraphics();
//    }
//
//    // Fill in background and stars.
//
//    offGraphics.setColor(Color.black);
//    offGraphics.fillRect(0, 0, d.width, d.height);
//    if (detail) {
//      offGraphics.setColor(Color.white);
//      for (i = 0; i < numStars; i++)
//        offGraphics.drawLine(stars[i].x, stars[i].y, stars[i].x, stars[i].y);
//    }
//
//    // Draw photon bullets.
//
//    offGraphics.setColor(Color.white);
//    Photon[] photons = asteroidsGameStateHandler.getPhotons();
//    for (i = 0; i < photons.length; i++)
//      if (photons[i].isActive())
//        offGraphics.drawPolygon(photons[i].getTransformedPolygon());
//
//    // Draw the guided missile, counter is used to quickly fade color to black
//    // when near expiration.
//
//    Missile missile = asteroidsGameStateHandler.getMisile();
//
//    c = Math.min(missile.getMissileCounter() * 24, 255);
//    offGraphics.setColor(new Color(c, c, c));
//    if (missile.isActive()) {
//      offGraphics.drawPolygon(missile.getTransformedPolygon());
//      offGraphics.drawLine(missile.getTransformedPolygon().xpoints[missile.getTransformedPolygon().npoints - 1], missile.getTransformedPolygon().ypoints[missile.getTransformedPolygon().npoints - 1],
//                           missile.getTransformedPolygon().xpoints[0], missile.getTransformedPolygon().ypoints[0]);
//    }
//
//    // Draw the asteroids.
//
//    Asteroid[] asteroids = asteroidsGameStateHandler.getAsteroids();
//
//    for (i = 0; i < asteroids.length; i++)
//      if (asteroids[i].isActive()) {
//        if (detail) {
//          offGraphics.setColor(Color.black);
//          offGraphics.fillPolygon(asteroids[i].getTransformedPolygon());
//        }
//        offGraphics.setColor(Color.white);
//        offGraphics.drawPolygon(asteroids[i].getTransformedPolygon());
//        offGraphics.drawLine(asteroids[i].getTransformedPolygon().xpoints[asteroids[i].getTransformedPolygon().npoints - 1], asteroids[i].getTransformedPolygon().ypoints[asteroids[i].getTransformedPolygon().npoints - 1],
//                             asteroids[i].getTransformedPolygon().xpoints[0], asteroids[i].getTransformedPolygon().ypoints[0]);
//      }
//
//    // Draw the flying saucer.
//
//    Ufo ufo = asteroidsGameStateHandler.getUfo();
//
//    if (ufo.isActive()) {
//      if (detail) {
//        offGraphics.setColor(Color.black);
//        offGraphics.fillPolygon(ufo.getTransformedPolygon());
//      }
//      offGraphics.setColor(Color.white);
//      offGraphics.drawPolygon(ufo.getTransformedPolygon());
//      offGraphics.drawLine(ufo.getTransformedPolygon().xpoints[ufo.
//                      getTransformedPolygon().npoints - 1], ufo.getTransformedPolygon().ypoints[ufo.getTransformedPolygon().npoints - 1],
//                           ufo.getTransformedPolygon().xpoints[0], ufo.getTransformedPolygon().ypoints[0]);
//    }
//
//    // Draw the ship, counter is used to fade color to white on hyperspace.
//
//    Ship ship = asteroidsGameStateHandler.getShip();
//    int hyperCount = asteroidsGameStateHandler.getHyperCount();
//
//    c = 255 - (255 / hyperCount) * ship.getHyperCounter();
//    if (ship.isActive()) {
//      if (detail && ship.getHyperCounter() == 0) {
//        offGraphics.setColor(Color.black);
//        offGraphics.fillPolygon(ship.getTransformedPolygon());
//      }
//      offGraphics.setColor(new Color(c, c, c));
//      offGraphics.drawPolygon(ship.getTransformedPolygon());
//
//
//      offGraphics.drawLine(ship.getTransformedPolygon().xpoints[ship.getTransformedPolygon().npoints - 1], ship.getTransformedPolygon().ypoints[ship.getTransformedPolygon().npoints - 1],
//                           ship.getTransformedPolygon().xpoints[0], ship.getTransformedPolygon().ypoints[0]);
//
//      // Draw thruster exhaust if thrusters are on. Do it randomly to get a
//      // flicker effect.
//
//      if (!paused && detail && Math.random() < 0.5) {
//        if (up) {
//          offGraphics.drawPolygon(ship.getFwdThruster().getTransformedPolygon());
//          offGraphics.drawLine(
//                  ship.getFwdThruster().getTransformedPolygon().xpoints[ship.getFwdThruster().getTransformedPolygon().npoints - 1],
//                  ship.getFwdThruster().getTransformedPolygon().ypoints[ship.getFwdThruster().getTransformedPolygon().npoints - 1],
//                  ship.getFwdThruster().getTransformedPolygon().xpoints[0], ship.getFwdThruster().getTransformedPolygon().ypoints[0]
//          );
//        }
//        if (down) {
//          offGraphics.drawPolygon(ship.getRevThruster().getTransformedPolygon());
//          offGraphics.drawLine(
//                  ship.getRevThruster().getTransformedPolygon().xpoints[ship.getRevThruster().getTransformedPolygon().npoints - 1],
//                  ship.getRevThruster().getTransformedPolygon().ypoints[ship.getRevThruster().getTransformedPolygon().npoints - 1],
//                  ship.getRevThruster().getTransformedPolygon().xpoints[0], ship.getRevThruster().getTransformedPolygon().ypoints[0]
//          );
//        }
//      }
//    }
//
//    // Draw any explosion debris, counters are used to fade color to black.
//
//    int maxScrap = asteroidsGameStateHandler.getMaxScrap();
//    int scrapCount = asteroidsGameStateHandler.getScrapCount();
//    Explosion[] explosions = asteroidsGameStateHandler.getExplosions();
//
//    for (i = 0; i < maxScrap; i++)
//      if (explosions[i].isActive()) {
//        c = (255 / scrapCount) * explosions[i].getCounter();
//        offGraphics.setColor(new Color(c, c, c));
//        offGraphics.drawPolygon(explosions[i].getTransformedPolygon());
//      }
//
//    // Display status and messages.
//
//    offGraphics.setFont(font);
//    offGraphics.setColor(Color.white);
//
//    offGraphics.drawString("Score: " + asteroidsGameStateHandler.getScore(), fontWidth, fontHeight);
//    offGraphics.drawString("Ships: " + ship.getShipsLeft(), fontWidth, d.height - fontHeight);
//    s = "High: " + asteroidsGameStateHandler.getHighScore();
//    offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), fontHeight);
//    if (!sound) {
//      s = "Mute";
//      offGraphics.drawString(s, d.width - (fontWidth + fm.stringWidth(s)), d.height - fontHeight);
//    }
//
//    if (!playing) {
//      s = copyName;
//      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 - 2 * fontHeight);
//      s = copyVers;
//      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 - fontHeight);
//      s = copyInfo;
//      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight);
//      s = copyLink;
//      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + 2 * fontHeight);
//      if (!loaded) {
//        s = "Loading sounds...";
//        w = 4 * fontWidth + fm.stringWidth(s);
//        h = fontHeight;
//        x = (d.width - w) / 2;
//        y = 3 * d.height / 4 - fm.getMaxAscent();
//        offGraphics.setColor(Color.black);
//          offGraphics.fillRect(x, y, w, h);
//        offGraphics.setColor(Color.gray);
//        if (clipTotal > 0)
//          offGraphics.fillRect(x, y,  (w * clipsLoaded / clipTotal), h);
//        offGraphics.setColor(Color.white);
//        offGraphics.drawRect(x, y, w, h);
//        offGraphics.drawString(s, x + 2 * fontWidth, y + fm.getMaxAscent());
//      }
//      else {
//        s = "Game Over";
//        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
//        s = "'S' to Start";
//        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + fontHeight);
//      }
//    }
//    else if (paused) {
//      s = "Game Paused";
//      offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
//    }
//
//    // Copy the off screen buffer to the screen.
    asteroidsView.paint(
            d, offDimension,
            offGraphics,
            offImage,
            asteroidsGameStateHandler.getHyperCount(),
            asteroidsGameStateHandler.getMaxScrap() ,
            asteroidsGameStateHandler.getScrapCount(),
            font, asteroidsGameStateHandler.getScore(),
            fontWidth,
            fontHeight,
            paused,
            asteroidsGameStateHandler.getHighScore(),
            fm,
            playing,
            loaded,
            clipTotal,
            clipsLoaded);
    g.drawImage(offImage, 0, 0, this);

  }
}

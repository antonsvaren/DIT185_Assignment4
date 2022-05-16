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

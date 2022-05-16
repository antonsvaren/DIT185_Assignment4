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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

public class AsteroidsController extends Applet implements Runnable, KeyListener {
  private static final int DELAY = 20;
  // Copyright information.
  private final String copyName = "controller.Asteroids";
  private final String copyVers = "Version 1.3";
  private final String copyInfo = "Copyright 1998-2001 by Mike Hall";
  private final String copyLink = "http://www.brainjar.com";
  private final String copyText = copyName + '\n' + copyVers + '\n'
                  + copyInfo + '\n' + copyLink;

  private Thread loadThread;
  private Thread loopThread;

  private boolean loaded = false;
  private boolean paused = false;
  private boolean playing = false;
  private boolean sound;
  private boolean detail;

  private boolean up;

  private boolean left  = false;
  private boolean right = false;
  private boolean down  = false;

  // Counter and total used to track the loading of the sound clips.
  private int clipTotal   = 0;
  private int clipsLoaded = 0;

  // Off screen image.
  private Dimension offDimension;
  private Image offImage;
  private Graphics offGraphics;

  // Data for the screen font.
  private final Font font = new Font("Helvetica", Font.BOLD, 12);
  private final FontMetrics fm = getFontMetrics(font);
  private final int fontWidth = fm.getMaxAdvance();
  private final int fontHeight = fm.getHeight();

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
    requestFocus();

    // Save the screen size.
    Entity.setWidth(d.width);
    Entity.setHeight(d.height);

    addKeyListener(this);

    sound = true;
    detail = true;

    asteroidsGameStateHandler = new AsteroidsGameStateHandler();
    asteroidsGameStateHandler.init();

    initView();
    endGame();
  }



  public void initGame() {
    asteroidsGameStateHandler.init();
    asteroidsGameStateHandler.setPlaying(true);
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
  public void start() {
    if (loopThread == null) {
      loopThread = new Thread(this);
      loopThread.start();
    }
    if (!loaded && loadThread == null) {
      loadThread = new Thread(this);
      loadThread.start();
    }
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
      loaded = true;
      loadThread.stop();
    }

    // This is the main loop.
    while (Thread.currentThread() == loopThread) {



      if (asteroidsGameStateHandler.isGameOver()) {
          endGame();
      }

      if (playing) {
        asteroidsGameStateHandler.moveShip(left, right, up, down);
      }

      if (!paused) {
        asteroidsGameStateHandler.update();
      }

      if (playing) {
        asteroidsView.playSounds();
      }

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
      loadAudioClip(crashSound);
      loadAudioClip(explosionSound);
      loadAudioClip(fireSound);
      loadAudioClip(missileSound);
      loadAudioClip(saucerSound);
      loadAudioClip(thrustersSound);
      loadAudioClip(warpSound);
    }
    catch (InterruptedException ignored) {}
    asteroidsView = new AsteroidsView(
            crashSound,
            explosionSound,
            fireSound,
            missileSound,
            saucerSound,
            thrustersSound,
            warpSound,
            asteroidsGameStateHandler
    );

    asteroidsView.setCopyInfo(copyInfo);
    asteroidsView.setCopyLink(copyLink);
    asteroidsView.setCopyName(copyName);
    asteroidsView.setCopyVers(copyVers);

  }

  private void loadAudioClip(AudioClip audioClip) throws InterruptedException {
    audioClip.play();
    audioClip.stop();
    clipsLoaded++;
    repaint();
    Thread.currentThread().sleep(DELAY);
  }

  /**
   * Check if any cursor keys have been pressed and set flags.
   */
  public void keyPressed(KeyEvent e) {

    char c;
    if (playing && e.getKeyCode() == KeyEvent.VK_LEFT)
      left = true;
    if (playing && e.getKeyCode() == KeyEvent.VK_RIGHT)
      right = true;
    if (playing && e.getKeyCode() == KeyEvent.VK_UP) {
      up = true;
      asteroidsView.setUp(true);
    }
    if (playing && e.getKeyCode() == KeyEvent.VK_DOWN) {
      down = true;
      asteroidsView.setDown(true);
    }

    // Spacebar: fire a photon and start its counter.
    if (playing && e.getKeyChar() == ' ') {
        asteroidsGameStateHandler.firePhoton();
    }

    // Allow upper or lower case characters for remaining keys.
    c = Character.toLowerCase(e.getKeyChar());

    // 'H' key: warp ship into hyperspace by moving to a random location and
    // starting counter.
    if (playing && c == 'h' ) {
      asteroidsGameStateHandler.warpShip();
    }

    // 'P' key: toggle pause mode and start or stop any active looping sound
    // clips.

    if (playing && c == 'p') {
      if (paused) {
        asteroidsView.unPause();
      }
      else {
        asteroidsView.pause();
      }
      paused = !paused;
    }

    // 'M' key: toggle sound on or off and stop any looping sound clips.

    if (c == 'm' && loaded) {
      if (sound) {
        asteroidsView.soundOff();
      }
      else {
        asteroidsView.soundOn(paused);
      }
      sound = !sound;
    }

    // 'D' key: toggle graphics detail on or off.

    if (c == 'd') {
      detail = !detail;
      asteroidsView.setDetailed(detail);
      asteroidsGameStateHandler.setDetailedExplosions(detail);

    }

    // 'S' key: start the game, if not already in progress.

    if (c == 's' && loaded && !playing)
      initGame();

    // 'HOME' key: jump to web site (undocumented).

    if (e.getKeyCode() == KeyEvent.VK_HOME)
      try {
        getAppletContext().showDocument(new URL(copyLink));
      }
      catch (Exception ignored) {}
  }

  /**
   * Check if any cursor keys where released and set flags.
   */
  public void keyReleased(KeyEvent e) {

    if (e.getKeyCode() == KeyEvent.VK_LEFT)
      left = false;
    if (e.getKeyCode() == KeyEvent.VK_RIGHT)
      right = false;
    if (e.getKeyCode() == KeyEvent.VK_UP) {
      up = false;
      asteroidsView.setUp(false);
    }
    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
      down = false;
      asteroidsView.setDown(false);
    }
  }

  public void update(Graphics g) {
    paint(g);
  }

  /**
   * Update image.
   */
  public void paint(Graphics g) {
    Dimension d = getSize();

    if (offGraphics == null || d.width != offDimension.width || d.height != offDimension.height) {
      offDimension = d;
      offImage = createImage(d.width, d.height);
      offGraphics = offImage.getGraphics();
    }

    // Copy the off screen buffer to the screen.
    asteroidsView.paint(
            d,
            offGraphics,
            font,
            fontWidth,
            fontHeight,
            paused,
            fm,
            playing,
            loaded,
            clipTotal,
            clipsLoaded);
    g.drawImage(offImage, 0, 0, this);
  }

  @Override
  public void keyTyped(KeyEvent keyEvent) {

  }
}

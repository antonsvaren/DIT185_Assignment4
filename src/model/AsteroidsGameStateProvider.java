package model;

import model.entity.*;

public interface AsteroidsGameStateProvider {
    boolean isGameOver();

    boolean isThrustersOn();

    Photon[] getPhotons();

    Missile getMissile();

    Asteroid[] getAsteroids();

    Ufo getUfo();

    Ship getShip();

    int getHyperCount();

    int getMaxScrap();

    Explosion[] getExplosions();

    int getScrapCount();

    int getScore();

    int getHighScore();

    boolean isNewExplosion();

    boolean isFiring();

    boolean isCollision();

    boolean isWarping();

    boolean isUfoPresent();

    boolean isMissilePresent();
}

public interface UserInputObserver {
    void up();
    void down();
    void left();
    void right();
    void releaseUp();
    void releaseDown();
    void releaseLeft();
    void releaseRight();
    void firePhoton();
    void warpShip();
    void pause();
    void toggleDetails();
    void toggleStart();
    void home();
}

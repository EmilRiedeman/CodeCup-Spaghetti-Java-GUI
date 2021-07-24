package spaghetti.game;

public interface BoardListener {
    void registerMove(Move m, BoardListener l);
    void start();
    void close();
    boolean isStartHandler();
    String getControllerName();
}

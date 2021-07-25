package spaghetti.game;

public interface BoardListener {
    void registerMove(Move m, BoardListener l);
    void announceControllers(BoardController c1, BoardController c2);
    void onGameStart(); // occurs after setSide(side) of BoardController
    void close();
    boolean isStartHandler();
}

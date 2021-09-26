package spaghetti.game;

public interface BoardListener {
    void registerMove(Move m, BoardListener l);
    void announceControllers(BoardController c1, BoardController c2);
    void onBoardStateChange(BoardState newState);
    boolean isStartHandler();
}

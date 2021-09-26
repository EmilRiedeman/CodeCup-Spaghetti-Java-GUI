package spaghetti.gui.swing;

import spaghetti.game.BoardController;
import spaghetti.game.BoardListener;
import spaghetti.game.BoardState;
import spaghetti.game.Move;

public class MouseInputBoardController extends BoardController {

    public final GraphicalBoard gb;

    protected MouseInputBoardController(String name, GraphicalBoard gb) {
        super(name, gb.board);
        this.gb = gb;
    }

    @Override
    public void registerMove(Move m, BoardListener l) {
    }

    @Override
    public void announceControllers(BoardController c1, BoardController c2) {
    }

    @Override
    public void onBoardStateChange(BoardState newState) {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isStartHandler() {
        return false;
    }
}

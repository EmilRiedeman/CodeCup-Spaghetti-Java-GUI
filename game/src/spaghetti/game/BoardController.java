package spaghetti.game;

public abstract class BoardController implements BoardListener {

    protected final Board board;
    protected String name;
    private boolean side;

    protected BoardController(String name, Board board) {
        this.name = name;
        this.board = board;
        board.addBoardListener(this);
    }

    protected BoardController(Board board) {
        this("-", board);
    }

    public String getName() {
        return name;
    }

    public boolean getSide() {
        return side;
    }

    public void setSide(boolean side) { // occurs after pre played moves
        this.side = side;
    }

    public abstract void close();
}

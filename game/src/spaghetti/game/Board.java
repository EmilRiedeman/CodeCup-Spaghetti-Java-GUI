package spaghetti.game;

import spaghetti.utils.Pair;
import spaghetti.utils.Triplet;

import java.util.*;

/************************************************************
 * The play function is written by Ludo Pulles in javascript
 * and is translated to Java.
 * https://www.codecup.nl/spaghetti/sample_game.php
 ***********************************************************/
public class Board {
    public static class Position extends Triplet<Integer, Integer, Integer> {
        public Position(Integer a, Integer b, Integer c) {
            super(a, b, c);
        }

        @Override
        public Position copy() {
            return new Position(a, b, c);
        }
    }
    public static class Point extends Pair<Integer, Integer> {
        public Point(Integer a, Integer b) {
            super(a, b);
        }

        @Override
        public Point copy() {
            return new Point(a, b);
        }
    }

    public final int width, height;
    public final Tile[][] tiles;
    public final int[] scores = {50, 50};
    protected final Set<BoardListener> listeners = new HashSet<>();
    protected final BoardController[] controllers = new BoardController[2];
    protected boolean turn = false;
    protected int moveCount = 0;
    protected Pair<Integer, Integer> lastMove = null;
    protected BoardState currentState = BoardState.PRE_START;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        tiles = new Tile[height][width];
        for (Tile[] t : tiles) {
            for (int i = 0; i < width; ++i) t[i] = new Tile();
        }
    }

    public Board(int width, int height, Move[] moves) {
        this(width, height);
        for (Move m : moves) play(m, null);
    }

    public void addBoardListener(BoardListener l) {
        listeners.add(l);
    }

    public void removeBoardListener(BoardListener l) {
        listeners.remove(l);
        if (controllers[0] == l || controllers[1] == l) close();
    }

    public void close() {
        if (currentState != BoardState.OVER) {
            System.err.println("Closed Game");
            setCurrentState(BoardState.OVER);
            for (int i = 0; i < 2; ++i) {
                try {
                    if (controllers[i] != null) controllers[i].close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public BoardState getCurrentState() {
        return currentState;
    }

    protected void setCurrentState(BoardState state) {
        currentState = state;
        for (BoardListener l : listeners) l.onBoardStateChange(state);
    }

    public int getMoveCount() {
        return moveCount;
    }

    public Pair<Integer, Integer> getLastMove() {
        return lastMove;
    }

    public boolean getTurn() {
        return turn;
    }

    public BoardController getControllerTurn() {
        return controllers[turn? 1: 0];
    }

    public BoardController getController(boolean c) {
        return controllers[c? 1: 0];
    }

    public BoardListener[] getBoardListeners() {
        return listeners.toArray(new BoardListener[0]);
    }

    public boolean isOccupied(int row, int col) {
        return tiles[row][col].type != '\0';
    }

    // sides: 0=N, 1=E, 2=S, 3=W
    private static final Map<Character, Integer[]> sides = new HashMap<Character, Integer[]>() {{
        put('l', new Integer[]{0, 3, 1, 2});
        put('s', new Integer[]{0, 2, 1, 3});
        put('r', new Integer[]{3, 2, 1, 0});
    }}, nrs = new HashMap<Character, Integer[]>() {{
        put('l', new Integer[]{0, 1, 1, 0});
        put('s', new Integer[]{0, 1, 0, 1});
        put('r', new Integer[]{0, 0, 1, 1});
    }}, perm = new HashMap<Character, Integer[]>(){{
        put('l', new Integer[]{1, 0, 3, 2});
        put('s', new Integer[]{0, 1, 2, 3});
        put('r', new Integer[]{3, 2, 1, 0});
    }};

    private static final int[]
            dx = new int[]{1, 0, -1, 0},
            dy = new int[]{0, -1, 0, 1};

    public boolean inBoard(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    private Position stepTile(Position pos) {
        int d = perm.get(tiles[pos.a][pos.b].type)[pos.c];
        return new Position(pos.a + dx[d], pos.b + dy[d], d);
    }

    private Pair<Integer, Integer> simulate(Position pos) {
        Position p = pos.copy();
        int len = 0;
        while (inBoard(p.a, p.b) && tiles[p.a][p.b].type != '\0') {
            p = stepTile(p);
            len++;
            if (p.equals(pos)) return new Pair<>(-1, 0); // cycle
        }
        if (inBoard(p.a, p.b)) return new Pair<>(-2, 0);

        if (p.b < 0) return new Pair<>(len, 1);
        if (p.b >= width) return new Pair<>(len, 2);
        return new Pair<>(0, 5);
    }

    private boolean visits(Position pos, Position opt1, Position opt2) {
        Position p = pos.copy();
        while (inBoard(p.a, p.b) && tiles[p.a][p.b].type != '\0') {
            p = stepTile(p);
            if (p.equals(opt1) || (p.equals(opt2))) return true;
            if (p.equals(pos)) return false;
        }
        return false;
    }

    private void colorBoth(Position pos0, Position pos1, int color) {
        if (color != 5)
            color += 2;
        Position p = pos0.copy();
        boolean cyc = false;
        while (inBoard(p.a, p.b) && tiles[p.a][p.b].type != '\0') {
            Tile tile = tiles[p.a][p.b];
            int n = nrs.get(tile.type)[p.c];
            tile.color[n] = color;

            p = stepTile(p);
            if (p.equals(pos0)) { cyc = true; break; }
        }
        p = pos1.copy();
        if (!cyc) while (inBoard(p.a, p.b) && tiles[p.a][p.b].type != '\0') {
            Tile tile = tiles[p.a][p.b];
            int n = nrs.get(tile.type)[p.c];
            tile.color[n] = color;

            p = stepTile(p);
        }
    }

    public void announceControllers(BoardController c1, BoardController c2) {
        for (BoardListener l : listeners) l.announceControllers(c1, c2);
    }

    public void start(BoardListener startHandler, BoardController blue, BoardController red) {
        assert startHandler.isStartHandler();
        privateStart(blue, red);
    }

    private void privateStart(BoardController blue, BoardController red) {
        lastMove = null;
        controllers[0] = blue;
        controllers[1] = red;
        if (!blue.isStartHandler()) blue.setSide(false);
        if (!red.isStartHandler()) red.setSide(true);
        setCurrentState(BoardState.RUNNING);
    }

    public void start(boolean prePlayedMoves, BoardController blue, BoardController red) {
        if (prePlayedMoves && width >= 4 && height >= 4) {
            Random rand = new Random();
            Move m1 = generatePreMove(rand); play(m1, null);
            Move m2 = generatePreMove(rand); play(m2, null);
            System.err.println("Pre Played Moves: " + m1 + ", " + m2);
        }
        privateStart(blue, red);
    }

    public Move generatePreMove(Random rand) {
        Point[] pos = new Point[width * height];
        int size = 0;
        for (int i = 1; i < height-1; ++i) for (int j = 1; j < width-1; ++j) {
            Point p = new Point(i, j);
            if (!isOccupied(p.a, p.b))
                pos[size++] = p;
        }
        Point m = pos[rand.nextInt(size)];
        char[] t = new char[] {'l', 's', 'r'};
        return new Move(m.a, m.b, t[rand.nextInt(3)]);
    }

    public void play(Move move, BoardListener player) {
        moveCount++;
        lastMove = new Pair<>(move.row, move.col);
        tiles[move.row][move.col].type = move.t;

        Position[] pos4 = new Position[4];
        for (int i=0; i<4; i++)
            pos4[i] = new Position(move.row, move.col, sides.get(move.t)[i]);

        for (int i=0; i<2; i++) {
            Position pos0 = pos4[2*i], pos1 = pos4[2*i+1];

            Pair<Integer, Integer> outcome0 = simulate(pos0), outcome1 = simulate(pos1);
            if (outcome0.a == -2 && outcome1.b != 5) continue;
            if (outcome1.a == -2 && outcome0.b != 5) continue;

            boolean choose_best = i == 0 && (visits(pos0, pos4[2], pos4[3]) || visits(pos1, pos4[2], pos4[3]));

            // color: current player
            int color = (turn? 2 : 1);
            if (outcome0.a == -1 || outcome1.a == -1) {
                colorBoth(pos0, pos1, color);
                // cycle penalty:
                scores[turn? 1: 0] -= 5;
            } else if (outcome0.b == 5 || outcome1.b == 5) {
                colorBoth(pos0, pos1, 5);
            } else if (outcome0.b.equals(outcome1.b)) {
                colorBoth(pos0, pos1, color);
                // same side penalty:
                scores[turn? 1: 0] -= 3;
            } else {
                if (choose_best) {
                    int len_01 = outcome0.b == color ? outcome0.a : outcome1.a;
                    Pair<Integer, Integer> outcome2 = simulate(pos4[2]), outcome3 = simulate(pos4[3]);

                    int len_23 = outcome2.b == color ? outcome2.a : outcome3.a;

                    if (len_23 > len_01) {
                        i = 1;
                        pos0 = pos4[2];
                        pos1 = pos4[3];
                        outcome0 = outcome2;
                    }
                }

                // Coloring one half color and other gray
                Position p = ((outcome0.b == color) ? pos0 : pos1).copy();
                while (inBoard(p.a, p.b)) {
                    scores[turn? 1: 0]++;

                    Tile tile = tiles[p.a][p.b];
                    int n = nrs.get(tile.type)[p.c];
                    tile.color[n] = color;
                    p = stepTile(p);
                }
                Position q = stepTile( (outcome0.b == color) ? pos1 : pos0 );
                while (inBoard(q.a, q.b)) {
                    Tile tile = tiles[q.a][q.b];
                    int n = nrs.get(tile.type)[q.c];
                    tile.color[n] = 5;
                    q = stepTile(q);
                }
            }
            if (choose_best) break;
        }

        if (moveCount == width * height) close();
        turn = !turn;
        for (BoardListener listener : listeners) listener.registerMove(move, player);
    }
}

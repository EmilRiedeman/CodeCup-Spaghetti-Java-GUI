package spaghetti.game;

import java.io.Serializable;

public class Move implements Serializable {
    public int row, col;
    public char t;

    public Move(String str) {
        row = str.charAt(0) - 'a';
        col = str.charAt(1) - 'a';
        t = str.charAt(2);
    }

    public Move(int row, int col, char t) {
        this.row = row;
        this.col = col;
        this.t = t;
    }

    @Override
    public String toString() {
        return "" + ((char)(row + 'a')) + ((char) (col + 'a')) + t;
    }
}

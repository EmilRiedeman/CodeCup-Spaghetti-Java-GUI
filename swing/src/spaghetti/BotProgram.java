package spaghetti;

import spaghetti.game.Board;
import spaghetti.game.BoardListener;
import spaghetti.game.Move;

import java.io.*;

public class BotProgram implements BoardListener {
    protected BufferedReader stdout;
    protected BufferedWriter stdin;
    protected Process process;
    public final String cmd;
    public final Board board;

    public BotProgram(String command, Board board) {
        System.err.println(command);
        this.board = board;
        board.addBoardListener(this);

        cmd = command;

        setup();
        if (process != null) listen();
    }

    public void setup() {
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e){
            System.err.println(e.getMessage());
            return;
        }
        stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    }

    public void listen() {
        new Thread(() -> {
            while (process.isAlive()) step();
        }).start();
    }

    public void step() {
        String in;
        try {
            in = stdout.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            process.destroy();
            return;
        }
        try {
            Move move = new Move(in);
            if (board.inBoard(move.row, move.col) && !board.isOccupied(move.row, move.col)) {
                board.play(move, this);
            }
            else throw new NullPointerException();
        } catch (NullPointerException e) {
            if (board.isRunning()) {
                System.err.println("Bot Program sends wrong move.");
            }
            process.destroy();
        }
    }

    @Override
    public void registerMove(Move m, BoardListener l) {
        if (l != this) {
            try {
                stdin.write(m.toString() + "\n");
                stdin.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() {
        try {
            stdin.write("Start\n");
            stdin.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            stdin.write("Quit\n");
            stdin.flush();
        } catch (IOException e) {
            process.destroyForcibly();
            return;
        }
        process.destroy();
    }

    @Override
    public boolean isStartHandler() {
        return false;
    }

    @Override
    public String getControllerName() {
        return "Bot";
    }
}

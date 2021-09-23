package spaghetti;

import spaghetti.game.*;

import java.io.*;

public class BotProgram extends BoardController {

    protected BufferedReader stdout;
    protected BufferedWriter stdin;
    protected Process process;
    protected boolean side;
    public final String cmd;

    public BotProgram(String name, String command, Board board) {
        super(name, board);
        System.err.println(command);
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
            process.destroyForcibly();
            return;
        }
        try {
            Move move = new Move(in);
            if (board.inBoard(move.row, move.col) && !board.isOccupied(move.row, move.col)) {
                board.play(move, this);
            }
            else throw new NullPointerException();
        } catch (NullPointerException e) {
            if (board.getCurrentState() == BoardState.RUNNING) {
                System.err.println("Bot Program sends wrong move.");
            }
            process.destroyForcibly();
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
    public void announceControllers(BoardController c1, BoardController c2) {
    }

    @Override
    public void onGameStart() {
        if (side) return;
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
        process.destroyForcibly();
    }

    @Override
    public boolean isStartHandler() {
        return false;
    }

    @Override
    public void setSide(boolean side) {
        this.side = side;
    }
}

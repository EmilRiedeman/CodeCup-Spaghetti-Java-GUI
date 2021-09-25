package spaghetti;

import spaghetti.game.*;

import java.io.*;

public class BotProgram extends BoardController {

    protected BufferedReader stdout;
    protected BufferedWriter stdin;
    protected BufferedReader stderr;
    protected final File logFile;
    protected Process process;
    protected boolean side;
    public final String cmd;

    public BotProgram(String name, String command, Board board, File logFile) {
        super(name, board);
        System.err.println(command);
        board.addBoardListener(this);

        this.logFile = logFile;
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
        if (logFile == null || logFile.isDirectory()) return;
        try {
            if (!logFile.createNewFile()) System.err.println("Overwriting \"" + logFile.getAbsolutePath() + "\"");
        } catch (IOException e) {
            System.err.println("Log file creation failed!");
            e.printStackTrace();
            return;
        }
        stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        new Thread(() -> {
            FileWriter writer;
            try {
                writer = new FileWriter(logFile);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            while (board.getCurrentState() != BoardState.CLOSED && board.getCurrentState() != BoardState.OVER) {
                try {
                    writer.write(stderr.read());
                } catch (IOException e) {
                    break;
                }
            }
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Failed to close log file.");
                e.printStackTrace();
            }
        }).start();
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
            close();
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
                close();
            }
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
                close();
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
        if (board.getCurrentState() != BoardState.CLOSED) {
            board.removeBoardListener(this);
        }
        try {
            stdin.write("Quit\n");
            stdin.flush();
        } catch (IOException ignored) {
        }

        try {
            if (stderr != null) stderr.close();
            stdout.close();
            stdin.close();
        } catch (IOException ignored) {
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

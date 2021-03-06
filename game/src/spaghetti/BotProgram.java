package spaghetti;

import spaghetti.game.*;

import javax.swing.*;
import java.io.*;

public class BotProgram extends BoardController {

    protected BufferedReader stdout;
    protected BufferedWriter stdin;
    protected BufferedReader stderr;
    protected final File logFile;
    protected Process process;
    protected boolean side;
    public final String[] cmd;

    public BotProgram(String name, String[] command, Board board, File logFile) {
        super(name, board);
        board.addBoardListener(this);

        this.logFile = logFile;
        cmd = command;

        setup();
        if (process != null) listen();
    }

    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    public void setup() {
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e){
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage());
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
            while (board.getCurrentState() != BoardState.OVER) {
                try {
                    int r = stderr.read();
                    if (r == -1) break;
                    writer.write(r);
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
            if (board.getCurrentState() == BoardState.RUNNING) {
                JOptionPane.showMessageDialog(null, "Bot " + name + " crashed.");
                board.close();
            }
            return;
        }
        try {
            Move move = new Move(in);
            if (board.inBoard(move.row, move.col) && board.getControllerTurn() == this && !board.isOccupied(move.row, move.col)) {
                board.play(move, this);
            }
            else throw new NullPointerException();
        } catch (NullPointerException e) {
            if (board.getCurrentState() == BoardState.RUNNING) {
                String msg = "Bot " + name + " sends wrong move or was exited too early.";
                System.err.println(msg);
                JOptionPane.showMessageDialog(null, msg);
                board.close();
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
                if (board.getCurrentState() != BoardState.OVER) e.printStackTrace();
                board.close();
            }
        }
    }

    @Override
    public void announceControllers(BoardController c1, BoardController c2) {
    }

    @Override
    public void onBoardStateChange(BoardState newState) {
        if (newState == BoardState.RUNNING) {
            if (side) return;
            try {
                stdin.write("Start\n");
                stdin.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (newState == BoardState.OVER) close();
    }

    @Override
    public void close() {
        if (!process.isAlive()) return;
        try {
            stdin.write("Quit\n");
            stdin.flush();
        } catch (IOException ignored) {
        }

        process.destroyForcibly();
        System.err.println("Bot " + name + " was destroyed.");
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

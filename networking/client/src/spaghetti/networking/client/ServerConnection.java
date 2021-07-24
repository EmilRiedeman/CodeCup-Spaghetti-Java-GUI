package spaghetti.networking.client;

import spaghetti.game.Board;
import spaghetti.game.BoardListener;
import spaghetti.game.Move;
import spaghetti.networking.ServerCommand;

import java.io.*;
import java.net.Socket;

public class ServerConnection implements BoardListener, Runnable {

    protected boolean connected = false;
    public final Board board;
    public Socket socket;
    public ObjectInputStream in;
    public ObjectOutputStream out;

    public ServerConnection(String address, int port, Board b) {
        try {
            socket = new Socket(address, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            board = null;
            e.printStackTrace();
            return;
        }

        board = b;
        b.addBoardListener(this);

        connected = true;
        System.out.printf("Connected to %s:%s%n", address, port);

        new Thread(this).start();
    }

    public void send(Serializable data) {
        try {
            out.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
            quit();
        }
    }

    public void quit() {
        System.err.println("QUITING");
        connected = false;
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerMove(Move m, BoardListener l) {
        if (l != this) send(m);
    }

    @Override
    public void start() {

    }

    @Override
    public void close() {
        quit();
    }

    @Override
    public boolean isStartHandler() {
        return true;
    }

    @Override
    public String getControllerName() {
        return "Server";
    }

    @Override
    public void run() {
        while (connected) {
            try {
                Object data = in.readObject();
                System.out.println("received: " + data);
                if (data instanceof ServerCommand) {
                    switch ((ServerCommand) data) {
                        case START:
                            if (board.getControllerTurn() == this) board.swapControllers();
                            board.getControllerTurn().start();
                            break;
                        case QUIT:
                            quit();
                            break;
                    }
                } else if (data instanceof Move) {
                    board.gameStarted = true;
                    if (board.getMoveCount() == 2 && board.getControllerTurn() != this) board.swapControllers();
                    board.play((Move) data, this);
                }
            } catch(IOException | ClassNotFoundException i) {
                i.printStackTrace();
                quit();
            }
        }
    }
}

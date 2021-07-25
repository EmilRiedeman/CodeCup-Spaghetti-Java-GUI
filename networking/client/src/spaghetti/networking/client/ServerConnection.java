package spaghetti.networking.client;

import spaghetti.game.Board;
import spaghetti.game.BoardController;
import spaghetti.game.BoardListener;
import spaghetti.game.Move;
import spaghetti.networking.ServerPacketType;

import java.io.*;
import java.net.Socket;

public class ServerConnection extends BoardController implements Runnable {

    protected boolean connected = false;
    public Socket socket;
    public ObjectInputStream in;
    public ObjectOutputStream out;

    public ServerConnection(String address, int port, Board b) {
        super(b);
        try {
            socket = new Socket(address, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        board.addBoardListener(this);

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
    public void announceControllers(BoardController c1, BoardController c2) {
        if (c1 != this && c2 != this) return;
        send(c1 == this? c2.getName(): c1.getName());
    }

    @Override
    public void onGameStart() {
        assert false;
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
    public void setSide(boolean side) {
        super.setSide(side);
    }

    @Override
    public void run() {
        while (connected) {
            try {
                Object data = in.readObject();
                System.out.println("received: " + data);
                if (data instanceof ServerPacketType) {
                    switch ((ServerPacketType) data) {
                        case SIDE:
                            boolean otherSide = (boolean)in.readObject(); // false=blue true=red
                            setSide(!otherSide);
                            boolean started = false;
                            for (BoardListener l : board.getBoardListeners()) {
                                if (l instanceof BoardController && l != this) {
                                    BoardController c = (BoardController) l;
                                    board.start(this, otherSide? this: c, otherSide? c: this);
                                    started = true;
                                    break;
                                }
                            }
                            if (!started) quit();
                            break;
                        case NAMES:
                            String[] names = (String[])in.readObject();
                            name = names[getSide()? 1: 0];
                            break;
                        case QUIT:
                            quit();
                            break;
                    }
                } else if (data instanceof Move) {
                    Move m = (Move) data;
                    if (board.isOccupied(m.row, m.col)) quit();
                    else board.play((Move) data, this);
                }
            } catch(IOException | ClassNotFoundException i) {
                i.printStackTrace();
                quit();
            }
        }
    }
}

package spaghetti.networking.server;

import spaghetti.game.BoardController;
import spaghetti.game.BoardListener;
import spaghetti.game.BoardState;
import spaghetti.game.Move;
import spaghetti.networking.ClientPacketType;
import spaghetti.networking.ServerPacketType;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientConnection extends BoardController {

    protected boolean connected = true;
    public final Socket socket;
    public final Server server;
    public final ObjectInputStream in;
    public final ObjectOutputStream out;

    public ClientConnection(Server server) throws IOException {
        super(server.board);
        this.server = server;
        server.connections.add(this);
        socket = server.socket.accept();
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        try {
            name = (String) in.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Sent name is not a String");
            quit();
        }
        System.out.println(this + " connected.");
    }

    public void send(Serializable... s) {
        System.err.println(this + " <- " + Arrays.toString(s));
        try {
            for (Serializable p : s)
                out.writeObject(p);
        } catch (IOException e) {
            quit();
        }
    }

    @Override
    public void registerMove(Move m, BoardListener l) {
        if (l != this) {
            send(m);
        }
    }

    @Override
    public void announceControllers(BoardController c1, BoardController c2) {
        send(ServerPacketType.NAMES, new String[]{c1.getName(), c2.getName()});
    }

    @Override
    public void onBoardStateChange(BoardState newState) {
        if (newState == BoardState.RUNNING) send(ServerPacketType.START_GAME);
    }

    @Override
    public String toString() {
        return String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());
    }

    @Override
    public void close() {
        send(ServerPacketType.QUIT);
    }

    @Override
    public boolean isStartHandler() {
        return false;
    }

    @Override
    public void setSide(boolean side) {
        super.setSide(side);
        send(ServerPacketType.SIDE, side);
    }

    public void quit() {
        if (connected) System.out.println(this + " disconnected.");
        connected = false;
        server.connections.remove(this);
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean play() {
        try {
            Move m = (Move) in.readObject();
            System.err.println(this + " -> " + m);
            if (board.getControllerTurn() == this && m != null && board.inBoard(m.row, m.col)) {
                board.play(m, this);
            } else throw new Exception();
        } catch (Exception e) {
            quit();
            return false;
        }
        return true;
    }
}

package spaghetti.networking.server;

import spaghetti.game.BoardListener;
import spaghetti.game.Move;
import spaghetti.networking.ServerCommand;

import java.io.*;
import java.net.Socket;

public class ClientConnection implements BoardListener {

    public final Socket socket;
    public final Server server;
    public final ObjectInputStream in;
    public final ObjectOutputStream out;

    public ClientConnection(Server server) throws IOException {
        this.server = server;
        server.connections.add(this);
        socket = server.socket.accept();
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        System.out.println(this + " connected.");
    }

    public void send(Serializable s) {
        System.err.println(this + " <- " + s);
        try {
            out.writeObject(s);
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
    public void start() {
        send(ServerCommand.START);
    }

    @Override
    public String toString() {
        return String.format("%s:%d", socket.getInetAddress().getHostAddress(), socket.getPort());
    }

    @Override
    public void close() {
        send(ServerCommand.QUIT);
    }

    @Override
    public boolean isStartHandler() {
        return false;
    }

    @Override
    public String getControllerName() {
        return "Client";
    }

    public void quit() {
        server.connections.remove(this);
        System.out.println(this + " disconnected.");
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
            if (server.board.getControllerTurn() == this && m != null && server.board.inBoard(m.row, m.col)) {
                server.board.play(m, this);
            } else throw new Exception();
        } catch (Exception e) {
            quit();
            return false;
        }
        return true;
    }
}

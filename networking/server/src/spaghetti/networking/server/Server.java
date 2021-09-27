package spaghetti.networking.server;

import spaghetti.game.Board;
import spaghetti.game.BoardState;
import spaghetti.networking.ServerPacketType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {

    public final ServerSocket socket = new ServerSocket();
    public final Board board = new Board(7, 9);
    public boolean running;
    public final List<ClientConnection> connections = new ArrayList<>();

    public Server(String address, int port) throws IOException {
        socket.bind(new InetSocketAddress(address, port));
        System.out.printf("Server started on %s:%d%n", address, port);
        running = true;
    }

    public void start(boolean prePlayedMoves) {
        board.addBoardListener(connections.get(0));
        board.addBoardListener(connections.get(1));
        board.start(prePlayedMoves, connections.get(0), connections.get(1));
        board.announceControllers(board.getController(false), board.getController(true));
    }

    public void waitForPlayers() {
        while (connections.size() < 2) {
            try {
                new ClientConnection(this);
            } catch (IOException e) {
                e.printStackTrace();
                running = false;
                return;
            }
            for (ClientConnection c : connections.toArray(new ClientConnection[0])) {
                c.send(ServerPacketType.TEST_CONNECTION);
            }
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        board.close();
        for (ClientConnection connection : connections.toArray(new ClientConnection[0])) {
            connection.quit();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Your IPv4 Address: " + InetAddress.getLocalHost().getHostAddress());
        System.out.print("Server Address: ");
        final String address = scanner.nextLine();
        System.out.print("Server Port:    ");
        int port = scanner.nextInt();

        System.out.print("Pre Played Moves (true/false): ");
        boolean prePlayed = scanner.nextBoolean();

        System.out.print("Loop (true/false): ");
        boolean loop = scanner.nextBoolean();

        do {
            try {
                Server server = new Server(address, port);

                server.waitForPlayers();
                System.out.println("Players found.");
                server.start(prePlayed);

                while (server.board.getCurrentState() == BoardState.RUNNING) {
                    if (!((ClientConnection) server.board.getControllerTurn()).play()) break;
                }
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        } while (loop);
    }
}

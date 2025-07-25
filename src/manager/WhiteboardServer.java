package manager;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Class: WhiteboardServer
 * ----------------------------
 * This class launches the server for the distributed whiteboard application.
 * It continuously listens on the specified port for client connection requests.
 * For each new client, it creates a dedicated ClientConnection thread and maintains
 * a list of all active clientConnections and usernames.
 */
public class WhiteboardServer {


    public static List<ClientConnection> clientConnections = new ArrayList<>();
    public static List<String> usernames = new ArrayList<>();
    private static int clientID = 0;

    /**
     * Launches the server on the given port and waits for client clientConnections.
     * When a client connects, a new ClientConnection thread is created and started.
     * @param port     Port number the server should listen on
     * @param username Username of the initial user (typically the manager)
     */
    protected static void launch(int port, String username) {
        ServerSocket server;
        Socket client;

        try {
            server = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            usernames.add(username);

            while (true) {
                client = server.accept();
                clientID++;
                System.out.println("Client #" + clientID + " connected.");

                ClientConnection clientConnection = new ClientConnection(client);
                clientConnections.add(clientConnection);
                clientConnection.start();
            }
        } catch (Exception e) {
            System.out.println("Server error: Unable to accept connections.");
            System.exit(1);
        }
    }
}

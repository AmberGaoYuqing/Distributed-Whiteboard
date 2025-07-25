package manager;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * ClientConnection class
 * ----------------------
 * Handles communication with a single connected client in the shared whiteboard system.
 * Each instance runs in its own thread, listening for and processing client commands such as:
 * - drawing updates
 * - join requests
 * - chat messages
 * - session termination
 */
public class ClientConnection extends Thread {

    public Socket socket;
    public String name;

    public DataInputStream inputStream;
    public DataOutputStream outputStream;

    public boolean kick = false;

    /**
     * Constructor
     * @param socket the socket associated with this client connection
     */
    public ClientConnection(Socket socket) {
        this.socket = socket;
    }

    /**
     * Thread logic to handle incoming messages and process commands.
     * Supported commands include: begin, request, draw, over, chat, drawlog
     */
    public void run() {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            String message;

            label:
            while ((message = inputStream.readUTF()) != null) {
                System.out.println("[Server] Received message from client " + this.name + ": " + message);
                String[] tokens = message.split(" ", 2);
                String command = tokens[0];
                String content = tokens.length > 1 ? tokens[1] : "";

                switch (command) {
                    case "begin":
                        handleBegin();
                        break;

                    case "request":
                        handleJoinRequest(content);
                        break;

                    case "draw":
                        System.out.println("[Manager received draw message]: " + message);
                        ConnectionManager.broadcastToOthers(this, message);
                        ConnectionManager.canvasRepaint(content);
                   
                        break;

                    case "over":
                        socket.close(); // Client disconnect
                        break label;

                    case "chat":
                        ConnectionManager.broadcast(message);
                        Manager.chatArea.append(content + "\n");
                        break;

                    case "drawlog":
                        ConnectionManager.broadcastDrawLog(content, "drawing");
                        break;
                }
            }
        } catch (SocketException e) {
            System.out.println("User " + this.name + " connection interrupted.");
            if (!this.kick) ConnectionManager.handleClientLeft(this);
        } catch (Exception e) {
            System.out.println("User " + this.name + " connection error.");
        }
    }

    /**
     * Handles the "begin" command from a newly joined client.
     * Sends drawing history and current user list to synchronize state.
     */
    private void handleBegin() throws IOException {
        ArrayList<String> drawingHistory = ManagerLogin.managerBoard.listenerRecord.getRecord();
        ConnectionManager.broadcastBatch(drawingHistory);

        String userList = "userJList " + String.join(",", WhiteboardServer.usernames);
        String[] tokens = userList.split(" ", 2);
        if (tokens.length == 2) {
            String[] clients = tokens[1].split(",");
            ConnectionManager.addUser(clients);
        }
        ConnectionManager.broadcast(userList);
    }

    /**
     * Handles a join request from a client.
     * Validates the username and asks the manager for approval.
     *
     * @param requestedName the username the client wants to use
     */
    private void handleJoinRequest(String requestedName) throws IOException {
        this.name = requestedName;

        if (WhiteboardServer.usernames.contains(requestedName)) {
            outputStream.writeUTF("feedback no");
            outputStream.flush();
            return;
        }

        int response = ConnectionManager.checkin(requestedName);
        if (response == JOptionPane.YES_OPTION) {
            if (WhiteboardServer.usernames.contains(requestedName)) {
                outputStream.writeUTF("feedback no because already exist a username");
                outputStream.flush();
                WhiteboardServer.clientConnections.remove(this);
                socket.close();
            } else {
                WhiteboardServer.usernames.add(requestedName);
                outputStream.writeUTF("feedback yes");
                outputStream.flush();
            }
        } else {
            outputStream.writeUTF("feedback rejected");
            outputStream.flush();
            WhiteboardServer.clientConnections.remove(this);
        }
    }
}

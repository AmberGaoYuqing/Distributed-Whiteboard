package manager;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ConnectionManager
 * ------------------
 * Manages all active client connections in the shared whiteboard system.
 * Responsibilities:
 * - Handle client join, leave, and kick events
 * - Broadcast drawing, chat, and system messages
 * - Maintain and distribute the user list
 */
public class ConnectionManager {

    // === 1. Client UserLogin & Exit ===

    public static synchronized int checkin(String name) {
        return ManagerLogin.managerBoard.showRequest(name);
    }

    @SuppressWarnings("unchecked")
    public static void addUser(String[] clients) {
        ManagerLogin.managerBoard.userJList.setListData(clients);
    }

    public static synchronized void handleClientLeft(ClientConnection client) {
        if (client.name == null || !WhiteboardServer.usernames.contains(client.name)) return;
        WhiteboardServer.clientConnections.remove(client);
        WhiteboardServer.usernames.remove(client.name);

        String userMessage = buildUserMessage("clientout", client.name);
        broadcastMessageToAll(userMessage);

        SwingUtilities.invokeLater(() -> {
            String[] userList = WhiteboardServer.usernames.toArray(new String[0]);
            ManagerLogin.managerBoard.userJList.setListData(userList);
            JOptionPane.showMessageDialog(ManagerLogin.managerBoard.frame,
                    "User " + client.name + " left.");
        });
    }

    public static synchronized void updateAndBroadcastUserList(String kickedUser) {
        String userListMessage = buildUserMessage("delete", kickedUser);
        broadcastMessageToAll(userListMessage);
    }

    // === 2. Canvas Management ===

    public static void canvasRepaint(String drawRecord) {
        Manager.listenerRecord.update(drawRecord);
        Manager.artCanvas.repaint();
    }

    // === 3. Drawing & Chat Events ===

    public static void broadcast(String message) throws IOException {
        for (ClientConnection client : WhiteboardServer.clientConnections) {
            sendMessage(client, message);
        }
    }

    public static void broadcastBatch(ArrayList<String> drawRecords) throws IOException {
        for (String record : drawRecords) {
            String msg = "draw " + record;
            for (ClientConnection client : WhiteboardServer.clientConnections) {
                sendMessage(client, msg);
            }
        }
    }

    public static void broadcastToOthers(ClientConnection sender, String message) {
        for (ClientConnection client : WhiteboardServer.clientConnections) {
            if (client != sender) {
                try {
                    sendMessage(client, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void broadcastUserList() {
        try {
            String userList = "userJList:" + String.join(",", WhiteboardServer.usernames);
            System.out.println("[Manager] Broadcasting user list: " + userList);
            broadcast(userList);
        } catch (IOException e) {
            System.err.println("[Manager] Error broadcasting user list: " + e.getMessage());
        }
    }

    // === 4. Logging & Feedback ===

    public static void broadcastDrawLog(String username, String action) {
        String logMessage = username + " is " + action;
        SwingUtilities.invokeLater(() -> {
            JTextArea logArea = ManagerLogin.managerBoard.drawLogArea;
            if (logArea != null) {
                logArea.append(logMessage + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
        for (ClientConnection client : WhiteboardServer.clientConnections) {
            try {
                sendMessage(client, "drawlog " + logMessage);
            } catch (IOException e) {
                System.err.println("Failed to broadcast draw log: " + e.getMessage());
            }
        }
    }

    // === 5. Utility Methods ===

    private static void sendMessage(ClientConnection client, String message) throws IOException {
        System.out.println("[Server] Sending message to client " + client.name + ": " + message);
        client.outputStream.writeUTF(message);
        client.outputStream.flush();
    }

    private static String buildUserMessage(String prefix, String user) {
        StringBuilder builder = new StringBuilder(prefix).append(" ").append(user);
        for (String u : WhiteboardServer.usernames) {
            builder.append(",").append(u);
        }
        return builder.toString();
    }

    private static void broadcastMessageToAll(String message) {
        for (ClientConnection client : WhiteboardServer.clientConnections) {
            try {
                sendMessage(client, message);
            } catch (IOException e) {
                System.err.println("[ConnectionManager] Failed to broadcast: " + e.getMessage());
            }
        }
    }
}
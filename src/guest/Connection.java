package guest;

import manager.WhiteboardServer;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Connection
 * ----------
 * Handles socket communication for the guest client.
 * Processes server instructions including drawing, chat, user list updates, and kick notifications.
 */
public class Connection {

    public DataInputStream inputStream;
    public DataOutputStream outputStream;
    // Kick indicator
    public boolean kick = false;
    // Socket and stream
    private Socket socket;
    // Connection status: "yes", "no", "rejected", etc.
    private String status;

    /**
     * Constructs a Connection object with the given socket.
     *
     * @param socket the connected socket instance
     */
    public Connection(Socket socket) {
        resetStatus();
        try {
            this.socket = socket;
            outputStream = new DataOutputStream(this.socket.getOutputStream());
            inputStream = new DataInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the listening loop to process incoming server messages.
     */
    public void launch() {
        try {
            while (true) {
                String request = inputStream.readUTF();
                System.out.println("[Guest] Received message from server: " + request);
                String[] splitedRequest = request.split(" ", 2);

                switch (splitedRequest[0]) {
                    case "draw" -> {
                        Guest.listenerRecord.update(splitedRequest[1]);
                        Guest.artCanvas.repaint();
                    }
                    case "chat" -> {
                        UserLogin.guestBoard.chatArea.append(splitedRequest[1] + "\n");
                    }
                    case "userJList" -> {
                        if (UserLogin.guestBoard != null) {
                            UserLogin.guestBoard.userJList.setListData(splitedRequest[1].split(","));
                        }
                    }
                    case "new_canvas" -> {
                        JOptionPane.showMessageDialog(UserLogin.guestBoard.frame, 
                            splitedRequest[1], "Whiteboard Update", JOptionPane.INFORMATION_MESSAGE);
                    
                        Guest.artCanvas.removeAll();
                        Guest.artCanvas.updateUI();
                        Guest.listenerRecord.clearRecord();
                    }
                    case "file_opened" -> {
                        JOptionPane.showMessageDialog(UserLogin.guestBoard.frame, 
                            splitedRequest[1], "File Operation", JOptionPane.INFORMATION_MESSAGE);
                    }
                    case "server_shutdown" -> {
                        JOptionPane.showMessageDialog(UserLogin.guestBoard.frame, 
                            "Server is shutting down. The application will close.");
                        System.exit(0);
                    }
                    case "delete" -> handleKickBroadcast(splitedRequest[1]);
                    case "kick" -> {
                        kick = true;
                        JOptionPane.showMessageDialog(UserLogin.guestBoard.frame, "You have been kicked out by the manager.");
                    }
                    case "feedback" -> {
                        switch (splitedRequest[1]) {
                            case "no" -> status = "no";
                            case "yes" -> status = "yes";
                            case "rejected" -> status = "rejected";
                        }
                    }
                    case "clientout" -> handleClientLeft(splitedRequest[1]);
                    case "drawlog" -> {
                        JTextArea logArea = UserLogin.guestBoard.drawLogArea;
                        logArea.append(splitedRequest[1] + "\n");
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                    }
                    default -> System.err.println("Unknown server message: " + request);
                }
            }
        } catch (IOException e) {
            if (kick) {
                JOptionPane.showMessageDialog(UserLogin.guestBoard.frame, "Disconnected from server.");
            }
            System.exit(-1);
        }
    }

    /**
     * Handles broadcast when a user is kicked.
     *
     * @param data the string containing kicked user and remaining user list
     */
    private void handleKickBroadcast(String data) {
        String[] allUsers = data.split(",");
        if (allUsers.length >= 1) {
            String kickedUser = allUsers[0];
            String[] remainingUsers = Arrays.copyOfRange(allUsers, 1, allUsers.length);

            JOptionPane.showMessageDialog(UserLogin.guestBoard.frame,
                    kickedUser + " has been kicked out by the manager.");
            UserLogin.guestBoard.userJList.setListData(remainingUsers);
        }
    }

    /**
     * Handles the scenario when another guest leaves the session.
     */
    private void handleClientLeft(String data) {
        String[] parts = data.split(",");
        if (parts.length > 0) {
            String leftUser = parts[0];

            if (parts.length > 1) {
                String[] remainingUsers = new String[parts.length - 1];
                System.arraycopy(parts, 1, remainingUsers, 0, parts.length - 1);
                UserLogin.guestBoard.userJList.setListData(remainingUsers);
            }
            JOptionPane.showMessageDialog(UserLogin.guestBoard.frame,
                    "Guest " + leftUser + " left!");

        }
    }

    /**
     * Returns the current join status from the server.
     *
     * @return current status: "wait", "yes", "no", or "rejected"
     */
    public String getCurrentStatus() {
        return status;
    }

    /**
     * Resets the status to "wait".
     */
    public void resetStatus() {
        status = "wait";
    }
}
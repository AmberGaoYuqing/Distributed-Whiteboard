package guest;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Class Name: UserLogin
 * This class renders the login GUI for a guest user, captures their username,
 * and sends a request to join the whiteboard session.
 */
public class UserLogin {
    static String address;
    static int port;
    static String username;
    public static Connection connection;
    public static Guest guestBoard;
    public static Socket socket;
    private static JFrame frame;
    private JTextField usernameInputField;

    /**
     * Handle login logic on button click
     */
    private static final String STATUS_WAIT = "wait";
    private static final String STATUS_NO = "no";
    private static final String STATUS_REJECTED = "rejected";
    private static final String STATUS_YES = "yes";
    private static final int TIMEOUT_THRESHOLD = 10000;
    private static final int TIMEOUT_STEP = 120;

    public UserLogin() {
        initialize();
    }

    private static void parseArguments(String[] args) {
        if (args.length > 2) {
            try {
                address = args[0];
                port = Integer.parseInt(args[1]);
                username = args[2];
            } catch (Exception e) {
                System.out.println("Invalid arguments. Expected: <address> <port> <username>");
                System.exit(1);
            }
        } else {
            address = "localhost";
            port = 4545;
            username = "User";
            System.out.println("User use default values.");
        }
    }

    public static void main(String[] args) {
        parseArguments(args);
        connectToServer ();
    }

    private static void connectToServer () {
        int maxRetries = 5;
        int retryCount = 0;
        int retryDelay = 2000;

        while (retryCount < maxRetries) {
            try {
                socket = new Socket(address, port);
                connection = new Connection(socket);
                EventQueue.invokeLater(UserLogin::new);
                connection.launch();
                break;
            } catch (IOException e) {
                retryCount++;
                if (retryCount < maxRetries) {
                    System.out.println("Connection failed. Retrying in " + (retryDelay / 1000) + " seconds... (Attempt " + retryCount + " of " + maxRetries + ")");
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    System.out.println("Failed to connect after " + maxRetries + " attempts. Please make sure the server is running.");
                    System.exit(1);
                }
            }
        }
    }

    /**
     * Initialize the login GUI
     */
    private void initialize() {
        frame = new JFrame("Whiteboard Guest Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 400);
        frame.setLocationRelativeTo(null); // Center on screen

        JPanel outerPanel = new JPanel(new GridBagLayout());
        frame.setContentPane(outerPanel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        outerPanel.add(contentPanel);


        JLabel titleLabel = new JLabel("Guest Panel - UserLogin Whiteboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 200));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));


        JLabel usernameLabel = new JLabel("Please enter your name");
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        contentPanel.add(usernameLabel);
        contentPanel.add(Box.createVerticalStrut(10));


        usernameInputField = new JTextField(username);
        usernameInputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        contentPanel.add(usernameInputField);
        contentPanel.add(Box.createVerticalStrut(20));


        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(loginButton);


        loginButton.addActionListener(e -> handleLogin());

        frame.setVisible(true);
    }


    private void handleLogin() {
        try {
            username = usernameInputField.getText();
            if (username.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid username.");
                return;
            }

            if (connection == null || !socket.isConnected()) {
                JOptionPane.showMessageDialog(frame, 
                    "Not connected to server. Please restart the application.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            connection.outputStream.writeUTF("request " + username);

            if (!waitForStatusUpdate()) {
                handleTimeout();
                return;
            }

            String status = connection.getCurrentStatus();
            handleStatusResponse(status, username);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, 
                "Error during login: " + ex.getMessage(),
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean waitForStatusUpdate() throws InterruptedException {
        int elapsedTime = 0;

        while (connection.getCurrentStatus().equals(STATUS_WAIT) && elapsedTime < TIMEOUT_THRESHOLD) {
            TimeUnit.MILLISECONDS.sleep(TIMEOUT_STEP);
            elapsedTime += TIMEOUT_STEP;
        }

        return !connection.getCurrentStatus().equals(STATUS_WAIT);
    }

    private void handleStatusResponse(String status, String username) throws IOException {
        switch (status) {
            case STATUS_NO -> showMessageAndReset("Username already exists!");
            case STATUS_REJECTED -> handleRejection();
            case STATUS_YES -> initializeGuestBoard(username);
            default -> handleTimeout();
        }
    }

    private void showMessageAndReset(String message) {
        JOptionPane.showMessageDialog(frame, message);
        connection.resetStatus();
    }

    private void handleRejection() throws IOException {
        JOptionPane.showMessageDialog(frame, "Rejected by manager.");
        cleanupAndExit();
    }

    private void handleTimeout() throws IOException {
        JOptionPane.showMessageDialog(frame, "Client Connection timeout.");
        cleanupAndExit();
    }

    private void initializeGuestBoard(String username) {
        frame.dispose();
        if (guestBoard == null) {
            guestBoard = new Guest(connection, username);
        }
    }

    private void cleanupAndExit() throws IOException {
        frame.dispose();
        connection.outputStream.writeUTF("over");
        connection.outputStream.flush();
        socket.close();
        System.exit(1);
    }
}

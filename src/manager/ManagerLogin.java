package manager;

import javax.swing.*;
import java.awt.*;

/**
 * Class: ManagerLogin
 * This class provides a login GUI for the user to input a username,
 * initializes the whiteboard server, and creates a Manager instance
 * upon confirmation.
 */
public class ManagerLogin {
    public static Manager managerBoard;
    static String address;
    static int port;
    static String username;
    private JFrame frame;
    private JTextField usernameInputField;


    public ManagerLogin() {
        initialize();
    }

    /**
     * Main method — application entry point.
     * Initializes server and starts login GUI.
     * @param args args[0] = server IP address, args[1] = port number, args[2] = manager username
     */
    public static void main(String[] args) {
        parseArguments(args);
        System.out.println("Starting Manager with: address=" + address + ", port=" + port + ", username=" + username);
        EventQueue.invokeLater(ManagerLogin::new);  // 只启动GUI界面
    }


    private static void parseArguments(String[] args) {
        if (args.length >= 3) {
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
            username = "Manager";
            System.out.println("Server use default values.");
        }
    }

    /**
     * Initialize the login GUI
     */
    private void initialize() {
        frame = new JFrame("Whiteboard Manager Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 400);
        frame.setLocationRelativeTo(null);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        frame.setContentPane(outerPanel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        outerPanel.add(contentPanel);

        JLabel titleLabel = new JLabel("Manager Panel - Login Whiteboard");
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

        loginButton.addActionListener(e -> {
            try {
                username = usernameInputField.getText();
                if (username.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid username.");
                    return;
                }

                //Creat manager white board
                managerBoard = new Manager(username);
                managerBoard.setFrame(managerBoard);
                
                // Start server in a new thread
                new Thread(() -> {
                    try {
                        System.out.println("Starting server on port " + port);
                        WhiteboardServer.launch(port, username);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error starting server: " + ex.getMessage());
                    }
                }).start();
                
                // Wait for a short time to ensure the server starts
                Thread.sleep(1000);

                frame.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error initializing manager: " + ex.getMessage());
            }
        });

        frame.setVisible(true);
    }
}

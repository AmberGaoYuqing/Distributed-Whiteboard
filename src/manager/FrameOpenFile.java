package manager;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * FrameOpenFile
 *
 * A modern UI window for opening a file in the whiteboard application.
 * Uses BoxLayout and consistent styling for a cleaner look.
 */
public class FrameOpenFile {
    public JFrame frmOpen;
    private JTextField txtFilename;
    private JLabel lblError;
    private Manager whiteBoard;

    public FrameOpenFile() {
        initialize();
    }

    public FrameOpenFile(Manager whiteBoard) {
        this.whiteBoard = whiteBoard;
        initialize();
    }

    private void initialize() {
        frmOpen = new JFrame("Open Drawing File");
        frmOpen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frmOpen.setSize(420, 260);
        frmOpen.setLocationRelativeTo(null); // Center on screen

        JPanel outerPanel = new JPanel(new GridBagLayout());
        frmOpen.setContentPane(outerPanel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        outerPanel.add(contentPanel);

        JLabel titleLabel = new JLabel("Open File - Whiteboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        JLabel promptLabel = new JLabel("Enter file path:");
        promptLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        promptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(promptLabel);
        contentPanel.add(Box.createVerticalStrut(10));

        txtFilename = new JTextField("GraphicsView");
        txtFilename.setFont(new Font("Arial", Font.PLAIN, 14));
        txtFilename.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        contentPanel.add(txtFilename);
        contentPanel.add(Box.createVerticalStrut(15));

        lblError = new JLabel("File does not exist!");
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Arial", Font.PLAIN, 12));
        lblError.setVisible(false);
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblError);
        contentPanel.add(Box.createVerticalStrut(10));

        JButton btnOpen = new JButton("Open File");
        btnOpen.setFont(new Font("Arial", Font.BOLD, 15));
        btnOpen.setBackground(new Color(66, 133, 244));
        btnOpen.setForeground(Color.WHITE);
        btnOpen.setFocusPainted(false);
        btnOpen.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(btnOpen);

        btnOpen.addActionListener(e -> {
            String file = txtFilename.getText().trim();
            try {
                new Scanner(new FileInputStream(file));
            } catch (FileNotFoundException ex) {
                lblError.setVisible(true);
                return;
            }

            lblError.setVisible(false);
            whiteBoard.openFile(file);
            frmOpen.dispose();
        });

        frmOpen.setVisible(true);
    }
}

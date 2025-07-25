package manager;

import javax.swing.*;
import java.awt.*;

/**
 * FrameSaveFile
 * --------------
 * Represents a modal frame that prompts the manager to enter a filename
 * and choose a file format (.jpg or .png) to save the whiteboard contents.
 */
public class FrameSaveFile {
    public JFrame saveWindow;
    private JTextField textField;
    private Manager whiteBoard;

    /**
     * Constructor that accepts a whiteboard controller for callback.
     *
     * @param whiteBoard the Manager instance that controls saving logic
     */
    public FrameSaveFile(Manager whiteBoard) {
        this.whiteBoard = whiteBoard;
        initialize();
    }

    /**
     * Initializes the GUI layout of the save file frame with modern styling.
     */
    private void initialize() {
        saveWindow = new JFrame("Save Drawing");
        saveWindow.setSize(420, 230);
        saveWindow.setLocationRelativeTo(null); // Center on screen
        saveWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        saveWindow.setContentPane(outerPanel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        outerPanel.add(contentPanel);

        JLabel titleLabel = new JLabel("Save Whiteboard As...");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Filename: ");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(nameLabel);

        textField = new JTextField("GraphicsView");
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setMaximumSize(new Dimension(250, 30));
        inputPanel.add(textField);

        JComboBox<String> comboBox = new JComboBox<>(new String[] { ".jpg", ".png" });
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setMaximumSize(new Dimension(70, 30));
        inputPanel.add(Box.createHorizontalStrut(10));
        inputPanel.add(comboBox);

        contentPanel.add(inputPanel);
        contentPanel.add(Box.createVerticalStrut(25));

        JButton btnSave = new JButton("Save");
        btnSave.setFont(new Font("Arial", Font.BOLD, 15));
        btnSave.setBackground(new Color(66, 133, 244));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(btnSave);

        btnSave.addActionListener(e -> {
            String name = textField.getText().trim();
            String format = comboBox.getSelectedItem().toString();
            whiteBoard.saveFile(name);
            whiteBoard.saveImg(name, format);
            saveWindow.dispose();
        });

        saveWindow.setVisible(true);
    }
}

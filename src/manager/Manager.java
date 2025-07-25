package manager;

import common.ShapeDrawingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Class: Manager
 *  * A manager whiteboard system.
 * Provides drawing, chat, and user list viewing functionalities
 * The manager can manage users, and perform file operations such as saving and loading drawings.
 */

public class Manager {
    // Shared GUI components
    public static JTextArea chatArea;
    static Manager createWhiteBoard;
    // Networking
    static Listener listenerRecord;
    public static ShapeDrawingPanel artCanvas;
    public static int x, y;

    // Frame and user-specific
    public JFrame frame;
    public JTextField inputField;
    public JButton sendButton;
    public JTextArea drawLogArea;
    public JList userJList;

    private int width;
    private int height;
    // Toolbar icons
    ImageIcon lineIcon = new ImageIcon(Manager.class.getResource("/icon/line.png"));
    ImageIcon ovalIcon = new ImageIcon(Manager.class.getResource("/icon/oval.png"));
    ImageIcon rectangleIcon = new ImageIcon(Manager.class.getResource("/icon/rectangle.png"));
    ImageIcon triangleIcon = new ImageIcon(Manager.class.getResource("/icon/triangle.png"));
    ImageIcon pencilIcon = new ImageIcon(Manager.class.getResource("/icon/pencil.png"));
    ImageIcon colorIcon = new ImageIcon(Manager.class.getResource("/icon/color.png"));
    ImageIcon eraserIcon = new ImageIcon(Manager.class.getResource("/icon/eraser.png"));
    ImageIcon[] icons = {pencilIcon, lineIcon, ovalIcon, rectangleIcon, triangleIcon, eraserIcon, colorIcon};

    private String file = "./save/canvas_save";


    public Manager(String mName) {
        initialize(mName);
    }

    public Manager() {}


    public int showRequest(String name) {
        return JOptionPane.showConfirmDialog(null,
                name + " wants to share your white board", "Confirmation",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Initializes the entire GUI layout, canvas, tools, and interactions for the manager.
     * @param name the name of the manager
     */
    private void initialize(String name) {
        // Frame setup
        frame = new JFrame();
        frame.setTitle("Canvas Manager" + name);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(783, 546);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    ConnectionManager.broadcast("server_shutdown");
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });

        listenerRecord = new Listener(frame);
        artCanvas = new ShapeDrawingPanel();


        // ========== Operator==========
        JPanel toolPanel = new JPanel();
        toolPanel.setBounds(5, 40, 130, 300);
        toolPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JPanel menuBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        menuBar.setBounds(0, 0, 800, 40);
        String[] menuItems = {"New", "Save", "Save As", "Open", "Exit"};
        for (String item : menuItems) {
            JButton btn = new JButton(item);
            btn.setPreferredSize(new Dimension(120, 30));
            btn.addActionListener(e -> handleMenuAction(item));
            menuBar.add(btn);
        }
        frame.getContentPane().add(menuBar);

        String[] tools = {"Free", "Line", "Oval", "Rect", "Triangle", "Erase", "Color"};
        for (int i = 0; i < tools.length; i++) {
            JButton button1 = new JButton(tools[i]);
            button1.setActionCommand(tools[i]);
            button1.setPreferredSize(new Dimension(120, 30));
             Image temp = icons[i].getImage().getScaledInstance(21, 21, Image.SCALE_DEFAULT);
            icons[i] = new ImageIcon(temp);
            button1.setIcon(icons[i]);

            button1.addActionListener(listenerRecord);
            toolPanel.add(button1);
        }

        JButton text = new JButton("Text");
        text.setFont(new Font(null, Font.PLAIN, 20));
        text.setPreferredSize(new Dimension(120, 30));
        text.addActionListener(listenerRecord);
        text.setActionCommand("Text");
        toolPanel.add(text);

        listenerRecord.setThickness(6);
        frame.getContentPane().setLayout(null);
        frame.getContentPane().add(toolPanel);

        artCanvas.setBorder(null);
        artCanvas.setBounds(140, 40, 420, 308);
        width = artCanvas.getWidth();
        height = artCanvas.getHeight();
        artCanvas.setBackground(Color.WHITE);
        artCanvas.setList(listenerRecord.getRecord());

        frame.setVisible(true);
        frame.setResizable(false);
        frame.addComponentListener(new ComponentAdapter() {
        });
        frame.getContentPane().add(artCanvas);
        artCanvas.addMouseListener(listenerRecord);
        artCanvas.addMouseMotionListener(listenerRecord);
        listenerRecord.setG(artCanvas.getGraphics());

        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);

        JScrollPane scrollTextArea = new JScrollPane(chatArea);
        scrollTextArea.setBounds(140, 360, 420, 88);
        scrollTextArea.setEnabled(false);
        scrollTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(scrollTextArea);

        inputField = new JTextField();
        inputField.setBounds(140, 460, 328, 21);
        frame.getContentPane().add(inputField);

        sendButton = new JButton("SEND");
        sendButton.setBounds(476, 460, 100, 23);
        frame.getContentPane().add(sendButton);


        // ========== User List ==========
        userJList = new JList<>();
        frame.getContentPane().add(userJList);
        userJList.setListData(new String[]{name});
        JScrollPane scrollList = new JScrollPane(userJList);
        scrollList.setBounds(589, 40, 149, 305);
        scrollList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(scrollList);

        JButton btnKickUser = new JButton("Kick User");
        btnKickUser.setBounds(589, 350, 100, 23);
        frame.getContentPane().add(btnKickUser);

        sendButton.addActionListener(e -> {
            String msg = name + ": " + inputField.getText();
            chatArea.append(msg + "\n");
            for (ClientConnection clientConnection : WhiteboardServer.clientConnections) {
                try {
                    clientConnection.outputStream.writeUTF("chat " + msg);
                    clientConnection.outputStream.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            inputField.setText("");
        });

        btnKickUser.addActionListener(e -> {
            String user = userJList.getSelectedValue().toString();
            if (name.equals(user)) return;
            for (int i = 0; i < WhiteboardServer.clientConnections.size(); i++) {
                ClientConnection tt = WhiteboardServer.clientConnections.get(i);
                if (user.equals(tt.name)) {
                    tt.kick = true;
                    try {
                        tt.outputStream.writeUTF("kick " + tt.name);
                        tt.outputStream.flush();
                        tt.socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    WhiteboardServer.clientConnections.remove(i);
                    WhiteboardServer.usernames.remove(user);
                    JOptionPane.showMessageDialog(frame, "User " + user + " is kicked out!");
                    ConnectionManager.updateAndBroadcastUserList(user);
                }
            }
            userJList.setListData(WhiteboardServer.usernames.toArray(new String[0]));
        });

        drawLogArea = new JTextArea();
        drawLogArea.setLineWrap(true);
        drawLogArea.setWrapStyleWord(true);
        drawLogArea.setEditable(false);
        JScrollPane drawLogScroll = new JScrollPane(drawLogArea);
        drawLogScroll.setBounds(589, 380, 149, 100);
        drawLogScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(drawLogScroll);
    }

    /**
     * Sets the instance reference of the current whiteboard.
     * @param createWhiteBoard the Manager instance to set
     */
    public void setFrame(Manager createWhiteBoard){
        this.createWhiteBoard = createWhiteBoard;
    }

    /**
     * Handles actions triggered by menu buttons.
     * @param cmd the command string (New, Save, Open, etc.)
     */
    private void handleMenuAction(String cmd) {
        switch (cmd) {
            case "New":
                artCanvas.removeAll();
                artCanvas.updateUI();
                listenerRecord.clearRecord();
                try { 
                    // Send new canvas notification
                    ConnectionManager.broadcast("new_canvas Manager created a new whiteboard");
                    // wait for message sending
                    Thread.sleep(500);
                } catch (Exception ex) { 
                    ex.printStackTrace(); 
                }
                break;
            case "Save": saveFile(); break;
            case "Save As": new FrameSaveFile(createWhiteBoard).saveWindow.setVisible(true); break;
            case "Open": new FrameOpenFile(createWhiteBoard).frmOpen.setVisible(true); break;
            case "Exit": 
                try {
                    ConnectionManager.broadcast("server_shutdown");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0); 
                break;
        }
    }

    /**
     * Saves current drawing records to a text file.
     * @param file the name of the file to save
     */
    public void saveFile(String file){
        this.file = "./" + file;
        this.saveFile();
    }

    /**
     * Saves drawing records to a default file.
     */
    public void saveFile() {
        try (PrintWriter outputStream = new PrintWriter(new FileOutputStream(file))) {
            ArrayList<String> recordList = listenerRecord.getRecord();
            for (String record : recordList) {
                outputStream.println(record);
            }
            System.out.println("Saved");
        } catch (IOException e1) {
            System.out.println("Error opening the file " + file);
        }
    }

    // Saves the canvas as an image file in the specified format.
    public void saveImg(String file, String format){
        BufferedImage targetImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = targetImg.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        ArrayList<String> recordList = listenerRecord.getRecord();
        artCanvas.draw(g, recordList);
        try {
            if (format.equals(".jpg")) {
                ImageIO.write(targetImg, "JPEG", new File("./" + file + format));
            } else if (format.equals(".png")) {
                ImageIO.write(targetImg, "PNG", new File("./" + file + format));
            }
            System.out.println("Saved");
        } catch (IOException e1) {
            System.out.println("Wrong file.");
        }
    }

    /**
     * Opens a file and redraws the canvas based on stored drawing commands.
     * @param file the file path to load from
     */
    public void openFile(String file){
        file = "./" + file;
        try (Scanner inputStream = new Scanner(new FileInputStream(file))) {
            listenerRecord.clearRecord();
            while (inputStream.hasNextLine()) {
                String line = inputStream.nextLine();
                listenerRecord.update(line);
            }

            // Send the massage that open the white board
            ConnectionManager.broadcast("file_opened Manager opened file: " + file);
            Thread.sleep(500);
            // Send new command
            ConnectionManager.broadcast("new");
            // Send draw command
            ConnectionManager.broadcastBatch(listenerRecord.getRecord());
            artCanvas.repaint();
        } catch (Exception e) {
            System.out.println("Problem opening file or broadcasting content.");
        }
    }
}

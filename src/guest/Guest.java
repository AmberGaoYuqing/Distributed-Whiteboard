package guest;

import common.ShapeDrawingPanel;
import manager.WhiteboardServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * 客户端 Guest 类，用于连接白板并进行绘图与聊天功能。
 */
/**
 * Guest 类代表连接白板服务器的客户端用户（访客）
 * 包含图形界面构造与连接逻辑。
 */
public class Guest {

    public static Connection connection;
    public static JTextArea chatArea;
    public static Listener listenerRecord;
    public JFrame frame;
    public static int x, y;
    public JTextField inputField;
    public JButton sendButton;
    public JTextArea drawLogArea;
    public JList userJList;
    private String username;
    static ShapeDrawingPanel artCanvas;
    int width;
    int height;

    public Guest(Connection connection) {
        this.connection = connection;
        initialize();
    }

    public Guest(Connection connection, String username) {
        this.connection = connection;
        this.username = username;
        initialize();
    }


    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Canvas Guest" + this.username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(783, 546);
        frame.setLocationRelativeTo(null);


        //Listener listenerRecord = new Listener(frame);
        Guest.listenerRecord = new Listener(frame);

        JPanel toolPanel = new JPanel();
        toolPanel.setBounds(5,40,130,300);
        toolPanel.setLayout(new FlowLayout(FlowLayout.LEFT));


        ImageIcon lineIcon = new ImageIcon(Guest.class.getResource("/icon/line.png"));
        ImageIcon ovalIcon = new ImageIcon(Guest.class.getResource("/icon/oval.png"));
        ImageIcon rectangleIcon = new ImageIcon(Guest.class.getResource("/icon/rectangle.png"));
        ImageIcon triangleIcon = new ImageIcon(Guest.class.getResource("/icon/triangle.png"));
        ImageIcon pencilIcon = new ImageIcon(Guest.class.getResource("/icon/pencil.png"));
        ImageIcon colorIcon = new ImageIcon(Guest.class.getResource("/icon/color.png"));
        ImageIcon eraserIcon = new ImageIcon(Guest.class.getResource("/icon/eraser.png"));
        ImageIcon[] icons = {pencilIcon, lineIcon, ovalIcon, rectangleIcon, triangleIcon, eraserIcon, colorIcon };

        commonUI(toolPanel, icons, username);

        try {
            connection.outputStream.writeUTF("begin");
            connection.outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     */
    private void commonUI(JPanel toolPanel, ImageIcon[] icons,String name) {
        //Listener listenerRecord = new Listener(frame);
        String[] tools = {"Free","Line", "Oval", "Rect", "Triangle", "Erase","Color"};
        for (int i = 0; i < tools.length; i++) {
            JButton button1 = new JButton(tools[i]);
            button1.setActionCommand(tools[i]);
            button1.setPreferredSize(new Dimension(120, 30)); //

            Image temp = icons[i].getImage().getScaledInstance(21, 21, icons[i].getImage().SCALE_DEFAULT);
            icons[i] = new ImageIcon(temp);
            button1.setIcon(icons[i]);

            button1.addActionListener(Guest.listenerRecord);
            toolPanel.add(button1);
        }


        JButton text = new JButton("Text");
        text.setFont(new Font(null, Font.PLAIN, 20));
        text.setPreferredSize(new Dimension(120, 30));
        text.addActionListener(listenerRecord);
        text.setActionCommand("Text");
        if (text != null) {
            toolPanel.add(text);
        }


        listenerRecord.setThickness(6);
        frame.getContentPane().setLayout(null);
        frame.getContentPane().add(toolPanel);


        artCanvas = new ShapeDrawingPanel();
        artCanvas.setBorder(null);
        artCanvas.setBounds(140, 40, 420, 308);
        artCanvas.setBackground(Color.WHITE);
        artCanvas.setList(listenerRecord.getRecord());
        artCanvas.setLayout(null);
        frame.getContentPane().add(artCanvas);
        width = artCanvas.getWidth();
        height = artCanvas.getHeight();


        frame.setVisible(true);
        frame.setResizable(false);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                Component comp = e.getComponent();
                x = comp.getX() + 200;
                y = comp.getY() + 200;
            }
        });


        artCanvas.addMouseListener(listenerRecord);
        artCanvas.addMouseMotionListener(listenerRecord);
        listenerRecord.setG(artCanvas.getGraphics());



        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);

        JScrollPane scrollTextArea = new JScrollPane(chatArea);
        scrollTextArea.setBounds(140, 360, 420, 88);

        scrollTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        frame.getContentPane().add(scrollTextArea);


        inputField = new JTextField();
        inputField.setBounds(140, 460, 328, 21);
        frame.getContentPane().add(inputField);


        sendButton = new JButton("SEND");
        sendButton.setBounds(476, 460, 100, 23);
        frame.getContentPane().add(sendButton);


        
        userJList = new JList<>();
        String uname = name;
        frame.getContentPane().add(userJList);
        

        //userJList.setListData(new String[]{uname});
        userJList.setListData(WhiteboardServer.usernames.toArray(new String[0]));

        JScrollPane scrollList = new JScrollPane(userJList);
        scrollList.setBounds(589, 40, 149, 305);
        scrollList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(scrollList);


        drawLogArea = new JTextArea();
        drawLogArea.setLineWrap(true);
        drawLogArea.setWrapStyleWord(true);
        drawLogArea.setEditable(false);

        JScrollPane drawLogScroll = new JScrollPane(drawLogArea);
        drawLogScroll.setBounds(589, 380, 149, 100);
        drawLogScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.getContentPane().add(drawLogScroll);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String msg = username + ": " + inputField.getText();
                try {
                    connection.outputStream.writeUTF("chat " + msg);
                    connection.outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputField.setText("");
            }
        });

    }
}
package common;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public abstract class BaseListener implements ActionListener, MouseListener, MouseMotionListener {
    // ========== Constants ==========
    protected static final String COLOR_CHOICE_DIALOG_TITLE = "Choose color";
    protected static final int TEXT_FONT_SIZE_BASE = 10;

    // ========== Global Variables ==========
    protected Graphics2D g;
    protected JFrame frame;
    protected int startX, startY, endX, endY;
    protected int thickness = 1;
    protected ToolType tool = ToolType.FREE;
    protected static Color color = Color.BLACK;
    protected String record;
    protected final ArrayList<String> recordList = new ArrayList<>();

    // ========= 构造函数 =========
    public BaseListener() {}

    public BaseListener(JFrame frame) {
        this.frame = frame;
    }

    // ========== 通用方法 ==========
    protected void displayColorChooser() {
        JFrame colorFrame = new JFrame(COLOR_CHOICE_DIALOG_TITLE);
        colorFrame.setSize(300, 300);
        colorFrame.setLocationRelativeTo(null);
        colorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Color selectedColor = JColorChooser.showDialog(colorFrame, COLOR_CHOICE_DIALOG_TITLE, null);
        if (selectedColor != null) {
            color = selectedColor;
        }
    }

    protected void configureCursorAndTool(String toolName) {
        try {
            tool = ToolType.valueOf(toolName.toUpperCase());
            Cursor cursor = configureCursor(tool);
            frame.setCursor(cursor);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame, "Unknown tool: " + toolName);
        }
    }

    protected Cursor configureCursor(ToolType tool) {
        return (tool == ToolType.FREE || tool == ToolType.TEXT)
                ? new Cursor(Cursor.DEFAULT_CURSOR)
                : new Cursor(Cursor.CROSSHAIR_CURSOR);
    }

    protected void handleFreeDrawing(MouseEvent e) {
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness));
        g.drawLine(startX, startY, startX, startY);
        record = buildRecord(tool, thickness, color, startX, startY, startX, startY, null, true);
        recordList.add(record);
    }

    protected String buildRecord(ToolType tool, int thickness, Color color,
                               int startX, int startY, int endX, int endY,
                               String extraText, boolean isIntermediate) {
        String rgb = color.getRed() + " " + color.getGreen() + " " + color.getBlue();
        String record;

        switch (tool) {
            case LINE:
            case OVAL:
            case RECT:
            case TRIANGLE:
            case ERASE:
                record = tool.name().charAt(0) + tool.name().substring(1).toLowerCase() +
                        " " + thickness + " " + rgb + " " + startX + " " + startY + " " + endX + " " + endY;
                break;
            case TEXT:
                record = "Text " + thickness + " " + rgb + " " + startX + " " + startY + " |" + extraText;
                break;
            case FREE:
                record = "Line " + thickness + " " + rgb + " " + startX + " " + startY + " " + endX + " " + endY;
                break;
            default:
                throw new IllegalArgumentException("Unsupported drawing tool: " + tool);
        }

        if (isIntermediate) {
            record += " !";
        }

        return record;
    }

    protected void drawShape(ToolType tool, int startX, int startY, int endX, int endY) {
        switch (tool) {
            case LINE -> g.drawLine(startX, startY, endX, endY);
            case OVAL -> g.drawOval(Math.min(startX, endX), Math.min(startY, endY),
                    Math.abs(endX - startX), Math.abs(endY - startY));
            case RECT -> g.drawRect(Math.min(startX, endX), Math.min(startY, endY),
                    Math.abs(startX - endX), Math.abs(startY - endY));
            case TRIANGLE -> g.drawPolygon(
                    new int[]{startX, endX, startX},
                    new int[]{startY, endY, endY},
                    3
            );
        }
    }

    protected void prepareGraphics() {
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness));
    }

    // ========== 抽象方法 ==========
    protected abstract void handleIntermediateDrawing();

    // ========== 公共方法 ==========
    public void setG(Graphics g) {
        this.g = (Graphics2D) g;
    }

    public ArrayList<String> getRecord() {
        return recordList;
    }

    public void update(String line) {
        recordList.add(line);
    }

    public void clearRecord() {
        recordList.clear();
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public int getThickness() {
        return this.thickness;
    }

    // ========== 未使用但必须实现的接口方法 ==========
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
} 
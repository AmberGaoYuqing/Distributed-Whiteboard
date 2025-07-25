package manager;

import common.BaseListener;
import common.ToolType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;


/**
 * Listener class for user interaction with the whiteboard.
 * Handles button actions, mouse presses, releases, and dragging events
 * to draw various shapes and broadcast drawing commands to connected clients.
 *
 * Inherits from BaseListener and implements drawing and communication logic.
 */
public class Listener extends BaseListener {
    private static final String ERROR_BROADCAST_MESSAGE = "Error broadcasting draw command: ";
    private static final String DRAW_COMMAND = "draw ";
    private static final String DRAWING_COMMAND = "drawing";


    public Listener() {
        super();
    }

    public Listener(JFrame frame) {
        super(frame);
    }

    // ========== ActionListener ==========
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Color".equals(e.getActionCommand())) {
            displayColorChooser();
        } else {
            configureCursorAndTool(e.getActionCommand());
        }
    }

    // ========== Mouse Press ==========
    @Override
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
        if (!g.getColor().equals(color)) {
            g.setColor(color);
        }
        if (tool == ToolType.FREE) {
            handleFreeDrawing(e);
        }
    }

    // ========== Mouse Release ==========
    @Override
    public void mouseReleased(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();
        prepareGraphics();

        if (tool == ToolType.ERASE) {
            handleEraseOperation();
        } else {
            record = buildRecordBasedOnTool(tool, startX, startY, endX, endY);
        }

        broadcastDrawingOperation();
    }

    // ========== Mouse Drag ==========
    // Handles continuous drawing or erasing while mouse is dragged.
    @Override
    public void mouseDragged(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();
        if (tool == ToolType.FREE || tool == ToolType.ERASE) {
            handleIntermediateDrawing();
            broadcastDrawLog();
        }
    }

    private void handleEraseOperation() {
        g.setColor(Color.WHITE);
        g.drawLine(startX, startY, endX, endY);
        record = buildRecord(ToolType.LINE, thickness, Color.WHITE, startX, startY, endX, endY, null, false);
    }


    // Draws intermediate segments while dragging the mouse.
    @Override
    protected void handleIntermediateDrawing() {
        Color drawColor = (tool == ToolType.ERASE) ? Color.WHITE : color;
        g.setColor(drawColor);
        g.setStroke(new BasicStroke(thickness));
        g.drawLine(startX, startY, endX, endY);
        record = buildRecord(ToolType.LINE, thickness, drawColor, startX, startY, endX, endY, null, true);
        recordList.add(record);
        startX = endX;
        startY = endY;
        broadcastDrawCommand();
    }


    private String buildRecordBasedOnTool(ToolType tool, int startX, int startY, int endX, int endY) {
        prepareGraphics();

        switch (tool) {
            case LINE, OVAL, RECT, TRIANGLE -> drawShape(tool, startX, startY, endX, endY);
            case TEXT -> {
                String inputText = JOptionPane.showInputDialog(frame, "Input Text Content", "Text Tool", JOptionPane.PLAIN_MESSAGE);
                if (inputText == null || inputText.trim().isEmpty()) return null;

                g.setFont(new Font(null, Font.PLAIN, thickness + TEXT_FONT_SIZE_BASE));
                g.drawString(inputText, startX, startY);
                return buildRecord(tool, thickness, color, startX, startY, 0, 0, inputText, false);
            }
        }
        return buildRecord(tool, thickness, color, startX, startY, endX, endY, null, false);
    }

    /**
     * Broadcasts the current drawing command to all connected clients.
     */
    private void broadcastDrawCommand() {
        try {
            ConnectionManager.broadcast(DRAW_COMMAND + record);
        } catch (IOException ex) {
            handleBroadcastError(ex);
        }
    }

    /**
     * Sends the final drawing operation and logs the draw action.
     */
    private void broadcastDrawingOperation() {
        if (record != null) {
            recordList.add(record);
            try {
                ConnectionManager.broadcast(DRAW_COMMAND + record);
                broadcastDrawLog();
            } catch (IOException ex) {
                handleBroadcastError(ex);
            }
        }
    }
    /**
     * Sends a draw log message with the current user's username.
     */
    private void broadcastDrawLog() {
        ConnectionManager.broadcastDrawLog(ManagerLogin.username, DRAWING_COMMAND);
    }

    private void handleBroadcastError(IOException ex) {
        System.err.println(ERROR_BROADCAST_MESSAGE + ex.getMessage());
        ex.printStackTrace();
    }

}

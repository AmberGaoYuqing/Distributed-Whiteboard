package guest;

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
    private static final String NETWORK_ERROR_MESSAGE = "Error sending draw command: ";

    /**
     * Default constructor.
     */
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
    /**
     * Handles drawing the final shape when mouse is released.
     * Based on the selected drawing tool.
     *
     * @param e mouse release event
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness));

        if (tool == ToolType.ERASE) {
            g.setColor(Color.WHITE);
            g.drawLine(startX, startY, endX, endY);
            record = buildRecord(ToolType.LINE, thickness, Color.WHITE, startX, startY, endX, endY, null, false);
        } else {
            record = buildRecordBasedOnTool(tool, startX, startY, endX, endY);
        }

        if (record != null) {
            recordList.add(record);
            sendDrawCommand(record);
        }
    }

    // ========== Mouse Drag ==========
    /**
     * Handles continuous drawing or erasing while mouse is dragged.
     *
     * @param e mouse drag event
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        endX = e.getX();
        endY = e.getY();
        if (tool == ToolType.FREE || tool == ToolType.ERASE) {
            handleIntermediateDrawing();
        }
    }

    /**
     * Draws a single line segment as part of a drag sequence.
     * Used for freehand drawing and erasing.
     */
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

        sendDrawCommand(record);
    }

    /**
     * Builds the drawing record string based on the selected tool and coordinates.
     * For text tool, prompts user input.
     *
     * @param tool   the drawing tool type
     * @param startX start x-coordinate
     * @param startY start y-coordinate
     * @param endX   end x-coordinate
     * @param endY   end y-coordinate
     * @return a string representing the drawing command
     */
    private String buildRecordBasedOnTool(ToolType tool, int startX, int startY, int endX, int endY) {
        prepareGraphics();

        switch (tool) {
            case LINE, OVAL, RECT, TRIANGLE -> drawShape(tool, startX, startY, endX, endY);
            case TEXT -> {
                String inputText = JOptionPane.showInputDialog(frame, "Enter text:", "Text Tool", JOptionPane.PLAIN_MESSAGE);
                if (inputText == null || inputText.trim().isEmpty()) return null;

                g.setFont(new Font(null, Font.PLAIN, thickness + TEXT_FONT_SIZE_BASE));
                g.drawString(inputText, startX, startY);
                return buildRecord(tool, thickness, color, startX, startY, 0, 0, inputText, false);
            }
        }
        return buildRecord(tool, thickness, color, startX, startY, endX, endY, null, false);
    }

    /**
     * Sends the draw command and draw log to the server.
     *
     * @param record the draw command string to send
     */
    private void sendDrawCommand(String record) {
        try {
            if (Guest.connection != null && Guest.connection.outputStream != null) {
                Guest.connection.outputStream.writeUTF("draw " + record);
                Guest.connection.outputStream.writeUTF("drawlog " + UserLogin.username);
                Guest.connection.outputStream.flush();
            } else {
                System.err.println("ClientConnection not initialized properly.");
            }
        } catch (IOException ex) {
            System.err.println(NETWORK_ERROR_MESSAGE + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

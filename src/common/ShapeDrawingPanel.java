package common;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class ShapeDrawingPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(ShapeDrawingPanel.class.getName());
    private ArrayList<String> recordList = new ArrayList<>();
    private static final int TEXT_FONT_SIZE_BASE = 10;

    @Override
    public void paint(Graphics gr) {
        super.paint(gr);
        draw((Graphics2D) gr, this.recordList);
    }

    public void setList(ArrayList<String> recordList) {
        this.recordList = recordList;
    }

    private DrawingParameters parseCommonParameters(String[] record) {
        int thickness = Integer.parseInt(record[1]);
        Color color = new Color(
                Integer.parseInt(record[2]),
                Integer.parseInt(record[3]),
                Integer.parseInt(record[4])
        );
        int startX = Integer.parseInt(record[5]);
        int startY = Integer.parseInt(record[6]);
        
        // For text drawing, we don't need endX and endY
        int endX = 0;
        int endY = 0;
        
        // Only parse endX and endY for non-text shapes
        if (!record[0].equals("Text") && record.length > 7) {
            endX = Integer.parseInt(record[7]);
            endY = record.length > 8 ? Integer.parseInt(record[8]) : 0;
        }

        return new DrawingParameters(thickness, color, startX, startY, endX, endY);
    }

    private void setupGraphics(Graphics2D g, DrawingParameters params) {
        g.setStroke(new BasicStroke(params.thickness()));
        g.setColor(params.color());
    }

    private void drawOval(Graphics2D g, DrawingParameters params) {
        int x = Math.min(params.startX(), params.endX());
        int y = Math.min(params.startY(), params.endY());
        int width = Math.abs(params.endX() - params.startX());
        int height = Math.abs(params.endY() - params.startY());
        g.drawOval(x, y, width, height);
    }

    private void drawRect(Graphics2D g, DrawingParameters params) {
        g.drawRect(
                Math.min(params.startX(), params.endX()),
                Math.min(params.startY(), params.endY()),
                Math.abs(params.startX() - params.endX()),
                Math.abs(params.startY() - params.endY())
        );
    }

    private void drawText(Graphics2D g, DrawingParameters params, String[] record) {
        // Find the text content after the delimiter
        String textContent = "";
        for (int i = 7; i < record.length; i++) {
            if (record[i].startsWith("|")) {
                textContent = record[i].substring(1);
                if (i + 1 < record.length) {
                    textContent += " " + String.join(" ", Arrays.copyOfRange(record, i + 1, record.length));
                }
                break;
            }
        }
        
        Font font = new Font(null, Font.PLAIN, params.thickness() + TEXT_FONT_SIZE_BASE);
        g.setFont(font);
        g.drawString(textContent, params.startX(), params.startY());
    }

    private void drawLine(Graphics2D g, DrawingParameters params) {
        g.drawLine(params.startX(), params.startY(), params.endX(), params.endY());
    }

    private void drawTriangle(Graphics2D g, DrawingParameters params) {
        int[] xPoints = {params.startX(), params.endX(), params.startX()};
        int[] yPoints = {params.startY(), params.endY(), params.endY()};
        g.drawPolygon(xPoints, yPoints, 3);
    }

    public void draw(Graphics2D g, ArrayList<String> recordList) {
        try {
            for (String line : recordList.toArray(new String[0])) {
                String[] record = line.split(" ");
                if (record[1].equals("!")) continue;

                DrawingParameters params = parseCommonParameters(record);
                setupGraphics(g, params);

                switch (ShapeType.valueOf(record[0].toUpperCase())) {
                    case OVAL -> drawOval(g, params);
                    case RECT -> drawRect(g, params);
                    case TEXT -> drawText(g, params, record);
                    case LINE -> drawLine(g, params);
                    case TRIANGLE -> drawTriangle(g, params);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Pain Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    public enum ShapeType {
        OVAL, RECT, TEXT, LINE, TRIANGLE
    }

    private record DrawingParameters(int thickness, Color color, int startX, int startY, int endX, int endY) {
    }
}

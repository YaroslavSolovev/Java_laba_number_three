package ui;

import model.RideRequest;
import model.TaxiState;
import taxi.Taxi;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class CityMapPanel extends JPanel {
    private static final int CITY_SIZE = 100;
    private static final Color BG_COLOR = new Color(240, 248, 255);
    private static final Color GRID_COLOR = new Color(200, 220, 240);
    private static final Color ROAD_COLOR = new Color(180, 180, 180);

    private final List<Taxi> taxiFleet;
    private final PriorityBlockingQueue<RideRequest> orderQueue;

    public CityMapPanel(List<Taxi> taxiFleet, PriorityBlockingQueue<RideRequest> orderQueue) {
        this.taxiFleet = taxiFleet;
        this.orderQueue = orderQueue;

        setBackground(BG_COLOR);
        setDoubleBuffered(true);
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Карта города",
                0,
                0,
                new Font("Arial", Font.BOLD, 14)
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        drawCityGrid(g2d);
        drawTaxis(g2d);
        drawLegend(g2d);
    }

    private void drawCityGrid(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        int gridSize = 10;

        g2d.setColor(GRID_COLOR);
        for (int i = 0; i <= gridSize; i++) {
            int x = (int) (width * i / (double) gridSize);
            int y = (int) (height * i / (double) gridSize);
            g2d.drawLine(x, 0, x, height);
            g2d.drawLine(0, y, width, y);
        }

        g2d.setColor(ROAD_COLOR);
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i <= gridSize; i += 2) {
            int x = (int) (width * i / (double) gridSize);
            int y = (int) (height * i / (double) gridSize);
            g2d.drawLine(x, 0, x, height);
            g2d.drawLine(0, y, width, y);
        }
        g2d.setStroke(new BasicStroke(1));
    }

    private void drawOrders(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();

        List<RideRequest> orders = new ArrayList<>(orderQueue);
        int maxOrders = Math.min(10, orders.size());
        for (int i = 0; i < maxOrders; i++) {
            RideRequest order = orders.get(i);
            model.Point pickup = order.getPickupLocation();
            model.Point dest = order.getDestination();

            int x1 = (int) (pickup.getX() * width / CITY_SIZE);
            int y1 = (int) (pickup.getY() * height / CITY_SIZE);
            int x2 = (int) (dest.getX() * width / CITY_SIZE);
            int y2 = (int) (dest.getY() * height / CITY_SIZE);

            g2d.setColor(new Color(100, 100, 100, 100));
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5}, 0));
            g2d.drawLine(x1, y1, x2, y2);
            g2d.setStroke(new BasicStroke(1));

            Color pickupColor = order.getPriority() == 2 ? Color.ORANGE :
                               order.getPriority() == 1 ? Color.YELLOW :
                               new Color(100, 200, 100);
            g2d.setColor(pickupColor);
            g2d.fillOval(x1 - 6, y1 - 6, 12, 12);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x1 - 6, y1 - 6, 12, 12);

            g2d.setColor(new Color(200, 100, 100));
            g2d.fillRect(x2 - 5, y2 - 5, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x2 - 5, y2 - 5, 10, 10);
        }
    }

    private static final Font TAXI_FONT = new Font("Arial", Font.BOLD, 12);

    private void drawTaxis(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();

        g2d.setFont(TAXI_FONT);

        for (Taxi taxi : taxiFleet) {
            model.Point location = taxi.getCurrentLocation();
            int x = (int) (location.getX() * width / CITY_SIZE);
            int y = (int) (location.getY() * height / CITY_SIZE);

            Color color;
            TaxiState state = taxi.getState();
            if (state == TaxiState.AVAILABLE) {
                color = Color.GREEN;
            } else if (state == TaxiState.GOING_TO_CLIENT) {
                color = Color.YELLOW;
            } else if (state == TaxiState.TRANSPORTING) {
                color = Color.RED;
            } else {
                color = Color.GRAY;
            }

            g2d.setColor(color);
            g2d.fillOval(x - 8, y - 8, 16, 16);

            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - 8, y - 8, 16, 16);

            String label = String.valueOf(taxi.getId());
            g2d.drawString(label, x - 4, y + 4);
        }
    }

    private void drawLegend(Graphics2D g2d) {
        int x = 10;
        int y = getHeight() - 80;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(x, y, 150, 70);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, 150, 70);

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int lineY = y + 15;

        g2d.setColor(Color.GREEN);
        g2d.fillOval(x + 10, lineY - 6, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Available", x + 25, lineY);
        lineY += 18;

        g2d.setColor(Color.YELLOW);
        g2d.fillOval(x + 10, lineY - 6, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Going to client", x + 25, lineY);
        lineY += 18;

        g2d.setColor(Color.RED);
        g2d.fillOval(x + 10, lineY - 6, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Transporting", x + 25, lineY);
    }
}

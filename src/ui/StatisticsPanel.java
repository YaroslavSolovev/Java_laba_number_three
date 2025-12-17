package ui;

import statistics.Statistics;

import javax.swing.*;
import java.awt.*;

public class StatisticsPanel extends JPanel {
    private final Statistics statistics;

    private final JLabel completedLabel;
    private final JLabel assignedLabel;
    private final JLabel failedLabel;
    private final JLabel revenueLabel;
    private final JLabel distanceLabel;

    public StatisticsPanel(Statistics statistics) {
        this.statistics = statistics;

        setLayout(new GridLayout(1, 5, 10, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Статистика работы системы"),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        setPreferredSize(new Dimension(0, 80));

        completedLabel = createStatLabel("Завершено поездок", "0");
        assignedLabel = createStatLabel("Назначено заказов", "0");
        failedLabel = createStatLabel("Не назначено", "0");
        revenueLabel = createStatLabel("Выручка", "0.00 руб");
        distanceLabel = createStatLabel("Дистанция", "0.0 км");

        add(completedLabel);
        add(assignedLabel);
        add(failedLabel);
        add(revenueLabel);
        add(distanceLabel);
    }

    private JLabel createStatLabel(String title, String value) {
        JLabel label = new JLabel();
        label.setLayout(new BorderLayout());
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        titleLabel.setForeground(Color.DARK_GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setForeground(new Color(0, 100, 200));

        label.add(titleLabel, BorderLayout.NORTH);
        label.add(valueLabel, BorderLayout.CENTER);

        return label;
    }

    public void updateStats() {
        updateLabelValue(completedLabel, String.valueOf(statistics.getTotalRidesCompleted()));
        updateLabelValue(assignedLabel, String.valueOf(statistics.getTotalOrdersAssigned()));
        updateLabelValue(failedLabel, String.valueOf(statistics.getTotalOrdersFailed()));
        updateLabelValue(revenueLabel, String.format("%.2f руб", statistics.getTotalRevenue()));
        updateLabelValue(distanceLabel, String.format("%.1f км", statistics.getTotalDistance()));
    }

    private void updateLabelValue(JLabel containerLabel, String newValue) {
        Component[] components = containerLabel.getComponents();
        if (components.length >= 2 && components[1] instanceof JLabel) {
            ((JLabel) components[1]).setText(newValue);
        }
    }
}

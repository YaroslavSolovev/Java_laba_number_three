package ui;

import model.HistoryEvent;
import statistics.RideHistory;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {
    private final RideHistory history;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel statsLabel;

    public HistoryPanel(RideHistory history) {
        this.history = history;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("История событий"));

        String[] columnNames = {"Время", "Событие", "Заказ", "Такси", "Клиент", "Детали"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setRowHeight(22);

        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(55);
        table.getColumnModel().getColumn(3).setPreferredWidth(55);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);

                if (!isSelected && column == 1) {
                    String event = value.toString();
                    if (event.equals("Новый заказ")) {
                        c.setBackground(new Color(220, 240, 255));
                    } else if (event.equals("Заказ назначен")) {
                        c.setBackground(new Color(220, 255, 220));
                    } else if (event.equals("Заказ не назначен")) {
                        c.setBackground(new Color(255, 220, 220));
                    } else if (event.equals("Поездка начата")) {
                        c.setBackground(new Color(255, 255, 200));
                    } else if (event.equals("Поездка завершена")) {
                        c.setBackground(new Color(200, 255, 200));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } else if (!isSelected) {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsLabel = new JLabel("Всего событий: 0");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        bottomPanel.add(statsLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void updateHistory() {
        int eventCount = history.getEventCount();

        List<HistoryEvent> events = history.getRecentEvents(50);

        int currentRowCount = tableModel.getRowCount();
        int displayCount = events.size();

        while (currentRowCount < displayCount) {
            tableModel.addRow(new Object[]{"", "", "", "", "", ""});
            currentRowCount++;
        }

        while (currentRowCount > displayCount) {
            tableModel.removeRow(currentRowCount - 1);
            currentRowCount--;
        }

        for (int i = 0; i < events.size(); i++) {
            HistoryEvent event = events.get(i);

            String timeStr = event.getFormattedTime();
            String typeStr = event.getType().getDisplayName();
            String orderStr = "#" + event.getOrderId();
            String taxiStr = event.getTaxiId() != null ? "#" + event.getTaxiId() : "-";
            String clientStr = event.getClientName() != null ? event.getClientName() : "-";
            String detailsStr = event.getDescription();

            if (!timeStr.equals(tableModel.getValueAt(i, 0))) {
                tableModel.setValueAt(timeStr, i, 0);
            }
            if (!typeStr.equals(tableModel.getValueAt(i, 1))) {
                tableModel.setValueAt(typeStr, i, 1);
            }
            if (!orderStr.equals(tableModel.getValueAt(i, 2))) {
                tableModel.setValueAt(orderStr, i, 2);
            }
            if (!taxiStr.equals(tableModel.getValueAt(i, 3))) {
                tableModel.setValueAt(taxiStr, i, 3);
            }
            if (!clientStr.equals(tableModel.getValueAt(i, 4))) {
                tableModel.setValueAt(clientStr, i, 4);
            }
            if (!detailsStr.equals(tableModel.getValueAt(i, 5))) {
                tableModel.setValueAt(detailsStr, i, 5);
            }
        }

        statsLabel.setText(String.format("Всего событий: %d | Показано: %d", eventCount, displayCount));
    }
}

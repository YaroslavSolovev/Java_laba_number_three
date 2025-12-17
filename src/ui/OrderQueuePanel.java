package ui;

import model.RideRequest;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class OrderQueuePanel extends JPanel {
    private final PriorityBlockingQueue<RideRequest> orderQueue;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel queueSizeLabel;

    public OrderQueuePanel(PriorityBlockingQueue<RideRequest> orderQueue) {
        this.orderQueue = orderQueue;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Очередь заказов"));

        String[] columnNames = {"ID", "Клиент", "Приоритет", "Дистанция"};
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

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);

                if (!isSelected && column == 2) {
                    String priority = value.toString();
                    if (priority.equals("VIP")) {
                        c.setBackground(new Color(255, 200, 100));
                    } else if (priority.equals("Высокий")) {
                        c.setBackground(new Color(255, 255, 150));
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
        queueSizeLabel = new JLabel("В очереди: 0 заказов");
        queueSizeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        bottomPanel.add(queueSizeLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void updateOrders() {
        int queueSize = orderQueue.size();

        List<RideRequest> orders = new ArrayList<>(Math.min(15, queueSize));
        int count = 0;
        for (RideRequest order : orderQueue) {
            if (count >= 15) break;
            orders.add(order);
            count++;
        }

        int currentRowCount = tableModel.getRowCount();
        int displayCount = orders.size();

        while (currentRowCount < displayCount) {
            tableModel.addRow(new Object[]{"", "", "", ""});
            currentRowCount++;
        }

        while (currentRowCount > displayCount) {
            tableModel.removeRow(currentRowCount - 1);
            currentRowCount--;
        }

        for (int i = 0; i < orders.size(); i++) {
            RideRequest order = orders.get(i);
            String priority = order.getPriority() == 2 ? "VIP" :
                             order.getPriority() == 1 ? "Высокий" : "Обычный";

            String idStr = "#" + order.getId();
            String clientStr = order.getClientName();
            String distStr = String.format("%.1f км", order.getDistance());

            if (!idStr.equals(tableModel.getValueAt(i, 0))) {
                tableModel.setValueAt(idStr, i, 0);
            }
            if (!clientStr.equals(tableModel.getValueAt(i, 1))) {
                tableModel.setValueAt(clientStr, i, 1);
            }
            if (!priority.equals(tableModel.getValueAt(i, 2))) {
                tableModel.setValueAt(priority, i, 2);
            }
            if (!distStr.equals(tableModel.getValueAt(i, 3))) {
                tableModel.setValueAt(distStr, i, 3);
            }
        }

        queueSizeLabel.setText(String.format("В очереди: %d заказов", queueSize));
    }
}

package ui;

import model.TaxiState;
import taxi.Taxi;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class TaxiListPanel extends JPanel {
    private final List<Taxi> taxiFleet;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel statsLabel;

    public TaxiListPanel(List<Taxi> taxiFleet) {
        this.taxiFleet = taxiFleet;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Статус такси"));

        String[] columnNames = {"ID", "Тип", "Состояние", "Локация"};
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

        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);

                if (!isSelected && column == 2) {
                    String state = value.toString();
                    if (state.equals("Доступен")) {
                        c.setBackground(new Color(200, 255, 200));
                    } else if (state.equals("Едет к клиенту")) {
                        c.setBackground(new Color(255, 255, 200));
                    } else if (state.equals("Везет пассажира")) {
                        c.setBackground(new Color(255, 220, 220));
                    } else {
                        c.setBackground(Color.LIGHT_GRAY);
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
        statsLabel = new JLabel("Доступно: 0 | Занято: 0");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        bottomPanel.add(statsLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void updateTaxis() {
        int available = 0;
        int busy = 0;

        int currentRowCount = tableModel.getRowCount();
        int taxiCount = taxiFleet.size();

        while (currentRowCount < taxiCount) {
            tableModel.addRow(new Object[]{"", "", "", ""});
            currentRowCount++;
        }

        while (currentRowCount > taxiCount) {
            tableModel.removeRow(currentRowCount - 1);
            currentRowCount--;
        }

        for (int i = 0; i < taxiFleet.size(); i++) {
            Taxi taxi = taxiFleet.get(i);
            TaxiState state = taxi.getState();

            String idStr = "#" + taxi.getId();
            String typeStr = taxi.getType().toString();
            String stateStr = state.toString();
            String locStr = taxi.getCurrentLocation().toString();

            if (!idStr.equals(tableModel.getValueAt(i, 0))) {
                tableModel.setValueAt(idStr, i, 0);
            }
            if (!typeStr.equals(tableModel.getValueAt(i, 1))) {
                tableModel.setValueAt(typeStr, i, 1);
            }
            if (!stateStr.equals(tableModel.getValueAt(i, 2))) {
                tableModel.setValueAt(stateStr, i, 2);
            }
            if (!locStr.equals(tableModel.getValueAt(i, 3))) {
                tableModel.setValueAt(locStr, i, 3);
            }

            if (state == TaxiState.AVAILABLE) {
                available++;
            } else if (state == TaxiState.GOING_TO_CLIENT ||
                      state == TaxiState.TRANSPORTING) {
                busy++;
            }
        }

        statsLabel.setText(String.format("Доступно: %d | Занято: %d", available, busy));
    }
}

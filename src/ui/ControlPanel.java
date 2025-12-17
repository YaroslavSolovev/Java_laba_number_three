package ui;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private final TaxiSystemUI parentUI;

    private final JButton startButton;
    private final JButton pauseButton;
    private final JButton resumeButton;
    private final JButton stopButton;
    private final JLabel statusLabel;

    private boolean isRunning = false;
    private boolean isPaused = false;

    public ControlPanel(TaxiSystemUI parentUI) {
        this.parentUI = parentUI;

        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Управление"));
        setPreferredSize(new Dimension(0, 80));

        startButton = new JButton("Запустить систему");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(100, 200, 100));
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> startSystem());
        startButton.setVisible(false);

        pauseButton = new JButton("⏸ Пауза");
        pauseButton.setFont(new Font("Arial", Font.PLAIN, 14));
        pauseButton.setEnabled(false);
        pauseButton.setFocusPainted(false);
        pauseButton.addActionListener(e -> pauseSystem());

        resumeButton = new JButton("▶ Возобновить");
        resumeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        resumeButton.setEnabled(false);
        resumeButton.setFocusPainted(false);
        resumeButton.addActionListener(e -> resumeSystem());

        stopButton = new JButton("⏹ Остановить");
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setBackground(new Color(255, 150, 150));
        stopButton.setEnabled(false);
        stopButton.setFocusPainted(false);
        stopButton.addActionListener(e -> stopSystem());

        statusLabel = new JLabel("⏳ Загрузка системы...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(0, 100, 200));

        add(pauseButton);
        add(resumeButton);
        add(stopButton);
        add(Box.createHorizontalStrut(20));
        add(statusLabel);
    }

    private void startSystem() {
        parentUI.startSystem();
    }

    private void pauseSystem() {
        parentUI.pauseSystem();
    }

    private void resumeSystem() {
        parentUI.resumeSystem();
    }

    private void stopSystem() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Вы уверены, что хотите остановить систему?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            parentUI.shutdown();
        }
    }

    public void onSystemStarted() {
        isRunning = true;
        isPaused = false;

        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        resumeButton.setEnabled(false);
        stopButton.setEnabled(true);

        statusLabel.setText("✅ Система работает | Заказы генерируются каждые 0.5 сек");
        statusLabel.setForeground(new Color(0, 150, 0));
    }

    public void onSystemPaused() {
        isPaused = true;

        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);

        statusLabel.setText("Система на паузе");
        statusLabel.setForeground(new Color(200, 150, 0));
    }

    public void onSystemResumed() {
        isPaused = false;

        pauseButton.setEnabled(true);
        resumeButton.setEnabled(false);

        statusLabel.setText("Система работает");
        statusLabel.setForeground(new Color(0, 150, 0));
    }
}

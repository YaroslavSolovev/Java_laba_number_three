package ui;

import dispatcher.Dispatcher;
import generator.ClientGenerator;
import model.RideRequest;
import model.TaxiType;
import statistics.Statistics;
import statistics.RideHistory;
import taxi.Taxi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class TaxiSystemUI extends JFrame {
    private static final int WINDOW_WIDTH = 1600;
    private static final int WINDOW_HEIGHT = 900;

    private final CityMapPanel mapPanel;
    private final StatisticsPanel statsPanel;
    private final OrderQueuePanel orderPanel;
    private final TaxiListPanel taxiPanel;
    private final HistoryPanel historyPanel;
    private final ControlPanel controlPanel;

    private final PriorityBlockingQueue<RideRequest> orderQueue;
    private final ConcurrentHashMap<Integer, BlockingQueue<RideRequest>> taxiQueues;
    private final List<Taxi> taxiFleet;
    private final ExecutorService executorService;
    private final Statistics statistics;
    private final RideHistory history;

    private Dispatcher dispatcher;
    private ClientGenerator clientGenerator;
    private javax.swing.Timer uiUpdateTimer;

    public TaxiSystemUI() {
        super("Система управления беспилотными такси v2.0");

        this.orderQueue = new PriorityBlockingQueue<>(100);
        this.taxiQueues = new ConcurrentHashMap<>();
        this.taxiFleet = new ArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.statistics = new Statistics();
        this.history = new RideHistory();

        mapPanel = new CityMapPanel(taxiFleet, orderQueue);
        statsPanel = new StatisticsPanel(statistics);
        orderPanel = new OrderQueuePanel(orderQueue);
        taxiPanel = new TaxiListPanel(taxiFleet);
        historyPanel = new HistoryPanel(history);
        controlPanel = new ControlPanel(this);

        setupUI();
        setupUpdateTimer();
        initializeTaxiFleet();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(statsPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.add(mapPanel);
        centerPanel.add(historyPanel);
        add(centerPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        rightPanel.setPreferredSize(new Dimension(350, WINDOW_HEIGHT));
        rightPanel.add(orderPanel);
        rightPanel.add(taxiPanel);
        add(rightPanel, BorderLayout.EAST);

        add(controlPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                javax.swing.Timer startTimer = new javax.swing.Timer(300, evt -> {
                    startSystem();
                });
                startTimer.setRepeats(false);
                startTimer.start();
            }
        });
    }

    private int updateCounter = 0;

    private void setupUpdateTimer() {
        uiUpdateTimer = new javax.swing.Timer(500, e -> updateUI());
    }

    private void updateUI() {
        try {
            updateCounter++;

            mapPanel.repaint();

            if (updateCounter % 2 == 0) {
                statsPanel.updateStats();
            }

            if (updateCounter % 3 == 0) {
                orderPanel.updateOrders();
                taxiPanel.updateTaxis();
                historyPanel.updateHistory();
            }
        } catch (Exception e) {
        }
    }

    private void initializeTaxiFleet() {
        Random random = new Random();
        int taxiId = 1;

        for (int i = 0; i < 3; i++) {
            createTaxi(taxiId++, TaxiType.ECONOMY, random);
        }

        for (int i = 0; i < 2; i++) {
            createTaxi(taxiId++, TaxiType.COMFORT, random);
        }

        createTaxi(taxiId++, TaxiType.PREMIUM, random);
    }

    private void createTaxi(int id, TaxiType type, Random random) {
        model.Point initialLocation = new model.Point(random.nextDouble() * 100, random.nextDouble() * 100);
        BlockingQueue<RideRequest> taxiQueue = new LinkedBlockingQueue<>();
        Taxi taxi = new Taxi(id, type, initialLocation, taxiQueue, statistics, history);

        taxiFleet.add(taxi);
        taxiQueues.put(id, taxiQueue);
    }

    public void startSystem() {
        new Thread(() -> {
            for (Taxi taxi : taxiFleet) {
                executorService.execute(taxi);
            }

            dispatcher = new Dispatcher(orderQueue, taxiFleet, taxiQueues, statistics, history);
            executorService.execute(dispatcher);

            clientGenerator = new ClientGenerator(orderQueue, 2000, 1000, history);
            executorService.execute(clientGenerator);

            SwingUtilities.invokeLater(() -> {
                uiUpdateTimer.start();
                controlPanel.onSystemStarted();
            });
        }, "System-Starter").start();
    }

    public void pauseSystem() {
        uiUpdateTimer.stop();
        controlPanel.onSystemPaused();
    }

    public void resumeSystem() {
        uiUpdateTimer.start();
        controlPanel.onSystemResumed();
    }

    public void shutdown() {
        if (uiUpdateTimer != null) {
            uiUpdateTimer.stop();
        }

        new Thread(() -> {
            try {
                if (clientGenerator != null) {
                    clientGenerator.shutdown();
                }

                if (dispatcher != null) {
                    dispatcher.shutdown();
                }

                for (Taxi taxi : taxiFleet) {
                    taxi.shutdown();
                }

                executorService.shutdown();
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }

                showFinalStatistics();
            } catch (Exception e) {
                executorService.shutdownNow();
            } finally {
                System.exit(0);
            }
        }, "Shutdown-Thread").start();
    }

    private void showFinalStatistics() {
        statistics.printSummary();
        statistics.printTaxiStats();
    }

    public static void main(String[] args) {
        java.util.logging.LogManager.getLogManager().reset();
        java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.OFF);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("ERROR: Could not set look and feel");
                e.printStackTrace();
            }

            try {
                TaxiSystemUI ui = new TaxiSystemUI();
                ui.setVisible(true);
            } catch (Exception e) {
                System.err.println("ERROR: Could not create UI");
                e.printStackTrace();
            }
        });
    }
}

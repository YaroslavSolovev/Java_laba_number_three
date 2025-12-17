import dispatcher.Dispatcher;
import generator.ClientGenerator;
import model.*;
import statistics.Statistics;
import statistics.RideHistory;
import taxi.Taxi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.logging.*;

public class TaxiSystem {
    private static final Logger LOGGER = Logger.getLogger(TaxiSystem.class.getName());

    private static final int TAXI_COUNT = 10;
    private static final int ECONOMY_TAXIS = 5;
    private static final int COMFORT_TAXIS = 3;
    private static final int PREMIUM_TAXIS = 2;

    private static final long CLIENT_GENERATION_INTERVAL_MS = 2000;
    private static final int TOTAL_REQUESTS = 50;
    private static final int ORDER_QUEUE_CAPACITY = 100;

    private final PriorityBlockingQueue<RideRequest> orderQueue;
    private final ConcurrentHashMap<Integer, BlockingQueue<RideRequest>> taxiQueues;
    private final List<Taxi> taxiFleet;
    private final ExecutorService executorService;
    private final Statistics statistics;
    private final RideHistory history;

    private Dispatcher dispatcher;
    private ClientGenerator clientGenerator;

    public TaxiSystem() {
        this.orderQueue = new PriorityBlockingQueue<>(ORDER_QUEUE_CAPACITY);
        this.taxiQueues = new ConcurrentHashMap<>();
        this.taxiFleet = new ArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.statistics = new Statistics();
        this.history = new RideHistory();

        setupLogging();
    }

    private void setupLogging() {
        try {
            LogManager.getLogManager().reset();
            Logger rootLogger = Logger.getLogger("");

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleFormatter() {
                private static final String format = "[%1$tT] [%2$s] %3$s %n";

                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                            lr.getMillis(),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage()
                    );
                }
            });
            rootLogger.addHandler(consoleHandler);

            FileHandler fileHandler = new FileHandler("taxi_system.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fileHandler);

            rootLogger.setLevel(Level.INFO);

        } catch (IOException e) {
            LOGGER.severe("Ошибка настройки логирования: " + e.getMessage());
        }
    }

    private void initializeTaxiFleet() {
        LOGGER.info("Инициализация парка такси...");
        Random random = new Random();

        int taxiId = 1;

        for (int i = 0; i < ECONOMY_TAXIS; i++) {
            Point initialLocation = new Point(random.nextDouble() * 100, random.nextDouble() * 100);
            BlockingQueue<RideRequest> taxiQueue = new LinkedBlockingQueue<>();
            Taxi taxi = new Taxi(taxiId, TaxiType.ECONOMY, initialLocation, taxiQueue, statistics, history);

            taxiFleet.add(taxi);
            taxiQueues.put(taxiId, taxiQueue);
            taxiId++;
        }

        for (int i = 0; i < COMFORT_TAXIS; i++) {
            Point initialLocation = new Point(random.nextDouble() * 100, random.nextDouble() * 100);
            BlockingQueue<RideRequest> taxiQueue = new LinkedBlockingQueue<>();
            Taxi taxi = new Taxi(taxiId, TaxiType.COMFORT, initialLocation, taxiQueue, statistics, history);

            taxiFleet.add(taxi);
            taxiQueues.put(taxiId, taxiQueue);
            taxiId++;
        }

        for (int i = 0; i < PREMIUM_TAXIS; i++) {
            Point initialLocation = new Point(random.nextDouble() * 100, random.nextDouble() * 100);
            BlockingQueue<RideRequest> taxiQueue = new LinkedBlockingQueue<>();
            Taxi taxi = new Taxi(taxiId, TaxiType.PREMIUM, initialLocation, taxiQueue, statistics, history);

            taxiFleet.add(taxi);
            taxiQueues.put(taxiId, taxiQueue);
            taxiId++;
        }

        LOGGER.info(String.format("Парк инициализирован: %d такси (Эконом: %d, Комфорт: %d, Премиум: %d)",
                taxiFleet.size(), ECONOMY_TAXIS, COMFORT_TAXIS, PREMIUM_TAXIS));
    }

    public void start() {
        LOGGER.info("╔════════════════════════════════════════════════════════════════╗");
        LOGGER.info("║        СИСТЕМА УПРАВЛЕНИЯ БЕСПИЛОТНЫМИ ТАКСИ v1.0              ║");
        LOGGER.info("╚════════════════════════════════════════════════════════════════╝");

        initializeTaxiFleet();

        LOGGER.info("Запуск такси...");
        for (Taxi taxi : taxiFleet) {
            executorService.execute(taxi);
        }

        LOGGER.info("Запуск диспетчера...");
        dispatcher = new Dispatcher(orderQueue, taxiFleet, taxiQueues, statistics, history);
        executorService.execute(dispatcher);

        LOGGER.info(String.format("Запуск генератора клиентов (интервал: %d мс, всего заказов: %d)...",
                CLIENT_GENERATION_INTERVAL_MS, TOTAL_REQUESTS));
        clientGenerator = new ClientGenerator(orderQueue, CLIENT_GENERATION_INTERVAL_MS, TOTAL_REQUESTS, history);
        executorService.execute(clientGenerator);

        LOGGER.info("Система успешно запущена!");
    }

    public void shutdown() {
        LOGGER.info("\nНачало завершения работы системы...");

        if (clientGenerator != null) {
            clientGenerator.shutdown();
            LOGGER.info("Генератор клиентов остановлен");
        }

        try {
            LOGGER.info("Ожидание завершения обработки заказов...");
            while (!orderQueue.isEmpty() || hasActiveTaxis()) {
                Thread.sleep(1000);
            }
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (dispatcher != null) {
            dispatcher.shutdown();
            LOGGER.info("Диспетчер остановлен");
        }

        LOGGER.info("Остановка такси...");
        for (Taxi taxi : taxiFleet) {
            taxi.shutdown();
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        LOGGER.info("Все потоки завершены");

        printFinalStatistics();
    }

    private boolean hasActiveTaxis() {
        return taxiFleet.stream()
                .anyMatch(taxi -> taxi.getState() != TaxiState.AVAILABLE &&
                                 taxi.getState() != TaxiState.OFFLINE);
    }

    private void printFinalStatistics() {
        System.out.println("\n\n");
        statistics.printSummary();
        statistics.printTaxiStats();

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              СИСТЕМА УСПЕШНО ЗАВЕРШИЛА РАБОТУ                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
    }

    public static void main(String[] args) {
        TaxiSystem system = new TaxiSystem();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("\nПолучен сигнал завершения работы...");
            system.shutdown();
        }));

        try {
            system.start();

            Thread.sleep(Long.MAX_VALUE);

        } catch (InterruptedException e) {
            LOGGER.info("Главный поток прерван");
            Thread.currentThread().interrupt();
        } finally {
            system.shutdown();
        }
    }
}

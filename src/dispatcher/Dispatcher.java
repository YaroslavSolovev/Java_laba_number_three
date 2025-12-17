package dispatcher;

import model.*;
import taxi.Taxi;
import statistics.Statistics;
import statistics.RideHistory;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Dispatcher implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());
    private static final int ASSIGNMENT_TIMEOUT_MS = 5000;

    private final PriorityBlockingQueue<RideRequest> orderQueue;
    private final List<Taxi> taxiFleet;
    private final ConcurrentHashMap<Integer, BlockingQueue<RideRequest>> taxiQueues;
    private final Statistics statistics;
    private final RideHistory history;
    private final ScheduledExecutorService monitorService;

    private volatile boolean running;

    public Dispatcher(PriorityBlockingQueue<RideRequest> orderQueue,
                     List<Taxi> taxiFleet,
                     ConcurrentHashMap<Integer, BlockingQueue<RideRequest>> taxiQueues,
                     Statistics statistics,
                     RideHistory history) {
        this.orderQueue = orderQueue;
        this.taxiFleet = taxiFleet;
        this.taxiQueues = taxiQueues;
        this.statistics = statistics;
        this.history = history;
        this.running = true;
        this.monitorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void run() {
        LOGGER.info("Диспетчер начал работу");

        monitorService.scheduleAtFixedRate(this::printSystemStatus, 5, 10, TimeUnit.SECONDS);

        try {
            while (running) {
                try {
                    RideRequest request = orderQueue.poll(1, TimeUnit.SECONDS);

                    if (request != null) {
                        assignOrder(request);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            monitorService.shutdown();
        }
    }

    private void assignOrder(RideRequest request) {
        LOGGER.info(String.format("Диспетчер обрабатывает: %s", request));

        Taxi bestTaxi = findBestTaxi(request);

        if (bestTaxi != null) {
            BlockingQueue<RideRequest> taxiQueue = taxiQueues.get(bestTaxi.getId());
            if (taxiQueue != null) {
                try {
                    if (taxiQueue.offer(request, ASSIGNMENT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                        statistics.recordOrderAssigned();
                        double distance = bestTaxi.getCurrentLocation().distanceTo(request.getPickupLocation());

                        history.recordOrderAssigned(
                            request.getId(),
                            bestTaxi.getId(),
                            request.getClientName(),
                            String.format("Тип: %s, Расст. до клиента: %.1f км", bestTaxi.getType(), distance)
                        );

                        LOGGER.info(String.format("Заказ #%d назначен такси #%d (%s) в точке %s (расстояние до клиента: %.1f км)",
                                request.getId(), bestTaxi.getId(), bestTaxi.getType(),
                                bestTaxi.getCurrentLocation(), distance));
                    } else {
                        handleFailedAssignment(request, "таймаут при назначении");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    handleFailedAssignment(request, "прерывание");
                }
            } else {
                handleFailedAssignment(request, "очередь такси не найдена");
            }
        } else {
            handleFailedAssignment(request, "нет доступных такси");
        }
    }

    private Taxi findBestTaxi(RideRequest request) {
        Taxi bestTaxi = null;
        double minDistance = Double.MAX_VALUE;

        for (Taxi taxi : taxiFleet) {
            if (taxi.isAvailable()) {
                double distance = taxi.getCurrentLocation().distanceTo(request.getPickupLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    bestTaxi = taxi;
                }
            }
        }

        return bestTaxi;
    }

    private void handleFailedAssignment(RideRequest request, String reason) {
        statistics.recordOrderFailed();

        LOGGER.warning(String.format("Не удалось назначить заказ #%d: %s. Возврат в очередь.",
                request.getId(), reason));

        try {
            Thread.sleep(1000);
            orderQueue.offer(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void printSystemStatus() {
        int available = 0;
        int busy = 0;
        int offline = 0;

        for (Taxi taxi : taxiFleet) {
            switch (taxi.getState()) {
                case AVAILABLE:
                    available++;
                    break;
                case GOING_TO_CLIENT:
                case TRANSPORTING:
                    busy++;
                    break;
                case OFFLINE:
                    offline++;
                    break;
            }
        }

        LOGGER.info(String.format(
                "\n=== СТАТУС СИСТЕМЫ ===\n" +
                "Заказов в очереди: %d\n" +
                "Такси доступно: %d\n" +
                "Такси занято: %d\n" +
                "Такси оффлайн: %d\n" +
                "======================",
                orderQueue.size(), available, busy, offline
        ));
    }

    public int getQueueSize() {
        return orderQueue.size();
    }

    public long getAvailableTaxiCount() {
        return taxiFleet.stream()
                .filter(Taxi::isAvailable)
                .count();
    }

    public void shutdown() {
        running = false;
        monitorService.shutdown();
        try {
            if (!monitorService.awaitTermination(5, TimeUnit.SECONDS)) {
                monitorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

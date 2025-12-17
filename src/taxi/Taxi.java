package taxi;

import model.*;
import statistics.Statistics;
import statistics.RideHistory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class Taxi implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Taxi.class.getName());
    private static final int SPEED_KM_PER_HOUR = 180;
    private static final int MS_PER_HOUR = 3600000;

    private final int id;
    private final TaxiType type;
    private final BlockingQueue<RideRequest> assignedOrders;
    private final Statistics statistics;
    private final RideHistory history;
    private final ReentrantLock stateLock;

    private volatile TaxiState state;
    private volatile Point currentLocation;
    private volatile boolean running;
    private volatile RideRequest currentRide;

    public Taxi(int id, TaxiType type, Point initialLocation,
                BlockingQueue<RideRequest> assignedOrders, Statistics statistics, RideHistory history) {
        this.id = id;
        this.type = type;
        this.currentLocation = initialLocation;
        this.assignedOrders = assignedOrders;
        this.statistics = statistics;
        this.history = history;
        this.state = TaxiState.AVAILABLE;
        this.stateLock = new ReentrantLock();
        this.running = true;
    }

    @Override
    public void run() {
        LOGGER.info(String.format("Такси #%d (%s) начало работу в точке %s",
                                  id, type, currentLocation));

        try {
            while (running) {
                try {
                    RideRequest request = assignedOrders.poll(1, TimeUnit.SECONDS);

                    if (request != null) {
                        processRide(request);
                    }
                } catch (InterruptedException e) {
                    LOGGER.info(String.format("Такси #%d прервано", id));
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            setState(TaxiState.OFFLINE);
            LOGGER.info(String.format("Такси #%d завершило работу", id));
        }
    }

    private void processRide(RideRequest request) {
        stateLock.lock();
        try {
            currentRide = request;

            LOGGER.info(String.format("Такси #%d (%s) получило заказ: %s",
                                      id, type, request));

            goToClient(request.getPickupLocation());

            transportClient(request);

            completeRide(request);

        } finally {
            currentRide = null;
            stateLock.unlock();
        }
    }

    private void goToClient(Point pickupLocation) {
        setState(TaxiState.GOING_TO_CLIENT);
        double distance = currentLocation.distanceTo(pickupLocation);
        long travelTime = calculateTravelTime(distance);

        LOGGER.info(String.format("Такси #%d едет к клиенту (%.1f км, ~%d сек)",
                                  id, distance, travelTime / 1000));

        simulateTravel(travelTime);
        currentLocation = pickupLocation;

        LOGGER.info(String.format("Такси #%d прибыло к клиенту в точке %s",
                                  id, currentLocation));
    }

    private void transportClient(RideRequest request) {
        setState(TaxiState.TRANSPORTING);
        double distance = request.getDistance();
        long travelTime = calculateTravelTime(distance);
        double price = calculatePrice(distance);

        history.recordRideStarted(
            request.getId(),
            id,
            request.getClientName(),
            String.format("Дистанция: %.1f км, Цена: %.2f руб", distance, price)
        );

        LOGGER.info(String.format("Такси #%d везет клиента [%s] (%.1f км, ~%d сек, стоимость: %.2f руб)",
                                  id, request.getClientName(), distance,
                                  travelTime / 1000, price));

        simulateTravel(travelTime);
        currentLocation = request.getDestination();

        LOGGER.info(String.format("Такси #%d завершило перевозку клиента [%s] в точке %s",
                                  id, request.getClientName(), currentLocation));
    }

    private void completeRide(RideRequest request) {
        setState(TaxiState.AVAILABLE);
        double distance = request.getDistance();
        double price = calculatePrice(distance);

        statistics.recordCompletedRide(id, distance, price);

        history.recordRideCompleted(
            request.getId(),
            id,
            request.getClientName(),
            String.format("Выручка: %.2f руб, Дистанция: %.1f км", price, distance)
        );

        LOGGER.info(String.format("Такси #%d доступно для новых заказов в точке %s",
                                  id, currentLocation));
    }

    private long calculateTravelTime(double distanceKm) {
        double baseTime = (distanceKm / SPEED_KM_PER_HOUR) * MS_PER_HOUR;
        double adjustedTime = baseTime / type.getSpeedMultiplier();
        return (long) (adjustedTime / 60);
    }

    private double calculatePrice(double distanceKm) {
        return distanceKm * type.getBasePricePerKm();
    }

    private void simulateTravel(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning(String.format("Такси #%d прервано во время поездки", id));
        }
    }

    private void setState(TaxiState newState) {
        stateLock.lock();
        try {
            this.state = newState;
        } finally {
            stateLock.unlock();
        }
    }

    public void shutdown() {
        running = false;
    }

    public int getId() {
        return id;
    }

    public TaxiType getType() {
        return type;
    }

    public TaxiState getState() {
        return state;
    }

    public Point getCurrentLocation() {
        return currentLocation;
    }

    public boolean isAvailable() {
        return state == TaxiState.AVAILABLE;
    }

    public RideRequest getCurrentRide() {
        return currentRide;
    }

    @Override
    public String toString() {
        return String.format("Такси #%d (%s) [%s] @ %s",
                             id, type, getState(), currentLocation);
    }
}

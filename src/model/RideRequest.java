package model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class RideRequest implements Comparable<RideRequest> {
    private static final AtomicLong idGenerator = new AtomicLong(0);

    private final long id;
    private final Point pickupLocation;
    private final Point destination;
    private final LocalDateTime timestamp;
    private final int priority;
    private final String clientName;

    public RideRequest(Point pickupLocation, Point destination, int priority, String clientName) {
        this.id = idGenerator.incrementAndGet();
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.timestamp = LocalDateTime.now();
        this.priority = priority;
        this.clientName = clientName;
    }

    public RideRequest(Point pickupLocation, Point destination) {
        this(pickupLocation, destination, 0, "Client-" + idGenerator.get());
    }

    public long getId() {
        return id;
    }

    public Point getPickupLocation() {
        return pickupLocation;
    }

    public Point getDestination() {
        return destination;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getPriority() {
        return priority;
    }

    public String getClientName() {
        return clientName;
    }

    public double getDistance() {
        return pickupLocation.distanceTo(destination);
    }

    @Override
    public int compareTo(RideRequest other) {
        int priorityComparison = Integer.compare(other.priority, this.priority);
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("Заказ #%d [%s] %s -> %s (приоритет: %d, дистанция: %.1f км)",
                id, clientName, pickupLocation, destination, priority, getDistance());
    }
}

package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryEvent {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final LocalDateTime timestamp;
    private final EventType type;
    private final long orderId;
    private final Integer taxiId;
    private final String clientName;
    private final String description;

    public enum EventType {
        ORDER_CREATED("Новый заказ"),
        ORDER_ASSIGNED("Заказ назначен"),
        ORDER_FAILED("Заказ не назначен"),
        RIDE_STARTED("Поездка начата"),
        RIDE_COMPLETED("Поездка завершена");

        private final String displayName;

        EventType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public HistoryEvent(EventType type, long orderId, Integer taxiId, String clientName, String description) {
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.orderId = orderId;
        this.taxiId = taxiId;
        this.clientName = clientName;
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public EventType getType() {
        return type;
    }

    public long getOrderId() {
        return orderId;
    }

    public Integer getTaxiId() {
        return taxiId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedTime() {
        return timestamp.format(TIME_FORMATTER);
    }

    @Override
    public String toString() {
        String taxiInfo = taxiId != null ? ", Такси #" + taxiId : "";
        return String.format("[%s] %s - Заказ #%d%s: %s",
                getFormattedTime(), type.getDisplayName(), orderId, taxiInfo, description);
    }
}

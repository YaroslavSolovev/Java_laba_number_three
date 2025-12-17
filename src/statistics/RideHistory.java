package statistics;

import model.HistoryEvent;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.List;
import java.util.ArrayList;

public class RideHistory {
    private static final int MAX_HISTORY_SIZE = 500;
    private final ConcurrentLinkedDeque<HistoryEvent> events;

    public RideHistory() {
        this.events = new ConcurrentLinkedDeque<>();
    }

    public void addEvent(HistoryEvent event) {
        events.addFirst(event);

        while (events.size() > MAX_HISTORY_SIZE) {
            events.removeLast();
        }
    }

    public void recordOrderCreated(long orderId, String clientName, String details) {
        addEvent(new HistoryEvent(
                HistoryEvent.EventType.ORDER_CREATED,
                orderId,
                null,
                clientName,
                details
        ));
    }

    public void recordOrderAssigned(long orderId, int taxiId, String clientName, String details) {
        addEvent(new HistoryEvent(
                HistoryEvent.EventType.ORDER_ASSIGNED,
                orderId,
                taxiId,
                clientName,
                details
        ));
    }

    public void recordOrderFailed(long orderId, String clientName, String reason) {
        addEvent(new HistoryEvent(
                HistoryEvent.EventType.ORDER_FAILED,
                orderId,
                null,
                clientName,
                reason
        ));
    }

    public void recordRideStarted(long orderId, int taxiId, String clientName, String details) {
        addEvent(new HistoryEvent(
                HistoryEvent.EventType.RIDE_STARTED,
                orderId,
                taxiId,
                clientName,
                details
        ));
    }

    public void recordRideCompleted(long orderId, int taxiId, String clientName, String details) {
        addEvent(new HistoryEvent(
                HistoryEvent.EventType.RIDE_COMPLETED,
                orderId,
                taxiId,
                clientName,
                details
        ));
    }

    public List<HistoryEvent> getRecentEvents(int count) {
        List<HistoryEvent> result = new ArrayList<>();
        int added = 0;
        for (HistoryEvent event : events) {
            if (added >= count) break;
            result.add(event);
            added++;
        }
        return result;
    }

    public List<HistoryEvent> getAllEvents() {
        return new ArrayList<>(events);
    }

    public int getEventCount() {
        return events.size();
    }

    public void clear() {
        events.clear();
    }
}

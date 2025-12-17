package statistics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.logging.Logger;

public class Statistics {
    private static final Logger LOGGER = Logger.getLogger(Statistics.class.getName());

    private final LocalDateTime startTime;
    private final AtomicInteger totalRidesCompleted;
    private final AtomicInteger totalOrdersAssigned;
    private final AtomicInteger totalOrdersFailed;
    private final DoubleAccumulator totalDistance;
    private final DoubleAccumulator totalRevenue;

    private final ConcurrentHashMap<Integer, TaxiStatistics> taxiStats;

    public Statistics() {
        this.startTime = LocalDateTime.now();
        this.totalRidesCompleted = new AtomicInteger(0);
        this.totalOrdersAssigned = new AtomicInteger(0);
        this.totalOrdersFailed = new AtomicInteger(0);
        this.totalDistance = new DoubleAccumulator(Double::sum, 0.0);
        this.totalRevenue = new DoubleAccumulator(Double::sum, 0.0);
        this.taxiStats = new ConcurrentHashMap<>();
    }

    public void recordCompletedRide(int taxiId, double distance, double price) {
        totalRidesCompleted.incrementAndGet();
        totalDistance.accumulate(distance);
        totalRevenue.accumulate(price);

        taxiStats.computeIfAbsent(taxiId, k -> new TaxiStatistics())
                .recordRide(distance, price);
    }

    public void recordOrderAssigned() {
        totalOrdersAssigned.incrementAndGet();
    }

    public void recordOrderFailed() {
        totalOrdersFailed.incrementAndGet();
    }

    public void printSummary() {
        Duration uptime = Duration.between(startTime, LocalDateTime.now());
        long uptimeSeconds = uptime.getSeconds();

        double avgDistance = totalRidesCompleted.get() > 0
                ? totalDistance.get() / totalRidesCompleted.get()
                : 0.0;

        double avgRevenue = totalRidesCompleted.get() > 0
                ? totalRevenue.get() / totalRidesCompleted.get()
                : 0.0;

        double ridesPerMinute = uptimeSeconds > 0
                ? (totalRidesCompleted.get() * 60.0) / uptimeSeconds
                : 0.0;

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║           ИТОГОВАЯ СТАТИСТИКА РАБОТЫ СИСТЕМЫ                   ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Время работы: %02d:%02d:%02d                                       ║%n",
                uptime.toHours(), uptime.toMinutesPart(), uptime.toSecondsPart());
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Всего поездок завершено:           %-8d                   ║%n",
                totalRidesCompleted.get());
        System.out.printf("║ Заказов назначено:                 %-8d                   ║%n",
                totalOrdersAssigned.get());
        System.out.printf("║ Заказов не назначено:              %-8d                   ║%n",
                totalOrdersFailed.get());
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Общая дистанция:                   %-10.1f км             ║%n",
                totalDistance.get());
        System.out.printf("║ Средняя дистанция поездки:         %-10.1f км             ║%n",
                avgDistance);
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Общая выручка:                     %-10.2f руб.          ║%n",
                totalRevenue.get());
        System.out.printf("║ Средняя стоимость поездки:         %-10.2f руб.          ║%n",
                avgRevenue);
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Поездок в минуту:                  %-10.2f                ║%n",
                ridesPerMinute);
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    public void printTaxiStats() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                  СТАТИСТИКА ПО ТАКСИ                           ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.println("║ ID  │ Поездок │ Дистанция (км) │ Выручка (руб.) │ Средний чек ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");

        taxiStats.forEach((taxiId, stats) -> {
            double avgPrice = stats.getRidesCompleted() > 0
                    ? stats.getTotalRevenue() / stats.getRidesCompleted()
                    : 0.0;

            System.out.printf("║ %-3d │ %-7d │ %-14.1f │ %-14.2f │ %-11.2f ║%n",
                    taxiId,
                    stats.getRidesCompleted(),
                    stats.getTotalDistance(),
                    stats.getTotalRevenue(),
                    avgPrice);
        });

        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private static class TaxiStatistics {
        private final AtomicInteger ridesCompleted;
        private final DoubleAccumulator totalDistance;
        private final DoubleAccumulator totalRevenue;

        public TaxiStatistics() {
            this.ridesCompleted = new AtomicInteger(0);
            this.totalDistance = new DoubleAccumulator(Double::sum, 0.0);
            this.totalRevenue = new DoubleAccumulator(Double::sum, 0.0);
        }

        public void recordRide(double distance, double price) {
            ridesCompleted.incrementAndGet();
            totalDistance.accumulate(distance);
            totalRevenue.accumulate(price);
        }

        public int getRidesCompleted() {
            return ridesCompleted.get();
        }

        public double getTotalDistance() {
            return totalDistance.get();
        }

        public double getTotalRevenue() {
            return totalRevenue.get();
        }
    }

    public int getTotalRidesCompleted() {
        return totalRidesCompleted.get();
    }

    public int getTotalOrdersAssigned() {
        return totalOrdersAssigned.get();
    }

    public int getTotalOrdersFailed() {
        return totalOrdersFailed.get();
    }

    public double getTotalDistance() {
        return totalDistance.get();
    }

    public double getTotalRevenue() {
        return totalRevenue.get();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}

package generator;

import model.*;
import statistics.RideHistory;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ClientGenerator implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientGenerator.class.getName());
    private static final double CITY_SIZE = 100.0;
    private static final int MIN_DISTANCE = 5;
    private static final int MAX_DISTANCE = 50;

    private final PriorityBlockingQueue<RideRequest> orderQueue;
    private final RideHistory history;
    private final Random random;
    private final long intervalMs;
    private final int totalRequests;

    private volatile boolean running;
    private int generatedCount;

    public ClientGenerator(PriorityBlockingQueue<RideRequest> orderQueue,
                          long intervalMs,
                          int totalRequests,
                          RideHistory history) {
        this.orderQueue = orderQueue;
        this.intervalMs = intervalMs;
        this.totalRequests = totalRequests;
        this.history = history;
        this.random = new Random();
        this.running = true;
        this.generatedCount = 0;
    }

    @Override
    public void run() {
        LOGGER.info(String.format("Генератор клиентов запущен (интервал: %d мс, всего заказов: %d)",
                intervalMs, totalRequests));

        try {
            while (running && (totalRequests <= 0 || generatedCount < totalRequests)) {
                RideRequest request = generateRequest();
                orderQueue.offer(request);
                generatedCount++;

                String priorityName = request.getPriority() == 2 ? "VIP" :
                                     request.getPriority() == 1 ? "Высокий" : "Обычный";
                history.recordOrderCreated(
                    request.getId(),
                    request.getClientName(),
                    String.format("Приоритет: %s, Дистанция: %.1f км", priorityName, request.getDistance())
                );

                LOGGER.info(String.format("Сгенерирован новый заказ: %s", request));
                LOGGER.info(String.format("Всего заказов в очереди: %d", orderQueue.size()));

                if (totalRequests > 0 && generatedCount >= totalRequests) {
                    LOGGER.info(String.format("Достигнут лимит заказов: %d", totalRequests));
                    break;
                }

                Thread.sleep(intervalMs);
            }
        } catch (InterruptedException e) {
            LOGGER.info("Генератор клиентов прерван");
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.info(String.format("Генератор клиентов завершил работу. Создано заказов: %d",
                    generatedCount));
        }
    }

    private RideRequest generateRequest() {
        Point pickup = generateRandomPoint();
        Point destination = generateDestination(pickup);
        int priority = generatePriority();
        String clientName = generateClientName();

        return new RideRequest(pickup, destination, priority, clientName);
    }

    private Point generateRandomPoint() {
        double x = random.nextDouble() * CITY_SIZE;
        double y = random.nextDouble() * CITY_SIZE;
        return new Point(x, y);
    }

    private Point generateDestination(Point pickup) {
        double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
        double angle = random.nextDouble() * 2 * Math.PI;

        double destX = pickup.getX() + distance * Math.cos(angle);
        double destY = pickup.getY() + distance * Math.sin(angle);

        destX = Math.max(0, Math.min(CITY_SIZE, destX));
        destY = Math.max(0, Math.min(CITY_SIZE, destY));

        return new Point(destX, destY);
    }

    private int generatePriority() {
        double rand = random.nextDouble();
        if (rand < 0.05) {
            return 2;
        } else if (rand < 0.20) {
            return 1;
        } else {
            return 0;
        }
    }

    private String generateClientName() {
        String[] firstNames = {"Александр", "Мария", "Дмитрий", "Анна", "Сергей",
                              "Елена", "Андрей", "Ольга", "Николай", "Татьяна"};
        String[] lastNames = {"Иванов", "Петров", "Сидоров", "Смирнов", "Кузнецов",
                             "Попов", "Васильев", "Соколов", "Михайлов", "Новиков"};

        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];

        return firstName + " " + lastName;
    }

    public int getGeneratedCount() {
        return generatedCount;
    }

    public void shutdown() {
        running = false;
    }
}

package com.bank.service;

import com.bank.dao.OutboxDAO;
import com.bank.model.OutboxEvent;
import com.bank.util.KafkaService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KafkaOutboxPublisher {

    private static final OutboxDAO outboxDAO = new OutboxDAO();
    private static ScheduledExecutorService executor;

    public static synchronized void start() {
        if (executor != null && !executor.isShutdown()) return;
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(KafkaOutboxPublisher::processPendingEvents, 2, 5, TimeUnit.SECONDS);
    }

    public static void processPendingEvents() {
        try {
            List<OutboxEvent> pending = outboxDAO.getPendingEvents(20);
            for (OutboxEvent event : pending) {
                try {
                    KafkaService.logTransaction(event.getAggregateId(), "KAFKA_OUTBOX", 0.0, event.getEventType());
                    outboxDAO.markProcessed(event.getEventId());
                } catch (Exception e) {
                    System.err.println("Outbox publish error for event " + event.getEventId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void stop() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }
}

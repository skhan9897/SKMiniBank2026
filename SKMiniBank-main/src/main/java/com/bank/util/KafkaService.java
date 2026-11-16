package com.bank.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;


/** Optional Kafka publisher. Banking requests continue if Kafka is unavailable. */
public final class KafkaService {
    private static final String TOPIC = env("KAFKA_TRANSACTION_TOPIC", "bank_transactions");
    private static volatile KafkaProducer<String, String> producer;
    private static volatile boolean disabled;

    private KafkaService() { }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static KafkaProducer<String, String> producer() {
        if (disabled) return null;
        KafkaProducer<String, String> current = producer;
        if (current != null) return current;
        synchronized (KafkaService.class) {
            if (producer != null || disabled) return producer;
            try {
                Properties props = new Properties();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                        env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
                props.put(ProducerConfig.ACKS_CONFIG, "all");
                props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
                props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "5000");
                props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "2000");
                props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000");
                producer = new KafkaProducer<>(props);
            } catch (Exception e) {
                System.err.println("Kafka unavailable; event publishing disabled: " + e.getMessage());
                disabled = true;
            }
            return producer;
        }
    }

    public static void logTransaction(String fromAcc, String toAcc, double amount, String type) {
        KafkaProducer<String, String> p = producer();
        if (p == null) return;
        // Keep the existing event contract; do not publish OTPs, credentials, or Aadhaar data.
        String message = String.format(java.util.Locale.ROOT,
                "{\"eventType\":\"TRANSACTION\",\"fromAccount\":\"%s\",\"toAccount\":\"%s\",\"amount\":%.2f,\"transactionType\":\"%s\"}",
                safe(fromAcc), safe(toAcc), amount, safe(type));
        try {
            p.send(new ProducerRecord<>(TOPIC, safe(fromAcc), message), (metadata, error) -> {
                if (error != null) System.err.println("Kafka transaction event failed: " + error.getMessage());
            });
        } catch (Exception e) {
            System.err.println("Kafka transaction event could not be queued: " + e.getMessage());
        }
    }

    private static String safe(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static void close() {
        KafkaProducer<String, String> p = producer;
        if (p != null) {
            try { p.flush(); p.close(java.time.Duration.ofSeconds(5)); }
            catch (Exception e) { System.err.println("Kafka shutdown issue: " + e.getMessage()); }
            finally { producer = null; }
        }
    }
}

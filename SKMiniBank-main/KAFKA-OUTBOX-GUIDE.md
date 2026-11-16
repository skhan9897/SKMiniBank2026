# SK Mini Bank: Transactional Outbox Pattern & Apache Kafka Architecture

This guide describes the Transactional Outbox Pattern implemented in SK Mini Bank to guarantee reliable asynchronous event streaming to Apache Kafka.

---

## 1. Outbox Pattern Architecture

```
[ Banking Action / Registration / KYC Approval ]
                     │
                     ▼ Single MySQL DB Transaction
   ┌──────────────────────────────────────────────────┐
   │ 1. Insert/Update Application / Account Record   │
   │ 2. Insert Event into `integration_outbox` Table  │
   └──────────────────────────────────────────────────┘
                     │
                     ▼ Async Background Poller
          [ KafkaOutboxPublisher ]
                     │
                     ▼ Publish JSON Event
              [ Apache Kafka ]
                     │
                     ▼ Mark as `PUBLISHED`
```

---

## 2. Supported Lifecycle Events

- `CUSTOMER_MOBILE_VERIFIED`
- `CUSTOMER_EMAIL_VERIFIED`
- `AADHAAR_VERIFICATION_COMPLETED`
- `PAN_VERIFICATION_COMPLETED`
- `DOCUMENT_UPLOADED`
- `ACCOUNT_APPLICATION_SUBMITTED`
- `KYC_APPROVED`
- `KYC_REJECTED`
- `ACCOUNT_ACTIVATED`

---

## 3. Kafka Topic Configuration

```env
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TRANSACTION_TOPIC=bank_transactions
```

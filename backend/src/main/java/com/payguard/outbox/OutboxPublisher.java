package com.payguard.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payguard.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private static final String TOPIC = "transactions";

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, TransactionEvent> kafka;
    private final ObjectMapper objectMapper;
    private final Set<UUID> inFlight = ConcurrentHashMap.newKeySet();

    public OutboxPublisher(
            OutboxEventRepository repository,
            KafkaTemplate<String, TransactionEvent> kafka,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.kafka = kafka;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${payguard.outbox.poll-ms:1000}")
    public void publishPendingEvents() {
        var events = repository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            if (!inFlight.add(event.getId())) {
                continue;
            }

            try {
                TransactionEvent transactionEvent =
                        objectMapper.readValue(event.getPayload(), TransactionEvent.class);

                kafka.send(TOPIC, event.getAggregateId(), transactionEvent)
                        .whenComplete((result, error) -> {
                            try {
                                if (error == null) {
                                    event.setPublishedAt(Instant.now());
                                    repository.save(event);
                                    log.info(
                                            "outbox.published eventId={} aggregateId={} topic={}",
                                            event.getId(),
                                            event.getAggregateId(),
                                            TOPIC
                                    );
                                } else {
                                    log.warn(
                                            "outbox.publish_failed eventId={} aggregateId={} reason={}",
                                            event.getId(),
                                            event.getAggregateId(),
                                            error.getClass().getSimpleName()
                                    );
                                }
                            } finally {
                                inFlight.remove(event.getId());
                            }
                        });
            } catch (Exception exception) {
                inFlight.remove(event.getId());
                log.error(
                        "outbox.serialization_failed eventId={} aggregateId={} reason={}",
                        event.getId(),
                        event.getAggregateId(),
                        exception.getClass().getSimpleName()
                );
            }
        }
    }
}

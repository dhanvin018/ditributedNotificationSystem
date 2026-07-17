package com.notificationapp.eventposting;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

@Service
public class EventPublisherService {

    private static final String TOPIC = "notification-events";
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public EventPublisherService(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String publish(String email, String message) throws ExecutionException, InterruptedException {
        NotificationEvent event = new NotificationEvent(email, message, Instant.now());
        kafkaTemplate.send(TOPIC, email, event).get();
        return "Event queued for " + email;
    }
}

package com.notificationapp.eventposting.service;

import com.notificationapp.eventposting.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

@Service
public class EventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(EventPublisherService.class);
    private static final String TOPIC = "notification-events";
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public EventPublisherService(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String publish(String email, String message) throws ExecutionException, InterruptedException {
        log.debug("Publishing notification event to topic '{}' for recipient '{}'", TOPIC, email);
        NotificationEvent event = new NotificationEvent(email, message, Instant.now());
        kafkaTemplate.send(TOPIC, email, event).get();
        log.info("Successfully published notification event to topic '{}' for recipient '{}'", TOPIC, email);
        return "Event queued for " + email;
    }
}

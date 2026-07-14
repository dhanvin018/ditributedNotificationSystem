package com.notificationapp.eventposting;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

@ShellComponent
public class EventCommands {

    private static final String TOPIC = "notification-events";
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public EventCommands(KafkaTemplate<String, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @ShellMethod(key = "post-event", value = "Publish a notification event to Kafka")
    public String postEvent(
            @ShellOption(help = "Recipient email address") String email,
            @ShellOption(help = "Message content") String message) throws ExecutionException, InterruptedException {

        NotificationEvent event = new NotificationEvent(email, message, Instant.now());
        kafkaTemplate.send(TOPIC, email, event).get();
        return "Event queued for " + email;
    }
}
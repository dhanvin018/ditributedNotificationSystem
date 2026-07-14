package com.notificationapp.notificationgenerator.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationapp.notificationgenerator.dto.EmailNotificationMessage;
import com.notificationapp.notificationgenerator.exception.EmailDispatchException;
import com.notificationapp.notificationgenerator.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final EmailNotificationService emailNotificationService;

    @Value("${app.kafka.topic}")
    private String topic;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String payload, Acknowledgment acknowledgment) {

        try {
            log.info("Received message from topic {} : {}", topic, payload);

            EmailNotificationMessage notificationMessage =
                    objectMapper.readValue(
                            payload,
                            EmailNotificationMessage.class
                    );

            validate(notificationMessage);

            emailNotificationService.sendEmail(notificationMessage);

            acknowledgment.acknowledge();

            log.info(
                    "Successfully processed notification for {}",
                    notificationMessage.getRecipientEmail()
            );

        } catch (JsonProcessingException ex) {

            log.error(
                    "Invalid notification payload received: {}",
                    payload,
                    ex
            );

            /*
             * Bad payload.
             * Acknowledge so Kafka does not endlessly retry
             * malformed messages.
             */
            acknowledgment.acknowledge();

        } catch (Exception ex) {

            log.error(
                    "Failed to process notification payload: {}",
                    payload,
                    ex
            );

            throw new EmailDispatchException(
                    "Failed to process email notification",
                    ex
            );
        }
    }

    private void validate(
            EmailNotificationMessage notificationMessage) {

        if (notificationMessage == null) {
            throw new IllegalArgumentException(
                    "Notification message is null"
            );
        }

        if (notificationMessage.getRecipientEmail() == null
                || notificationMessage.getRecipientEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email is missing"
            );
        }

        if (notificationMessage.getMessage() == null
                || notificationMessage.getMessage().isBlank()) {
            throw new IllegalArgumentException(
                    "Message content is missing"
            );
        }
    }
}
package com.notificationapp.notificationgenerator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationMessage {

    @JsonProperty("recipientEmail")
    private String recipientEmail;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private BigDecimal timestamp;
}
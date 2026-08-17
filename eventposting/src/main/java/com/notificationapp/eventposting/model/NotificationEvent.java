package com.notificationapp.eventposting.model;

import java.time.Instant;

public record NotificationEvent(String recipientEmail, String message, Instant timestamp) {}
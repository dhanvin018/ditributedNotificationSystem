package com.notificationapp.eventposting;

import java.time.Instant;

public record NotificationEvent(String recipientEmail, String message, Instant timestamp) {}
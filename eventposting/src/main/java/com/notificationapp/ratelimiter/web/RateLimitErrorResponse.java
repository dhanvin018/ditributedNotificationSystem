package com.notificationapp.ratelimiter.web;

public record RateLimitErrorResponse(String message, Long retryAfterSeconds) {}

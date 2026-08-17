package com.notificationapp.ratelimiter.model;

public record TokenBucketState(long tokens, long lastRefillTimestampMillis) {}

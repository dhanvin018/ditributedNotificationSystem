package com.notificationapp.ratelimiter.model;

public enum LimitStrategyType {
    TOKEN_BUCKET,
    FIXED_WINDOW,
    SLIDING_WINDOW_LOG,
    SLIDING_WINDOW_COUNTER
}

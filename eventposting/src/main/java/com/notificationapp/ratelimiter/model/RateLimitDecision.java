package com.notificationapp.ratelimiter.model;

import java.time.Duration;

public record RateLimitDecision(
        boolean allowed,
        DenyReason reason,
        String violatedRule,
        Duration retryAfter,
        String userMessage
) {

    //TODO: Refine these methods
    public static RateLimitDecision allow(){
        return new RateLimitDecision(true,
                null,
                null,
                null,
                null);
    }
    public static RateLimitDecision rateExceeded(
            String rule,
            Duration retryAfter,
            String message){
        return new RateLimitDecision(false,
                DenyReason.RATE_EXCEEDED,
                "rateExceeded",
                null,
                "429: Payloads exceeded rate, try soon");
    }
    public static RateLimitDecision costExceedsCapacity(String rule, String message){
        return new RateLimitDecision(false,
                DenyReason.COST_EXCEEDED,
                "costExceeded",
                null,
                "413: Payload Too Large");

    }
}

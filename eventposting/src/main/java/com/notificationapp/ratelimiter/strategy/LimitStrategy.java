package com.notificationapp.ratelimiter.strategy;

import com.notificationapp.ratelimiter.model.LimitStrategyType;
import com.notificationapp.ratelimiter.model.WindowRule;
import com.notificationapp.ratelimiter.store.RateLimitStore;

import java.time.Duration;

public interface LimitStrategy {
    LimitStrategyType type();
    boolean tryConsume(String key, WindowRule rule, int cost, RateLimitStore store);
    Duration retryAfter(String key, WindowRule rule, RateLimitStore store);
    //TODO: Enrich the different strategies
}

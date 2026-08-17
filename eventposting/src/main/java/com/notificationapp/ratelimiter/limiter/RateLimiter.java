package com.notificationapp.ratelimiter.limiter;

import com.notificationapp.ratelimiter.model.RateLimitContext;
import com.notificationapp.ratelimiter.model.RateLimitDecision;

public interface RateLimiter {
    RateLimitDecision evaluate(RateLimitContext context);
}

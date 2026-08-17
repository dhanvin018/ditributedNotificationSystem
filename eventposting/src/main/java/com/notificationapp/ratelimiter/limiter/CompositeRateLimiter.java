package com.notificationapp.ratelimiter.limiter;

import com.notificationapp.ratelimiter.model.RateLimitContext;
import com.notificationapp.ratelimiter.model.RateLimitDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CompositeRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(CompositeRateLimiter.class);

    private final List<DimensionLimiter> orderedLimiters;

    public CompositeRateLimiter(List<DimensionLimiter> orderedLimiters) {
        this.orderedLimiters = orderedLimiters;
    }

    @Override
    public RateLimitDecision evaluate(RateLimitContext context) {
        log.debug("Evaluating composite rate limits for context: {}", context);

        for (DimensionLimiter limiter : orderedLimiters) {
            RateLimitDecision decision = limiter.evaluate(context);
            if (!decision.allowed()) {
                log.debug("Rate limit exceeded during evaluation for context: {}. Decision: {}", context, decision);
                return decision;
            }
        }

        log.debug("All composite rate limit checks passed for context: {}", context);
        return RateLimitDecision.allow();
    }
}

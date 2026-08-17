package com.notificationapp.ratelimiter.limiter;

import com.notificationapp.ratelimiter.config.RateLimitConfigProvider;
import com.notificationapp.ratelimiter.model.*;
import com.notificationapp.ratelimiter.store.RateLimitStore;
import com.notificationapp.ratelimiter.strategy.LimitStrategy;
import com.notificationapp.ratelimiter.strategy.StrategyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class DimensionLimiter {

    private static final Logger log = LoggerFactory.getLogger(DimensionLimiter.class);

    private final Dimension dimension;
    private final RateLimitConfigProvider rateLimiterConfigProvider;
    private final RateLimitStore store;
    private final StrategyRegistry strategyRegistry;

    DimensionLimiter(Dimension dimension,
                     RateLimitConfigProvider configProvider,
                     RateLimitStore store,
                     StrategyRegistry strategyRegistry) {
        this.dimension = dimension;
        this.rateLimiterConfigProvider = configProvider;
        this.store = store;
        this.strategyRegistry = strategyRegistry;
    }

    RateLimitDecision evaluate(RateLimitContext context) {
        log.debug("Evaluating rate limit for dimension: {} with context: {}", dimension, context);

        if (dimension == Dimension.USER_ID && context.userId() == null) {
            log.debug("Skipping USER_ID dimension rate limit check due to missing user ID");
            return RateLimitDecision.allow();
        }

        List<WindowRule> rules = rateLimiterConfigProvider.rulesFor(context.userTier(), dimension);
        String identity = resolveKey(dimension, context);

        for (WindowRule rule : rules) {
            String ruleId = dimension + ":" + rule.windowSize() + ":" + rule.effectiveCapacity();

            if (context.cost() > rule.effectiveCapacity()) {
                log.debug("Request cost ({}) exceeds rule effective capacity ({}) for rule: {}",
                        context.cost(), rule.effectiveCapacity(), ruleId);
                return RateLimitDecision.costExceedsCapacity(ruleId, "Request exceeds the maximum allowed batch size (" + rule.effectiveCapacity() +
                        ") for " + dimension.name().toLowerCase() + ".");
            }

            LimitStrategy strategy = strategyRegistry.get(rule.strategyType());
            String key = identity + ":" + rule.windowSize();

            if (!strategy.tryConsume(key, rule, context.cost(), store)) {
                Duration retry = strategy.retryAfter(key, rule, store);
                log.debug("Rate limit exceeded for key: {} on rule: {}. Retry after: {}s", key, ruleId, retry.toSeconds());
                return RateLimitDecision.rateExceeded(ruleId, retry,
                        "Rate limit exceeded for " + dimension.name().toLowerCase() +
                                ". Try again in " + retry.toSeconds() + "s.");
            }
        }

        log.debug("Rate limit checks passed for dimension: {}", dimension);
        return RateLimitDecision.allow();
    }

    private String resolveKey(Dimension dimension, RateLimitContext ctx) {
        String identity = switch (dimension) {
            case USER_ID -> ctx.userId();
            case IP_ADDRESS -> ctx.ipAddress();
            case GLOBAL -> "*";   // one shared bucket, no per-entity identity to key on
        };
        return "rl:" + dimension.name().toLowerCase() + ":" + identity;
    }
}

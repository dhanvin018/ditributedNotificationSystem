package com.notificationapp.ratelimiter.strategy;

import com.notificationapp.ratelimiter.model.LimitStrategyType;
import com.notificationapp.ratelimiter.model.WindowRule;
import com.notificationapp.ratelimiter.store.RateLimitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Component
public class FixedWindowStrategy implements LimitStrategy {

    private static final Logger log = LoggerFactory.getLogger(FixedWindowStrategy.class);

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> fixedWindowScript;

    public FixedWindowStrategy(StringRedisTemplate redisTemplate,
                               @Qualifier("fixedWindowScript") RedisScript<Long> fixedWindowScript) {
        this.redisTemplate = redisTemplate;
        this.fixedWindowScript = fixedWindowScript;
    }

    @Override
    public LimitStrategyType type() {
        return LimitStrategyType.FIXED_WINDOW;
    }

    @Override
    public boolean tryConsume(String key, WindowRule rule, int cost, RateLimitStore store) {
        long now = System.currentTimeMillis();
        long windowSizeMs = rule.windowSize().toMillis();

        // Calculate the current discrete window bucket (e.g., timestamp divided by 60000)
        long currentWindowBucket = now / windowSizeMs;
        String windowKey = key + ":" + currentWindowBucket;

        log.debug("Evaluating fixed window strategy for key: {}, windowKey: {}, cost: {}, capacity: {}",
                key, windowKey, cost, rule.effectiveCapacity());

        Long result = redisTemplate.execute(
                fixedWindowScript,
                Collections.singletonList(windowKey),
                String.valueOf(rule.effectiveCapacity()),
                String.valueOf(windowSizeMs),
                String.valueOf(cost)
        );

        boolean allowed = result != null && result == 1L;
        log.debug("Fixed window consumption result for key: {}: allowed = {}", windowKey, allowed);
        return allowed;
    }

    @Override
    public Duration retryAfter(String key, WindowRule rule, RateLimitStore store) {
        long now = System.currentTimeMillis();
        long windowSizeMs = rule.windowSize().toMillis();

        // Time remaining until the next window starts
        long timeIntoCurrentWindow = now % windowSizeMs;
        long remainingInWindow = windowSizeMs - timeIntoCurrentWindow;
        Duration retry = Duration.ofMillis(Math.max(100, remainingInWindow));

        log.debug("Calculated fixed window retry duration for key: {}: {}ms", key, retry.toMillis());
        return retry;
    }
}

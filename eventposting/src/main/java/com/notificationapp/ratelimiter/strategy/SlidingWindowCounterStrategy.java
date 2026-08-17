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
import java.util.List;

@Component
public class SlidingWindowCounterStrategy implements LimitStrategy {

    private static final Logger log = LoggerFactory.getLogger(SlidingWindowCounterStrategy.class);

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> slidingWindowCounterScript;

    public SlidingWindowCounterStrategy(StringRedisTemplate redisTemplate,
                                        @Qualifier("slidingWindowCounterScript") RedisScript<Long> slidingWindowCounterScript) {
        this.redisTemplate = redisTemplate;
        this.slidingWindowCounterScript = slidingWindowCounterScript;
    }

    @Override
    public LimitStrategyType type() {
        return LimitStrategyType.SLIDING_WINDOW_COUNTER;
    }

    @Override
    public boolean tryConsume(String key, WindowRule rule, int cost, RateLimitStore store) {
        long now = System.currentTimeMillis();
        long windowMs = rule.windowSize().toMillis();
        long currentWindowBucket = now / windowMs;
        long previousWindowBucket = currentWindowBucket - 1;

        String currentKey = key + ":" + currentWindowBucket;
        String previousKey = key + ":" + previousWindowBucket;

        log.debug("Evaluating sliding window counter strategy for key: {}, currentKey: {}, previousKey: {}, cost: {}, capacity: {}",
                key, currentKey, previousKey, cost, rule.effectiveCapacity());

        Long result = redisTemplate.execute(
                slidingWindowCounterScript,
                List.of(currentKey, previousKey),
                String.valueOf(rule.effectiveCapacity()),
                String.valueOf(windowMs),
                String.valueOf(now),
                String.valueOf(cost)
        );

        boolean allowed = result != null && result == 1L;
        log.debug("Sliding window counter consumption result for key: {}: allowed = {}", key, allowed);
        return allowed;
    }

    @Override
    public Duration retryAfter(String key, WindowRule rule, RateLimitStore store) {
        long now = System.currentTimeMillis();
        long windowMs = rule.windowSize().toMillis();
        long timeIntoWindow = now % windowMs;

        // Wait until the current window rolls over enough to drop below capacity
        long remainingInWindow = windowMs - timeIntoWindow;
        Duration retry = Duration.ofMillis(Math.max(100, remainingInWindow));

        log.debug("Calculated sliding window counter retry duration for key: {}: {}ms", key, retry.toMillis());
        return retry;
    }
}

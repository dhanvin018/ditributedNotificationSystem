package com.notificationapp.ratelimiter.strategy;

import com.notificationapp.ratelimiter.model.LimitStrategyType;
import com.notificationapp.ratelimiter.model.WindowRule;
import com.notificationapp.ratelimiter.store.RateLimitStore;
import com.notificationapp.ratelimiter.store.RedisRateLimitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBucketStrategy implements LimitStrategy {

    private static final Logger log = LoggerFactory.getLogger(TokenBucketStrategy.class);

    private final RedisRateLimitStore redisStore;

    // Concurrent map keeping active local buckets in memory
    private final ConcurrentHashMap<String, LocalTokenBucket> localBuckets = new ConcurrentHashMap<>();

    public TokenBucketStrategy(RedisRateLimitStore redisStore) {
        this.redisStore = redisStore;
    }

    @Override
    public LimitStrategyType type() {
        return LimitStrategyType.TOKEN_BUCKET;
    }

    @Override
    public boolean tryConsume(String key, WindowRule rule, int cost, RateLimitStore unusedStore) {
        double refillRatePerMs = (double) rule.limit() / rule.windowSize().toMillis();
        long capacity = rule.effectiveCapacity();

        log.trace("Attempting to consume cost={} for key={}", cost, key);

        // 1. Get or create the local bucket for this key
        LocalTokenBucket localBucket = localBuckets.computeIfAbsent(
                key,
                k -> new LocalTokenBucket(capacity, refillRatePerMs)
        );

        // 2. Try consuming from local memory first (nanosecond execution)
        if (localBucket.tryConsumeLocal(cost)) {
            log.trace("Successfully consumed tokens locally for key={}", key);
            return true;
        }

        // 3. Local bucket is empty -> Calculate lease batch size to request from Redis
        int batchToRequest = (int) Math.min(capacity, Math.max(cost, 20)); // Batch size of 20 or cost
        log.debug("Local bucket depleted for key={}. Requesting token lease batch of size={} from Redis", key, batchToRequest);

        // 4. Request tokens from Redis (1 atomic Lua script call)
        long grantedTokens = redisStore.leaseTokenBucket(key, capacity, refillRatePerMs, batchToRequest);

        if (grantedTokens >= cost) {
            log.debug("Leased {} tokens from Redis for key={}. Depositing remaining {} into local bucket", grantedTokens, key, grantedTokens - cost);
            // Deposit leased tokens into local bucket and consume the cost
            localBucket.depositLeasedTokens(grantedTokens - cost);
            return true;
        }

        // Global limits exceeded in Redis
        log.debug("Rate limit exceeded in Redis for key={}. Granted {} tokens, needed cost={}", key, grantedTokens, cost);
        return false;
    }

    @Override
    public Duration retryAfter(String key, WindowRule rule, RateLimitStore unusedStore) {
        log.debug("Calculating retry\\-after duration for key={}", key);
        return redisStore.getRetryAfter(key, rule);
    }
}

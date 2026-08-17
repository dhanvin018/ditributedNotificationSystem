package com.notificationapp.ratelimiter.store;

import java.time.Duration;
import java.util.Optional;

public interface RateLimitStore {
    <T> Optional<T> get(String key, Class<T> type);
    void put(String key, Object value, Duration ttl);
    long incrementAndGet(String key, long delta, Duration ttl);
    boolean compareAndSwap(String key, Object expected, Object updated, Duration ttl);

    //TODO:Enrich the different store types
}

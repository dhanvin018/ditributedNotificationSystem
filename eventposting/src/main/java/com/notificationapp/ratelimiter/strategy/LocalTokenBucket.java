package com.notificationapp.ratelimiter.strategy;

import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalTokenBucket {
    private static final Logger log = LoggerFactory.getLogger(LocalTokenBucket.class);

    private final AtomicReference<State> state;
    private final double refillRatePerMs;
    private final long capacity;

    private record State(double tokens, long lastRefillMs) {}

    public LocalTokenBucket(long capacity, double refillRatePerMs) {
        this.capacity = capacity;
        this.refillRatePerMs = refillRatePerMs;
        // Start local bucket empty so it triggers a Redis lease on first call
        this.state = new AtomicReference<>(new State(0.0, System.currentTimeMillis()));
        log.debug("Initialized LocalTokenBucket with capacity={} and refillRatePerMs={}", capacity, refillRatePerMs);
    }

    /**
     * Tries to consume tokens locally after calculating time\-based refill.
     */
    public boolean tryConsumeLocal(int cost) {
        while (true) {
            long now = System.currentTimeMillis();
            State current = state.get();

            long elapsed = Math.max(0, now - current.lastRefillMs());
            double refilled = elapsed * refillRatePerMs;
            double updatedTokens = Math.min(capacity, current.tokens() + refilled);

            if (updatedTokens < cost) {
                log.trace("Insufficient local tokens for cost={}. Available tokens: {}", cost, updatedTokens);
                return false; // Not enough local tokens
            }

            State newState = new State(updatedTokens - cost, now);
            if (state.compareAndSet(current, newState)) {
                log.trace("Consumed {} local tokens. Remaining tokens: {}", cost, newState.tokens());
                return true; // Successfully consumed locally
            }
            // Retries automatically if another thread updated the state concurrently
        }
    }

    /**
     * Deposits tokens leased from Redis into the local bucket.
     */
    public void depositLeasedTokens(long leasedCount) {
        while (true) {
            long now = System.currentTimeMillis();
            State current = state.get();

            long elapsed = Math.max(0, now - current.lastRefillMs());
            double refilled = elapsed * refillRatePerMs;
            double updatedTokens = Math.min(capacity, current.tokens() + refilled + leasedCount);

            State newState = new State(updatedTokens, now);
            if (state.compareAndSet(current, newState)) {
                log.debug("Deposited {} leased tokens. Total tokens in local bucket: {}", leasedCount, newState.tokens());
                return;
            }
        }
    }
}

package com.notificationapp.ratelimiter.model;

import java.time.Duration;

public record WindowRule(
        Dimension dimension,
        Duration windowSize,
        long limit,
        LimitStrategyType strategyType,
        Long burstCapacity
) {
    public long effectiveCapacity(){
        return burstCapacity!=null?burstCapacity:limit;
    }
}

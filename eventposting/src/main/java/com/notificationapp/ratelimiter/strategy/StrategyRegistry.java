package com.notificationapp.ratelimiter.strategy;

import com.notificationapp.ratelimiter.model.LimitStrategyType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StrategyRegistry {
    private final Map<LimitStrategyType, LimitStrategy> byType;

    // Spring auto-collects every LimitStrategy bean into this List — this constructor
    // IS "an application-level context loading strategy objects at startup." No custom
    // container needed; the ApplicationContext already is one.
    public StrategyRegistry(List<LimitStrategy> strategies) {
        this.byType = strategies.stream()
                .collect(Collectors.toMap(LimitStrategy::type, Function.identity()));
    }

    public LimitStrategy get(LimitStrategyType type) {
        LimitStrategy strategy = byType.get(type);
        if (strategy == null) {
            throw new IllegalStateException("No LimitStrategy registered for " + type);
        }
        return strategy;
    }
}

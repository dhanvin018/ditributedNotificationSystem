package com.notificationapp.ratelimiter.config;

import com.notificationapp.ratelimiter.model.Dimension;
import com.notificationapp.ratelimiter.model.LimitStrategyType;
import com.notificationapp.authentication.model.UserTier;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimitProperties {

    private static final Logger log = LoggerFactory.getLogger(RateLimitProperties.class);

    private List<Dimension> dimensionOrder = new ArrayList<>(List.of(Dimension.GLOBAL, Dimension.IP_ADDRESS, Dimension.USER_ID));
    private DimensionSection global;
    private DimensionSection ip;
    private UserSection user;

    @PostConstruct
    public void logLoadedConfiguration() {
        log.info("=================================================");
        log.info(">>> RATE LIMITER PROPERTIES INITIALIZATION <<<");
        log.info("Dimension Order: {}", dimensionOrder);

        if (global != null && global.getWindows() != null) {
            log.info("GLOBAL Rules Loaded: {} rule(s)", global.getWindows().size());
            global.getWindows().forEach(w ->
                    log.info("  -> [GLOBAL] Window: size={}, limit={}, strategy={}", w.getSize(), w.getLimit(), w.getStrategy())
            );
        } else {
            log.warn("GLOBAL Rules: Not Configured or NULL");
        }

        if (ip != null && ip.getWindows() != null) {
            log.info("IP Rules Loaded: {} rule(s)", ip.getWindows().size());
            ip.getWindows().forEach(w ->
                    log.info("  -> [IP] Window: size={}, limit={}, strategy={}", w.getSize(), w.getLimit(), w.getStrategy())
            );
        } else {
            log.warn("IP Rules: Not Configured or NULL");
        }

        if (user != null && user.getTiers() != null) {
            log.info("USER Tiers Loaded: {}", user.getTiers().keySet());
            user.getTiers().forEach((tier, section) -> {
                if (section != null && section.getWindows() != null) {
                    section.getWindows().forEach(w ->
                            log.info("  -> [USER TIER: {}] Window: size={}, limit={}, strategy={}, burstCapacity={}",
                                    tier, w.getSize(), w.getLimit(), w.getStrategy(), w.getBurstCapacity())
                    );
                }
            });
        } else {
            log.warn("USER Tiers: Not Configured or NULL");
        }
        log.info("=================================================");
    }

    // --- Top-Level Getters & Setters ---
    public List<Dimension> getDimensionOrder() { return dimensionOrder; }
    public void setDimensionOrder(List<Dimension> dimensionOrder) { this.dimensionOrder = dimensionOrder; }

    public DimensionSection getGlobal() { return global; }
    public void setGlobal(DimensionSection global) { this.global = global; }

    public DimensionSection getIp() { return ip; }
    public void setIp(DimensionSection ip) { this.ip = ip; }

    public UserSection getUser() { return user; }
    public void setUser(UserSection user) { this.user = user; }

    // --- Inner Static Classes ---

    public static class DimensionSection {
        private List<WindowRuleProperties> windows;

        public List<WindowRuleProperties> getWindows() { return windows; }
        public void setWindows(List<WindowRuleProperties> windows) { this.windows = windows; }
    }

    public static class UserSection {
        private Map<UserTier, DimensionSection> tiers;

        public Map<UserTier, DimensionSection> getTiers() { return tiers; }
        public void setTiers(Map<UserTier, DimensionSection> tiers) { this.tiers = tiers; }
    }

    public static class WindowRuleProperties {
        private Duration size;
        private long limit;
        private LimitStrategyType strategy;
        private Long burstCapacity;

        public Duration getSize() { return size; }
        public void setSize(Duration size) { this.size = size; }

        public long getLimit() { return limit; }
        public void setLimit(long limit) { this.limit = limit; }

        public LimitStrategyType getStrategy() { return strategy; }
        public void setStrategy(LimitStrategyType strategy) { this.strategy = strategy; }

        public Long getBurstCapacity() { return burstCapacity; }
        public void setBurstCapacity(Long burstCapacity) { this.burstCapacity = burstCapacity; }
    }
}
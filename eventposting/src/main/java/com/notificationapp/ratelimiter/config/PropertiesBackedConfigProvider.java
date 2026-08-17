package com.notificationapp.ratelimiter.config;

import com.notificationapp.ratelimiter.model.Dimension;
import com.notificationapp.authentication.model.UserTier;
import com.notificationapp.ratelimiter.model.WindowRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        name = "rate-limiter.config-source",
        havingValue = "properties"
)
public class PropertiesBackedConfigProvider implements RateLimitConfigProvider {

    private static final Logger log = LoggerFactory.getLogger(PropertiesBackedConfigProvider.class);

    private final RateLimitProperties props;

    public PropertiesBackedConfigProvider(RateLimitProperties props) {
        this.props = props;
    }

    @Override
    public List<WindowRule> rulesFor(UserTier userTier, Dimension dimension) {
        log.debug("Fetching rate limit rules for user tier: {} and dimension: {}", userTier, dimension);

        List<RateLimitProperties.WindowRuleProperties> raw = switch (dimension) {
            case GLOBAL -> props.getGlobal().getWindows();
            case IP_ADDRESS -> props.getIp().getWindows();
            case USER_ID -> props.getUser().getTiers().get(userTier).getWindows();
        };

        List<WindowRule> rules = raw.stream()
                .map(w -> new WindowRule(dimension, w.getSize(), w.getLimit(), w.getStrategy(), w.getBurstCapacity()))
                .toList();

        log.debug("Loaded {} rule(s) for user tier: {} and dimension: {}", rules.size(), userTier, dimension);
        return rules;
    }
}

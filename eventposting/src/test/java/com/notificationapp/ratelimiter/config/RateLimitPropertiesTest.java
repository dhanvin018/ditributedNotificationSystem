package com.notificationapp.ratelimiter.config;

import com.notificationapp.authentication.model.UserTier;
import com.notificationapp.ratelimiter.model.Dimension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RateLimitPropertiesTest {

    @Autowired
    private RateLimitProperties properties;

    @Test
    @DisplayName("Should bind rate-limiter properties from rate-limit-config.yml")
    void testRateLimitPropertiesBinding() {
        // 1. Verify Bean Injection
        assertThat(properties)
                .as("RateLimitProperties bean should be loaded")
                .isNotNull();

        // 2. Verify Dimension Order default initialization
        assertThat(properties.getDimensionOrder())
                .containsExactly(Dimension.GLOBAL, Dimension.IP_ADDRESS, Dimension.USER_ID);

        // 3. Verify User Tiers section (if configured)
        if (properties.getUser() != null && properties.getUser().getTiers() != null) {
            assertThat(properties.getUser().getTiers())
                    .as("User tiers map should contain FREE tier")
                    .containsKey(UserTier.FREE);

            RateLimitProperties.DimensionSection freeTierSection = properties.getUser().getTiers().get(UserTier.FREE);
            assertThat(freeTierSection.getWindows())
                    .as("FREE tier should have defined window rules")
                    .isNotNull()
                    .isNotEmpty();

            RateLimitProperties.WindowRuleProperties firstRule = freeTierSection.getWindows().get(0);
            assertThat(firstRule.getLimit()).isGreaterThan(0);
            assertThat(firstRule.getSize()).isNotNull(); // Duration (e.g., Duration.ofMinutes(1))
        }

        System.out.println("RateLimitProperties test passed!");
    }
}
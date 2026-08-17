package com.notificationapp.ratelimiter.model;

import com.notificationapp.authentication.model.UserTier;

public record RateLimitContext(
        String userId,
        String ipAddress,
        UserTier userTier,
        int cost

) {
}

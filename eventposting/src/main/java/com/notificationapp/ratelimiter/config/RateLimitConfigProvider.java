package com.notificationapp.ratelimiter.config;

import com.notificationapp.ratelimiter.model.Dimension;
import com.notificationapp.authentication.model.UserTier;
import com.notificationapp.ratelimiter.model.WindowRule;

import java.util.List;

public interface RateLimitConfigProvider {
    List<WindowRule> rulesFor(UserTier userTier, Dimension dimension);
}

package com.notificationapp.ratelimiter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisTransactionScriptConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisTransactionScriptConfig.class);

    @Bean
    public RedisScript<Long> tokenBucketLeaseScript() {
        log.info("Initializing Redis script bean for token bucket lease");
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/token_bucket_lease.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public RedisScript<Long> slidingWindowCounterScript() {
        log.info("Initializing Redis script bean for sliding window counter");
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/sliding_window_counter.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public RedisScript<Long> slidingWindowLogScript() {
        log.info("Initializing Redis script bean for sliding window log");
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/sliding_window_log.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public RedisScript<Long> fixedWindowScript() {
        log.info("Initializing Redis script bean for fixed window");
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/fixed_window.lua"));
        script.setResultType(Long.class);
        return script;
    }
}

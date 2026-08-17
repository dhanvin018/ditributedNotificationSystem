package com.notificationapp.ratelimiter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationapp.ratelimiter.model.DenyReason;
import com.notificationapp.ratelimiter.model.RateLimitDecision;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitResponseWriter {
    public void write(HttpServletResponse response, RateLimitDecision decision) throws IOException {
        response.setContentType("application/json");
        if(decision.reason()== DenyReason.COST_EXCEEDED){
            response.setStatus(413);
            new ObjectMapper().writeValue(response.getWriter(),
                    new RateLimitErrorResponse(
                            decision.userMessage(),null
                    ));
        }else{
            response.setStatus(429);
            response.setHeader("RetryAfter", String.valueOf(decision.retryAfter().toSeconds()));
            new ObjectMapper().writeValue(response.getWriter(),
                    new RateLimitErrorResponse(decision.userMessage(),decision.retryAfter().toSeconds()));
        }
    }
}

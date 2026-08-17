package com.notificationapp.ratelimiter.web;

import com.notificationapp.ratelimiter.limiter.RateLimiter;
import com.notificationapp.ratelimiter.model.RateLimitContext;
import com.notificationapp.ratelimiter.model.RateLimitDecision;
import com.notificationapp.authentication.model.UserTier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final RateLimitResponseWriter responseWriter;
//    private final TierResolver tierResolver;   // looks up a user's tier, cached — see §9 (low latency)

    public RateLimitInterceptor(RateLimiter rateLimiter,
                                RateLimitResponseWriter responseWriter
                                ) {
        this.rateLimiter = rateLimiter;
        this.responseWriter = responseWriter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        RateLimitContext context = buildContext(request);
        RateLimitDecision decision = rateLimiter.evaluate(context);
        if (!decision.allowed()) {
            responseWriter.write(response, decision);
            return false;   // short-circuits before the controller / Kafka publish ever runs
        }
        return true;
    }

    private RateLimitContext buildContext(HttpServletRequest request) {
        String userId = resolveUserId();
//        Tier tier = userId != null ? tierResolver.resolve(userId) : null;
        UserTier userTier = UserTier.FREE;
        String ip = resolveClientIp(request);
        int cost = resolveCost(request);
        return new RateLimitContext(userId, ip, userTier, cost);
    }

    private String resolveUserId() {
        // FUTURE SCOPE — no authentication exists in the app yet (see §13). Once
        // spring-boot-starter-security is added with a DB-backed UserDetailsService,
        // swap this for the real lookup below; nothing else in the interceptor or
        // rate limiter changes, since §3.1 already treats userId == null as a first-
        // class, correctly-handled case (USER_ID dimension skips itself, IP + GLOBAL
        // still apply).
        //
        //   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //   boolean authenticated = auth != null && auth.isAuthenticated()
        //       && !(auth instanceof AnonymousAuthenticationToken);
        //   return authenticated ? auth.getName() : null;
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        // Deliberately NOT reading X-Forwarded-For here — see §7.4. Trusting it in
        // application code means trusting whatever the client sends, since nothing
        // upstream is guaranteed to have stripped/overwritten it. Real-IP resolution
        // behind a load balancer belongs at the container/infra layer (Spring Boot's
        // `server.forward-headers-strategy`, or the proxy itself), configured with a
        // known set of trusted proxy hops — not hand-parsed per request here.
        return request.getRemoteAddr();
    }

    private int resolveCost(HttpServletRequest request) {
        String header = request.getHeader("X-Event-Count");
        if (header == null) return 1;
        try {
            return Math.max(1, Integer.parseInt(header));
        } catch (NumberFormatException e) {
            return 1;   // fall back safely; the controller-level consistency check catches abuse
        }
    }
}

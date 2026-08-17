package com.notificationapp.authentication.authfilter;

import com.notificationapp.authentication.model.UserTier;
import com.notificationapp.authentication.services.JwtService;
import com.notificationapp.ratelimiter.limiter.RateLimiter;
import com.notificationapp.ratelimiter.model.RateLimitContext;
import com.notificationapp.ratelimiter.model.RateLimitDecision;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class RateLimitAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAuthFilter.class);

    private final JwtService jwtService;
    private final RateLimiter rateLimiter;
    private final boolean authenticationRequired;

    public RateLimitAuthFilter(
            JwtService jwtService,
            RateLimiter rateLimiter,
            @Value("${authentication.required:true}") boolean authenticationRequired
    ) {
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
        this.authenticationRequired = authenticationRequired;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // Skip auth/rate-limiting endpoints
        if (requestUri.startsWith("/api/v1/auth")) {
            log.debug("Bypassing RateLimitAuthFilter for authentication endpoint: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = request.getRemoteAddr();
        String authHeader = request.getHeader("Authorization");
        String username = null;
        UserTier userTier = UserTier.FREE;

        if (authenticationRequired && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.isTokenValid(token)) {
                username = jwtService.extractUsername(token);
                String tierClaim = jwtService.extractTier(token).toString();
                if (tierClaim != null) {
                    try {
                        userTier = UserTier.valueOf(tierClaim.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown tier claim '{}' for user '{}'. Falling back to default tier FREE.", tierClaim, username);
                        userTier = UserTier.FREE;
                    }
                }
                log.debug("Successfully authenticated user '{}' with tier '{}'", username, userTier);
            } else {
                log.warn("Invalid or expired JWT token received from IP: {}", ipAddress);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.emptyList()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } else if (!authenticationRequired) {
            log.trace("Authentication is disabled by configuration for request to {}", requestUri);
        }

        RateLimitContext context = new RateLimitContext(
                username,
                ipAddress,
                userTier,
                1
        );

        log.debug("Evaluating rate limit for request [URI: {}, IP: {}, User: {}, Tier: {}]",
                requestUri, ipAddress, username, userTier);

        RateLimitDecision decision = rateLimiter.evaluate(context);

        if (!decision.allowed()) {
            log.warn("Rate limit exceeded for IP: {}, User: {}, URI: {}. Reason: {}",
                    ipAddress, username, requestUri, decision.reason());
            response.setStatus(429);
            response.getWriter().write(decision.reason().toString());
            return;
        }

        log.trace("Rate limit check passed for request [URI: {}, IP: {}]", requestUri, ipAddress);
        filterChain.doFilter(request, response);
    }
}

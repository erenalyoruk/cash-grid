package com.erenalyoruk.cashgrid.common.filter;

import com.erenalyoruk.cashgrid.common.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;

    private final Map<String, Bucket> defaultBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = resolveKey(request);
        boolean isAuthPath = request.getRequestURI().startsWith("/api/v1/auth/");

        Bucket bucket = isAuthPath ? resolveAuthBucket(key) : resolveDefaultBucket(key);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));

        if (!probe.isConsumed()) {
            long retryAfterSeconds =
                    Duration.ofNanos(probe.getNanosToWaitForRefill()).getSeconds() + 1;
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter()
                    .write(
                            "{\"status\":429,"
                                    + "\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many"
                                    + " requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs");
    }

    private String resolveKey(HttpServletRequest request) {
        // Use authenticated username if available, otherwise fall back to IP
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Use the token hash as key to avoid parsing JWT here
            return "token:" + authHeader.hashCode();
        }
        return "ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket resolveDefaultBucket(String key) {
        return defaultBuckets.computeIfAbsent(
                key,
                k ->
                        createBucket(
                                properties.getDefaultCapacity(),
                                properties.getDefaultRefillTokens(),
                                properties.getDefaultRefillSeconds()));
    }

    private Bucket resolveAuthBucket(String key) {
        return authBuckets.computeIfAbsent(
                key,
                k ->
                        createBucket(
                                properties.getAuthCapacity(),
                                properties.getAuthRefillTokens(),
                                properties.getAuthRefillSeconds()));
    }

    private Bucket createBucket(int capacity, int refillTokens, int refillSeconds) {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(capacity)
                                .refillGreedy(refillTokens, Duration.ofSeconds(refillSeconds))
                                .build())
                .build();
    }
}

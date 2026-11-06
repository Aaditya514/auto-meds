package com.automeds.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token-bucket sliding window rate limiter as specified in SENIOR_ENGINEERING_CONVERSATION_TRANSCRIPT.md.
 * Defends /api/auth/login against credential stuffing and brute force attacks (5 attempts/min per IP).
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private final Map<String, ClientRequestInfo> ipRequestCounts = new ConcurrentHashMap<>();

    private static class ClientRequestInfo {
        long windowStart;
        final AtomicInteger count;

        ClientRequestInfo(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        boolean isLogin = "POST".equalsIgnoreCase(request.getMethod()) && uri.endsWith("/api/auth/login");
        boolean isScan = "POST".equalsIgnoreCase(request.getMethod()) && uri.endsWith("/api/prescriptions/scan");

        if (isLogin || isScan) {
            String clientIp = getClientIP(request);
            String bucketKey = (isLogin ? "login:" : "scan:") + clientIp;
            long currentTime = System.currentTimeMillis();
            int maxAllowed = isLogin ? MAX_REQUESTS_PER_MINUTE : 10;

            // Evict stale entries if map exceeds 500 records to prevent memory leak on 1GB RAM instances
            if (ipRequestCounts.size() > 500) {
                ipRequestCounts.entrySet().removeIf(entry -> (currentTime - entry.getValue().windowStart) > 120_000);
            }

            ClientRequestInfo info = ipRequestCounts.compute(bucketKey, (key, existing) -> {
                if (existing == null || (currentTime - existing.windowStart) > 60000) {
                    return new ClientRequestInfo(currentTime);
                } else {
                    existing.count.incrementAndGet();
                    return existing;
                }
            });

            if (info.count.get() > maxAllowed) {
                response.setStatus(429); // 429 Too Many Requests
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                String detailMsg = isLogin
                        ? "Too many login attempts. Please wait 1 minute before trying again."
                        : "Too many prescription scan requests. Please wait 1 minute before trying again.";
                response.getWriter().write("{\"type\":\"https://automeds.com/errors/too-many-requests\"," +
                        "\"title\":\"Too Many Requests\"," +
                        "\"status\":429," +
                        "\"detail\":\"" + detailMsg + "\"," +
                        "\"instance\":\"" + uri + "\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}

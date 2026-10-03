package com.automeds.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * CorrelationIdFilter — Phase 6 Observability
 *
 * Every incoming HTTP request receives a unique Correlation ID:
 *   1. If the client (Angular) sends an X-Correlation-ID header, it is reused.
 *   2. Otherwise, a fresh UUID is generated.
 *
 * The Correlation ID is:
 *   - Stored in SLF4J MDC so every log line in this request thread includes [corrId=...]
 *   - Echoed back in the X-Correlation-ID response header so the Angular frontend
 *     can match server-side log lines to the exact browser request.
 *
 * This enables production debugging scenarios like:
 *   "Show me all backend logs for this specific failed payment click"
 */
@Component
@Order(1) // Run before JWT and rate-limiting filters
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Accept incoming correlation ID from Angular, or generate a new one
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        // Bind to MDC for log pattern interpolation: [corrId=abc123]
        MDC.put(MDC_KEY, correlationId);

        // Echo the correlation ID back in the response header
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            chain.doFilter(servletRequest, servletResponse);
        } finally {
            // Always clean up MDC to prevent thread-pool leaks
            MDC.remove(MDC_KEY);
        }
    }
}

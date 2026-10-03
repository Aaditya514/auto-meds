import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * CorrelationIdInterceptor — Phase 6 Observability
 *
 * Attaches a unique X-Correlation-ID header to every outbound API request.
 * This ID is echoed back by the backend (CorrelationIdFilter.java) and
 * injected into every log line for that request via SLF4J MDC.
 *
 * This means when something breaks, you can:
 *   1. Find the X-Correlation-ID in the browser Network tab
 *   2. grep that ID in the Spring Boot logs to see the full server-side trace
 *
 * Example:
 *   Frontend sends: X-Correlation-ID: a3f2b1c9d4e5f678
 *   Backend logs:   [corrId=a3f2b1c9d4e5f678] PaymentService - Processing UPI payment...
 */
@Injectable()
export class CorrelationIdInterceptor implements HttpInterceptor {

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Only attach to API calls, not to CDN or analytics requests
    const isApiUrl = request.url.includes('/api/') || request.url.includes('localhost:8080');
    if (!isApiUrl) {
      return next.handle(request);
    }

    const correlationId = this.generateCorrelationId();
    const correlatedRequest = request.clone({
      setHeaders: {
        'X-Correlation-ID': correlationId
      }
    });

    return next.handle(correlatedRequest);
  }

  private generateCorrelationId(): string {
    // 16-char alphanumeric ID matching the backend's UUID-trimmed format
    return Math.random().toString(36).substring(2, 10)
         + Math.random().toString(36).substring(2, 10);
  }
}

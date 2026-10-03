/**
 * Auto-Meds Universal API Configuration
 * Supports seamless execution across:
 * 1. Local Angular Dev Server (`ng serve` on port 4200 -> direct backend on 8080)
 * 2. Production Docker / NGINX Reverse Proxy (relative `/api` -> no CORS, zero port mismatch)
 * 3. Custom runtime environment overrides via `window.__env.apiUrl`
 */
export function getApiBaseUrl(): string {
  if (typeof window !== 'undefined' && (window as any)?.__env?.apiUrl) {
    return (window as any).__env.apiUrl;
  }
  if (typeof window !== 'undefined' && window.location.port === '4200') {
    return 'http://localhost:8080/api';
  }
  return '/api';
}

export const API_BASE = getApiBaseUrl();

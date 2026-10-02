package com.automeds.controller;

import com.automeds.dto.AuthRequest;
import com.automeds.dto.AuthResponse;
import com.automeds.dto.RefreshTokenRequest;
import com.automeds.dto.RegisterRequest;
import com.automeds.exception.BadRequestException;
import com.automeds.security.UserPrincipal;
import com.automeds.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", refreshToken != null ? refreshToken : "")
                .httpOnly(true)
                .secure(false) // works for localhost HTTP; automatically sent over HTTPS in production
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerPatient(request);
        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken(), 30L * 24 * 60 * 60);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    // Protected: requires ROLE_ADMIN (enforced by SecurityConfig filter chain)
    @PostMapping("/register-admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerAdmin(request);
        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken(), 30L * 24 * 60 * 60);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken(), 30L * 24 * 60 * 60);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken) {
        String tokenStr = (request != null && request.getRefreshToken() != null && !request.getRefreshToken().trim().isEmpty())
                ? request.getRefreshToken()
                : cookieRefreshToken;

        if (tokenStr == null || tokenStr.trim().isEmpty()) {
            throw new BadRequestException("Refresh token is required either in request body or HttpOnly cookie.");
        }

        AuthResponse response = authService.refreshToken(new RefreshTokenRequest(tokenStr));
        ResponseCookie cookie = createRefreshTokenCookie(response.getRefreshToken(), 30L * 24 * 60 * 60);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody(required = false) Map<String, String> body,
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String refreshToken = (body != null && body.get("refreshToken") != null) ? body.get("refreshToken") : cookieRefreshToken;
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;

        authService.logout(refreshToken, userId);
        ResponseCookie cleanCookie = createRefreshTokenCookie("", 0);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(Map.of("message", "Logged out successfully."));
    }
}

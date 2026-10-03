package com.automeds.config;

import com.automeds.security.JwtAuthenticationFilter;
import com.automeds.security.RateLimitingFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private RateLimitingFilter rateLimitingFilter;

    public SecurityConfig() {
    }

    @org.springframework.beans.factory.annotation.Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitingFilter rateLimitingFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:8080", "http://127.0.0.1:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    response.getWriter().write("{\"type\":\"https://automeds.com/errors/unauthorized\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"" 
                            + authException.getMessage() + "\",\"instance\":\"" + request.getRequestURI() + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    response.getWriter().write("{\"type\":\"https://automeds.com/errors/forbidden\",\"title\":\"Forbidden\",\"status\":403,\"detail\":\"" 
                            + accessDeniedException.getMessage() + "\",\"instance\":\"" + request.getRequestURI() + "\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                // Only an authenticated ADMIN can register a new admin account
                .requestMatchers(HttpMethod.POST, "/api/auth/register-admin").hasAuthority("ROLE_ADMIN")
                // Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Public catalog search and medicine lookup
                .requestMatchers(HttpMethod.GET, "/api/medicines/**").permitAll()
                // Public health probes, error controller, and Prometheus metrics scrape endpoint
                .requestMatchers("/actuator/health", "/actuator/prometheus", "/actuator/info", "/error").permitAll()
                // Admin & Pharmacist procurement, restock operations, and compliance audit trail
                .requestMatchers("/api/admin/procurement/**", "/api/admin/audit-logs/**", "/api/audit-logs/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_PHARMACIST")
                // Admin specific management endpoints
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                // Pharmacist and clinical endpoints
                .requestMatchers("/api/pharmacist/**", "/api/prescriptions/verify/**", "/api/clinical/**", "/api/clinical-safety/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_PHARMACIST", "ROLE_PATIENT")
                // Patient specific operations
                .requestMatchers("/api/cart/**").hasAuthority("ROLE_PATIENT")
                // Caregiver proxy endpoints — WhatsApp webhook is open; all other caregiver routes require auth
                .requestMatchers("/api/caregiver/whatsapp-command").permitAll()
                .requestMatchers("/api/caregiver/**").authenticated()
                // Authenticated patient and staff operations
                .requestMatchers("/api/orders/**", "/api/payments/**", "/api/subscriptions/**", "/api/prescriptions/**").hasAnyAuthority("ROLE_PATIENT", "ROLE_ADMIN", "ROLE_PHARMACIST")
                .requestMatchers("/api/notifications/**").authenticated()
                // Deny all other unauthenticated requests by default
                .anyRequest().authenticated()
            );

        if (rateLimitingFilter != null) {
            http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        }

        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}

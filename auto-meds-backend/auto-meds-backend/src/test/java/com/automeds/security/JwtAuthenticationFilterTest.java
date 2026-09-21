package com.automeds.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtTokenProvider tokenProvider;
    private UserDetailsServiceImpl customUserDetailsService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        tokenProvider = Mockito.mock(JwtTokenProvider.class);
        customUserDetailsService = Mockito.mock(UserDetailsServiceImpl.class);
        filter = new JwtAuthenticationFilter(tokenProvider, customUserDetailsService);

        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternalWithValidBearerToken() throws Exception {
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        Mockito.when(tokenProvider.validateToken("valid.jwt.token")).thenReturn(true);
        Mockito.when(tokenProvider.getUserIdFromJWT("valid.jwt.token")).thenReturn(1L);

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(customUserDetailsService.loadUserById(1L)).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Mockito.verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithQueryParameterToken() throws Exception {
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);
        Mockito.when(request.getParameter("token")).thenReturn("query.jwt.token");
        Mockito.when(tokenProvider.validateToken("query.jwt.token")).thenReturn(true);
        Mockito.when(tokenProvider.getUserIdFromJWT("query.jwt.token")).thenReturn(2L);

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(customUserDetailsService.loadUserById(2L)).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Mockito.verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithNoToken() throws Exception {
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);
        Mockito.when(request.getParameter("token")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        Mockito.verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalExceptionHandling() throws Exception {
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        Mockito.when(tokenProvider.validateToken("valid.jwt.token")).thenReturn(true);
        Mockito.when(tokenProvider.getUserIdFromJWT("valid.jwt.token")).thenThrow(new RuntimeException("Token error"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        Mockito.verify(filterChain).doFilter(request, response);
    }
}



package com.automeds.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        filterChain = Mockito.mock(FilterChain.class);
    }

    @Test
    void testUnderLimitAllowsRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitingFilter.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
        Mockito.verify(filterChain).doFilter(request, response);
    }

    @Test
    void testExceedingLimitBlocksWith429() throws Exception {
        String ip = "192.168.1.50";

        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr(ip);
            MockHttpServletResponse res = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(req, res, filterChain);
            assertEquals(200, res.getStatus());
        }

        // 6th attempt within the minute window should be rejected with 429
        MockHttpServletRequest rejectedReq = new MockHttpServletRequest("POST", "/api/auth/login");
        rejectedReq.setRemoteAddr(ip);
        MockHttpServletResponse rejectedRes = new MockHttpServletResponse();
        rateLimitingFilter.doFilterInternal(rejectedReq, rejectedRes, filterChain);

        assertEquals(429, rejectedRes.getStatus());
    }
}

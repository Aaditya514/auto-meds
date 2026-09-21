package com.automeds.security;

import com.automeds.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "abcdefghijklmnopqrstuvwxyz12345678901234567890"; // 256+ bit key

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", 3600000);
    }

    @Test
    void testGenerateAndValidateToken() {
        User u = new User(100L, "Test", "test@example.com", "pass", "PATIENT", "123", "A", "C", "S", "111");
        UserPrincipal userPrincipal = UserPrincipal.create(u);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getPrincipal()).thenReturn(userPrincipal);

        String token = tokenProvider.generateToken(auth);
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(100L, tokenProvider.getUserIdFromJWT(token));

        // Direct user token generation test
        String userToken = tokenProvider.generateTokenForUser(200L, "other@example.com");
        assertNotNull(userToken);
        assertTrue(tokenProvider.validateToken(userToken));
        assertEquals(200L, tokenProvider.getUserIdFromJWT(userToken));
    }

    @Test
    void testInvalidTokens() {
        assertFalse(tokenProvider.validateToken("invalid.token.string"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
        assertFalse(tokenProvider.validateToken("eyJhbGciOiJIUzI1NiJ9.invalid.invalid"));

        // Expired token test
        JwtTokenProvider shortExpiryProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpiryProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(shortExpiryProvider, "jwtExpirationInMs", -1000);

        User u = new User(100L, "Test", "test@example.com", "pass", "PATIENT", "123", "A", "C", "S", "111");
        UserPrincipal userPrincipal = UserPrincipal.create(u);
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getPrincipal()).thenReturn(userPrincipal);
        String expiredToken = shortExpiryProvider.generateToken(auth);

        assertFalse(tokenProvider.validateToken(expiredToken));
    }
}



package com.automeds.service;

import com.automeds.entity.RefreshToken;
import com.automeds.entity.User;
import com.automeds.exception.BadRequestException;
import com.automeds.repository.RefreshTokenRepository;
import com.automeds.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class RefreshTokenServiceTest {

    private RefreshTokenRepository refreshTokenRepository;
    private UserRepository userRepository;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository);
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationInMs", 2592000000L);
    }

    @Test
    void testCreateRefreshToken() {
        User user = new User();
        user.setId(1L);

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        Mockito.when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(1L);
        assertNotNull(token);
        assertNotNull(token.getToken());
        assertFalse(token.isRevoked());
        assertTrue(token.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void testVerifyExpirationValid() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusSeconds(3600));
        token.setRevoked(false);

        RefreshToken verified = refreshTokenService.verifyExpiration(token);
        assertEquals(token, verified);
    }

    @Test
    void testVerifyExpirationExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(3600));
        token.setRevoked(false);

        assertThrows(BadRequestException.class, () -> refreshTokenService.verifyExpiration(token));
    }

    @Test
    void testVerifyExpirationRevoked() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusSeconds(3600));
        token.setRevoked(true);

        assertThrows(BadRequestException.class, () -> refreshTokenService.verifyExpiration(token));
    }

    @Test
    void testRevokeToken() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(false);
        Mockito.when(refreshTokenRepository.findByToken("tok123")).thenReturn(Optional.of(token));

        refreshTokenService.revokeToken("tok123");
        assertTrue(token.isRevoked());
        Mockito.verify(refreshTokenRepository).save(token);
    }
}

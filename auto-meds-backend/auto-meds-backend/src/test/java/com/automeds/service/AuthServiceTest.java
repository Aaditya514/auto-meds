package com.automeds.service;

import com.automeds.dto.AuthRequest;
import com.automeds.dto.AuthResponse;
import com.automeds.dto.RegisterRequest;
import com.automeds.entity.Cart;
import com.automeds.entity.User;
import com.automeds.exception.BadRequestException;
import com.automeds.repository.CartRepository;
import com.automeds.repository.UserRepository;
import com.automeds.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class AuthServiceTest {

    private UserRepository userRepository;
    private CartRepository cartRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        cartRepository = Mockito.mock(CartRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        tokenProvider = Mockito.mock(JwtTokenProvider.class);

        authService = new AuthService(userRepository, cartRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void testRegisterPatientSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("password123");

        Mockito.when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("password123")).thenReturn("encoded");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setRole("PATIENT");

        Mockito.when(userRepository.save(any(User.class))).thenReturn(savedUser);
        Mockito.when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());
        Mockito.when(tokenProvider.generateTokenForUser(eq(1L), eq("john@example.com"))).thenReturn("jwt.token");

        AuthResponse res = authService.registerPatient(req);

        assertNotNull(res);
        assertEquals("jwt.token", res.getToken());
        assertEquals(1L, res.getUserId());
        assertEquals("John Doe", res.getName());
    }

    @Test
    void testRegisterPatientInvalidNameWithNumbers() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John123");
        req.setEmail("john@example.com");
        req.setPassword("password123");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registerPatient(req));
        assertTrue(ex.getMessage().contains("Full Name should only contain alphabetic characters and spaces"));
    }

    @Test
    void testRegisterPatientInvalidNameWithSpecialChars() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John@Doe!");
        req.setEmail("john@example.com");
        req.setPassword("password123");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registerPatient(req));
        assertTrue(ex.getMessage().contains("Full Name should only contain alphabetic characters and spaces"));
    }

    @Test
    void testRegisterPatientNullOrEmptyName() {
        RegisterRequest req = new RegisterRequest();
        req.setName("   ");
        req.setEmail("john@example.com");
        req.setPassword("password123");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registerPatient(req));
        assertEquals("Full Name is required.", ex.getMessage());
    }

    @Test
    void testRegisterPatientInvalidEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John Doe");
        req.setEmail("johnexamplecom");
        req.setPassword("password123");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registerPatient(req));
        assertEquals("Please enter a valid email address.", ex.getMessage());
    }

    @Test
    void testRegisterPatientShortPassword() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("12345");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registerPatient(req));
        assertEquals("Password must be at least 6 characters long.", ex.getMessage());
    }

    @Test
    void testRegisterPatientDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("password123");
        Mockito.when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.registerPatient(req));
        assertEquals("An account with this email address already exists!", ex.getMessage());
    }

    @Test
    void testLoginSuccess() {
        AuthRequest req = new AuthRequest("john@example.com", "password123");

        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encoded");
        user.setRole("PATIENT");

        Mockito.when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        Mockito.when(tokenProvider.generateTokenForUser(1L, "john@example.com")).thenReturn("jwt.token");

        AuthResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals("jwt.token", res.getToken());
        assertEquals(1L, res.getUserId());
    }

    @Test
    void testLoginInvalidPassword() {
        AuthRequest req = new AuthRequest("john@example.com", "wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encoded");
        user.setRole("PATIENT");

        Mockito.when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrongpassword", "encoded")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }

    @Test
    void testLoginEmptyEmailOrPassword() {
        AuthRequest req1 = new AuthRequest("", "password123");
        assertThrows(BadRequestException.class, () -> authService.login(req1));

        AuthRequest req2 = new AuthRequest("john@example.com", "");
        assertThrows(BadRequestException.class, () -> authService.login(req2));
    }

    @Test
    void testLoginUserNotFound() {
        AuthRequest req = new AuthRequest("nonexistent@example.com", "password123");
        Mockito.when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }
}





package com.automeds.security;

import com.automeds.entity.User;
import com.automeds.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        service = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        User u = new User();
        u.setId(1L);
        u.setEmail("test@example.com");
        u.setPassword("pass");
        u.setRole("PATIENT");

        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("test@example.com");
        assertNotNull(details);
        assertEquals("test@example.com", details.getUsername());
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        Mockito.when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("notfound@example.com"));
    }

    @Test
    void testLoadUserByIdSuccess() {
        User u = new User();
        u.setId(1L);
        u.setEmail("test@example.com");
        u.setRole("ADMIN");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserById(1L);
        assertNotNull(details);
        assertEquals("test@example.com", details.getUsername());
    }

    @Test
    void testLoadUserByIdNotFound() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserById(99L));
    }
}



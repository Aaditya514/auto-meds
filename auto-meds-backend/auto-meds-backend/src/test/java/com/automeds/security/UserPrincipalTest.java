package com.automeds.security;

import com.automeds.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    @Test
    void testUserPrincipalGettersAndMethods() {
        UserPrincipal up = new UserPrincipal();
        assertNull(up.getId());
        assertNull(up.getName());
        assertNull(up.getEmail());

        UserPrincipal up2 = new UserPrincipal(1L, "Name", "email@test.com", "pass", Collections.emptyList());
        assertEquals(1L, up2.getId());
        assertEquals("Name", up2.getName());
        assertEquals("email@test.com", up2.getEmail());
        assertEquals("email@test.com", up2.getUsername());
        assertEquals("pass", up2.getPassword());
        assertTrue(up2.getAuthorities().isEmpty());
        assertTrue(up2.isAccountNonExpired());
        assertTrue(up2.isAccountNonLocked());
        assertTrue(up2.isCredentialsNonExpired());
        assertTrue(up2.isEnabled());
        assertTrue(up2.toString().contains("Name"));

        User u = new User();
        u.setId(2L);
        u.setName("Admin");
        u.setEmail("admin@test.com");
        u.setPassword("secret");
        u.setRole("ADMIN");

        UserPrincipal created = UserPrincipal.create(u);
        assertEquals(2L, created.getId());
        assertEquals("ROLE_ADMIN", created.getAuthorities().iterator().next().getAuthority());
    }
}



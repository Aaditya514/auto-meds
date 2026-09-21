package com.automeds.config;

import com.automeds.AutoMedsApplication;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void testOracleJdbcConfig() {
        OracleJdbcConfig config = new OracleJdbcConfig();
        assertNotNull(config);
    }

    @Test
    void testSecurityConfigBeans() {
        SecurityConfig securityConfig = new SecurityConfig();

        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);

        CorsConfigurationSource cors = securityConfig.corsConfigurationSource();
        assertNotNull(cors);
    }

    @Test
    void testAutoMedsApplicationInstantiation() {
        AutoMedsApplication app = new AutoMedsApplication();
        assertNotNull(app);
    }
}



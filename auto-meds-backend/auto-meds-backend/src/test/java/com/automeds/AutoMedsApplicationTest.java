package com.automeds;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class AutoMedsApplicationTest {

    @Test
    void testAutoMedsApplicationInstantiation() {
        AutoMedsApplication app = new AutoMedsApplication();
        assertNotNull(app);
    }

    @Test
    void testMainMethod() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = Mockito.mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(eq(AutoMedsApplication.class), any(String[].class)))
                    .thenReturn(Mockito.mock(ConfigurableApplicationContext.class));

            AutoMedsApplication.main(new String[]{});

            mockedSpringApplication.verify(() -> SpringApplication.run(eq(AutoMedsApplication.class), any(String[].class)));
        }
    }
}



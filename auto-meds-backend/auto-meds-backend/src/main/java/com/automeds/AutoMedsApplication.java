package com.automeds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AutoMedsApplication
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class AutoMedsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoMedsApplication.class, args);
    }
}

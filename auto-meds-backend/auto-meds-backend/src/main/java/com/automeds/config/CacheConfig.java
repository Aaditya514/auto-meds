package com.automeds.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: CacheConfig
 * Description: High-speed caching architecture (P3).
 * Uses embedded Caffeine near-cache with automatic LRU eviction and time-to-live policies,
 * providing sub-millisecond retrieval for medicine catalogs, live searches, and safety checks.
 */
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "medicines",
                "medicines_search",
                "medicine_by_id",
                "clinical_safety"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(2000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
        );

        log.info("Initialized high-performance Caffeine CacheManager (TTL=10m, MaxSize=2000 entries)");
        return cacheManager;
    }
}

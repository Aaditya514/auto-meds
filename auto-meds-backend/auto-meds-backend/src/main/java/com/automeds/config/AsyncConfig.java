package com.automeds.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AsyncConfig
 * Description: High-throughput asynchronous thread pool configuration (P0) for non-blocking
 * OCR document parsing, notification fan-out, and heavy background clinical workloads.
 */
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "ocrTaskExecutor")
    public Executor ocrTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("OcrWorker-");
        // CallerRunsPolicy guarantees zero task drops under peak saturation
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("Initialized dedicated OCR ThreadPoolTaskExecutor with coreSize=4, maxSize=8, queueCapacity=100");
        return executor;
    }
}

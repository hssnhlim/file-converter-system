package com.crumoria.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class FileConverterConfig {

    @Value("${file.converter.async.core-pool-size}")
    private int corePoolSize;

    @Value("${file.converter.async.max-pool-size}")
    private int maxPoolSize;

    @Value("${file.converter.async.queue-capacity}")
    private int queueCapacity;

    @Bean(name = "fileConverterTaskExecutor")
    public Executor fileConverterTaskExecutor() {
        ThreadPoolTaskExecutor executor =new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("FileConverter-");
        executor.initialize();
        return executor;
    }
}

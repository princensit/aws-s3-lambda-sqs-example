package com.prince.serverless.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Spring configuration
 *
 * @author Prince Raj
 */
@Configuration
@EnableRetry
@ComponentScan("com.prince")
@PropertySource("classpath:application-${profile:dev}.properties")
public class SpringConfig {
}

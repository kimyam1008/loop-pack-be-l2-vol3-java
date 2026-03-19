package com.loopers.infrastructure.pg;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class PgClientConfig {

    @Bean
    public RestTemplate pgRestTemplate(
        @Value("${pg.timeout.connect-millis:3000}") int connectMillis,
        @Value("${pg.timeout.read-millis:5000}") int readMillis
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectMillis);
        factory.setReadTimeout(readMillis);
        return new RestTemplate(factory);
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> pgCircuitBreakerCustomizer() {
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();

        // TimeLimiter는 RestTemplate read timeout(5s)보다 여유를 두어 설정
        TimeLimiterConfig tlConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(6))
            .build();

        return factory -> factory.configureDefault(id ->
            new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(cbConfig)
                .timeLimiterConfig(tlConfig)
                .build()
        );
    }
}

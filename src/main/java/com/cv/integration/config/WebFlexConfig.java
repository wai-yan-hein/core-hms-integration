package com.cv.integration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Objects;

@Configuration
@Slf4j
@PropertySource(value = {"file:config/application.properties"})
public class WebFlexConfig {

    @Autowired
    private Environment environment;

    @Bean
    public WebClient accountApi() {
        log.info("account api : " + environment.getProperty("account.url"));
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(config -> config
                                .defaultCodecs()
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .baseUrl(Objects.requireNonNull(environment.getProperty("account.url")))
                .clientConnector(reactorClientHttpConnector())
                .build();
    }
    @Bean
    public WebClient userApi() {
        log.info("user api : " + environment.getProperty("user.url"));
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(config -> config
                                .defaultCodecs()
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .baseUrl(Objects.requireNonNull(environment.getProperty("user.url")))
                .clientConnector(reactorClientHttpConnector())
                .build();
    }
    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("custom-provider")
                .maxConnections(10) // maximum number of connections
                .maxIdleTime(Duration.ofSeconds(10)) // maximum idle time
                .maxLifeTime(Duration.ofSeconds(60)) // maximum life time
                .pendingAcquireTimeout(Duration.ofSeconds(5)) // pending acquire timeout
                .evictInBackground(Duration.ofSeconds(30)) // eviction interval
                .build();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.create(connectionProvider());
    }

    @Bean
    public ReactorClientHttpConnector reactorClientHttpConnector() {
        return new ReactorClientHttpConnector(httpClient());
    }
}

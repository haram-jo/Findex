package com.codeit.findex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class WebClientConfig {

    @Value("${external.finance.base-url}")
    private String baseUrl;

    @Bean
    public WebClient financeWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setAccept(List.of(
                            MediaType.APPLICATION_JSON,
                            new MediaType("application", "json", StandardCharsets.UTF_8)
                    ));
                })
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer ->
                                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
                        )
                        .build()
                )
                .build();
    }
}
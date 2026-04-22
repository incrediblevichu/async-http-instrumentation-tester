package com.wizoz.async.tester.client.async.impl.vertX;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertXConfig {

    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public WebClient webClient(Vertx vertx) {
        return WebClient.create(vertx);
    }
}

package com.bitbox.chatting.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;

public class FeignServiceClientConfig {
    @Bean
    public RequestInterceptor requestInterceptor(@RequestHeader("Authorization") String authorization) {
        return template -> {
            template.header("Content-Type", "application/json");
            template.header(HttpHeaders.AUTHORIZATION, "Bearer "+authorization);
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}

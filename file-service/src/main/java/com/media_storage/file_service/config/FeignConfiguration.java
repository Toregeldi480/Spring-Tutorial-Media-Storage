package com.media_storage.file_service.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {
    private final ApplicationConfiguration applicationConfiguration;

    public FeignConfiguration(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Bean
    public RequestInterceptor gatewayTokenInterceptor() {
        return requestTemplate -> requestTemplate.header("X-Gateway-Token", applicationConfiguration.getGatewayToken());
    }
}

package com.media_storage.file_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Value("${gateway.token}")
    private String gatewayToken;

    public String getGatewayToken() {
        return this.gatewayToken;
    }
}

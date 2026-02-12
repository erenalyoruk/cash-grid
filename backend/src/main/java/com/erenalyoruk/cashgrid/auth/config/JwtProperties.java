package com.erenalyoruk.cashgrid.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private long accessTokenExpiration = 900000; // 15 minutes
    private long refreshTokenExpiration = 604800000; // 7 days
}

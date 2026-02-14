package com.erenalyoruk.cashgrid.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    private int defaultCapacity = 60;
    private int defaultRefillTokens = 60;
    private int defaultRefillSeconds = 60;

    private int authCapacity = 10;
    private int authRefillTokens = 10;
    private int authRefillSeconds = 60;
}

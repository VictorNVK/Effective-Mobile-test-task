package com.example.bankcards.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String secret;

    private long accessTokenTtl;

    private long refreshTokenTtl;

    private String issuer;
}

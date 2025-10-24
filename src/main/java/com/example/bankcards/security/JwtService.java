package com.example.bankcards.security;

import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.exception.JwtValidationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtService {

    public static final String ROLE_CLAIM = "role";
    public static final String TOKEN_TYPE_CLAIM = "token_type";

    private final JwtProperties properties;

    private Key signingKey;

    @PostConstruct
    void initKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String subject, RoleType role) {
        return buildToken(subject, role, JwtTokenType.ACCESS, properties.getAccessTokenTtl());
    }

    public String generateRefreshToken(String subject, RoleType role) {
        return buildToken(subject, role, JwtTokenType.REFRESH, properties.getRefreshTokenTtl());
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            return ex.getClaims();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtValidationException("Invalid JWT token", ex);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails, JwtTokenType expectedType) {
        Claims claims = parseClaims(token);
        String username = claims.getSubject();
        if (username == null || !username.equals(userDetails.getUsername())) {
            return false;
        }
        if (!expectedType.name().equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            return false;
        }
        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            return false;
        }
        return true;
    }

    public RoleType extractRole(Claims claims) {
        String roleValue = claims.get(ROLE_CLAIM, String.class);
        if (roleValue == null) {
            throw new JwtValidationException("Token does not contain role information");
        }
        return RoleType.valueOf(roleValue);
    }

    public JwtTokenType extractTokenType(Claims claims) {
        String tokenTypeValue = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (tokenTypeValue == null) {
            throw new JwtValidationException("Token does not contain token type information");
        }
        return JwtTokenType.valueOf(tokenTypeValue);
    }

    public long getAccessTokenTtl() {
        return properties.getAccessTokenTtl();
    }

    public long getRefreshTokenTtl() {
        return properties.getRefreshTokenTtl();
    }

    private String buildToken(String subject, RoleType role, JwtTokenType tokenType, long ttlSeconds) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(now.plusSeconds(ttlSeconds));

        return Jwts.builder()
                .subject(subject)
                .issuer(properties.getIssuer())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .claims(Map.of(
                        ROLE_CLAIM, role.name(),
                        TOKEN_TYPE_CLAIM, tokenType.name()
                ))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}

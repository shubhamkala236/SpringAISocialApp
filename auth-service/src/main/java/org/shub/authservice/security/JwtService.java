package org.shub.authservice.security;

import org.shub.authservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
    public static final String CLAIM_USERNAME = "name";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_AVATAR_URL = "avatarUrl";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.expirationDays(), ChronoUnit.DAYS);

        var builder = Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_EMAIL, user.getEmail())
                .issuer(jwtProperties.issuer())
                .audience().add(jwtProperties.audience()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey);

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            builder.claim(CLAIM_AVATAR_URL, user.getAvatarUrl());
        }

        return builder.compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

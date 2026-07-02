package org.shub.aiservice.security;

import io.jsonwebtoken.Claims;

import java.util.UUID;

public record AuthenticatedUser(String subject, Claims claims) {

    public UUID userId() {
        return UUID.fromString(subject);
    }

    public String username() {
        return claims.get(JwtService.CLAIM_USERNAME, String.class);
    }
}
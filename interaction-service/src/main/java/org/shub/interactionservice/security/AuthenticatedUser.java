package org.shub.interactionservice.security;

import io.jsonwebtoken.Claims;

import java.util.UUID;

public record AuthenticatedUser(String subject, Claims claims) {

    public UUID userId() {
        return UUID.fromString(subject);
    }

    public String username() {
        return claims.get(JwtService.CLAIM_USERNAME, String.class);
    }

    public String email() {
        return claims.get(JwtService.CLAIM_EMAIL, String.class);
    }

    public String avatarUrl() {
        return claims.get(JwtService.CLAIM_AVATAR_URL, String.class);
    }
}
package org.shub.postservice.dto;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID userId,
        String username,
        String userAvatarUrl,
        String title,
        String content,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
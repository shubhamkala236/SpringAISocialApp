package org.shub.interactionservice.dto;

import java.time.Instant;
import java.util.UUID;

// Equivalent of SavedPostDto.cs (the response shape returned to the client)
public record SavedPostResponse(
        UUID postId,
        String postTitle,
        String postContent,
        String postUsername,
        String postImageUrl,
        Instant postCreatedAt,
        Instant savedAt
) {
}
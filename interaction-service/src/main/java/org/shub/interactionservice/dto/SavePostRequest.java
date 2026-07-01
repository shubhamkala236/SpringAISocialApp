// SavePostRequest.java
package org.shub.interactionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

// Equivalent of SavePostDto.cs (the request shape sent by the client)
public record SavePostRequest(
        @NotNull UUID postId,
        @NotBlank String postTitle,
        @NotBlank String postContent,
        @NotBlank String postUsername,
        String postImageUrl,
        @NotNull Instant postCreatedAt
) {
}
package org.shub.interactionservice.dto;

import java.util.UUID;

public record PostInteractionResponse(
        UUID postId,
        int likesCount,
        boolean isLiked,
        boolean isSaved
) {
}
package org.shub.interactionservice.dto;

public record LikeResultResponse(
        boolean isLiked,
        int likesCount
) {
}
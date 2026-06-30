package org.shub.userservice.dto;

import java.util.UUID;

/**
 * Equivalent of UserService.Application/DTO/FollowDto.cs - a lightweight
 * summary used in followers/following list endpoints (not the full profile).
 */
public record FollowResponse(
        UUID userId,
        String username,
        String avatarUrl
) {
}
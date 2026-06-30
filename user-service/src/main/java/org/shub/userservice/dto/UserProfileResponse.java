package org.shub.userservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Equivalent of UserService.Application/DTO/UserProfileDto.cs.
 * followerCount/followingCount are computed at read time in the service
 * layer (via FollowRepository counts), not stored columns - same approach
 * the .NET version used.
 */
public record UserProfileResponse(
        UUID userId,
        String username,
        String bio,
        String avatarUrl,
        long followerCount,
        long followingCount,
        boolean isFollowedByCurrentUser,
        Instant createdAt
) {
}
package shared;

import java.time.Instant;
import java.util.UUID;

public record UserFollowedEvent(
        UUID followerId,
        String followerUsername,
        UUID followingId,
        String followingUsername,
        Instant followedAt
) {
}
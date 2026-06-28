package shared;

import java.time.Instant;
import java.util.UUID;

public record PostLikedEvent(
        UUID postId,
        UUID userId,
        String username,
        UUID postOwnerId,
        Instant likedAt
) {
}
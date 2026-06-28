package shared;

import java.time.Instant;
import java.util.UUID;

public record PostDeletedEvent(
        UUID postId,
        UUID userId,
        Instant deletedAt
) {
}
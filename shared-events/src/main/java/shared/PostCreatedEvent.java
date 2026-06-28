package shared;

import java.time.Instant;
import java.util.UUID;

public record PostCreatedEvent(
        UUID postId,
        UUID userId,
        String username,
        String title,
        Instant createdAt
) {
}
package shared;

import java.time.Instant;
import java.util.UUID;

public record UserDeletedEvent(
        UUID userId,
        String username,
        Instant deletedAt
) {
}
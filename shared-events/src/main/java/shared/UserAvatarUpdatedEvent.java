package shared;

import java.util.UUID;

public record UserAvatarUpdatedEvent(
        UUID userId,
        String avatarUrl
) {
}
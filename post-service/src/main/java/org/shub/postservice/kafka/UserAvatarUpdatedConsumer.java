package org.shub.postservice.kafka;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shub.postservice.repository.PostRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shared.UserAvatarUpdatedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserAvatarUpdatedConsumer {
    private final PostRepository postRepository;

    @KafkaListener(topics = "user.avatar-updated", groupId = "post-service-group")
    @Transactional
    public void handleAvatarUpdated(UserAvatarUpdatedEvent event) {
        log.info("Received UserAvatarUpdatedEvent for userId={}", event.userId());
        postRepository.updateAvatarUrlForUser(event.userId(), event.avatarUrl());
        log.info("Updated avatar on all posts for userId={}", event.userId());
    }
}

package org.shub.userservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shub.userservice.entity.UserProfile;
import org.shub.userservice.repository.FollowRepository;
import org.shub.userservice.repository.UserProfileRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shared.UserDeletedEvent;
import shared.UserRegisteredEvent;

// NOTE: adjust this import to match your actual shared-events package -
// confirm with: find shared-events/src -name "*.java" | head -1

/**
 * Equivalent of UserService.Infrastructure/Consumers/UserRegisteredConsumer.cs
 * and UserDeletedConsumer.cs. Spring Kafka's @KafkaListener replaces
 * MassTransit's IConsumer<T> interface - one method per event type, each
 * annotated with the topic it listens to.*
 * groupId matters: all instances of user-service sharing the same groupId
 * split the partitions between them (consumer group semantics), same
 * concept as MassTransit's queue-per-consumer-group behavior.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;

    @KafkaListener(topics = "user.registered", groupId = "user-service-group")
    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for userId={}", event.userId());

        // Equivalent of the .NET UserRegisteredConsumer creating a UserProfile
        // row keyed by the same userId - idempotency check first, since
        // Kafka delivers at-least-once and this listener could see the
        // same message twice on rebalance/retry.
        if (userProfileRepository.existsById(event.userId())) {
            log.info("Profile already exists for userId={}, skipping", event.userId());
            return;
        }

        UserProfile profile = UserProfile.builder()
                .userId(event.userId())
                .username(event.username())
                .build();

        userProfileRepository.save(profile);
        log.info("Created profile for userId={}", event.userId());
    }

    @KafkaListener(topics = "user.deleted", groupId = "user-service-group")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Received UserDeletedEvent for userId={}", event.userId());

        // Equivalent of the .NET UserDeletedConsumer - clean up the profile
        // row and any follow relationships involving this user, in either
        // direction (as follower or as the one being followed).
        userProfileRepository.deleteById(event.userId());

        followRepository.findByFollowerId(event.userId())
                .forEach(follow -> followRepository.deleteById(follow.getId()));
        followRepository.findByFollowingId(event.userId())
                .forEach(follow -> followRepository.deleteById(follow.getId()));

        log.info("Deleted profile and follow relationships for userId={}", event.userId());
    }
}
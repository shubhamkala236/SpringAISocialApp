package org.shub.interactionservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shub.interactionservice.repository.PostLikeRepository;
import org.shub.interactionservice.repository.SavedPostRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shared.PostDeletedEvent;
import shared.UserDeletedEvent;


/**
 * Equivalent of PostDeletedConsumer.cs + UserDeletedConsumer.cs combined.
 * Uses RemoveRange-equivalent bulk deletes via Spring Data's
 * deleteAll(Iterable) instead of loading entities one by one, mirroring
 * the original _context.PostLikes.RemoveRange(likes) pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InteractionEventConsumer {

    private final PostLikeRepository postLikeRepository;
    private final SavedPostRepository savedPostRepository;

    @KafkaListener(topics = "post.deleted", groupId = "interaction-service-group")
    @Transactional
    public void handlePostDeleted(PostDeletedEvent event) {
        log.info("Received PostDeletedEvent for postId={}", event.postId());

        var likes = postLikeRepository.findByPostId(event.postId());
        var saves = savedPostRepository.findByPostId(event.postId());

        postLikeRepository.deleteAll(likes);
        savedPostRepository.deleteAll(saves);

        log.info("Cleaned {} likes and {} saves for deleted postId={}", likes.size(), saves.size(), event.postId());
    }

    @KafkaListener(topics = "user.deleted", groupId = "interaction-service-group")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Received UserDeletedEvent for userId={}", event.userId());

        var likes = postLikeRepository.findByUserId(event.userId());
        var saves = savedPostRepository.findByUserId(event.userId());

        postLikeRepository.deleteAll(likes);
        savedPostRepository.deleteAll(saves);

        log.info("Cleaned {} likes and {} saves for deleted userId={}", likes.size(), saves.size(), event.userId());
    }
}
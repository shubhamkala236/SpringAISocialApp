package org.shub.interactionservice.service;

import lombok.RequiredArgsConstructor;
import org.shub.interactionservice.dto.*;
import org.shub.interactionservice.entity.PostLike;
import org.shub.interactionservice.entity.SavedPost;
import org.shub.interactionservice.exception.AlreadySavedException;
import org.shub.interactionservice.exception.SavedPostNotFoundException;
import org.shub.interactionservice.repository.PostLikeRepository;
import org.shub.interactionservice.repository.SavedPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Equivalent of InteractionService.Infrastructure/Services/InteractionService.cs.
 * Full faithful port including the batch interactions endpoint which does
 * GROUP BY in the repository layer rather than N+1 per-post queries.
 */
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final PostLikeRepository postLikeRepository;
    private final SavedPostRepository savedPostRepository;

    // ── Likes ──────────────────────────────────────────────────────────────

    @Transactional
    public LikeResultResponse toggleLike(UUID postId, UUID userId, String username) {
        var existing = postLikeRepository.findByPostIdAndUserId(postId, userId);

        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
        } else {
            postLikeRepository.save(PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .username(username)
                    .build());
        }

        int count = postLikeRepository.countByPostId(postId);
        boolean isLiked = existing.isEmpty(); // if it existed before, we unliked it

        return new LikeResultResponse(isLiked, count);
    }

    public int getLikesCount(UUID postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public boolean isLikedByUser(UUID postId, UUID userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    // ── Saves ──────────────────────────────────────────────────────────────

    @Transactional
    public boolean savePost(UUID userId, SavePostRequest request) {
        if (savedPostRepository.existsByPostIdAndUserId(request.postId(), userId)) {
            throw new AlreadySavedException("Post already saved.");
        }

        savedPostRepository.save(SavedPost.builder()
                .postId(request.postId())
                .userId(userId)
                .postTitle(request.postTitle())
                .postContent(request.postContent())
                .postUsername(request.postUsername())
                .postImageUrl(request.postImageUrl())
                .postCreatedAt(request.postCreatedAt())
                .build());

        return true;
    }

    @Transactional
    public boolean unsavePost(UUID postId, UUID userId) {
        var saved = savedPostRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new SavedPostNotFoundException("Saved post not found."));

        savedPostRepository.delete(saved);
        return true;
    }

    public boolean isPostSaved(UUID postId, UUID userId) {
        return savedPostRepository.existsByPostIdAndUserId(postId, userId);
    }

    public List<SavedPostResponse> getSavedPosts(UUID userId) {
        return savedPostRepository.findByUserIdOrderBySavedAtDesc(userId).stream()
                .map(s -> new SavedPostResponse(
                        s.getPostId(),
                        s.getPostTitle(),
                        s.getPostContent(),
                        s.getPostUsername(),
                        s.getPostImageUrl(),
                        s.getPostCreatedAt(),
                        s.getSavedAt()
                ))
                .toList();
    }

    // ── Combined ───────────────────────────────────────────────────────────

    public PostInteractionResponse getPostInteractions(UUID postId, UUID userId) {
        int likesCount = postLikeRepository.countByPostId(postId);
        boolean isLiked = userId != null && postLikeRepository.existsByPostIdAndUserId(postId, userId);
        boolean isSaved = userId != null && savedPostRepository.existsByPostIdAndUserId(postId, userId);

        return new PostInteractionResponse(postId, likesCount, isLiked, isSaved);
    }

    /**
     * Equivalent of GetPostsInteractionsAsync - batch fetch for multiple
     * posts in one call. Uses the GROUP BY query in PostLikeRepository and
     * the IN-clause queries in both repositories to avoid N+1 per post.
     * This is the most performance-sensitive method in this service since
     * the frontend calls it for every post in a feed render.
     */
    public List<PostInteractionResponse> getPostsInteractions(List<UUID> postIds, UUID userId) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }

        // One GROUP BY query for all like counts
        List<Object[]> likeCounts = postLikeRepository.countByPostIdIn(postIds);
        Map<UUID, Integer> likeCountMap = likeCounts.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        // One IN-clause query for all posts this user liked
        List<UUID> likedPostIds = userId != null
                ? postLikeRepository.findLikedPostIdsByUser(postIds, userId)
                : List.of();

        // One IN-clause query for all posts this user saved
        List<UUID> savedPostIds = userId != null
                ? savedPostRepository.findSavedPostIdsByUser(postIds, userId)
                : List.of();

        return postIds.stream()
                .map(postId -> new PostInteractionResponse(
                        postId,
                        likeCountMap.getOrDefault(postId, 0),
                        likedPostIds.contains(postId),
                        savedPostIds.contains(postId)
                ))
                .toList();
    }
}
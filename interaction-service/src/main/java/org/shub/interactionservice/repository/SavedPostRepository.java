package org.shub.interactionservice.repository;

import org.shub.interactionservice.entity.SavedPost;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedPostRepository extends JpaRepository<SavedPost, UUID> {

    Optional<SavedPost> findByPostIdAndUserId(UUID postId, UUID userId);

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    List<SavedPost> findByUserIdOrderBySavedAtDesc(UUID userId);

    List<SavedPost> findByPostId(UUID postId);

    List<SavedPost> findByUserId(UUID userId);

    /**
     * Batch saved-by-user check - mirrors the .NET LINQ Where+Select
     * approach for the batch interactions endpoint.
     */
    @Query("SELECT s.postId FROM SavedPost s WHERE s.postId IN :postIds AND s.userId = :userId")
    List<UUID> findSavedPostIdsByUser(@Param("postIds") List<UUID> postIds, @Param("userId") UUID userId);
}
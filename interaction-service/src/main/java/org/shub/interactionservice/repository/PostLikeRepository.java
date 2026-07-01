package org.shub.interactionservice.repository;

import org.shub.interactionservice.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId);

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    int countByPostId(UUID postId);

    List<PostLike> findByPostId(UUID postId);

    List<PostLike> findByUserId(UUID userId);

    /**
     * Batch count query for the GetPostsInteractionsAsync equivalent -
     * returns like counts for multiple posts in one query using GROUP BY,
     * mirroring the original .NET LINQ GroupBy approach.
     */
    @Query("SELECT p.postId, COUNT(p) FROM PostLike p WHERE p.postId IN :postIds GROUP BY p.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") List<UUID> postIds);

    /**
     * Batch liked-by-user check - returns all postIds from the given list
     * that the user has liked, in one query.
     */
    @Query("SELECT p.postId FROM PostLike p WHERE p.postId IN :postIds AND p.userId = :userId")
    List<UUID> findLikedPostIdsByUser(@Param("postIds") List<UUID> postIds, @Param("userId") UUID userId);
}
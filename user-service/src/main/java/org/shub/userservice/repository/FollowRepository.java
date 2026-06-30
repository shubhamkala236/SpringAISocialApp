package org.shub.userservice.repository;

import org.shub.userservice.dto.FollowResponse;
import org.shub.userservice.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    Optional<Follow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    List<Follow> findByFollowerId(UUID followerId);

    List<Follow> findByFollowingId(UUID followingId);

    long countByFollowerId(UUID followerId);

    long countByFollowingId(UUID followingId);

    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
    /**
     * Single JOIN query replacing the old "fetch all Follow rows, then
     * fetch each profile individually" N+1 pattern. JPQL's constructor
     * expression (new ...FollowResponse(...)) builds the DTO directly from
     * the joined result set - no UserProfile entities are loaded or
     * managed, just the three columns we actually need.
     *
     * Joining Follow.followerId to UserProfile.userId explicitly (rather
     * than via a JPA-mapped relationship) keeps Follow and UserProfile as
     * independent entities, which is what we want given Follow carries its
     * own metadata (createdAt) - see the earlier discussion on why
     * @ManyToMany doesn't fit this shape.
     */
    @Query("""
            SELECT new org.shub.userservice.dto.FollowResponse(p.userId, p.username, p.avatarUrl)
            FROM Follow f
            JOIN UserProfile p ON f.followerId = p.userId
            WHERE f.followingId = :userId
            ORDER BY f.createdAt DESC
            """)
    List<FollowResponse> findFollowersOf(@Param("userId") UUID userId);

    @Query("""
            SELECT new org.shub.userservice.dto.FollowResponse(p.userId, p.username, p.avatarUrl)
            FROM Follow f
            JOIN UserProfile p ON f.followingId = p.userId
            WHERE f.followerId = :userId
            ORDER BY f.createdAt DESC
            """)
    List<FollowResponse> findFollowingOf(@Param("userId") UUID userId);
}

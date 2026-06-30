package org.shub.postservice.repository;

import org.shub.postservice.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByUserId(UUID userId, Pageable pageable);

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Bulk update for the avatar-denormalization consumer - one UPDATE
     * statement touching every post by this author, instead of loading
     * every Post entity into memory, mutating each, and saving one by one.
     * Equivalent of an EF Core ExecuteUpdateAsync bulk update.
     */
    @Modifying
    @Query("UPDATE Post p SET p.userAvatarUrl = :avatarUrl WHERE p.userId = :userId")
    void updateAvatarUrlForUser(@Param("userId") UUID userId, @Param("avatarUrl") String avatarUrl);

    List<Post> findByUserId(UUID userId);
}

package org.shub.interactionservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Equivalent of InteractionService.Domain/Entity/SavedPost.cs.
 * Full snapshot of post data at save-time (PostTitle, PostContent,
 * PostUsername, PostImageUrl, PostCreatedAt) - interaction-service never
 * calls post-service at runtime; it snapshots what it needs when the user
 * explicitly saves the post, exactly as the original .NET design does.
 */
@Entity
@Table(name = "saved_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedPost {

    @Id
    @Builder.Default
    @Column(updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "post_title", nullable = false, length = 200)
    private String postTitle;

    @Column(name = "post_content", nullable = false, length = 2000)
    private String postContent;

    @Column(name = "post_username", nullable = false, length = 100)
    private String postUsername;

    @Column(name = "post_image_url")
    private String postImageUrl;

    @Column(name = "post_created_at", nullable = false)
    private Instant postCreatedAt;

    @Column(name = "saved_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant savedAt = Instant.now();
}
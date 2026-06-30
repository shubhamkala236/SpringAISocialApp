package org.shub.postservice.service;

import lombok.RequiredArgsConstructor;
import org.shub.postservice.dto.CreatePostRequest;
import org.shub.postservice.dto.PostResponse;
import org.shub.postservice.dto.UpdatePostRequest;
import org.shub.postservice.entity.Post;
import org.shub.postservice.exception.PostNotFoundException;
import org.shub.postservice.exception.UnauthorizedActionException;
import org.shub.postservice.kafka.EventPublisher;
import org.shub.postservice.repository.PostRepository;
import org.shub.postservice.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shared.PostCreatedEvent;
import shared.PostDeletedEvent;

// Adjust to match your actual shared-events package

import java.time.Instant;
import java.util.UUID;

/**
 * Equivalent of PostService.Infrastructure/Service/PostServices.cs.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PhotoService photoService;
    private final EventPublisher eventPublisher;

    @Transactional
    public PostResponse createPost(AuthenticatedUser currentUser, CreatePostRequest request, MultipartFile image) {
        String imageUrl = null;
        String imagePublicId = null;

        if (image != null && !image.isEmpty()) {
            var uploadResult = photoService.uploadPostImage(image);
            imageUrl = uploadResult.url();
            imagePublicId = uploadResult.publicId();
        }

        Post post = Post.builder()
                .userId(currentUser.userId())
                .username(currentUser.username())
                .userAvatarUrl(currentUser.avatarUrl())
                .title(request.title())
                .content(request.content())
                .imageUrl(imageUrl)
                .imagePublicId(imagePublicId)
                .build();

        post = postRepository.save(post);

        eventPublisher.publish(
                "post.created",
                post.getId().toString(),
                new PostCreatedEvent(post.getId(), post.getUserId(), post.getUsername(), post.getTitle(), Instant.now())
        );

        return toResponse(post);
    }

    public PostResponse getPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found."));
        return toResponse(post);
    }

    public Page<PostResponse> getFeed(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    public Page<PostResponse> getPostsByUser(UUID userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional
    public PostResponse updatePost(UUID postId, UUID currentUserId, UpdatePostRequest request, MultipartFile image) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found."));

        if (!post.getUserId().equals(currentUserId)) {
            throw new UnauthorizedActionException("You can only edit your own posts.");
        }

        if (request.title() != null) {
            post.setTitle(request.title());
        }
        if (request.content() != null) {
            post.setContent(request.content());
        }

        if (image != null && !image.isEmpty()) {
            // Clean up the old image before uploading the replacement
            if (post.getImagePublicId() != null) {
                photoService.deleteImage(post.getImagePublicId());
            }
            var uploadResult = photoService.uploadPostImage(image);
            post.setImageUrl(uploadResult.url());
            post.setImagePublicId(uploadResult.publicId());
        }

        post.setUpdatedAt(Instant.now());
        postRepository.save(post);

        return toResponse(post);
    }

    @Transactional
    public void deletePost(UUID postId, UUID currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found."));

        if (!post.getUserId().equals(currentUserId)) {
            throw new UnauthorizedActionException("You can only delete your own posts.");
        }

        if (post.getImagePublicId() != null) {
            photoService.deleteImage(post.getImagePublicId());
        }

        postRepository.deleteById(postId);

        eventPublisher.publish(
                "post.deleted",
                postId.toString(),
                new PostDeletedEvent(postId, currentUserId, Instant.now())
        );
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getUserId(),
                post.getUsername(),
                post.getUserAvatarUrl(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
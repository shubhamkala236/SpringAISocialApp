package org.shub.postservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shub.postservice.dto.CreatePostRequest;
import org.shub.postservice.dto.PostResponse;
import org.shub.postservice.dto.UpdatePostRequest;
import org.shub.postservice.security.AuthenticatedUser;
import org.shub.postservice.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestPart @Valid CreatePostRequest request,
            @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(currentUser, request, image));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getFeed(Pageable pageable) {
        return ResponseEntity.ok(postService.getFeed(pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getPostsByUser(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByUser(userId, pageable));
    }

    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestPart(required = false) @Valid UpdatePostRequest request,
            @RequestPart(required = false) MultipartFile image) {
        UpdatePostRequest effectiveRequest = request != null ? request : new UpdatePostRequest(request.title(), request.content());
        return ResponseEntity.ok(postService.updatePost(postId, currentUser.userId(), effectiveRequest, image));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        postService.deletePost(postId, currentUser.userId());
        return ResponseEntity.noContent().build();
    }
}
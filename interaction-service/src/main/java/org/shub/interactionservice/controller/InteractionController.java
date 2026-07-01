package org.shub.interactionservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shub.interactionservice.dto.*;
import org.shub.interactionservice.security.AuthenticatedUser;
import org.shub.interactionservice.service.InteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Equivalent of InteractionsController.cs.
 * Route is /api/interactions matching [Route("api/[controller]")] which
 * lowercases "Interactions" to "interactions" in ASP.NET convention.
 */
@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    // ── Likes ──────────────────────────────────────────────────────────────

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<LikeResultResponse> toggleLike(
            @PathVariable UUID postId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(interactionService.toggleLike(postId, currentUser.userId(), currentUser.username()));
    }

    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<?> getLikesCount(@PathVariable UUID postId) {
        return ResponseEntity.ok(new java.util.LinkedHashMap<String, Object>() {{
            put("likesCount", interactionService.getLikesCount(postId));
        }});
    }

    @GetMapping("/posts/{postId}/is-liked")
    public ResponseEntity<?> isLiked(
            @PathVariable UUID postId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(new java.util.LinkedHashMap<String, Object>() {{
            put("isLiked", interactionService.isLikedByUser(postId, currentUser.userId()));
        }});
    }

    // ── Saves ──────────────────────────────────────────────────────────────

    @PostMapping("/posts/save")
    public ResponseEntity<MessageResponse> savePost(
            @RequestBody @Valid SavePostRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        interactionService.savePost(currentUser.userId(), request);
        return ResponseEntity.ok(new MessageResponse("Post saved."));
    }

    @DeleteMapping("/posts/{postId}/save")
    public ResponseEntity<MessageResponse> unsavePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        interactionService.unsavePost(postId, currentUser.userId());
        return ResponseEntity.ok(new MessageResponse("Post unsaved."));
    }

    @GetMapping("/posts/saved")
    public ResponseEntity<List<SavedPostResponse>> getSavedPosts(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(interactionService.getSavedPosts(currentUser.userId()));
    }

    // ── Combined ───────────────────────────────────────────────────────────

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostInteractionResponse> getPostInteractions(
            @PathVariable UUID postId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        UUID userId = currentUser != null ? currentUser.userId() : null;
        return ResponseEntity.ok(interactionService.getPostInteractions(postId, userId));
    }

    @PostMapping("/posts/batch")
    public ResponseEntity<List<PostInteractionResponse>> getBatchInteractions(
            @RequestBody List<UUID> postIds,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        UUID userId = currentUser != null ? currentUser.userId() : null;
        return ResponseEntity.ok(interactionService.getPostsInteractions(postIds, userId));
    }
}
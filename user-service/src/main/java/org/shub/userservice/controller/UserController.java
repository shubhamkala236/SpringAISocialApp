package org.shub.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shub.userservice.dto.FollowResponse;
import org.shub.userservice.dto.UpdateProfileRequest;
import org.shub.userservice.dto.UserProfileResponse;
import org.shub.userservice.security.AuthenticatedUser;
import org.shub.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Equivalent of UserService.API/Controllers/UsersController.cs.
 * AuthenticationPrincipal injects the AuthenticatedUser our
 * JwtAuthenticationFilter placed in the SecurityContext - equivalent of
 * reading User.FindFirstValue(ClaimTypes.NameIdentifier) in the C# version.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(userService.getProfile(userId, currentUser.userId()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(userService.getProfile(currentUser.userId(), currentUser.userId()));
    }

    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestPart(required = false) @Valid UpdateProfileRequest request,
            @RequestPart(required = false) MultipartFile avatar) {

        UpdateProfileRequest effectiveRequest = request != null ? request : new UpdateProfileRequest(null);
        return ResponseEntity.ok(userService.updateProfile(currentUser.userId(), effectiveRequest, avatar));
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> follow(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        userService.follow(currentUser.userId(), userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<Void> unfollow(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        userService.unfollow(currentUser.userId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<FollowResponse>> getFollowing(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getFollowing(userId));
    }
}
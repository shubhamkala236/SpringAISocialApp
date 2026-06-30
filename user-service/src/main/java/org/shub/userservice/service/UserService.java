package org.shub.userservice.service;

import lombok.RequiredArgsConstructor;
import org.shub.userservice.dto.FollowResponse;
import org.shub.userservice.dto.UpdateProfileRequest;
import org.shub.userservice.dto.UserProfileResponse;
import org.shub.userservice.entity.Follow;
import org.shub.userservice.entity.UserProfile;
import org.shub.userservice.exception.ProfileNotFoundException;
import org.shub.userservice.kafka.EventPublisher;
import org.shub.userservice.repository.FollowRepository;
import org.shub.userservice.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shared.UserAvatarUpdatedEvent;
import shared.UserFollowedEvent;
import shared.UserUnfollowedEvent;

// Adjust to match your actual shared-events package, same as UserEventConsumer

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Equivalent of UserService.Infrastructure/Services/UserService.cs.
 * Covers: get profile, update profile (bio + avatar), follow, unfollow,
 * list followers/following.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;
    private final PhotoService photoService;
    private final EventPublisher eventPublisher;

    public UserProfileResponse getProfile(UUID profileUserId, UUID currentUserId) {
        UserProfile profile = userProfileRepository.findById(profileUserId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found."));

        long followerCount = followRepository.countByFollowingId(profileUserId);
        long followingCount = followRepository.countByFollowerId(profileUserId);
        boolean isFollowed = currentUserId != null
                && followRepository.existsByFollowerIdAndFollowingId(currentUserId, profileUserId);

        return toResponse(profile, followerCount, followingCount, isFollowed);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request, MultipartFile avatarFile) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found."));

        if (request.bio() != null) {
            profile.setBio(request.bio());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = photoService.uploadAvatar(avatarFile);
            profile.setAvatarUrl(avatarUrl);

            // Equivalent of publishing UserAvatarUpdatedEvent so auth-service's
            // JWT claim and post-service's denormalized avatar can stay in sync.
            eventPublisher.publish(
                    "user.avatar-updated",
                    userId.toString(),
                    new UserAvatarUpdatedEvent(userId, avatarUrl)
            );
        }

        userProfileRepository.save(profile);

        long followerCount = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByFollowerId(userId);

        return toResponse(profile, followerCount, followingCount, false);
    }

    @Transactional
    public void follow(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself.");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return; // already following - idempotent, no error
        }

        UserProfile follower = userProfileRepository.findById(followerId)
                .orElseThrow(() -> new ProfileNotFoundException("Follower profile not found."));
        UserProfile following = userProfileRepository.findById(followingId)
                .orElseThrow(() -> new ProfileNotFoundException("User to follow not found."));

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();

        followRepository.save(follow);

        eventPublisher.publish(
                "user.followed",
                followerId.toString(),
                new UserFollowedEvent(followerId, follower.getUsername(), followingId, following.getUsername(), Instant.now())
        );
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followingId) {
        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return; // not following - idempotent, no error
        }

        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);

        eventPublisher.publish(
                "user.unfollowed",
                followerId.toString(),
                new UserUnfollowedEvent(followerId, followingId, Instant.now())
        );
    }

    public List<FollowResponse> getFollowers(UUID userId) {
        return followRepository.findFollowersOf(userId);
    }

    public List<FollowResponse> getFollowing(UUID userId) {
        return followRepository.findFollowingOf(userId);
    }

    private UserProfileResponse toResponse(UserProfile profile, long followerCount, long followingCount, boolean isFollowed) {
        return new UserProfileResponse(
                profile.getUserId(),
                profile.getUsername(),
                profile.getBio(),
                profile.getAvatarUrl(),
                followerCount,
                followingCount,
                isFollowed,
                profile.getCreatedAt()
        );
    }
}
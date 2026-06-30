package org.shub.userservice.dto;

import jakarta.validation.constraints.Size;

/**
 * Equivalent of UserService.Application/DTO/UpdateProfileDto.cs.
 * Both fields optional/nullable - a PATCH-style partial update where the
 * service layer only overwrites fields that were actually provided.
 */
public record UpdateProfileRequest(

        @Size(max = 500, message = "Bio must be 500 characters or fewer")
        String bio
) {
}
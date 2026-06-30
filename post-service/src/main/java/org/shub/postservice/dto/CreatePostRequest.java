package org.shub.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be 200 characters or fewer")
        String title,

        @NotBlank(message = "Content is required")
        @Size(max = 2000, message = "Content must be 2000 characters or fewer")
        String content
) {
}
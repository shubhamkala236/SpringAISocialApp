package org.shub.postservice.dto;

import jakarta.validation.constraints.Size;

public record UpdatePostRequest(

        @Size(max = 200, message = "Title must be 200 characters or fewer")
        String title,

        @Size(max = 2000, message = "Content must be 2000 characters or fewer")
        String content
) {
}
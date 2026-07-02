package org.shub.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.shub.aiservice.domain.PostTone;

public record ImprovePostRequest(
        String title,
        @NotBlank(message = "Content is required")
        String content,
        PostTone tone
) {
    public ImprovePostRequest {
        if (tone == null) tone = PostTone.Casual;
    }
}
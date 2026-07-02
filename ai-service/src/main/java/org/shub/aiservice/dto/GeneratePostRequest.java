package org.shub.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.shub.aiservice.domain.PostTone;

public record GeneratePostRequest(
        @NotBlank(message = "Idea is required")
        String idea,
        PostTone tone
) {
    public GeneratePostRequest {
        if (tone == null) tone = PostTone.Casual;
    }
}
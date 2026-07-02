package org.shub.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.shub.aiservice.domain.PostTone;

public record RephrasePostRequest(
        @NotBlank(message = "Content is required")
        String content,
        PostTone tone
) {
    public RephrasePostRequest {
        if (tone == null) tone = PostTone.Casual;
    }
}
package org.shub.aiservice.dto;

public record PostAssistantResponse(
        String title,
        String content,
        String actionTaken
) {
}
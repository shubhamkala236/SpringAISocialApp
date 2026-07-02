package org.shub.aiservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shub.aiservice.dto.*;
import org.shub.aiservice.service.PostAssistantService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/ai/posts")
@RequiredArgsConstructor
public class PostAssistantController {

    private final PostAssistantService postAssistantService;

    @PostMapping("/generate")
    public ResponseEntity<PostAssistantResponse> generatePost(
            @RequestBody @Valid GeneratePostRequest request) {
        return ResponseEntity.ok(postAssistantService.generatePost(request));
    }

    @PostMapping("/improve")
    public ResponseEntity<PostAssistantResponse> improvePost(
            @RequestBody @Valid ImprovePostRequest request) {
        return ResponseEntity.ok(postAssistantService.improvePost(request));
    }

    @PostMapping("/rephrase")
    public ResponseEntity<PostAssistantResponse> rephrasePost(
            @RequestBody @Valid RephrasePostRequest request) {
        return ResponseEntity.ok(postAssistantService.rephrasePost(request));
    }

    @PostMapping("/summarize")
    public ResponseEntity<PostAssistantResponse> summarizePost(
            @RequestBody String content) {
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(postAssistantService.summarizePost(content));
    }

    @PostMapping("/hook")
    public ResponseEntity<PostAssistantResponse> makeHook(
            @RequestBody String content) {
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(postAssistantService.makeHook(content));
    }

    /**
     * SSE streaming endpoint - equivalent of StreamGeneratePost in
     * PostAssistantController.cs. Returns text/event-stream, each chunk
     * is one "data: <chunk>\n\n" frame. Spring MVC handles the SSE
     * framing automatically when produces = TEXT_EVENT_STREAM_VALUE and
     * the return type is Flux<String>.
     */
    @PostMapping(value = "/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamGeneratePost(
            @RequestBody @Valid GeneratePostRequest request) {
        return postAssistantService.streamGeneratePost(request);
    }
}
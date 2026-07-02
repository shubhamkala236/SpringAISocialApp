package org.shub.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shub.aiservice.dto.*;
import org.shub.aiservice.prompts.PostPrompts;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Equivalent of PostAssistantService.cs.
 *
 * Semantic Kernel → Spring AI mapping:
 *   _kernel.CreateFunctionFromPrompt(prompt, OllamaPromptExecutionSettings)
 *   → chatModel.call(new Prompt(text, OllamaOptions.builder()...build()))
 *
 *   _kernel.InvokeStreamingAsync<string>()
 *   → chatModel.stream(new Prompt(...)) returns Flux<ChatResponse>
 *
 * The ParseResponse JSON-extraction logic is preserved verbatim - the
 * same "find first { to last }" approach, same fallback on parse failure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostAssistantService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private static final String MODEL = "llama3.2";

    // ── Generate ──────────────────────────────────────────────────────────

    public PostAssistantResponse generatePost(GeneratePostRequest request) {
        String prompt = PostPrompts.generatePost(request.idea(), request.tone().name());
        String raw = call(prompt, 0.8);
        return parseResponse(raw, "Generated");
    }

    // ── Improve ───────────────────────────────────────────────────────────

    public PostAssistantResponse improvePost(ImprovePostRequest request) {
        String prompt = PostPrompts.improvePost(
                request.title() != null ? request.title() : "",
                request.content(),
                request.tone().name());
        String raw = call(prompt, 0.6);
        return parseResponse(raw, "Improved");
    }

    // ── Rephrase ──────────────────────────────────────────────────────────

    public PostAssistantResponse rephrasePost(RephrasePostRequest request) {
        String prompt = PostPrompts.rephrasePost(request.content(), request.tone().name());
        String raw = call(prompt, 0.9);
        return parseResponse(raw, "Rephrased");
    }

    // ── Summarize ─────────────────────────────────────────────────────────

    public PostAssistantResponse summarizePost(String content) {
        String prompt = PostPrompts.summarizePost(content);
        String raw = call(prompt, 0.4);
        return parseResponse(raw, "Summarized");
    }

    // ── Make Hook ─────────────────────────────────────────────────────────

    public PostAssistantResponse makeHook(String content) {
        String prompt = PostPrompts.makeHook(content);
        String raw = call(prompt, 0.85);
        return parseResponse(raw, "Hook Added");
    }

    // ── Stream Generate ───────────────────────────────────────────────────

    /**
     * Equivalent of StreamGeneratePostAsync - returns a Flux<String> of
     * text chunks. The controller pipes this directly to SSE.
     * Flux replaces C#'s IAsyncEnumerable<string>; both are lazy
     * async sequences, Flux just uses reactive-streams semantics.
     */
    public Flux<String> streamGeneratePost(GeneratePostRequest request) {
        String prompt = PostPrompts.streamGeneratePost(request.idea(), request.tone().name());

        return chatModel.stream(
                new Prompt(prompt, OllamaChatOptions.builder()
                        .model(MODEL)
                        .temperature(0.8)
                        .build())
        ).mapNotNull(response -> {
            if (response.getResult() != null && response.getResult().getOutput() != null) {
                return response.getResult().getOutput().getText();
            }
            return null;
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String call(String promptText, double temperature) {
        var response = chatModel.call(
                new Prompt(promptText, OllamaChatOptions.builder()
                        .model(MODEL)
                        .temperature(temperature)
                        .build())
        );
        return response.getResult().getOutput().getText();
    }

    /**
     * Equivalent of ParseResponse() in PostAssistantService.cs.
     * Exact same logic: find first '{', last '}', extract that substring,
     * deserialize, fallback to raw text if parsing fails.
     */
    private PostAssistantResponse parseResponse(String raw, String actionTaken) {
        try {
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');

            if (start == -1 || end == -1) {
                throw new IllegalStateException("No JSON found in response");
            }

            String json = raw.substring(start, end + 1);
            JsonNode parsed = objectMapper.readTree(json);

            return new PostAssistantResponse(
                    parsed.path("title").asText(""),
                    parsed.path("content").asText(""),
                    actionTaken
            );
        } catch (Exception ex) {
            log.error("Failed to parse AI response: {}", raw, ex);
            // Fallback - same behavior as the C# version
            return new PostAssistantResponse("AI Generated Post", raw, actionTaken);
        }
    }
}
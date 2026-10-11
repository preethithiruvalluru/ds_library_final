package com.mbu.dslibrary.service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlockParam;
import com.mbu.dslibrary.dto.ChatRequest;
import com.mbu.dslibrary.exception.UpstreamException;

/**
 * Asks Claude through the official Anthropic Java SDK. The API key comes from the ANTHROPIC_API_KEY environment
 * variable and stays on the server: the browser only ever talks to /api/chat.
 */
@Component
public class ClaudeChatModel implements ChatModel {

    private static final Logger log = LoggerFactory.getLogger(ClaudeChatModel.class);

    private final AnthropicClient client;   // null when no key is configured
    private final String model;
    private final long maxTokens;

    public ClaudeChatModel(@Value("${anthropic.api-key:}") String apiKey,
                           @Value("${chat.model:claude-opus-5-5}") String model,
                           @Value("${chat.max-output-tokens:1500}") long maxTokens) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.client = (apiKey == null || apiKey.isBlank()) ? null
                : AnthropicOkHttpClient.builder().apiKey(apiKey.trim()).timeout(Duration.ofSeconds(90)).maxRetries(2).build();
        log.info(client == null ? "AI assistant is OFF (set ANTHROPIC_API_KEY to turn it on)."
                : "AI assistant is ON (model {}).", model);
    }

    @Override
    public boolean available() {
        return client != null;
    }

    @Override
    public String reply(String systemPrompt, List<ChatRequest.Message> conversation) {
        if (client == null) {
            throw new UpstreamException(503, "The AI assistant is not set up yet.");
        }
        MessageCreateParams.Builder request = MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens)
                .systemOfTextBlockParams(List.of(TextBlockParam.builder().text(systemPrompt).build()))
                // Quick everyday answers: low effort keeps replies fast and the cost small.
                .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build());
        for (ChatRequest.Message m : conversation) {
            if ("assistant".equals(m.role())) request.addAssistantMessage(m.content());
            else request.addUserMessage(m.content());
        }

        Message response;
        try {
            response = client.messages().create(request.build());
        } catch (RateLimitException e) {
            throw new UpstreamException(503, "The assistant is very busy right now. Please try again in a minute.");
        } catch (AnthropicServiceException e) {
            log.warn("Claude API error {}: {}", e.statusCode(), e.getMessage());
            throw new UpstreamException(502, "The assistant could not answer right now. Please try again later.");
        } catch (RuntimeException e) {
            log.warn("Could not reach Claude: {}", e.getMessage());
            throw new UpstreamException(502, "Could not reach the AI service. Check the internet connection and try again.");
        }

        // Safety systems may decline a request; say so plainly instead of showing nothing.
        if (response.stopReason().map(r -> r.equals(StopReason.REFUSAL)).orElse(false)) {
            return "I can't help with that request. Please ask me about your subjects, lab work or the college.";
        }
        String text = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(t -> t.text())
                .collect(Collectors.joining("\n"))
                .trim();
        return text.isEmpty() ? "Sorry, I could not come up with an answer. Could you ask it another way?" : text;
    }
}

package com.mbu.dslibrary.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** The conversation so far, sent by the chat window. The last message must be the student's new question. */
public record ChatRequest(
        @NotNull(message = "messages are required")
        @Size(min = 1, max = 20, message = "Send between 1 and 20 messages")
        List<@Valid Message> messages) {

    public record Message(
            @NotBlank(message = "role is required")
            @Pattern(regexp = "user|assistant", message = "role must be user or assistant")
            String role,
            @NotBlank(message = "A message cannot be empty")
            @Size(max = 2000, message = "A message can have at most 2000 characters")
            String content) {}
}

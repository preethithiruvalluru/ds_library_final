package com.mbu.dslibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A student's question for the study bot. */
public record ChatRequest(
        @NotBlank(message = "Please type a question")
        @Size(max = 300, message = "Please keep the question under 300 characters")
        String message) {
}

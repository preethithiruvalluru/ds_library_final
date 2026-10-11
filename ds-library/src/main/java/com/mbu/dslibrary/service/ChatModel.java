package com.mbu.dslibrary.service;

import java.util.List;

import com.mbu.dslibrary.dto.ChatRequest;

/** The AI behind the chat window. A small interface so the real Claude call can be swapped for a fake in tests. */
public interface ChatModel {

    /** False when no API key has been configured (the chat window then says the assistant is not set up). */
    boolean available();

    /** Sends the system prompt and the conversation, and returns the assistant's reply text. */
    String reply(String systemPrompt, List<ChatRequest.Message> conversation);
}

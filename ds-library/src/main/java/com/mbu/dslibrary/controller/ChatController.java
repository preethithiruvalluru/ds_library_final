package com.mbu.dslibrary.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mbu.dslibrary.dto.ChatRequest;
import com.mbu.dslibrary.service.ChatService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/** The chat window talks to these two endpoints. The AI key never leaves the server. */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chat;

    public ChatController(ChatService chat) {
        this.chat = chat;
    }

    /** Tells the chat window whether the assistant is switched on (so it can say so honestly when it is not). */
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("enabled", chat.enabled());
        body.put("questionsPerHour", chat.perVisitorPerHour());
        body.put("maxMessageChars", 2000);
        return body;
    }

    @PostMapping
    public Map<String, String> ask(@Valid @RequestBody ChatRequest request, HttpServletRequest http) {
        return Map.of("reply", chat.ask(http.getRemoteAddr(), request.messages()));
    }
}

package com.mbu.dslibrary.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mbu.dslibrary.chat.ChatMatcher;
import com.mbu.dslibrary.dto.ChatRequest;
import com.mbu.dslibrary.service.ChatService;

import jakarta.validation.Valid;

/** The chat window talks to these two endpoints. Everything is answered on this server from a ready-made list. */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chat;

    public ChatController(ChatService chat) {
        this.chat = chat;
    }

    /** Suggested first questions for the chat window. */
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("enabled", true);
        body.put("answers", chat.answerCount());
        body.put("starters", chat.starters());
        body.put("maxMessageChars", 300);
        return body;
    }

    @PostMapping
    public Map<String, Object> ask(@Valid @RequestBody ChatRequest request) {
        ChatMatcher.Reply reply = chat.ask(request.message());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reply", reply.text());
        body.put("matched", reply.matched());
        body.put("suggestions", reply.suggestions());
        return body;
    }
}

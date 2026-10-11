package com.mbu.dslibrary.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mbu.dslibrary.chat.ChatMatcher;
import com.mbu.dslibrary.service.ChatService;

@WebMvcTest(controllers = ChatController.class)
class ChatControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ChatService chat;

    private static String body(String message) {
        return "{\"message\":\"" + message + "\"}";
    }

    @Test
    void statusListsTheSuggestedFirstQuestions() throws Exception {
        when(chat.answerCount()).thenReturn(107);
        when(chat.starters()).thenReturn(List.of("What is Python?", "What is overfitting?"));
        mvc.perform(get("/api/chat/status")).andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.answers").value(107))
                .andExpect(jsonPath("$.starters[0]").value("What is Python?"));
    }

    @Test
    void returnsTheMatchedAnswerWithRelatedQuestions() throws Exception {
        when(chat.ask("what is a dataframe"))
                .thenReturn(new ChatMatcher.Reply("A DataFrame is a table.", true, List.of("What is Pandas?"), null));
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(body("what is a dataframe")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("A DataFrame is a table."))
                .andExpect(jsonPath("$.matched").value(true))
                .andExpect(jsonPath("$.suggestions[0]").value("What is Pandas?"));
    }

    @Test
    void sayssoWhenNothingMatched() throws Exception {
        when(chat.ask(anyString())).thenReturn(new ChatMatcher.Reply("Sorry, I don't have an answer for that yet.", false, List.of(), null));
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(body("how to cook rice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matched").value(false));
    }

    @Test
    void refusesEmptyOrTooLongQuestions() throws Exception {
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(body("")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(body("   ")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content(body("x".repeat(301))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}

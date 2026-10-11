package com.mbu.dslibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mbu.dslibrary.dto.ChatRequest;
import com.mbu.dslibrary.exception.UpstreamException;

/** These tests never contact Claude: without a key the assistant must switch itself off cleanly. */
class ClaudeChatModelTest {

    @Test
    void isOffWithoutAnApiKeyAndSaysSoInsteadOfInventingAnAnswer() {
        ClaudeChatModel model = new ClaudeChatModel("", "claude-opus-5-5", 1500);
        assertFalse(model.available());
        UpstreamException e = assertThrows(UpstreamException.class,
                () -> model.reply("system", List.of(new ChatRequest.Message("user", "hi"))));
        assertEquals(503, e.getStatus());
    }

    @Test
    void isOnWhenAKeyIsGiven() {
        // A fake key is enough to switch it on; nothing is sent until a question is asked.
        assertTrue(new ClaudeChatModel("sk-ant-fake-key-for-test", "claude-opus-5-5", 1500).available());
        assertFalse(new ClaudeChatModel("   ", "claude-opus-5-5", 1500).available());
    }
}

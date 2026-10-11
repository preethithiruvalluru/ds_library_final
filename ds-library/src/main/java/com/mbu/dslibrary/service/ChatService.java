package com.mbu.dslibrary.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mbu.dslibrary.chat.ChatMatcher;
import com.mbu.dslibrary.chat.KnowledgeEntry;
import com.mbu.dslibrary.chat.KnowledgeLoader;

/**
 * The study bot. It answers by matching the words of a question against a ready-made list of answers
 * (src/main/resources/chat/knowledge/*.md). Nothing is sent over the internet and no key is needed.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatMatcher matcher;

    public ChatService(@Value("${chat.knowledge-dir:}") String knowledgeDir) {
        List<KnowledgeEntry> entries = KnowledgeLoader.load(knowledgeDir);
        this.matcher = new ChatMatcher(entries);
        log.info("Study bot ready with {} ready-made answers (offline, no AI service).", entries.size());
    }

    public ChatMatcher.Reply ask(String question) {
        return matcher.answer(question);
    }

    public List<String> starters() {
        return matcher.starters();
    }

    public int answerCount() {
        return matcher.size();
    }
}

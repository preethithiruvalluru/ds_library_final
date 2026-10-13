package com.mbu.dslibrary.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mbu.dslibrary.chat.ChatMatcher;
import com.mbu.dslibrary.chat.KnowledgeEntry;
import com.mbu.dslibrary.chat.KnowledgeLoader;
import com.mbu.dslibrary.chat.PortalKnowledge;
import com.mbu.dslibrary.repository.ExperimentRepository;
import com.mbu.dslibrary.repository.ModuleRepository;
import com.mbu.dslibrary.repository.NoteRepository;
import com.mbu.dslibrary.repository.ResourceRepository;
import com.mbu.dslibrary.repository.ToolRepository;

/**
 * The study bot. It answers by matching the words of a question against ready-made answers: the answer files in
 * src/main/resources/chat/knowledge/*.md plus everything on the website itself (modules and PDFs, lab experiments,
 * tools, resources, notes). Nothing is sent over the internet and no key is needed.
 * The website part is re-read from the database at most once a minute, so new content is picked up automatically.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final long REFRESH_MS = 60_000;

    private final List<KnowledgeEntry> fileEntries;
    private final ModuleRepository modules;
    private final ToolRepository tools;
    private final ResourceRepository resources;
    private final ExperimentRepository experiments;
    private final NoteRepository notes;

    private ChatMatcher matcher;
    private Set<String> portalStarters = Set.of();
    private long builtAt;

    public ChatService(@Value("${chat.knowledge-dir:}") String knowledgeDir, ModuleRepository modules, ToolRepository tools,
                       ResourceRepository resources, ExperimentRepository experiments, NoteRepository notes) {
        this.fileEntries = KnowledgeLoader.load(knowledgeDir);
        this.modules = modules;
        this.tools = tools;
        this.resources = resources;
        this.experiments = experiments;
        this.notes = notes;
        log.info("Study bot ready with {} ready-made answers (offline, no AI service). Website content is added when first used.", fileEntries.size());
    }

    /** The matcher, rebuilt when the website content may have changed. If the database cannot be read, the ready-made answers still work. */
    private synchronized ChatMatcher current() {
        long now = System.currentTimeMillis();
        if (matcher != null && now - builtAt < REFRESH_MS) return matcher;
        List<KnowledgeEntry> all = new ArrayList<>(fileEntries);
        Set<String> starters = new LinkedHashSet<>();
        try {
            List<KnowledgeEntry> site = PortalKnowledge.build(modules.findAllByOrderBySubjectAscModuleNumberAsc(), tools.findAll(),
                    resources.findAll(), experiments.findAll(), notes.findAll());
            site.stream().filter(KnowledgeEntry::starter).forEach(e -> starters.add(e.title()));
            all.addAll(site);
        } catch (RuntimeException e) {
            log.warn("Study bot could not read the website content ({}). Using the ready-made answers only.", e.toString());
        }
        matcher = new ChatMatcher(all);
        portalStarters = starters;
        builtAt = now;
        return matcher;
    }

    public ChatMatcher.Reply ask(String question) {
        return current().answer(question);
    }

    /** Suggested first questions: a few about the website first, then the general subject questions. */
    public List<String> starters() {
        ChatMatcher m = current();
        List<String> out = new ArrayList<>();
        for (String s : portalStarters) if (out.size() < 3) out.add(s);
        for (String s : m.starters()) if (!out.contains(s) && out.size() < 8) out.add(s);
        return out;
    }

    public int answerCount() {
        return current().size();
    }
}

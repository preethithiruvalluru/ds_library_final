package com.mbu.dslibrary.chat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Collects the answer files: the ones bundled in the app (chat/knowledge/*.md) and, optionally, files from a folder you choose.
 * A file in your folder with the SAME NAME as a bundled file replaces it; any other name is added.
 * Files are read in name order (10-python.md before 20-data-analysis.md), which also decides ties between equal matches.
 */
public final class KnowledgeLoader {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeLoader.class);

    private KnowledgeLoader() {}

    public static List<KnowledgeEntry> load(String externalDir) {
        Map<String, String> files = new TreeMap<>();
        try {
            for (Resource r : new PathMatchingResourcePatternResolver().getResources("classpath:chat/knowledge/*.md")) {
                files.put(r.getFilename(), new String(r.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.warn("Could not read the bundled answer files: {}", e.getMessage());
        }
        if (externalDir != null && !externalDir.isBlank()) {
            Path dir = Path.of(externalDir.trim());
            if (Files.isDirectory(dir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.md")) {
                    for (Path p : stream) files.put(p.getFileName().toString(), Files.readString(p, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    log.warn("Could not read your answer folder {}: {}", dir, e.getMessage());
                }
            } else {
                log.warn("The answer folder {} does not exist; using only the bundled answers.", dir);
            }
        }
        List<KnowledgeEntry> all = new ArrayList<>();
        files.forEach((name, text) -> all.addAll(KnowledgeParser.parse(text, name)));
        return all;
    }
}

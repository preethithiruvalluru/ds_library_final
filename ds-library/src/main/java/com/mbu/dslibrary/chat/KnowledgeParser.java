package com.mbu.dslibrary.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads an answer file. The format is plain text anyone can edit:
 *
 * <pre>
 * category: subject            (optional lines before the first entry give defaults for the whole file)
 * topic: Python
 *
 * ## What is a list?           (the main question: every entry starts with "## ")
 * keywords: list, lists, array  (words that point to this answer; short phrases are fine)
 * questions: what is a list in python | explain lists   (other ways to ask it, separated by |)
 * starter: yes                 (optional: show it as a suggested first question)
 * threshold: 0.3               (optional: how sure the bot must be, 0 to 1)
 *
 * The answer, written in Markdown, until the next "## " line.
 * </pre>
 *
 * A line starting with "## " inside a fenced code block (```) is part of the answer, not a new entry.
 */
public final class KnowledgeParser {

    private static final Pattern HEADER = Pattern.compile("^(category|topic|keywords|questions|starter|threshold):\\s*(.*)$", Pattern.CASE_INSENSITIVE);

    private KnowledgeParser() {}

    public static List<KnowledgeEntry> parse(String text, String source) {
        List<KnowledgeEntry> entries = new ArrayList<>();
        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

        String defaultCategory = "subject", defaultTopic = "";
        int i = 0;
        // File-level defaults: header lines before the first entry.
        for (; i < lines.length && !isEntryStart(lines[i]); i++) {
            Matcher m = HEADER.matcher(lines[i].trim());
            if (m.matches()) {
                if (m.group(1).equalsIgnoreCase("category")) defaultCategory = m.group(2).trim().toLowerCase(Locale.ROOT);
                if (m.group(1).equalsIgnoreCase("topic")) defaultTopic = m.group(2).trim();
            }
        }

        while (i < lines.length) {
            if (!isEntryStart(lines[i])) { i++; continue; }
            String title = lines[i].substring(3).trim();
            i++;

            String category = defaultCategory, topic = defaultTopic;
            List<String> keywords = new ArrayList<>(), questions = new ArrayList<>();
            boolean starter = false;
            Double threshold = null;
            for (; i < lines.length; i++) {
                Matcher m = HEADER.matcher(lines[i].trim());
                if (!m.matches()) break;
                String key = m.group(1).toLowerCase(Locale.ROOT), value = m.group(2).trim();
                switch (key) {
                    case "category" -> category = value.toLowerCase(Locale.ROOT);
                    case "topic" -> topic = value;
                    case "keywords" -> keywords.addAll(split(value, ","));
                    case "questions" -> questions.addAll(split(value, "\\|"));
                    case "starter" -> starter = value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
                    case "threshold" -> threshold = parseThreshold(value);
                    default -> { }
                }
            }

            StringBuilder answer = new StringBuilder();
            boolean inFence = false;
            for (; i < lines.length; i++) {
                String line = lines[i];
                if (line.trim().startsWith("```")) inFence = !inFence;
                if (!inFence && isEntryStart(line)) break;
                answer.append(line).append('\n');
            }
            String text2 = answer.toString().strip();
            if (!title.isEmpty() && !text2.isEmpty()) {
                entries.add(new KnowledgeEntry(title, category, topic, List.copyOf(keywords), List.copyOf(questions), starter, threshold, text2, source));
            }
        }
        return entries;
    }

    private static boolean isEntryStart(String line) {
        return line.startsWith("## ");
    }

    private static List<String> split(String value, String regex) {
        List<String> out = new ArrayList<>();
        for (String part : value.split(regex)) {
            String p = part.trim();
            if (!p.isEmpty()) out.add(p);
        }
        return out;
    }

    private static Double parseThreshold(String value) {
        try {
            double d = Double.parseDouble(value);
            return d >= 0 && d <= 1 ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

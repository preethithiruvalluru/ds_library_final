package com.mbu.dslibrary.chat;

import java.util.List;

/**
 * One ready-made answer in the study bot's list.
 *
 * @param title      the main question, for example "What is overfitting?"
 * @param category   subject, portal, college or smalltalk
 * @param topic      a short label such as "Python" or "SQL"
 * @param keywords   words and phrases that point to this answer (single words and short phrases)
 * @param questions  other ways a student might ask the same thing
 * @param starter    true if it is shown as a suggested first question
 * @param threshold  how sure the bot must be before replying with this answer (0 to 1); null means the normal level
 * @param answer     the reply, written in simple Markdown
 * @param source     the file it came from (used in messages)
 */
public record KnowledgeEntry(String title, String category, String topic, List<String> keywords, List<String> questions,
                             boolean starter, Double threshold, String answer, String source) {
}

package com.mbu.dslibrary.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class KnowledgeParserTest {

    private static final String SAMPLE = """
            category: subject
            topic: Python

            <!-- notes for the owner, ignored -->

            ## What is a list?
            keywords: list, lists, array
            questions: what is a list in python | explain lists
            starter: yes

            A **list** holds items.

            ```python
            ## this line starts with two hashes but is code, not a new entry
            x = [1, 2]
            ```

            ## What is the attendance rule?
            category: college
            topic: Rules
            keywords: attendance
            threshold: 0.3

            At least 75%.

            ## An entry with no answer
            keywords: nothing
            """;

    @Test
    void readsEntriesHeadersAndAnswers() {
        List<KnowledgeEntry> entries = KnowledgeParser.parse(SAMPLE, "test.md");
        assertEquals(2, entries.size(), "the entry without an answer is skipped");

        KnowledgeEntry list = entries.get(0);
        assertEquals("What is a list?", list.title());
        assertEquals("subject", list.category());
        assertEquals("Python", list.topic());
        assertEquals(List.of("list", "lists", "array"), list.keywords());
        assertEquals(List.of("what is a list in python", "explain lists"), list.questions());
        assertTrue(list.starter());
        assertNull(list.threshold());
        assertEquals("test.md", list.source());
    }

    @Test
    void aLineWithHashesInsideACodeBlockIsPartOfTheAnswer() {
        KnowledgeEntry list = KnowledgeParser.parse(SAMPLE, "test.md").get(0);
        assertTrue(list.answer().contains("## this line starts with two hashes"));
        assertTrue(list.answer().contains("x = [1, 2]"));
        assertTrue(list.answer().startsWith("A **list** holds items."));
    }

    @Test
    void anEntryCanOverrideTheFileDefaults() {
        KnowledgeEntry rule = KnowledgeParser.parse(SAMPLE, "test.md").get(1);
        assertEquals("college", rule.category());
        assertEquals("Rules", rule.topic());
        assertEquals(0.3, rule.threshold());
        assertFalse(rule.starter());
        assertEquals("At least 75%.", rule.answer());
    }

    @Test
    void handlesWindowsLineEndingsAndEmptyFiles() {
        assertEquals(1, KnowledgeParser.parse("## Q\r\nkeywords: a\r\n\r\nAnswer\r\n", "x.md").size());
        assertTrue(KnowledgeParser.parse("", "x.md").isEmpty());
        assertTrue(KnowledgeParser.parse("just some text, no entries", "x.md").isEmpty());
    }

    @Test
    void ignoresAnOutOfRangeThreshold() {
        assertNull(KnowledgeParser.parse("## Q\nthreshold: 7\n\nA", "x.md").get(0).threshold());
        assertNull(KnowledgeParser.parse("## Q\nthreshold: abc\n\nA", "x.md").get(0).threshold());
    }
}

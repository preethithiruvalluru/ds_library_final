package com.mbu.dslibrary.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class TextNormalizerTest {

    @Test
    void removesFillerWordsAndPunctuation() {
        assertEquals(List.of("overfit"), TextNormalizer.tokens("What is overfitting?"));
        assertEquals(List.of("overfit"), TextNormalizer.tokens("  Could you PLEASE explain overfitting!!  "));
        assertTrue(TextNormalizer.tokens("what is the").isEmpty());
        assertTrue(TextNormalizer.tokens("").isEmpty());
        assertTrue(TextNormalizer.tokens(null).isEmpty());
    }

    @Test
    void differentFormsOfAWordAgree() {
        assertEquals(TextNormalizer.tokens("join"), TextNormalizer.tokens("joins"));
        assertEquals(TextNormalizer.tokens("join"), TextNormalizer.tokens("joined"));
        assertEquals(TextNormalizer.tokens("join"), TextNormalizer.tokens("joining"));
        assertEquals(TextNormalizer.tokens("plot"), TextNormalizer.tokens("plotting"));
        assertEquals(TextNormalizer.tokens("query"), TextNormalizer.tokens("queries"));
        assertEquals(TextNormalizer.tokens("dataframe"), TextNormalizer.tokens("dataframes"));
    }

    @Test
    void shortFormsAreSpelledOut() {
        assertEquals(List.of("dataframe"), TextNormalizer.tokens("df"));
        assertEquals(List.of("machine", "learn"), TextNormalizer.tokens("ML"));
        assertEquals(List.of("neural", "network"), TextNormalizer.tokens("NN"));
        assertEquals(List.of("mongodb"), TextNormalizer.tokens("mongo"));
        assertEquals(List.of("javascript"), TextNormalizer.tokens("js"));
    }

    @Test
    void keepsTheLanguagesRAndCAndSpecialNames() {
        assertTrue(TextNormalizer.tokens("R programming").contains("r"));
        assertTrue(TextNormalizer.tokens("c language").contains("c"));
        assertTrue(TextNormalizer.tokens("C++ basics").contains("cpp"));
        assertEquals(List.of("kmean"), TextNormalizer.tokens("K-Means"));
        assertEquals(TextNormalizer.tokens("k-means"), TextNormalizer.tokens("k means"));
    }

    @Test
    void ignoresApostrophes() {
        assertEquals(TextNormalizer.tokens("python"), TextNormalizer.tokens("What's python"));
        assertEquals(TextNormalizer.tokens("python"), TextNormalizer.tokens("python's"));
    }
}

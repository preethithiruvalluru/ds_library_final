package com.mbu.dslibrary.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks the REAL answer files with realistic student questions. If you change an answer file and a test here fails,
 * the new wording stole a question from another answer (or lost one): adjust the keywords.
 */
class ChatMatcherTest {

    private static List<KnowledgeEntry> entries;
    private static ChatMatcher bot;

    @BeforeAll
    static void load() {
        entries = KnowledgeLoader.load(null);
        bot = new ChatMatcher(entries);
    }

    /** Asserts that the question is answered by the entry whose title contains {@code titlePart}. */
    private static void expect(String question, String titlePart) {
        ChatMatcher.Reply r = bot.answer(question);
        assertTrue(r.matched() && r.entry() != null && r.entry().title().toLowerCase().contains(titlePart.toLowerCase()),
                () -> "\"" + question + "\" should be answered by \"" + titlePart + "\" but got "
                        + (r.entry() == null ? "[no answer: " + r.text().substring(0, Math.min(40, r.text().length())) + "] suggestions=" + r.suggestions()
                                : "\"" + r.entry().title() + "\""));
    }

    private static void expectNoAnswer(String question) {
        ChatMatcher.Reply r = bot.answer(question);
        assertFalse(r.matched(), () -> "\"" + question + "\" must not get an answer, but got \"" + (r.entry() == null ? "?" : r.entry().title()) + "\"");
        assertNull(r.entry());
    }

    // ---------- the answer files themselves ----------

    @Test
    void thereAreEnoughWellFormedAnswers() {
        assertTrue(entries.size() >= 90, "expected at least 90 answers, found " + entries.size());
        Set<String> titles = new HashSet<>();
        for (KnowledgeEntry e : entries) {
            assertTrue(titles.add(e.title().toLowerCase()), "duplicate question title: " + e.title());
            assertFalse(e.answer().isBlank(), "empty answer: " + e.title());
            assertFalse(e.keywords().isEmpty(), "no keywords: " + e.title() + " (" + e.source() + ")");
            assertTrue(Set.of("subject", "portal", "college", "smalltalk").contains(e.category()), "unknown category in " + e.title());
        }
        assertTrue(bot.starters().size() >= 6, "there should be at least 6 suggested first questions");
    }

    @Test
    void everyAnswerIsFoundWhenAskedByItsOwnTitle() {
        List<String> problems = new ArrayList<>();
        for (KnowledgeEntry e : entries) {
            if ("smalltalk".equals(e.category())) continue;
            ChatMatcher.Reply r = bot.answer(e.title());
            if (!r.matched() || r.entry() == null || !r.entry().title().equals(e.title())) {
                problems.add("\"" + e.title() + "\" -> " + (r.entry() == null ? "NO ANSWER" : r.entry().title()));
            }
        }
        assertTrue(problems.isEmpty(), "These titles are not found by themselves:\n" + String.join("\n", problems));
    }

    @Test
    void everyListedAlternativeQuestionGetsAnAnswer() {
        List<String> problems = new ArrayList<>();
        for (KnowledgeEntry e : entries) {
            if ("smalltalk".equals(e.category())) continue;
            for (String q : e.questions()) {
                ChatMatcher.Reply r = bot.answer(q);
                if (!r.matched() || r.entry() == null) problems.add("\"" + q + "\" (from \"" + e.title() + "\")");
            }
        }
        assertTrue(problems.isEmpty(), "These listed questions get no answer:\n" + String.join("\n", problems));
    }

    // ---------- Python ----------

    @Test
    void pythonQuestions() {
        expect("difference between list and tuple", "list and a tuple");
        expect("can i change a tuple", "list and a tuple");
        expect("what is a dictionary in python", "dictionary");
        expect("how to remove duplicates from a list", "set in Python");
        expect("explain lambda", "lambda");
        expect("how to handle exceptions", "try and except");
        expect("what is oop", "class and an object");
        expect("what does self mean in python", "class and an object");
        expect("no module named numpy", "ModuleNotFoundError");
        expect("how do i reverse a string", "slicing");
        expect("what is a virtual environment", "virtual environment");
        expect("how to take input from user", "input from the user");
        expect("what is *args and **kwargs", "args");
        expect("how to open a text file in python", "read and write a file");
        expect("list comprehension", "list comprehension");
    }

    // ---------- NumPy, Pandas, charts ----------

    @Test
    void dataAnalysisQuestions() {
        expect("why is numpy faster than a list", "NumPy array and a Python list");
        expect("what is axis in numpy", "shape, reshape and axis");
        expect("what is broadcasting", "broadcasting");
        expect("how to read csv file", "read a CSV");
        expect("how to check null values in a dataframe", "missing values");
        expect("how to fill nan values", "missing values");
        expect("difference between loc and iloc", "loc and iloc");
        expect("how to filter rows in pandas", "filter rows");
        expect("group by in pandas", "groupby in Pandas");
        expect("how to join two dataframes", "merge, join or concat");
        expect("how to plot a histogram", "basic chart with Matplotlib");
        expect("seaborn vs matplotlib", "Matplotlib, Seaborn and Plotly");
        expect("which chart should i use", "Which chart");
        expect("what is eda", "EDA");
        expect("convert string to datetime", "data types");
        expect("how to sort a dataframe", "sort values");
        expect("what is data cleaning", "data cleaning");
        expect("what is a dataframe", "DataFrame and what is a Series");
    }

    // ---------- SQL and databases ----------

    @Test
    void sqlQuestions() {
        expect("inner join vs left join", "types of SQL joins");
        expect("how to join two tables in sql", "types of SQL joins");
        expect("what is primary key", "primary key and a foreign key");
        expect("delete vs truncate", "DELETE, TRUNCATE and DROP");
        expect("where vs having", "WHERE and HAVING");
        expect("what is normalization", "normalization");
        expect("what is mongodb", "MongoDB");
        expect("how to count rows per group in sql", "GROUP BY");
        expect("what is sql", "What is SQL");
        expect("how to make sql queries faster with an index", "index in a database");
    }

    // ---------- statistics and machine learning ----------

    @Test
    void statisticsAndMachineLearningQuestions() {
        expect("what is overfitting", "overfitting");
        expect("what is underfitting", "underfitting");
        expect("supervised vs unsupervised learning", "supervised and unsupervised");
        expect("what is precision and recall", "precision, recall");
        expect("how does k-means work", "K-means");
        expect("what is knn", "K-nearest neighbours");
        expect("what is logistic regression", "logistic regression");
        expect("explain linear regression", "linear regression");
        expect("why do we scale features", "feature scaling");
        expect("what is one hot encoding", "categorical variables");
        expect("what is gradient descent", "gradient descent");
        expect("how does a neural network work", "neural network");
        expect("mean median mode", "mean, median and mode");
        expect("what is standard deviation", "variance and standard deviation");
        expect("correlation vs causation", "correlation");
        expect("what is p value", "p-value");
        expect("how to find outliers", "outliers");
        expect("what is data science", "What is data science");
        expect("how to improve accuracy of model", "improve my model");
        expect("what is train test split", "train-test split");
        expect("what is cross validation", "cross-validation");
        expect("what is bias and variance", "bias-variance");
        expect("random forest", "decision trees and random forests");
    }

    // ---------- tools and the website ----------

    @Test
    void toolQuestions() {
        expect("git vs github", "Git and GitHub");
        expect("how to push code to github", "push my code");
        expect("what is a pull request", "pull request");
        expect("how to open jupyter notebook", "Jupyter");
        expect("what is colab", "Google Colab");
        expect("how to run python in vs code", "VS Code");
        expect("what is kaggle", "Kaggle");
        expect("how to start learning data science", "start learning data science");
        expect("what is anaconda", "Anaconda");
    }

    @Test
    void websiteQuestions() {
        expect("how to open the menu", "open or close the menu");
        expect("how to add a module", "add or edit a module");
        expect("where is the syllabus", "syllabus");
        expect("why is the run button disabled for java", "Code Editor");
        expect("is there a login", "log in");
        expect("how to change my profile photo", "update my profile");
        expect("are you chatgpt", "chat bot work");
        expect("what can this website do", "What can I do on this website");
        expect("where are the youtube playlists", "YouTube playlists");
        expect("how to add a lab experiment", "lab experiment");
    }

    // ---------- college ----------

    @Test
    void collegeQuestionsAreAnsweredHonestlyNeverInvented() {
        expect("tell me about mbu", "What is MBU");
        for (String q : List.of("what are the hostel fees", "when are the exams", "how to apply for admission",
                "college phone number", "what is the placement percentage", "how is cgpa calculated", "library timings")) {
            ChatMatcher.Reply r = bot.answer(q);
            assertTrue(r.matched(), q);
            assertEquals("college", r.entry().category(), q);
            assertTrue(r.text().contains("don't have official college details"), q + " must say it does not know");
            assertFalse(r.text().matches("(?s).*\\d{4,}.*"), "no invented numbers such as phone numbers: " + q);
        }
    }

    // ---------- small talk ----------

    @Test
    void smallTalk() {
        expect("hi", "Hello");
        expect("hello there", "Hello");
        expect("good morning", "Hello");
        expect("thanks", "Thank you");
        expect("thank you so much", "Thank you");
        expect("bye", "Goodbye");
        expect("help", "Help");
        ChatMatcher.Reply who = bot.answer("who are you");
        assertTrue(who.matched());
        assertTrue(who.text().contains("MBU Study Bot"));
        assertTrue(bot.answer("what can you do").text().contains("You can ask me about"));
        assertTrue(bot.answer("   ").text().contains("You can ask me about"));
    }

    @Test
    void aGreetingInFrontOfARealQuestionDoesNotHideTheQuestion() {
        expect("hi what is pandas", "What is Pandas");
        expect("hello sir please explain overfitting", "overfitting");
    }

    // ---------- forgiving matching ----------

    @Test
    void spellingMistakesShortFormsAndExtraWordsAreForgiven() {
        expect("what is overfiting", "overfitting");
        expect("numpay tutorial", "NumPy");
        expect("what is a dataframee", "DataFrame and what is a Series");
        expect("matplotlip graph", "Matplotlib");
        expect("explain regresion", "regression");
        expect("what is df and series", "DataFrame and what is a Series");
        expect("ml vs ai vs dl", "machine learning");
        expect("sir i have a doubt please explain me what is overfitting with example", "overfitting");
        expect("OVERFITTING???", "overfitting");
        expect("overfitting overfitting overfitting", "overfitting");
    }

    // ---------- being honest when it does not know ----------

    @Test
    void unrelatedQuestionsGetNoMadeUpAnswer() {
        expectNoAnswer("what is the weather today");
        expectNoAnswer("who is the prime minister of india");
        expectNoAnswer("tell me a joke");
        expectNoAnswer("how to cook rice");
        expectNoAnswer("asdfghjkl qwerty");
        expectNoAnswer("what is the capital of france");
        expectNoAnswer("who won the cricket match");
        expectNoAnswer("12345");
    }

    @Test
    void whenItCannotHelpItSuggestsThingsToAsk() {
        ChatMatcher.Reply r = bot.answer("how to cook rice");
        assertTrue(r.text().startsWith("Sorry, I don't have an answer"));
        assertFalse(r.suggestions().isEmpty(), "starter questions are offered");
        for (String s : r.suggestions()) {
            assertTrue(bot.answer(s).matched(), "every suggestion must be answerable: " + s);
        }
    }

    @Test
    void answersComeWithRelatedQuestionsThatCanBeAskedNext() {
        ChatMatcher.Reply r = bot.answer("what is a dataframe");
        assertNotNull(r.entry());
        assertTrue(r.suggestions().size() <= 3);
        for (String s : r.suggestions()) {
            assertFalse(s.equals(r.entry().title()), "an answer must not suggest itself");
            assertTrue(bot.answer(s).matched(), "suggestion must be answerable: " + s);
        }
    }

    @Test
    void anAmbiguousOneWordQuestionStillAnswersAndOffersAlternatives() {
        ChatMatcher.Reply r = bot.answer("join");
        assertTrue(r.matched());
        assertFalse(r.suggestions().isEmpty(), "joins exist in SQL and in Pandas, so alternatives are offered");
    }

    @Test
    void editDistanceCountsSwapsAndGivesUpEarly() {
        assertEquals(0, ChatMatcher.editDistance("numpy", "numpy", 2));
        assertEquals(1, ChatMatcher.editDistance("numpy", "numpay", 2));
        assertEquals(1, ChatMatcher.editDistance("panda", "pnada", 2), "swapped neighbours count as one slip");
        assertTrue(ChatMatcher.editDistance("abcdefgh", "zzzzzzzz", 2) > 2);
    }
}

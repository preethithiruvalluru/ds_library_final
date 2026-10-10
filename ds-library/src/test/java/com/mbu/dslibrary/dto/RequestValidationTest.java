package com.mbu.dslibrary.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** The rules every form must obey, checked without a server or database. */
class RequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void start() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void stop() {
        factory.close();
    }

    private static <T> Set<String> badFields(T bean) {
        return validator.validate(bean).stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test
    void moduleNeedsNumberNameAndSubject() {
        ModuleRequest m = new ModuleRequest();
        assertEquals(Set.of("moduleNumber", "name", "subject"), badFields(m));
        m.setModuleNumber(1);
        m.setName("Intro");
        m.setSubject("Data Science");
        assertTrue(badFields(m).isEmpty());
    }

    @Test
    void noteNeedsTitleAndContentAndAtMostTwentyTags() {
        NoteRequest n = new NoteRequest();
        assertEquals(Set.of("title", "content"), badFields(n));
        n.setTitle("t");
        n.setContent("c");
        n.setTags(java.util.Collections.nCopies(21, "tag"));
        assertEquals(Set.of("tags"), badFields(n));
        n.setTags(List.of("ok"));
        assertTrue(badFields(n).isEmpty());
    }

    @Test
    void experimentChecksLanguageAndGithubLink() {
        ExperimentRequest e = new ExperimentRequest();
        e.setExperimentNumber("1");
        e.setTitle("Pandas");
        assertTrue(badFields(e).isEmpty());

        e.setLanguage("cobol");
        assertEquals(Set.of("language"), badFields(e));
        e.setLanguage("java");

        for (String ok : List.of("", "https://github.com/octocat", "https://github.com/octocat/Hello-World", "https://github.com/octocat/Hello-World/tree/main/src")) {
            e.setGithubRepoUrl(ok);
            assertTrue(badFields(e).isEmpty(), "should accept: " + ok);
        }
        for (String bad : List.of("http://github.com/octocat/x", "https://evil.com/octocat/x", "https://github.com.evil.com/a/b", "javascript:alert(1)", "https://github.com/a b/c")) {
            e.setGithubRepoUrl(bad);
            assertEquals(Set.of("githubRepoUrl"), badFields(e), "should refuse: " + bad);
        }
    }

    @Test
    void toolAndResourceLinksMustBeHttp() {
        ToolRequest t = new ToolRequest();
        t.setName("Tool");
        t.setCategory("Dev");
        t.setOfficialUrl("javascript:alert(1)");
        assertEquals(Set.of("officialUrl"), badFields(t));
        t.setOfficialUrl("https://example.com");
        t.setIcon("bi-database");
        assertTrue(badFields(t).isEmpty());
        t.setIcon("<script>");
        assertEquals(Set.of("icon"), badFields(t));

        ResourceRequest r = new ResourceRequest();
        r.setTitle("R");
        r.setType("LINK");
        r.setUrl("data:text/html,<script>alert(1)</script>");
        assertEquals(Set.of("url"), badFields(r));
        r.setUrl("https://example.com/page");
        assertTrue(badFields(r).isEmpty());
        r.setType("EXE");
        assertEquals(Set.of("type"), badFields(r));
    }

    @Test
    void profileChecksEmailAndSocialLinks() {
        ProfileRequest p = new ProfileRequest();
        assertTrue(badFields(p).isEmpty(), "an empty profile is allowed (nothing invented)");
        p.setEmail("not-an-email");
        p.setMentorEmail("also bad");
        p.setGithubUrl("https://evil.com/me");
        p.setLinkedinUrl("https://evil.com/in/me");
        assertEquals(Set.of("email", "mentorEmail", "githubUrl", "linkedinUrl"), badFields(p));
        p.setEmail("a@b.co");
        p.setMentorEmail("m@b.co");
        p.setGithubUrl("https://github.com/octocat");
        p.setLinkedinUrl("https://www.linkedin.com/in/octo-cat");
        assertTrue(badFields(p).isEmpty());
    }

    @Test
    void savedCodeRejectsUnknownLanguages() {
        SavedCodeRequest s = new SavedCodeRequest();
        s.setTitle("t");
        s.setLanguage("brainfuck");
        assertEquals(Set.of("language"), badFields(s));
        s.setLanguage("python");
        assertTrue(validator.validate(s).stream().map(ConstraintViolation::getMessage).findAny().isEmpty());
    }
}

package com.mbu.dslibrary.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Bad links must be refused BEFORE any request is sent, so these tests need no internet. */
class GitHubServiceTest {

    private final GitHubService github = new GitHubService("");

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com.evil.com/a/b",
            "http://github.com/octocat/Hello-World",
            "https://github.com/../etc/passwd",
            "https://github.com/octocat",
            "https://gitlab.com/octocat/Hello-World",
            "http://169.254.169.254/latest/meta-data",
            "https://github.com/octocat/Hello World",
            "ftp://github.com/octocat/Hello-World",
            "octocat/Hello-World",
            ""})
    void refusesRepositoryLinksThatAreNotGitHub(String url) {
        assertThrows(IllegalArgumentException.class, () -> github.repository(url));
    }

    @Test
    void refusesNullRepositoryLink() {
        assertThrows(IllegalArgumentException.class, () -> github.repository(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "https://github.com/octocat/Hello-World",
            "https://github.com.evil.com/octocat",
            "http://github.com/octocat",
            "https://github.com/-bad_name!"})
    void refusesProfileLinksThatAreNotAGitHubUser(String url) {
        assertThrows(IllegalArgumentException.class, () -> github.userRepositories(url));
    }
}

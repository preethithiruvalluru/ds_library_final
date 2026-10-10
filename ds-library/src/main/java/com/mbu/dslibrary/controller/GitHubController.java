package com.mbu.dslibrary.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mbu.dslibrary.service.GitHubService;
import com.mbu.dslibrary.service.ProfileService;

/** Read-only GitHub information. Nothing here pushes, creates or changes anything on GitHub. */
@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService github;
    private final ProfileService profile;

    public GitHubController(GitHubService github, ProfileService profile) {
        this.github = github;
        this.profile = profile;
    }

    /** Details of one public repository, e.g. /api/github/repo?url=https://github.com/octocat/Hello-World */
    @GetMapping("/repo")
    public Map<String, Object> repo(@RequestParam String url) {
        return github.repository(url);
    }

    /** Public repositories of the GitHub account set in the student profile. */
    @GetMapping("/profile-repos")
    public Map<String, Object> profileRepos() {
        return github.userRepositories(profile.get().getGithubUrl());
    }
}

package com.mbu.dslibrary.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.mbu.dslibrary.exception.NotFoundException;
import com.mbu.dslibrary.exception.UpstreamException;

/**
 * Reads PUBLIC repository information from the GitHub REST API.
 * - Only api.github.com is ever contacted, and only for an owner/repo or username parsed from a github.com link,
 *   so a visitor cannot make the server fetch some other address.
 * - GITHUB_TOKEN (optional, read-only is enough) raises the rate limit from 60 to 5000 requests per hour.
 *   It is never sent to the browser.
 * - Answers are cached for 10 minutes to stay far below the rate limit.
 */
@Service
public class GitHubService {

    private static final Pattern REPO_URL = Pattern.compile(
            "^https://github\\.com/([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+?)(?:\\.git)?/?(?:[/?#].*)?$");
    private static final Pattern USER_URL = Pattern.compile("^https://github\\.com/([A-Za-z0-9-]{1,39})/?$");
    private static final Pattern NAME = Pattern.compile("^[A-Za-z0-9_.-]{1,100}$");
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final int MAX_CACHE = 200;

    private record Cached(Instant at, Object value) {}

    private final RestClient client;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    public GitHubService(@Value("${github.token:}") String token) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        RestClient.Builder b = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("User-Agent", "ds-digital-library");
        if (token != null && !token.isBlank()) {
            b.defaultHeader("Authorization", "Bearer " + token.trim());
        }
        this.client = b.build();
    }

    /** Details of one repository from a link like https://github.com/owner/repo. */
    public Map<String, Object> repository(String url) {
        Matcher m = REPO_URL.matcher(url == null ? "" : url.trim());
        if (!m.matches() || m.group(1).equals(".") || m.group(1).equals("..") || m.group(2).equals(".") || m.group(2).equals("..")) {
            throw new IllegalArgumentException("That is not a GitHub repository link (https://github.com/owner/repo).");
        }
        String owner = m.group(1), repo = m.group(2);
        return cached("repo:" + (owner + "/" + repo).toLowerCase(), () -> {
            JsonNode n = get("/repos/" + owner + "/" + repo);
            return summary(n);
        });
    }

    /** The user's public repositories (newest activity first, at most 30), from a profile link like https://github.com/username. */
    public Map<String, Object> userRepositories(String profileUrl) {
        Matcher m = USER_URL.matcher(profileUrl == null ? "" : profileUrl.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Add your GitHub profile link (https://github.com/username) to the profile first.");
        }
        String user = m.group(1);
        return cached("user:" + user.toLowerCase(), () -> {
            JsonNode list = get("/users/" + user + "/repos?type=owner&sort=updated&per_page=30");
            List<Map<String, Object>> repos = new ArrayList<>();
            for (JsonNode n : list) repos.add(summary(n));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("username", user);
            body.put("profileUrl", "https://github.com/" + user);
            body.put("repositories", repos);
            return body;
        });
    }

    private Map<String, Object> summary(JsonNode n) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("fullName", n.path("full_name").asText(""));
        r.put("name", n.path("name").asText(""));
        r.put("description", n.path("description").isNull() ? "" : n.path("description").asText(""));
        r.put("htmlUrl", n.path("html_url").asText(""));
        r.put("language", n.path("language").isNull() ? "" : n.path("language").asText(""));
        r.put("stars", n.path("stargazers_count").asInt(0));
        r.put("forks", n.path("forks_count").asInt(0));
        r.put("openIssues", n.path("open_issues_count").asInt(0));
        r.put("fork", n.path("fork").asBoolean(false));
        r.put("pushedAt", n.path("pushed_at").isNull() ? "" : n.path("pushed_at").asText(""));
        r.put("updatedAt", n.path("updated_at").isNull() ? "" : n.path("updated_at").asText(""));
        return r;
    }

    private JsonNode get(String path) {
        try {
            JsonNode body = client.get().uri(path).retrieve().body(JsonNode.class);
            if (body == null) throw new UpstreamException(502, "GitHub returned an empty answer.");
            return body;
        } catch (RestClientResponseException e) {
            int s = e.getStatusCode().value();
            if (s == 404) throw new NotFoundException("GitHub could not find that repository or user (it may be private or misspelled).");
            if (s == 403 || s == 429) {
                throw new UpstreamException(503, "GitHub's rate limit was reached. Try again later, or set a GITHUB_TOKEN on the server.");
            }
            throw new UpstreamException(502, "GitHub answered with an error (" + s + ").");
        } catch (RestClientException e) {
            throw new UpstreamException(502, "Could not reach GitHub. Check the internet connection.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T cached(String key, java.util.function.Supplier<T> loader) {
        Cached c = cache.get(key);
        if (c != null && Duration.between(c.at(), Instant.now()).compareTo(TTL) < 0) return (T) c.value();
        T value = loader.get();   // failures are not cached
        if (cache.size() >= MAX_CACHE) cache.clear();
        cache.put(key, new Cached(Instant.now(), value));
        return value;
    }
}
